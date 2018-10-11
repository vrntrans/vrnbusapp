package ru.boomik.vrnbus.managers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
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
    val stationVisibleZoom = 17.5
    val stationVisibleZoomSmall = 15

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
        mLocationButton.visibility = View.INVISIBLE

        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.isTrafficEnabled = mTraffic

        val vrn = LatLng(51.6754966, 39.2088823)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vrn, 12f))

        mMap.setOnInfoWindowClickListener {
            mBusClickedCallback(it.snippet)
        }
        mMap.setOnMarkerClickListener {
            mStationClickedCallback(it.tag as StationOnMap)
            true
        }
        mMap.setOnCameraMoveListener {
            var showBig = false
            var showSmall = false
            if (mMap.cameraPosition.zoom > stationVisibleZoom) showBig = true
            else if (mMap.cameraPosition.zoom > stationVisibleZoomSmall) showSmall = true

            if (showBig) {
                if (stationVisible && !stationVisibleSmall) return@setOnCameraMoveListener
                if (!stationVisible) setVisibleStationBig(true)
                if (stationVisibleSmall) setVisibleStationSmall(false)
            } else
                if (showSmall) {
                    if (!stationVisible && stationVisibleSmall) return@setOnCameraMoveListener
                    if (stationVisible) setVisibleStationBig(false)
                    if (!stationVisibleSmall) setVisibleStationSmall(true)
                } else {
                    if (stationVisible) setVisibleStationBig(false)
                    if (stationVisibleSmall) setVisibleStationSmall(false)
                }

        }
        mReadyCallback()
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
            mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.route).snippet(it.getSnippet()).icon(createImageRounded(size, size, it.route, it.getAzimuth())).zIndex(1.0f))
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
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(icon).zIndex(1.0f).visible(showBig))
            marker.tag = it
            marker
        }
        val newStationSmallMarkers = stationsOnMap.map {
            val marker = mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.getTitle()).icon(iconSmall).zIndex(1.0f).visible(showSmall))
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
        goToMyLocation()


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
        mLocationButton.callOnClick()
    }


    fun setTrafficJam(show: Boolean) {
        mTraffic = show
       if (::mMap.isInitialized) mMap.isTrafficEnabled = show
    }
}