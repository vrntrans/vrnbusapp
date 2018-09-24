package ru.boomik.vrnbus.ui_utils

import android.annotation.SuppressLint
import android.app.Activity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.createImageRounded


/**
 * Created by boomv on 18.03.2018.
 */
class MapManager(activity: Activity, mapFragment: SupportMapFragment) : OnMapReadyCallback {
    private var mActivity: Activity = activity
    private var mMapFragment: SupportMapFragment = mapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mClusterManager: ClusterManager<StationOnMap>
    private lateinit var mReadyCallback: () -> Unit
    private lateinit var mBusClickedCallback: (String) -> Unit
    private lateinit var mStationClickedCallback: (String) -> Unit

    private var mBusesMarkers: List<Marker>? = null
    private val mBusIcon: BitmapDescriptor

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

    fun subscribeStationClick(callback: (String) -> Unit) {
        mStationClickedCallback = callback
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled=true
        mMap.uiSettings.isCompassEnabled=true
        mMap.uiSettings.isMapToolbarEnabled=true
        mClusterManager = ClusterManager(mActivity, mMap)
        val vrn = LatLng(51.6754966, 39.2088823)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vrn, 12f))
        mMap.setOnCameraIdleListener { mClusterManager.cluster() }
        mClusterManager.renderer = StationRenderer()

        mMap.setOnInfoWindowClickListener {
          /*  if (it.snippet.isNullOrEmpty()) {
                mStationClickedCallback(it.title)
            } else */
                mBusClickedCallback(it.snippet)
        }
        mClusterManager.setOnClusterClickListener { cluster ->
            mMap.animateCamera(getMapCameraUpdateFromCluster(cluster), 300, null)
            true
        }
        mClusterManager.setOnClusterItemClickListener {
            mStationClickedCallback(it.name)
            true
        }
        mMap.setOnMarkerClickListener(mClusterManager);
        mReadyCallback()
    }

    private fun getMapCameraUpdateFromCluster(cluster : Cluster<StationOnMap> ) : CameraUpdate{

        val list = cluster.items
        if (list == null || list.count()==0) return CameraUpdateFactory.newLatLngZoom(cluster.position, mMap.cameraPosition.zoom + 1.toFloat())
        val minLat = list.minBy { it.lat }!!.lat
        val minLon = list.minBy { it.lon }!!.lon
        val maxLat = list.maxBy { it.lat }!!.lat
        val maxLon = list.maxBy { it.lon }!!.lon

        return CameraUpdateFactory.newLatLngBounds(LatLngBounds(LatLng(minLat, minLon), LatLng(maxLat, maxLon)), 16)
    }

    fun clearBusesOnMap(){
        mBusesMarkers?.forEach { it.remove() }
    }

    private inner class StationRenderer : DefaultClusterRenderer<StationOnMap>(mActivity.applicationContext, mMap, mClusterManager) {

        private val mClusterIconGenerator = IconGenerator(mActivity.applicationContext)
        private val mIcon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station)

        override fun onBeforeClusterItemRendered(stationOnMap: StationOnMap?, markerOptions: MarkerOptions?) {
            markerOptions!!.icon(mIcon)
        }

        override fun onBeforeClusterRendered(cluster: Cluster<StationOnMap>, markerOptions: MarkerOptions) {
            val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }
    }

    fun showBusesOnMap(it: List<Bus>) {
        val d = mActivity.resources.displayMetrics.density
        val size = (36*d).toInt()
        val newBusesMarkers = it.map {
            //mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.route).snippet(it.getSnippet()).icon(mBusIcon).zIndex(1.0f))
            mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.route).snippet(it.getSnippet()).icon(createImageRounded(mActivity, size, size, it.route, it.getAzimuth())).zIndex(1.0f))
        }
        mBusesMarkers = newBusesMarkers
    }

    fun showStations(stationsOnMap: List<StationOnMap>?) {
        if (stationsOnMap==null) return
        stationsOnMap.forEach { mClusterManager.addItem(it) }
        mClusterManager.cluster()
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        mMap.isMyLocationEnabled=true
        fusedLocationClient.lastLocation.continueWith {
            var location = it.result
            if (location!=null && it.isSuccessful) {
                val vrn = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vrn, 16f))
            }
        }


    }
}