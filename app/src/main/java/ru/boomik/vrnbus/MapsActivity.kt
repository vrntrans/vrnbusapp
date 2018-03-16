package ru.boomik.vrnbus

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import ru.boomik.vrnbus.objects.Station




class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mRoutes: String? = null
    private lateinit var mMap: GoogleMap
    private var mBusesMarkers: List<Marker>? = null
    private lateinit var mClusterManager: ClusterManager<Station>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                mRoutes=query
                showBuses(query)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.refresh -> {
                if (mRoutes!=null) showBuses(mRoutes!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vrn, 14f))
        mMap.setOnCameraIdleListener { mClusterManager.cluster() }
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.renderer = StationRenderer()
        showBusStations()
        mMap.setOnInfoWindowClickListener {
            if (it.snippet.isNullOrEmpty()) {
                //station
                Toast.makeText(this, "${it.title}\n\n загрузка...", Toast.LENGTH_SHORT).show()
                DataService.loadBusStopInfo(it.title){
                    if (!it.isNullOrEmpty()) Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                }
            } else Toast.makeText(this, it.snippet, Toast.LENGTH_LONG).show()
        }

    }

    private fun showBuses(q : String) {
        if (!q.isNotEmpty()) return
        try {
            val bus: BitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.bus_round)

            mBusesMarkers?.forEach { it.remove() }
            Toast.makeText(this, "Загрузка", Toast.LENGTH_SHORT).show()
            DataService.loadBusInfo(q) {
                if (it!=null) {
                    if (it.count()==0)
                        Toast.makeText(this, "Не найдено маршруток", Toast.LENGTH_SHORT).show()
                    else  Toast.makeText(this, "Загружено ${it.count()} МТС ", Toast.LENGTH_SHORT).show()
                    var newBusesMarkers = it.map {
                        mMap.addMarker(MarkerOptions().position(it.getPosition()).title(it.route).snippet(it.getSnippet()).icon(bus).zIndex(1.0f)).installTag(it)
                    }
                    mBusesMarkers = newBusesMarkers
                } else {
                    Toast.makeText(this, "Не найдено маршруток", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (exception: Throwable) {
            Log.e("VrnBus", "Hm..", exception)
        }
    }

    private fun showBusStations() {
        try {
            DataService.loadBusStations(this)?.forEach { mClusterManager.addItem(it) }
            mClusterManager.cluster()
        } catch (exception: Throwable) {
            Log.e("VrnBus", "Hm..", exception)
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

private fun Marker.installTag(it: Any): Marker {
    this.tag=it
    return this
}

