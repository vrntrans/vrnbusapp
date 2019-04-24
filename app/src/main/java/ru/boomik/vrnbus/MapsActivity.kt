package ru.boomik.vrnbus

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import ru.boomik.vrnbus.dialogs.SelectBusDialog
import ru.boomik.vrnbus.dialogs.StationInfoDialog
import ru.boomik.vrnbus.dialogs.progressDialog
import ru.boomik.vrnbus.managers.*
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.color
import ru.boomik.vrnbus.utils.requestPermission
import java.util.*
import android.widget.TextView as WidgetTextView


class MapsActivity : AppCompatActivity() {

    private lateinit var menuManager: MenuManager
    private lateinit var mapManager: MapManager
    private lateinit var settingsManager: SettingsManager

    private var mActive: Boolean = true
    private lateinit var mInsets: WindowInsetsCompat

    private lateinit var timer: Timer


    override fun onCreate(savedInstanceState: Bundle?) {
        //region SetupView
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)

        DataBus.unsubscribeAll()

        settingsManager = SettingsManager
        settingsManager.initialize(this)
        AnalyticsManager.initByActivity(this, settingsManager.getBool(Consts.SETTINGS_ANALYTICS))
        val night = settingsManager.getString(Consts.SETTINGS_NIGHT)
        setUiMode(night, false)

        setContentView(R.layout.activity_drawer)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val container = findViewById<ConstraintLayout>(R.id.container)
        val zoomButtons = findViewById<LinearLayout>(R.id.zoomButtons)
        val plus = findViewById<FloatingActionButton>(R.id.plus)
        val minus = findViewById<FloatingActionButton>(R.id.minus)
        val appVersion = findViewById<WidgetTextView>(R.id.app_version)
        val osmCopyright = findViewById<WidgetTextView>(R.id.osmCopyright)
        val fragmentParent = findViewById<FrameLayout>(R.id.fragmentParent)


        val showOsm = SettingsManager.getBool(Consts.SETTINGS_OSM)
        zoomButtons.visibility = if (settingsManager.getBool(Consts.SETTINGS_ZOOM)) View.VISIBLE else View.GONE
        appVersion.text = "Версия " + getVersionString()


        osmCopyright.text = HtmlCompat.fromHtml(getString(R.string.osm_copyright), HtmlCompat.FROM_HTML_MODE_COMPACT)
        osmCopyright.movementMethod = LinkMovementMethod.getInstance()

        osmCopyright.visibility = if (showOsm) View.VISIBLE else View.GONE

// настройка поведения нижнего экрана
        //   bottomSheetBehavior = BottomSheetBehavior.from<View>(stationView)
        //     bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        if (supportFragmentManager.backStackEntryCount > 0) fragmentParent.setBackgroundColor(R.color.background.color(this))

        mapManager = MapManager(this, mapFragment)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.parseColor("#40111111")
            window.navigationBarColor = Color.parseColor("#40111111")

            ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
                val params = v.layoutParams as ViewGroup.MarginLayoutParams
                val d = resources.displayMetrics.density
                mInsets = insets
                params.leftMargin = insets.systemWindowInsetLeft
                params.topMargin = insets.systemWindowInsetTop
                params.rightMargin = insets.systemWindowInsetRight
                params.bottomMargin = insets.systemWindowInsetBottom
                app_version.setPadding((16 * d).toInt(), 0, 0, (insets.systemWindowInsetBottom + 16 * d).toInt())
                val rect = Rect(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
                mapManager.padding = rect
                fragmentParent.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)

                GlobalScope.async(Dispatchers.Main) {
                    delay(1000)
                    showWhatsNew(this@MapsActivity, insets)
                }

                insets.consumeSystemWindowInsets()
            }
        } else {
            mInsets = WindowInsetsCompat(null)
            fragmentParent.setPadding(0, resources.getDimension(R.dimen.activity_vertical_margin).toInt(), 0, 0)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val count = supportFragmentManager.backStackEntryCount
            val colorFill = R.color.background.color(this)
            val colorTransparent = android.R.color.transparent.color(this)

            val colorFrom = if (count > 0) colorTransparent else colorFill
            val colorTo = if (count == 0) colorTransparent else colorFill
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.duration = 250 // milliseconds
            colorAnimation.addUpdateListener {
                fragmentParent.setBackgroundColor(it.animatedValue as Int)
            }
            colorAnimation.start()
        }

        menu.setOnClickListener {
            if (drawer_layout.isDrawerOpen(GravityCompat.START)) drawer_layout.closeDrawer(GravityCompat.START) else drawer_layout.openDrawer(GravityCompat.START)
        }

        myLocation.setOnClickListener {
            goToMyLocation()
        }


        busButton.setOnClickListener {
            SelectBusDialog.show(this, mRoutes, mInsets) { routes ->
                onQuerySubmit(routes)
            }
        }

        //endregion

        mapManager.subscribeReady {
            Toast.makeText(this@MapsActivity, "Выберите на карте остановку или номер маршрута нажав кнопку с автобусом", Toast.LENGTH_LONG).show()
        }

        menuManager = MenuManager(this)
        menuManager.initialize(nav_view)


        settingsManager.loadPreferences()


        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                updateBuses()
            }
        }, 0, 30 * 1000)
        restoreInstanceState(savedInstanceState)

        plus.setOnClickListener {
            mapManager.zoomIn()
            AnalyticsManager.logEvent("zoom", "in")
        }
        minus.setOnClickListener {
            mapManager.zoomOut()
            AnalyticsManager.logEvent("zoom", "out")
        }

        DataBus.subscribe<String>(Consts.SETTINGS_REFERER) { DataService.setReferer(it.data) }
        DataBus.subscribe<Bus>(DataBus.BusClick) { onBusClicked(it.data.route) }
        DataBus.subscribe<StationOnMap>(DataBus.StationClick) { onStationClicked(it.data) }
        DataBus.subscribe<Boolean>(Consts.SETTINGS_ZOOM) { zoomButtons.visibility = if (it.data) View.VISIBLE else View.GONE }
        DataBus.subscribe<String>(Consts.SETTINGS_NIGHT) { setUiMode(it.data, true) }
        DataBus.subscribe<String>(DataBus.ResetRoutes) { resetRoutes(it.data) }
        DataBus.subscribe<String>(DataBus.AddRoutes) { addRoutes(it.data) }


    }

    private fun addRoutes(route: String) {
        val searchRoutes: String
        searchRoutes = if (mRoutes.isNotEmpty()) {
            val routesString = "$mRoutes, $route"
            val routes = routesString.split(',').asSequence().distinct().map { it.trim() }.toList()
            routes.joinToString()
        } else route
        onQuerySubmit(searchRoutes)
    }

    private fun resetRoutes(route: String) {
        onQuerySubmit(route)
    }

    private fun setUiMode(data: String?, needRecreate: Boolean = false) {
        val mode = when (data?.toIntOrNull()) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            1 -> AppCompatDelegate.MODE_NIGHT_AUTO
            2 -> AppCompatDelegate.MODE_NIGHT_NO
            3 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        if (needRecreate) {
            recreate()
        }
    }

    private fun getVersionString(): CharSequence? {
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            val versionCode = pInfo.versionCode
            return "$version ($versionCode)"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun onStationClicked(stationOnMap: StationOnMap) {
        StationInfoDialog.show(this, mInsets, stationOnMap) {
            onQuerySubmit(it)
        }
    }


    private var mAutoUpdateRoutes: Boolean = true
    private var mRoutes: String = ""
    private var mRouteUpdate: String = ""

    private fun updateBuses() {

        val stationId = DataStorageManager.searchStationId
        if (!mAutoUpdateRoutes || (mRoutes.isBlank() && stationId > 0) || !mActive) return
        if (stationId > 0) runOnUiThread { showBusesFromStation(stationId) }
        else if (!mRoutes.isBlank()) runOnUiThread { showBuses(mRoutes) }
    }


    override fun onResume() {
        super.onResume()

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        mActive = true
        updateBuses()

    }


    override fun onStart() {
        super.onStart()


        GlobalScope.async(Dispatchers.Main) {

            Log.e("Loaded start 2")
            var dialog: AlertDialog? = null
            try {
                dialog = progressDialog(this@MapsActivity)
                DataStorageManager.load(this@MapsActivity.applicationContext)
            } catch (e: Throwable) {
                // if one of the long operation throw, this should be called once, even if multiple long operations failed.

                Log.e("something went wrong", e)
            }
            dialog?.hide()
            showBusStations()
            Log.e("Loaded done 2")
        }
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
        DataBus.unsubscribeAll()
        val map = supportFragmentManager?.findFragmentById(R.id.map)
        if (map != null) supportFragmentManager.beginTransaction().remove(map).commit()

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        AnalyticsManager.logPermission(requestCode, permissions, grantResults)
        when (requestCode) {
            Consts.LOCATION_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    goToMyLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun goToMyLocation() {
        if (requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, Consts.LOCATION_PERMISSION_REQUEST)) mapManager.goToMyLocation()
    }

    private fun onQuerySubmit(query: String) {
        mRoutes = query
        showBuses(query)
    }

    private fun showBusesFromStation(stationId: Int) {
        try {
            mapManager.clearRoutes()

            GlobalScope.async(Dispatchers.Main) {
                progress.startAnimate()
                val stationInfo = DataService.loadArrivalInfoAsync(stationId).await()
                if (stationInfo != null) {
                    val buses = stationInfo.buses
                    DataBus.sendEvent(DataBus.BusToMap, buses)
                }
                progress.stopAnimate()
            }

        } catch (exception: Throwable) {
            Log.e("Hm..", exception)
            progress.stopAnimate()
        }
    }


    private fun showBuses(q: String) {
        DataStorageManager.searchStationId=-1
        mAutoUpdateRoutes = true
        if (q.isEmpty()) {
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
            GlobalScope.async(Dispatchers.Main) {
                delay(500)
                val data = DataStorageManager.loadBusStations(this@MapsActivity)
                runOnUiThread {
                    mapManager.showStations(data)
                }
            }
        } catch (exception: Throwable) {
            Log.e("VrnBus", "Hm..", exception)
        }
    }
}