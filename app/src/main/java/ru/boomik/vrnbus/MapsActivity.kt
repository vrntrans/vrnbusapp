package ru.boomik.vrnbus

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.activity_maps.*
import ru.boomik.vrnbus.dialogs.SelectBusDialog
import ru.boomik.vrnbus.dialogs.StationInfoDialog
import ru.boomik.vrnbus.managers.*
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.requestPermission
import java.util.*


class MapsActivity : AppCompatActivity() {

    private var mRoutes: String = ""
    private lateinit var menuManager: MenuManager
    private lateinit var mapManager: MapManager
    //private lateinit var mapManager: MapManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var dataStorageManager: DataStorageManager

    private var mActive: Boolean = true
    private lateinit var mInsets: WindowInsetsCompat

    private lateinit var timer: Timer


    override fun onCreate(savedInstanceState: Bundle?) {
        //region SetupView
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN


        setContentView(R.layout.activity_drawer)
        //  val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val container = findViewById<ConstraintLayout>(R.id.container)
        //val mapview = findViewById<MapView>(R.id.mapview)

        window.statusBarColor = Color.parseColor("#40111111")
        window.navigationBarColor = Color.parseColor("#40111111")
        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            mInsets = insets
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = insets.systemWindowInsetTop
            params.bottomMargin = insets.systemWindowInsetBottom
            params.leftMargin = insets.systemWindowInsetLeft
            params.rightMargin = insets.systemWindowInsetRight

            mapFragment.getMapAsync {
                it.setPadding((insets.systemWindowInsetLeft + 8 * resources.displayMetrics.density).toInt(), (insets.systemWindowInsetTop + (32 + 40) * resources.displayMetrics.density).toInt(), insets.systemWindowInsetRight, (-30* resources.displayMetrics.density).toInt())
            }
            insets.consumeSystemWindowInsets()
        }

        menu.setOnClickListener {
            if (drawer_layout.isDrawerOpen(GravityCompat.START)) drawer_layout.closeDrawer(GravityCompat.START) else drawer_layout.openDrawer(GravityCompat.START)
        }

        myLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mapManager.goToMyLocation()
            else enableMyLocation()
        }


        busButton.setOnClickListener {
            SelectBusDialog.show(this, mRoutes, mInsets) { routes ->
                onQuerySubmit(routes)
            }
        }

        //endregion

        mapManager = MapManager(this, mapFragment)


        mapManager.subscribeReady {
           // Toast.makeText(this@MapsActivity, "Выберите на карте остановку или номер маршрута нажав кнопку с автобусом", Toast.LENGTH_LONG).show()
            showBusStations()
        }

        menuManager = MenuManager(this)
        menuManager.initialize(nav_view)

        settingsManager = SettingsManager()


        DataBus.subscribe<String?>(DataBus.Referer) { DataService.setReferer(it) }
        DataBus.subscribe<Bus>(DataBus.BusClick) { onBusClicked(it.route) }
        DataBus.subscribe<StationOnMap>(DataBus.StationClick) { onStationClicked(it) }

        dataStorageManager = DataStorageManager()
        dataStorageManager.setActivity(this)



        settingsManager.initialize(this)
        dataStorageManager.load()

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                updateBuses()
            }
        }, 0, 30 * 1000)
        restoreInstanceState(savedInstanceState)




    }

    private fun onStationClicked(stationOnMap: StationOnMap) {
        StationInfoDialog.show(this, mInsets, stationOnMap) {
            onQuerySubmit(it)
        }
    }


    private var mAutoUpdateRoutes: Boolean = true

    private fun updateBuses() {

        if (!mAutoUpdateRoutes || mRoutes.isBlank() || !mActive) return
        runOnUiThread { showBuses(mRoutes) }
    }


    override fun onResume() {
        super.onResume()
        mActive = true
        updateBuses()
        // mapManager.resume()
    }

    override fun onPause() {
        super.onPause()
        mActive = false
        // mapManager.pause()
    }


    private fun onBusClicked(bus: String) {
        Toast.makeText(this, bus, Toast.LENGTH_LONG).show()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        mapManager.getInstanceStateBundle(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreInstanceState(savedInstanceState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        mapManager.restoreInstanceStateBundle(savedInstanceState)
    }

    public override fun onDestroy() {
        super.onDestroy()
        val map = supportFragmentManager?.findFragmentById(R.id.map)
        if (map != null) supportFragmentManager.beginTransaction().remove(map).commit()

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Consts.LOCATION_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    enableMyLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, Consts.LOCATION_PERMISSION_REQUEST)) mapManager.enableMyLocation()
    }

    private fun onQuerySubmit(query: String) {
        mRoutes = query
        showBuses(query)
    }

    private fun showBuses(q: String) {
        mAutoUpdateRoutes = true
        if (!q.isNotEmpty()) {
            mapManager.clearBusesOnMap()
            mapManager.clearRoutes()
            return
        }
        try {
            progress.startAnimate()
            mapManager.clearRoutes()
            if (!q.contains(',')) {
                DataService.loadRouteByName(this, q.trim()) {
                    runOnUiThread {
                        if (it != null) mapManager.showRoute(it)
                    }
                }
            }
            Toast.makeText(this, "Поиск маршрутов:\n$q", Toast.LENGTH_SHORT).show()
            DataService.loadBusInfo(q) {
                if (it != null) {
                    if (it.count() == 0)
                        Toast.makeText(this, "Не найдено МТС на выбранных маршрутах", Toast.LENGTH_SHORT).show()
                    //  else Toast.makeText(this, "Загружено ${it.count()} МТС ", Toast.LENGTH_SHORT).show()
                    runOnUiThread {
                        mapManager.clearBusesOnMap()
                        mapManager.showBusesOnMap(it)
                    }
                } else {
                    Toast.makeText(this, "Не найдено маршруток", Toast.LENGTH_SHORT).show()
                }
                progress.stopAnimate()
            }
        } catch (exception: Throwable) {
            Log.e("Hm..", exception)
            progress.stopAnimate()
        }
    }

    private fun showBusStations() {
        try {
            DataService.loadBusStations(this) {
                runOnUiThread {
                    mapManager.showStations(it)
                }
            }
        } catch (exception: Throwable) {
            Log.e("VrnBus", "Hm..", exception)
        }
    }
}