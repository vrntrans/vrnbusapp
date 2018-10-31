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
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import ru.boomik.vrnbus.Log
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.createImageRounded


/**
 * Created by boomv on 18.03.2018.
 */
class MapManager(activity: Activity, mapFragment: SupportMapFragment) : OnMapReadyCallback {
    private var mActivity: Activity = activity
    private var mMapFragment: SupportMapFragment = mapFragment
    private var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mMap: GoogleMap
    private lateinit var mReadyCallback: () -> Unit
    private lateinit var mBusClickedCallback: (String) -> Unit
    private lateinit var mStationClickedCallback: (StationOnMap) -> Unit
    private lateinit var mLocationButton: View

    private var mBusesMarkers: List<Marker>? = null
    private var mRouteOnMap: Polyline? = null
    private val mBusIcon: BitmapDescriptor
    private var mTraffic: Boolean = false

    init {
        mapFragment.getMapAsync(this)
        mBusIcon = BitmapDescriptorFactory.fromResource(R.drawable.bus_round)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)
    }

    fun subscribeReady(callback: () -> Unit) {
        mReadyCallback = callback
    }


    fun subscribeBusClick(callback: (String) -> Unit) {
        mBusClickedCallback = callback
    }

    fun subscribeStationClick(callback: (StationOnMap) -> Unit) {
        mStationClickedCallback = callback
    }

    var stationVisible: Boolean = true
    var stationVisibleSmall: Boolean = true
    val stationVisibleZoom = 17
    val stationVisibleZoomSmall = 15
    private var initPosition: LatLng? = null
    private var initZoom: Float = 12F

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mActivity, R.raw.map_style_json))
            if (!success) {
                Log.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Can't find style. Error: ", e)
        }

        mLocationButton = (mMapFragment.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(Integer.parseInt("2"))


        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.isTrafficEnabled = mTraffic


        val vrn = LatLng(51.6754966, 39.2088823)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vrn, 12f))

        mMap.setOnInfoWindowClickListener {
            mBusClickedCallback(it.snippet)
        }
        mMap.setOnMarkerClickListener {
            if (it.tag is StationOnMap) {
                mStationClickedCallback(it.tag as StationOnMap)
                return@setOnMarkerClickListener true
            }
            false
        }
        mMap.setOnCameraMoveListener {
            checkZoom()
        }
        mReadyCallback()

        mLocationButton.visibility = View.INVISIBLE


        if (initPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition, initZoom))
        } else if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.continueWith {
                val location = it.result
                if (location != null && it.isSuccessful) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    checkZoom()
                }
            }
        }

        async(UI) {
            delay(500)
            mLocationButton.visibility = View.INVISIBLE
        }
        initPosition = null
    }

    private fun checkZoom() {
        var showBig = false
        var showSmall = false
        if (mMap.cameraPosition.zoom >= stationVisibleZoom) showBig = true
        else if (mMap.cameraPosition.zoom >= stationVisibleZoomSmall) showSmall = true

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


    fun setVisibleStationBig(visible: Boolean) {
        stationVisible = visible
        mStationMarkers.forEach { it.isVisible = visible }
    }

    fun setVisibleStationSmall(visible: Boolean) {
        stationVisibleSmall = visible
        mStationMarkersSmall.forEach { it.isVisible = visible }
    }

    fun clearBusesOnMap() {
        mBusesMarkers?.forEach { it.remove() }
    }

    fun showBusesOnMap(it: List<Bus>) {
        val d = mActivity.resources.displayMetrics.density
        val size = (36 * d).toInt()
        val newBusesMarkers = it.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.route).snippet(it.getSnippet()).icon(createImageRounded(size, size, it.route, it.getAzimuth())).zIndex(1.0f).anchor(.5f, .5f))
            marker.tag = it
            marker
        }
        mBusesMarkers = newBusesMarkers
    }

    private lateinit var mStationMarkers: List<Marker>
    private lateinit var mStationMarkersSmall: List<Marker>

    fun showStations(stationsOnMap: List<StationOnMap>?) {
        if (stationsOnMap == null) return

        val showBig = mMap.cameraPosition.zoom >= stationVisibleZoomSmall
        val showSmall = mMap.cameraPosition.zoom >= stationVisibleZoom
        val icon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station)
        val iconSmall: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station_small)

        val newStationMarkers = stationsOnMap.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(icon).zIndex(1.0f).anchor(.5f, .5f).visible(showBig))
            marker.tag = it
            marker
        }
        val newStationSmallMarkers = stationsOnMap.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(iconSmall).zIndex(1.0f).anchor(.5f, .5f).visible(showSmall))
            marker.tag = it
            marker
        }
        stationVisible = showBig
        stationVisibleSmall = showSmall

        mStationMarkers = newStationMarkers
        mStationMarkersSmall = newStationSmallMarkers
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        mMap.isMyLocationEnabled = true
        mLocationButton.visibility = View.INVISIBLE
        fusedLocationClient.lastLocation.continueWith {
            val location = it.result
            if (location != null && it.isSuccessful) {
                val latLng = LatLng(location.latitude, location.longitude)
                if (location.latitude == .0) return@continueWith
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
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
                mMap.isMyLocationEnabled = true
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


    fun setTrafficJam(show: Boolean) {
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
}