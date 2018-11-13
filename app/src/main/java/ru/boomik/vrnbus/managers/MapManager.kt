package ru.boomik.vrnbus.managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.BusType
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.CustomUrlTileProvider
import ru.boomik.vrnbus.utils.createImageRounded
import kotlin.random.Random


/**
 * Created by boomv on 18.03.2018.
 */
class MapManager(activity: Activity, mapFragment: SupportMapFragment) : OnMapReadyCallback {
    private var mActivity: Activity = activity
    private var mMapFragment: SupportMapFragment = mapFragment
    private var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap
    private lateinit var mReadyCallback: () -> Unit

    private var mBusesMarkers: List<Marker>? = null
    private var mRouteOnMap: Polyline? = null
    private val mBusIcon: BitmapDescriptor
    private var mTraffic: Boolean = false

    init {
        mapFragment.getMapAsync(this)
        mBusIcon = BitmapDescriptorFactory.fromResource(R.drawable.bus_round)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)

        DataBus.subscribe<Boolean>(DataBus.Traffic) {
            setTrafficJam(it.data ?: false)
        }
    }

    fun subscribeReady(callback: () -> Unit) {
        mReadyCallback = callback
    }

    private var stationVisible: Boolean = true
    private var stationVisibleSmall: Boolean = true
    private val stationVisibleZoom = 16
    private val stationVisibleZoomSmall = 14
    private var initPosition: LatLng? = null
    private var initZoom: Float = 12F

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val r = Random(System.currentTimeMillis()).nextInt(1, 3)
        val mTileProvider = CustomUrlTileProvider(256, 256, when (r) {
            1 -> Consts.TILES_URL_A
            2 -> Consts.TILES_URL_B
            3 -> Consts.TILES_URL_C
            else -> Consts.TILES_URL_A
        })
        mMap.addTileOverlay(TileOverlayOptions().tileProvider(mTileProvider).zIndex(0f))
        mMap.mapType = GoogleMap.MAP_TYPE_NONE
        mMap.setMaxZoomPreference(19f)

        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isRotateGesturesEnabled = false
        mMap.uiSettings.isIndoorLevelPickerEnabled = false
        mMap.uiSettings.isTiltGesturesEnabled = false
        mMap.isTrafficEnabled = mTraffic


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


        if (initPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition, initZoom))
            checkZoom()
        } else if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.continueWith {
                val location = it.result
                if (location != null && it.isSuccessful) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                }
            }
        }

        initPosition = null
    }

    private var mShowBig: Boolean = false
    private var mShowSmall: Boolean = false

    private fun checkZoom() {
        var showBig = false
        var showSmall = false
        if (mMap.cameraPosition.zoom >= stationVisibleZoom) showBig = true
        else if (mMap.cameraPosition.zoom >= stationVisibleZoomSmall) showSmall = true

        mShowBig = showBig
        mShowSmall = showSmall

        if (showBig) {
            if (stationVisible && !stationVisibleSmall) return
            if (!stationVisible) setVisibleStationBig(true)
            if (stationVisibleSmall) setVisibleStationSmall(false)
        } else
            if (showSmall) {
                if (!stationVisible && stationVisibleSmall) return
                if (stationVisible) setVisibleStationBig(false)
                if (!stationVisibleSmall) setVisibleStationSmall(true)
            } else {
                if (stationVisible) setVisibleStationBig(false)
                if (stationVisibleSmall) setVisibleStationSmall(false)
            }

    }

    private fun setVisibleStationBig(visible: Boolean) {
        stationVisible = visible
        mStationMarkers.filter { !mInFavoriteStationMarkers.contains(it) }.forEach { it.isVisible = visible }
    }

    private fun setVisibleStationSmall(visible: Boolean) {
        stationVisibleSmall = visible
        mStationMarkersSmall.filter { !mInFavoriteStationSmallMarkers.contains(it) }.forEach { it.isVisible = visible }
    }

    fun clearBusesOnMap() {
        mBusesMarkers?.forEach { it.remove() }
    }

    fun showBusesOnMap(it: List<Bus>) {
        val d = mActivity.resources.displayMetrics.density
        val size = (36 * d).toInt()

        val res = mActivity.resources
        val theme = mActivity.theme
        val small = res.getDrawable(R.drawable.ic_bus_small, theme)
        val medium = res.getDrawable(R.drawable.ic_bus_middle, theme)
        val big = res.getDrawable(R.drawable.ic_bus_large, theme)
        val bigFloor = res.getDrawable(R.drawable.ic_bus_large_low_floor, theme)
        val trolleybus = res.getDrawable(R.drawable.ic_trolleybus, theme)


        val newBusesMarkers = it.map {
            val icon = when {
                it.type == BusType.Small -> small
                it.type == BusType.Medium -> medium
                it.type == BusType.Big -> big
                it.type == BusType.BigLowFloor -> bigFloor
                it.type == BusType.Trolleybus -> trolleybus
                else -> big
            }
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.route).snippet(it.getSnippet()).icon(createImageRounded(icon, size, it.route, it.getAzimuth())).zIndex(1.0f).anchor(1 / 6f, .5f))
            marker.tag = it
            marker
        }
        mBusesMarkers = newBusesMarkers
    }

    private lateinit var mStationMarkers: List<Marker>
    private lateinit var mStationMarkersSmall: List<Marker>
    private lateinit var mFavoritesStationMarkers: MutableList<Marker>
    private lateinit var mInFavoriteStationMarkers: MutableList<Marker>
    private lateinit var mInFavoriteStationSmallMarkers: MutableList<Marker>

    private lateinit var mAllStations: List<StationOnMap>

    fun showStations(stationsOnMap: List<StationOnMap>?) {
        if (stationsOnMap == null) return

        mAllStations = stationsOnMap

        val showBig = mMap.cameraPosition.zoom >= stationVisibleZoomSmall
        val showSmall = mMap.cameraPosition.zoom >= stationVisibleZoom
        val icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station)
        val iconSmall: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station_small)

        val newStationMarkers = stationsOnMap.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(icon).zIndex(0.9f).anchor(.5f, .5f).visible(showBig))
            marker.tag = it
            marker
        }
        val newStationSmallMarkers = stationsOnMap.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(iconSmall).zIndex(0.8f).anchor(.5f, .5f).visible(showSmall))
            marker.tag = it
            marker
        }
        stationVisible = showBig
        stationVisibleSmall = showSmall

        mStationMarkers = newStationMarkers
        mStationMarkersSmall = newStationSmallMarkers

        loadPreferences()

        checkZoom()
    }


    private var favoriteStations: List<Int>? = null

    private fun loadPreferences() {
        mInFavoriteStationMarkers = mutableListOf()
        mInFavoriteStationSmallMarkers = mutableListOf()
        mFavoritesStationMarkers = mutableListOf()
        favoriteStations = SettingsManager.instance.getIntArray(Consts.SETTINGS_FAVORITE_STATIONS)
        checkFavoritesStations()
        DataBus.subscribe<Pair<Int, Boolean>>(DataBus.FavoriteStation) {
            favoriteStations = SettingsManager.instance.getIntArray(Consts.SETTINGS_FAVORITE_STATIONS)
            checkFavoritesStations()
        }
    }


    private fun checkFavoritesStations() {
        if (mShowBig) mInFavoriteStationMarkers.forEach { it.isVisible = true }
        if (mShowSmall) mInFavoriteStationSmallMarkers.forEach { it.isVisible = true }

        if (favoriteStations != null) {

            mInFavoriteStationMarkers.clear()
            mInFavoriteStationSmallMarkers.clear()

            mInFavoriteStationMarkers.addAll(mStationMarkers.filter {
                val station = it.tag as StationOnMap
                favoriteStations!!.contains(station.id)
            })
            mInFavoriteStationSmallMarkers.addAll(mStationMarkersSmall.filter {
                val station = it.tag as StationOnMap
                favoriteStations!!.contains(station.id)
            })

            mInFavoriteStationMarkers.forEach { it.isVisible = false }
            mInFavoriteStationSmallMarkers.forEach { it.isVisible = false }

            mFavoritesStationMarkers.forEach { it.remove() }
            mFavoritesStationMarkers.clear()

            val icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station_favorite)
            val favoritesMarkers = mAllStations.filter { favoriteStations!!.contains(it.id) }.map {
                val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(icon).zIndex(1.0f).anchor(.5f, .5f))
                marker.tag = it
                marker
            }.toMutableList()

            mFavoritesStationMarkers = favoritesMarkers
        } else {
            mInFavoriteStationMarkers.clear()
            mInFavoriteStationSmallMarkers.clear()
            mFavoritesStationMarkers.forEach { it.remove() }
            mFavoritesStationMarkers.clear()
        }
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
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.continueWith {
            val location = it.result
            if (location != null && it.isSuccessful) {
                val latLng = LatLng(location.latitude, location.longitude)
                if (location.latitude == .0) return@continueWith
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
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