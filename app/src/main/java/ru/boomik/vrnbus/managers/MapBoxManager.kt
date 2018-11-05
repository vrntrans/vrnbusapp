package ru.boomik.vrnbus.managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.*
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.SupportMapFragment
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.createImageRounded
import ru.boomik.vrnbus.utils.createImageRoundedBitmap


/**
 * Created by boomv on 18.03.2018.
 */
class MapBoxManager(activity: Activity) : OnMapReadyCallback {
    private var mActivity: Activity = activity
    private lateinit var mMapFragment: SupportMapFragment
    private var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: MapboxMap
    private lateinit var mReadyCallback: () -> Unit
    private lateinit var mLocationButton: View

    private var mBusesMarkers: List<Marker>? = null
    private var mRouteOnMap: Polyline? = null
    private var mTraffic: Boolean = false

    init {
        Mapbox.getInstance(activity, "pk.eyJ1Ijoia2lyaWxsYXNoaWtobWluIiwiYSI6ImNqbzRneGNzODE3OGwzdnFxdWs2M2d3YmcifQ.Y3EQH1Fl4SVplVy5J2XL6g");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)

        DataBus.subscribe<Boolean>(DataBus.Traffic) {
            setTrafficJam(it)
        }
    }

    fun setMapFragment(mapFragment : SupportMapFragment) {
        mapFragment.getMapAsync(this)
        mMapFragment=mapFragment
    }

    fun subscribeReady(callback: () -> Unit) {
        mReadyCallback = callback
    }

    private var stationVisible: Boolean = true
    private var stationVisibleSmall: Boolean = true
    private val stationVisibleZoom = 17
    private val stationVisibleZoomSmall = 15
    private var initPosition: LatLng? = null
    private var initZoom: Double = 12.0

    override fun onMapReady(mapboxMap: MapboxMap) {
        mMap = mapboxMap
        /* try {
             mapboxMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, R.raw.map_style_json))
         } catch (e: Resources.NotFoundException) {
         }*/

        mLocationButton = (mMapFragment.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(Integer.parseInt("2"))

        //mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        //mMap.uiSettings.isMapToolbarEnabled = false
        //mMap.isTrafficEnabled = mTraffic


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Consts.INITIAL_POSITION_BOX, Consts.INITIAL_ZOOM_OSM))
/*
        mMap.setOnInfoWindowClickListener {
             DataBus.sendEvent(DataBus.BusClick, it.tag as Bus)

        }*/
        mMap.setOnMarkerClickListener {
            /*  if (it.tag is StationOnMap) {
                  DataBus.sendEvent(DataBus.StationClick, it.tag as StationOnMap)
                  return@setOnMarkerClickListener true
              }*/
            true
        }
        mMap.setOnCameraMoveListener {
            checkZoom()
        }
        mMap.setOnCameraIdleListener {
            checkZoom()
        }
        mReadyCallback()

        mLocationButton.visibility = View.INVISIBLE


        if (initPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition!!, initZoom))
            checkZoom()
        } else if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.continueWith {
                val location = it.result
                if (location != null && it.isSuccessful) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0))
                }
            }
        }

        async(UI) {
            delay(500)
            mLocationButton.visibility = View.INVISIBLE
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
        //mStationMarkers.filter { !mInFavoriteStationMarkers.contains(it) }.forEach { it.isVisible = visible }
    }

    private fun setVisibleStationSmall(visible: Boolean) {
        stationVisibleSmall = visible
        //mStationMarkersSmall.filter { !mInFavoriteStationSmallMarkers.contains(it) }.forEach { it.isVisible = visible }
    }

    fun clearBusesOnMap() {
        mBusesMarkers?.forEach { it.remove() }
    }

    fun showBusesOnMap(it: List<Bus>) {
        val d = mActivity.resources.displayMetrics.density
        val size = (36 * d).toInt()
        val newBusesMarkers = it.map {
            val icon = IconFactory.getInstance(mActivity).fromBitmap(createImageRoundedBitmap(size, size, it.route, it.getAzimuth()))
            val marker = mMap.addMarker(MarkerOptions().position(LatLng(it.lat, it.lon)).title(it.route).snippet(it.getSnippet()).icon(icon))
            //marker.tag = it
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
        val icon = IconFactory.getInstance(mActivity).fromResource(R.drawable.ic_station)
        val iconSmall = IconFactory.getInstance(mActivity).fromResource(R.drawable.ic_station_small)

        val newStationMarkers = stationsOnMap.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPositionBox()).title(it.getTitle()).icon(icon))
            //marker.tag = it
            marker
        }
        val newStationSmallMarkers = stationsOnMap.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPositionBox()).title(it.getTitle()).icon(iconSmall))
           // marker.tag = it
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
      /*  if (mShowBig) mInFavoriteStationMarkers.forEach { it.isVisible=true }
        if (mShowSmall) mInFavoriteStationSmallMarkers.forEach { it.isVisible=true }

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
        checkZoom()*/
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
      //  mMap.isMyLocationEnabled = true
        mLocationButton.visibility = View.INVISIBLE
        fusedLocationClient.lastLocation.continueWith {
            val location = it.result
            if (location != null && it.isSuccessful) {
                val latLng = LatLng(location.latitude, location.longitude)
                if (location.latitude == .0) return@continueWith
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0))
                checkZoom()
            }
        }

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
        PolylineOptions()
        mRouteOnMap = mMap.addPolyline(line)
    }


    fun goToMyLocation() {
        if (mLocationButton.hasOnClickListeners()) {
            mLocationButton.callOnClick()
        } else {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
             //   mMap.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.continueWith {
                    val location = it.result
                    if (location != null && it.isSuccessful) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    }
                }
            }
        }
        checkZoom()
        mLocationButton.visibility = View.INVISIBLE
    }


    private fun setTrafficJam(show: Boolean) {
        mTraffic = show
      //  if (::mMap.isInitialized) mMap.isTrafficEnabled = show
    }

    fun getInstanceStateBundle(outState: Bundle?) {
        if (::mMap.isInitialized && outState != null) {
            val center = mMap.projection.visibleRegion.latLngBounds.center
            outState.putDouble("lat", center.latitude)
            outState.putDouble("lon", center.longitude)
            outState.putDouble("zoom", mMap.cameraPosition.zoom)
        }
    }


    fun restoreInstanceStateBundle(savedInstanceState: Bundle?) {
        initPosition = null
        if (savedInstanceState != null) {
            val lat = savedInstanceState.getDouble("lat")
            val lon = savedInstanceState.getDouble("lon")
            val zoom = savedInstanceState.getDouble("zoom")
            if (lat != .0 && lon != .0 && zoom != 0.0) {
                if (::mMap.isInitialized)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom))
                else {
                    initPosition = LatLng(lat, lon)
                    initZoom = zoom
                }
            }
        }
    }

}