package ru.boomik.vrnbus.ui_utils

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import ru.boomik.vrnbus.DataService
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Station


/**
 * Created by boomv on 18.03.2018.
 */
class MapManager(activity: Activity, mapFragment: SupportMapFragment) : OnMapReadyCallback {
    private var mActivity: Activity = activity
    private var mMapFragment: SupportMapFragment = mapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mClusterManager: ClusterManager<Station>
    private lateinit var mReadyCallback: () -> Unit

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
            if (it.snippet.isNullOrEmpty()) {
                //station
                Toast.makeText(mActivity, "${it.title}\n\n загрузка...", Toast.LENGTH_SHORT).show()
                DataService.loadBusStopInfo(it.title){
                    if (!it.isNullOrEmpty()) Toast.makeText(mActivity, it, Toast.LENGTH_LONG).show()
                }
            } else Toast.makeText(mActivity, it.snippet, Toast.LENGTH_LONG).show()
        }
        mClusterManager.setOnClusterClickListener { cluster ->
            mMap.animateCamera(getMapCameraUpdateFromCluster(cluster), 300, null)
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.position, mMap.cameraPosition.zoom + 1.toFloat()), 300, null)
            true
        }

        mMap.setOnMarkerClickListener(mClusterManager);
        mReadyCallback()
    }

    private fun getMapCameraUpdateFromCluster(cluster : Cluster<Station> ) : CameraUpdate{

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

    private inner class StationRenderer : DefaultClusterRenderer<Station>(mActivity.applicationContext, mMap, mClusterManager) {

        private val mClusterIconGenerator = IconGenerator(mActivity.applicationContext)
        private val mIcon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station)

        override fun onBeforeClusterItemRendered(station: Station?, markerOptions: MarkerOptions?) {
            markerOptions!!.icon(mIcon)
        }

        override fun onBeforeClusterRendered(cluster: Cluster<Station>, markerOptions: MarkerOptions) {
            val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }
    }

    fun showBusesOnMap(it: List<Bus>) {
        var newBusesMarkers = it.map {
            mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.route).snippet(it.getSnippet()).icon(mBusIcon).zIndex(1.0f))
        }
        mBusesMarkers = newBusesMarkers
    }

    fun showStations(stations: List<Station>?) {
        if (stations==null) return
        stations.forEach { mClusterManager.addItem(it) }
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