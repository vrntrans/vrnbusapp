package ru.boomik.vrnbus

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.gms.maps.SupportMapFragment
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.managers.MapManager
import ru.boomik.vrnbus.managers.MenuManager
import ru.boomik.vrnbus.utils.alertMultipleChoiceItems
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.requestPermission
import java.util.*


class MapsActivity : AppCompatActivity() {

    private var mRoutes: String = ""
    private lateinit var menuManager: MenuManager
    private lateinit var mapManager: MapManager
    private var mSelectBusDialog: DialogPlus? = null
    private var mRoutesList: List<String>? = null


    private var mActive: Boolean = true
    private lateinit var mInsets: WindowInsetsCompat

    private lateinit var timer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_drawer)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val container = findViewById<ConstraintLayout>(R.id.container)

        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            mInsets = insets
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = insets.systemWindowInsetTop
            params.bottomMargin = insets.systemWindowInsetBottom
            params.leftMargin = insets.systemWindowInsetLeft
            params.rightMargin = insets.systemWindowInsetRight
            mapFragment.getMapAsync {
                it.setPadding(insets.systemWindowInsetLeft, (insets.systemWindowInsetTop + (32 + 40) * resources.displayMetrics.density).toInt(), insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
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

        mapManager = MapManager(this, mapFragment)
        mapManager.subscribeReady { onReady() }
        mapManager.subscribeBusClick { onBusClicked(it) }
        mapManager.subscribeStationClick { onStationClicked(it) }

        menuManager = MenuManager(this)
        menuManager.createOptionsMenu(nav_view)

        SettingsManager.instance.initByActivity(this)
        SettingsManager.instance.setManagers(menuManager, mapManager)

        DataService.loadRoutes(this) {
            mRoutesList = it
        }

        busButton.setOnClickListener {
            onSelectBus()
        }
        Toast.makeText(this, "Выберите на карте остановку или номер маршрута нажав кнопку с автобусом", Toast.LENGTH_LONG).show()

        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                updateBuses()
            }
        }, 0, 30 * 1000)
        restoreInstanceState(savedInstanceState)
    }

    private fun updateBuses() {

        if (!mAutoUpdateRoutes || mRoutes.isBlank() || !mActive) return
        runOnUiThread { showBuses(mRoutes) }
    }


    override fun onResume() {
        super.onResume()
        mActive = true
        updateBuses()
    }

    override fun onPause() {
        super.onPause()
        mActive = false
    }


    private var mActiveStationId: Int = 0

    private fun onStationClicked(station: StationOnMap) {

        mapManager.clearRoutes()
        mActiveStationId = station.id
        val dialogView = View.inflate(this, R.layout.station_view, null) as LinearLayout

        val params = dialogView.getChildAt(0).layoutParams as ViewGroup.MarginLayoutParams
        if (::mInsets.isInitialized) params.bottomMargin += mInsets.systemWindowInsetBottom

        val time: TextView = dialogView.findViewById(R.id.time)
        val title: TextView = dialogView.findViewById(R.id.title)
        val list: ListView = dialogView.findViewById(R.id.list)
        val close: ImageButton = dialogView.findViewById(R.id.close)
        val progress: ProgressBar = dialogView.findViewById(R.id.progress)
        val progressIndeterminate: ProgressBar = dialogView.findViewById(R.id.progressIndeterminate)
        TextViewCompat.setAutoSizeTextTypeWithDefaults(title, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
        title.text = station.name
        progress.isIndeterminate = false

        val dialog = DialogPlus.newDialog(this)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setContentHolder(ViewHolder(dialogView))
                .setContentBackgroundResource(android.R.color.transparent)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .create()
        dialog.show()

        close.setOnClickListener {
            dialog.dismiss()
            mActiveStationId = 0
        }

        list.setOnItemClickListener { parent, _, position, _ ->
            val adapter: RoutesAdapter? = parent.adapter as? RoutesAdapter
                    ?: return@setOnItemClickListener
            val item = parent.adapter.getItem(position) as Bus
            val routes = mRoutes.split(',').asSequence().distinct().map { it.trim() }.toList()
            val containAll = adapter?.dataEquals(mRoutes) ?: false
            if (containAll) onQuerySubmit(item.route)
            else if (!routes.contains(item.route))
                onQuerySubmit(if (mRoutes.isNotEmpty()) "$mRoutes, ${item.route}" else item.route)

        }
        list.setOnItemLongClickListener { parent, _, position, _ ->
            run {
                val item = parent.adapter.getItem(position) as Bus
                onQuerySubmit(item.route)
                dialog.dismiss()
                mActiveStationId = 0
            }
            true
        }

        startUpdateStationInfo(station, dialog, time, list, progress, progressIndeterminate)


    }

    private var mAutoUpdateRoutes: Boolean = true

    private fun startUpdateStationInfo(station: StationOnMap, dialog: DialogPlus?, time: TextView, list: ListView, progress: ProgressBar, progressIndeterminate: ProgressBar) {

        launch(UI) {
            var first = true
            var ok = true
            val anim = ValueAnimator.ofInt(30)
            anim.addUpdateListener { progress.progress = it.animatedValue as Int }
            anim.duration = 30000

            progressIndeterminate.visibility = View.GONE
            do {
                try {
                    if (dialog == null || !dialog.isShowing || mActiveStationId != station.id) {
                        ok = false
                    } else {
                        progressIndeterminate.visibility = View.VISIBLE
                        progress.visibility = View.GONE

                        val stationInfo = DataService.loadArrivalInfoAsync(station.id).await()
                        time.text = stationInfo.time
                        val adapter = RoutesAdapter(this@MapsActivity, stationInfo.routes)
                        list.adapter = adapter
                        if (first) {
                            mapManager.clearBusesOnMap()
                            mapManager.showBusesOnMap(stationInfo.buses)
                            mRoutes = stationInfo.routes.joinToString(", ") { it.route }
                            mAutoUpdateRoutes = false
                        } else {
                            val containAll = adapter.dataEquals(mRoutes)
                            if (containAll) {
                                mRoutes = stationInfo.routes.joinToString(", ") { it.route }
                                mapManager.clearBusesOnMap()
                                mapManager.showBusesOnMap(stationInfo.buses)
                                mAutoUpdateRoutes = false

                            }
                        }
                        progressIndeterminate.visibility = View.GONE
                        progress.visibility = View.VISIBLE
                        anim.start()
                        anim.resume()
                        delay(30000)
                    }
                } catch (e: Exception) {
                    if (first) {
                        Toast.makeText(this@MapsActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                        dialog?.dismiss()
                        if (mActiveStationId == station.id) mActiveStationId = 0
                        anim.cancel()
                    }
                    delay(10000)
                    progressIndeterminate.visibility = View.GONE
                    progress.visibility = View.VISIBLE
                    anim.start()
                    anim.resume()
                }
                first = false
            } while (ok)
        }
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

    private fun restoreInstanceState(savedInstanceState : Bundle?) {
        mapManager.restoreInstanceStateBundle(savedInstanceState)
    }

    public override fun onDestroy() {
        super.onDestroy()
        val map = supportFragmentManager?.findFragmentById(R.id.map)
        if (map != null) supportFragmentManager.beginTransaction().remove(map).commit()

    }

    private fun onSelectBus() {

        if (mRoutesList == null) {
            Toast.makeText(this, "Дождитесь загрузки данных", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = View.inflate(this, R.layout.select_bus_dialog, null) as LinearLayout
        val params = dialogView.getChildAt(0).layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin += mInsets.systemWindowInsetTop


        val adapter = ArrayAdapter(this, R.layout.bus_complete_view, mRoutesList!!)
        val nachos = dialogView.findViewById<NachoTextView>(R.id.nacho_text_view)
        nachos.setAdapter(adapter)
        nachos.addChipTerminator(',', BEHAVIOR_CHIPIFY_ALL)
        nachos.addChipTerminator(' ', BEHAVIOR_CHIPIFY_ALL)
        nachos.addChipTerminator(';', BEHAVIOR_CHIPIFY_ALL)
        nachos.enableEditChipOnTouch(false, true)
        if (mRoutes.isNotEmpty()) {
            val routes = mRoutes.split(',').asSequence().distinct().map { it.trim() }.toList()
            nachos.setText(routes)
        }

        nachos.imeOptions = EditorInfo.IME_ACTION_SEARCH
        nachos.setRawInputType(InputType.TYPE_CLASS_TEXT)

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        val dialog = DialogPlus.newDialog(this)
                .setGravity(Gravity.TOP)
                .setCancelable(true)
                .setOnDismissListener {
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                    nachos.clearFocus()
                }
                .setOnCancelListener {
                    imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                    nachos.clearFocus()
                }
                .setContentHolder(ViewHolder(dialogView))
                .setContentBackgroundResource(android.R.color.transparent)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .create()

        nachos.setOnFocusChangeListener { _, _ ->
            nachos.chipifyAllUnterminatedTokens()
        }

        nachos.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                nachos.chipifyAllUnterminatedTokens()
                val routes = nachos.chipValues.asSequence().distinct().joinToString(",")
                onQuerySubmit(routes)
                dialog.dismiss()
                nachos.clearFocus()
                imm.hideSoftInputFromWindow(nachos.windowToken, 0)
                true
            } else
                false
        }

        val searchButton = dialogView.findViewById<Button>(R.id.search)
        searchButton.setOnClickListener {
            nachos.chipifyAllUnterminatedTokens()
            val routes = nachos.chipValues.asSequence().distinct().joinToString(",")
            onQuerySubmit(routes)
            dialog.dismiss()
            imm.hideSoftInputFromWindow(nachos.windowToken, 0)
        }


        val showList = dialogView.findViewById<ImageButton>(R.id.showList)
        showList.setOnClickListener { _ ->
            nachos.chipifyAllUnterminatedTokens()
            alertMultipleChoiceItems(this, mRoutesList!!) {
                if (it != null) {
                    val buses = nachos.chipValues
                    buses.addAll(it)
                    nachos.setText(buses.distinct())
                }
            }
        }

        dialog.show()
        mSelectBusDialog = dialog

        nachos.requestFocus()
        imm.showSoftInput(nachos, InputMethodManager.SHOW_FORCED)
    }

    override fun onBackPressed() {
        if (mSelectBusDialog != null && mSelectBusDialog!!.isShowing) mSelectBusDialog?.dismiss()
        else super.onBackPressed()
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

    private fun onReady() {
        showBusStations()
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