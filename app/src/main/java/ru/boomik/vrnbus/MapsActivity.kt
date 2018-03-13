package ru.boomik.vrnbus

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.beust.klaxon.Klaxon
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import ru.boomik.vrnbus.objects.Station
import ru.boomik.vrnbus.utils.loadJSONFromAsset




class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager : ClusterManager<Station>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val vrn = LatLng(51.6754966, 39.2088823)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vrn,14f))
        mMap.setOnCameraIdleListener { mClusterManager.cluster() }
        mClusterManager  = ClusterManager(this, mMap)
        mClusterManager.renderer = StationRenderer()
        showBusStations()
    }

    private fun showBusStations() {
        try {
            val json = loadJSONFromAsset(this, "bus_stops.json")
            val stations = Klaxon().parseArray<Station>(json) ?: return

            stations.filter { it.lat != 0.0 && it.lon != 0.0 }
                    .forEach { mClusterManager.addItem(it) }
            mClusterManager.cluster()
        } catch (exception : Throwable) {
            Log.e("VrnBus", "Hm..",exception);
        }
    }


    private inner class StationRenderer : DefaultClusterRenderer<Station>(applicationContext, mMap, mClusterManager) {

        private val mClusterIconGenerator = IconGenerator(applicationContext)
        private val mIcon: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_station)

        override fun onBeforeClusterItemRendered(station: Station?, markerOptions: MarkerOptions?) {
            markerOptions!!.icon(mIcon)
        }

        override fun onBeforeClusterRendered(cluster: Cluster<Station>, markerOptions: MarkerOptions) {
            val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }
    }
}

