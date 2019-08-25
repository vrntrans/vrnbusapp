package ru.boomik.vrnbus.managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.Log
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.BusType
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.CustomUrlTileProvider
import ru.boomik.vrnbus.utils.createImageRoundedBitmap
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


/**
 * Created by boomv on 18.03.2018.
 */
class MapManager(activity: Activity, mapFragment: SupportMapFragment) : OnMapReadyCallback {
    private var mLastZoom: Float = -1f
    private var mActivity: Activity = activity
    private var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap
    private lateinit var mReadyCallback: () -> Unit

    private var mBusesMarkers: List<Marker>? = null
    private var mRouteOnMap: Polyline? = null
    private var mTraffic: Boolean = false

    private var small: Drawable?
    private var medium: Drawable?
    private var big: Drawable?
    private var bigFloor: Drawable?
    private var trolleybus: Drawable?

    init {
        MapsInitializer.initialize(activity.applicationContext)

        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)

        DataBus.subscribe<Boolean>(DataBus.Traffic) {
            setTrafficJam(it.data)
        }
        DataBus.subscribe<Boolean>(Consts.SETTINGS_ROTATE) {
            loadRotate()
        }
        DataBus.subscribe<List<Bus>>(DataBus.BusToMap) {
            showBusesOnMap(it.data)
        }


        small = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_small)
        medium = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_middle)
        big = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_large)
        bigFloor = ContextCompat.getDrawable(mActivity, R.drawable.ic_bus_large_low_floor)
        trolleybus = ContextCompat.getDrawable(mActivity, R.drawable.ic_trolleybus)
    }


    fun subscribeReady(callback: () -> Unit) {
        mReadyCallback = callback
    }

    private var stationVisibleSmall: Boolean = true
    private val stationVisibleZoomSmall = 14
    private var initPosition: LatLng? = null
    private var initZoom: Float = 12F
    private var mBusesMarkerType = -1
    private val markerZoom = 14
    private val markerSmallZoom = 12

    private var mStationMarkersSmall: List<Marker> = listOf()
    private var mInFavoriteStationSmallMarkers: MutableList<Marker> = mutableListOf()

    private var mFavoriteStationMarkers: MutableList<Marker> = mutableListOf()

    private lateinit var mAllStations: List<StationOnMap>
    private var favoriteStations: List<Int>? = null
    var padding: Rect = Rect()


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setPadding(padding.left, (padding.top + 64 * mActivity.resources.displayMetrics.density).toInt(), padding.right, padding.bottom)
        val currentNightMode = mActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val night = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, if (night) R.raw.map_style_json_dark else R.raw.map_style_json))
        } catch (e: Resources.NotFoundException) {
            Log.e("Can't find style. Error: ", e)
        }

        val showOsm = SettingsManager.getBool(Consts.SETTINGS_OSM)

        if (showOsm) {
            val r = Random(System.currentTimeMillis()).nextInt(1, 3)
            val url = if (!night) when (r) {
                1 -> Consts.TILES_URL_A
                2 -> Consts.TILES_URL_B
                3 -> Consts.TILES_URL_C
                else -> Consts.TILES_URL_A
            } else when (r) {
                1 -> Consts.TILES_URL_DARK_A
                2 -> Consts.TILES_URL_DARK_B
                3 -> Consts.TILES_URL_DARK_C
                else -> Consts.TILES_URL_DARK_A
            }
            val mTileProvider = CustomUrlTileProvider(256, 256, url)
            mMap.addTileOverlay(TileOverlayOptions().tileProvider(mTileProvider).zIndex(0f))
            mMap.mapType = GoogleMap.MAP_TYPE_NONE
        }

        mMap.setMaxZoomPreference(19f)

        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = false
        mMap.isTrafficEnabled = mTraffic
        loadRotate()


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Consts.INITIAL_POSITION, Consts.INITIAL_ZOOM))

        mMap.setOnInfoWindowClickListener {
            DataBus.sendEvent(DataBus.BusClick, it.tag as Bus)
        }
        mMap.setOnMarkerClickListener {
            if (it.tag is StationOnMap) {
                DataBus.sendEvent(DataBus.StationClick, it.tag as StationOnMap)
                return@setOnMarkerClickListener true
            }
            false
        }
        mMap.setOnCameraMoveListener {
            checkZoom()
        }
        mMap.setOnCameraIdleListener {
            checkZoom()
        }
        mReadyCallback()

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {

                val info = LinearLayout(mActivity)
                info.orientation = LinearLayout.VERTICAL

                val title = TextView(mActivity)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title

                val snippet = TextView(mActivity)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet

                info.addView(title)
                info.addView(snippet)

                return info
            }
        })



        if (initPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition, initZoom))
            checkZoom()
        } else if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.continueWith {
                val location = it.result
                if (location != null && it.isSuccessful) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val distance = distanceBetween(latLng, LatLng(51.673909, 39.207646))
                    if (distance<20000)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            }
        }
        try {
            mActivity.reportFullyDrawn()
        } catch (e: Throwable) {
        }
        initPosition = null
    }

    private fun distanceBetween(latLng1: LatLng, latLng2: LatLng): Float {

        val loc1 = Location(LocationManager.GPS_PROVIDER)
        loc1.latitude = latLng1.latitude
        loc1.longitude = latLng1.longitude

        val loc2 = Location(LocationManager.GPS_PROVIDER)
        loc2.latitude = latLng2.latitude
        loc2.longitude = latLng2.longitude


        return loc1.distanceTo(loc2)
    }

    private var mShowSmall: Boolean = false

    private fun checkZoom() {
        if (!::mMap.isInitialized) return
        val zoom = mMap.cameraPosition.zoom
        if (mLastZoom == zoom) return
        mLastZoom = mMap.cameraPosition.zoom


        var showSmall = false

       if (mMap.cameraPosition.zoom >= stationVisibleZoomSmall) showSmall = true
        mShowSmall = showSmall

        showBusesOnMap(mBuses, false)

        if (stationVisibleSmall == showSmall) return
        setVisibleStationSmall(showSmall)



    }

    private fun setVisibleStationSmall(visible: Boolean) {
        mStationMarkersSmall.filter { !mInFavoriteStationSmallMarkers.contains(it) }.forEach { it.isVisible = visible }
        stationVisibleSmall = visible
    }

    fun clearBusesOnMap() {
        mBusesMarkers?.forEach { it.remove() }
        mBusesMarkers = null
        mBusesMarkerType = -1
    }

    private var mBuses: List<Bus>? = null

    fun showBusesOnMap(buses: List<Bus>?, ignoreType : Boolean = true) {

        if (buses == null) {
            clearBusesOnMap()
            return
        }

        var neededType = 0
        if (mMap.cameraPosition.zoom >= markerZoom) {
            neededType = 2
        } else if (mMap.cameraPosition.zoom >= markerSmallZoom) {
            neededType = 1
        }

        if (!ignoreType && mBusesMarkerType == neededType) return
        clearBusesOnMap()
        mBuses = buses
        mBusesMarkerType = neededType

        val d = mActivity.resources.displayMetrics.density
        var size = (36 * d).toInt()
        if (neededType == 0) size /= 2

        val now = Calendar.getInstance()
       /*
        Log.e("Start")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        */
        val newBusesMarkers = buses.map {
            val typeIcon = when {
                it.type == BusType.Small -> small
                it.type == BusType.Medium -> medium
                it.type == BusType.Big -> big
                it.type == BusType.BigLowFloor -> bigFloor
                it.type == BusType.Trolleybus -> trolleybus
                else -> big
            }
            val color = when {
                it.type == BusType.Small -> Consts.COLOR_BUS_SMALL
                it.type == BusType.Medium -> Consts.COLOR_BUS_MEDIUM
                it.type == BusType.Big -> Consts.COLOR_BUS
                it.type == BusType.BigLowFloor -> Consts.COLOR_BUS
                it.type == BusType.Trolleybus -> Consts.COLOR_BUS_TROLLEYBUS
                else -> Consts.COLOR_BUS
            }


            val options = MarkerOptions().position(it.getPosition()).title(it.route).zIndex(1.0f).flat(true)
            if (it.getSnippet() != null) options.snippet(it.getSnippet())
            try {
                options.icon(BitmapDescriptorFactory.fromBitmap(createImageRoundedBitmap(neededType, typeIcon, size, it.route, it.getAzimuth(), color)))
            } catch (e: Throwable) {
                try {
                    options.icon(BitmapDescriptorFactory.fromBitmap(createImageRoundedBitmap(if (neededType >= 2) 1 else neededType, typeIcon, size, it.route, it.getAzimuth(), color)))
                } catch (e: Throwable) {

                }
            }
            if (neededType == 2) {
                options.anchor(1 / 6f, .5f)
                options.infoWindowAnchor(1 / 6f, .2f)
            } else {
                options.anchor(.5f, .5f)
                options.infoWindowAnchor(.5f, .2f)
            }

            if (it.time != null) {
                val difference = (now.timeInMillis - it.time!!.timeInMillis) / 1000
                when {
                    difference > 180L -> options.alpha(0.5f)
                    difference > 60L -> options.alpha(0.8f)
                }
/*
                if (difference < 0) {
                    val date = it.time!!.time
                    val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ru"))
                    Log.e("Route ${it.route} | Nimber = ${it.number} | Difference $difference sec. | Time ${format1.format(date)} | Now time: ${format1.format(now.time)}")
                }*/
            }

            val marker = mMap.addMarker(options)
            marker.tag = it
            marker
        }
        /*
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e(" ")
        Log.e("End")
        */
        mBusesMarkers = newBusesMarkers
    }

    fun showStations(stationsOnMap: List<StationOnMap>?) {
        if (stationsOnMap == null) return
        if (::mAllStations.isInitialized && mAllStations.any()) return


        mAllStations = stationsOnMap

        var showSmall = false

        if (mMap.cameraPosition.zoom >= stationVisibleZoomSmall) showSmall = true



        val iconSmall: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station_small)


        stationVisibleSmall = showSmall


        val newStationSmallMarkers = stationsOnMap.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(iconSmall).zIndex(0.8f).anchor(.5f, .5f).visible(showSmall))
            marker.tag = it
            marker
        }

        mStationMarkersSmall = newStationSmallMarkers

        loadPreferences()

        checkZoom()
    }


    private fun loadPreferences() {
        mInFavoriteStationSmallMarkers = mutableListOf()
        loadFavoriteStations()
        DataBus.subscribe<Pair<Int, Boolean>>(DataBus.FavoriteStation) {
            loadFavoriteStations()
        }
    }

    private fun loadFavoriteStations() {
        favoriteStations = SettingsManager.getIntArray(Consts.SETTINGS_FAVORITE_STATIONS)
        checkFavoritesStations()
    }

    private fun loadRotate() {
        if (!::mMap.isInitialized) return
        val rotate = SettingsManager.getBool(Consts.SETTINGS_ROTATE)

        if (rotate) {
             mMap.uiSettings.isCompassEnabled = true
             mMap.uiSettings.isRotateGesturesEnabled = true
             mMap.uiSettings.isTiltGesturesEnabled = true
        } else {
            mMap.uiSettings.isCompassEnabled = false
            mMap.uiSettings.isRotateGesturesEnabled = false
            mMap.uiSettings.isTiltGesturesEnabled = false

            val newCamPos = CameraPosition(mMap.cameraPosition.target, mMap.cameraPosition.zoom, 0f, 0f)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos))
        }
    }


    private fun checkFavoritesStations() {

        if (favoriteStations != null) {

            mInFavoriteStationSmallMarkers.clear()

            mInFavoriteStationSmallMarkers.addAll(mStationMarkersSmall.filter {
                val station = it.tag as StationOnMap
                favoriteStations!!.contains(station.id)
            })

            mInFavoriteStationSmallMarkers.forEach { it.isVisible = false }

            mFavoriteStationMarkers.forEach { it.remove() }
            mFavoriteStationMarkers.clear()
            val icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station_favorite)
            val favoritesMarkers = mAllStations.filter { favoriteStations!!.contains(it.id) }.map {
                val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(icon).zIndex(1.0f).anchor(.5f, .5f))
                marker.tag = it
                marker
            }.toMutableList()

            mFavoriteStationMarkers = favoritesMarkers
        } else {
            mFavoriteStationMarkers.forEach { it.remove() }
            mFavoriteStationMarkers.clear()
            mInFavoriteStationSmallMarkers.clear()
        }
        setVisibleStationSmall(mShowSmall)
        checkZoom()
    }

    fun clearRoutes() {
        mRouteOnMap?.remove()
    }


    fun showRoute(route: Route) {
        val line = PolylineOptions()
        val points = route.stations.map { LatLng(it.lat, it.lon) }
        line.addAll(points)
        val d = mActivity.resources.displayMetrics.density
        line.width(2 * d)
        line.color(Color.BLUE)
        line.zIndex(1f)
        PolylineOptions()
        mRouteOnMap = mMap.addPolyline(line)
    }


    @SuppressLint("MissingPermission")
    fun goToMyLocation() {
        if (!::mMap.isInitialized) return
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.continueWith {
            val location = it.result
            if (location != null && it.isSuccessful) {
                val latLng = LatLng(location.latitude, location.longitude)
                if (location.latitude == .0) return@continueWith
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        }
        checkZoom()
    }


    private fun setTrafficJam(show: Boolean) {
        mTraffic = show
        if (::mMap.isInitialized) mMap.isTrafficEnabled = show
    }

    fun getInstanceStateBundle(outState: Bundle?) {
        if (::mMap.isInitialized && outState != null) {
            val center = mMap.projection.visibleRegion.latLngBounds.center
            outState.putDouble("lat", center.latitude)
            outState.putDouble("lon", center.longitude)
            outState.putFloat("zoom", mMap.cameraPosition.zoom)
        }
    }


    fun restoreInstanceStateBundle(savedInstanceState: Bundle?) {
        initPosition = null
        if (savedInstanceState != null) {
            val lat = savedInstanceState.getDouble("lat")
            val lon = savedInstanceState.getDouble("lon")
            val zoom = savedInstanceState.getFloat("zoom")
            if (lat != .0 && lon != .0 && zoom != 0f) {
                if (::mMap.isInitialized)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom))
                else {
                    initPosition = LatLng(lat, lon)
                    initZoom = zoom
                }
            }
        }
    }

    fun zoomIn() {
        if (!::mMap.isInitialized) return
        mMap.animateCamera(CameraUpdateFactory.zoomIn())
    }

    fun zoomOut() {
        if (!::mMap.isInitialized) return
        mMap.animateCamera(CameraUpdateFactory.zoomOut())
    }
}