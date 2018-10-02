package ru.boomik.vrnbus

import android.Manifest
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
import ru.boomik.vrnbus.ui_utils.MapManager
import ru.boomik.vrnbus.ui_utils.MenuManager
import ru.boomik.vrnbus.ui_utils.alertMultipleChoiceItems
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.android.synthetic.main.activity_maps.*
import ru.boomik.vrnbus.utils.requestPermission


class MapsActivity : AppCompatActivity() {

    private var mRoutes: String? = null
    private lateinit var menuManager: MenuManager
    private lateinit var mapManager: MapManager
    private var mSelectBusDialog: DialogPlus? = null
    private var mRoutesList: List<String>? = null


    private lateinit var mInsets: WindowInsetsCompat

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val container = findViewById<ConstraintLayout>(R.id.container)


        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            mInsets=insets
            val params = v.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = insets.systemWindowInsetTop
            params.bottomMargin = insets.systemWindowInsetBottom
            mapFragment.getMapAsync{
                it.setPadding(insets.systemWindowInsetLeft,insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets.consumeSystemWindowInsets()
        }

        mapManager = MapManager(this, mapFragment)
        mapManager.subscribeReady { onReady() }
        mapManager.subscribeBusClick { onBusClicked(it) }
        mapManager.subscribeStationClick { onStationClicked(it) }


        menuManager = MenuManager(this)
        menuManager.subscribeRefresh { onRefresh() }
        menuManager.subscribeBus { onSelectBus() }

        DataService.loadRoutes(this) {
            mRoutesList = it
        }

        busButton.setOnClickListener {
            onSelectBus()
        }

    }


    private fun onStationClicked(station: String) {

        val dialogView = View.inflate(this, R.layout.station_view, null) as LinearLayout

        val params = dialogView.getChildAt(0).layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin += mInsets.systemWindowInsetBottom

        val time: TextView = dialogView.findViewById(R.id.time)
        val title: TextView = dialogView.findViewById(R.id.title)
        val list: ListView = dialogView.findViewById(R.id.list)
        val findAll: Button = dialogView.findViewById(R.id.findAll)
        val close: ImageButton = dialogView.findViewById(R.id.close)
        findAll.visibility = View.GONE
        title.text = station


        val dialog = DialogPlus.newDialog(this)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setContentHolder(ViewHolder(dialogView))
                .setContentBackgroundResource(android.R.color.transparent)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .create()
        dialog.show()

        DataService.loadBusStopInfo(station) { stationInfo ->
            if (stationInfo == null) {
                Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                findAll.visibility = View.GONE
            } else {
                time.text = stationInfo.header
                val adapter = ArrayAdapter(this, R.layout.bus_complete_view, stationInfo.routes)
                list.adapter = adapter
                findAll.visibility = View.VISIBLE
                findAll.tag = stationInfo.routes.map { it.route }.distinct().joinToString()
            }
        }
        findAll.setOnClickListener {
            onQuerySubmit(it.tag as String)
            dialog.dismiss()
        }
        close.setOnClickListener {
            dialog.dismiss()
        }
        list.setOnItemClickListener { parent, _, position, _ ->
            run {
                val item = parent.adapter.getItem(position) as Bus
                onQuerySubmit(item.route)
                dialog.dismiss()
            }
        }

    }

    private fun onBusClicked(bus: String) {
        Toast.makeText(this, bus, Toast.LENGTH_LONG).show()
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
        nachos.addChipTerminator('.', BEHAVIOR_CHIPIFY_ALL)
        nachos.enableEditChipOnTouch(false, true)
        if (mRoutes != null && mRoutes!!.isNotEmpty()) {
            val routes = mRoutes!!.split(',').map { it.trim() }
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


        nachos.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val routes = nachos.chipValues.distinct().joinToString(",")
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
            val routes = nachos.chipValues.distinct().joinToString(",")
            onQuerySubmit(routes)
            dialog.dismiss()
            imm.hideSoftInputFromWindow(nachos.windowToken, 0)
        }


        val showList = dialogView.findViewById<ImageButton>(R.id.showList)
        showList.setOnClickListener {
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
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    enableMyLocation()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuManager.createOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val value = menuManager.optionsItemSelected(item)
        return if (!value) super.onOptionsItemSelected(item)
        else true
    }

    private fun onReady() {
        enableMyLocation()
        showBusStations()
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, Consts.LOCATION_PERMISSION_REQUEST))     mapManager.enableMyLocation()
    }

    private fun onQuerySubmit(query: String) {
        mRoutes = query
        showBuses(query)
    }

    private fun onRefresh() {
        if (mRoutes != null) showBuses(mRoutes!!)
    }


    private fun showBuses(q: String) {
        if (!q.isNotEmpty()) {
            mapManager.clearBusesOnMap()
            mapManager.clearRoutes()
            return
        }
        try {
            progress.startAnimate()
            mapManager.clearRoutes()
            Toast.makeText(this, "Загрузка", Toast.LENGTH_SHORT).show()
            if (!q.contains(',')) {
                DataService.loadRouteByName(this, q.trim()) {
                    runOnUiThread {
                        if (it != null) mapManager.showRoute(it)
                    }
                }
            }
            DataService.loadBusInfo(q) {
                if (it != null) {
                    if (it.count() == 0)
                        Toast.makeText(this, "Не найдено маршруток", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, "Загружено ${it.count()} МТС ", Toast.LENGTH_SHORT).show()
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
            menuManager.stopUpdate()
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