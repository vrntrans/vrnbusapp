package ru.boomik.vrnbus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.canelmas.let.AskPermission
import com.canelmas.let.Let
import com.google.android.gms.maps.SupportMapFragment
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.ui_utils.MapManager
import ru.boomik.vrnbus.ui_utils.MenuManager
import ru.boomik.vrnbus.ui_utils.alertMultipleChoiceItems


class MapsActivity : AppCompatActivity() {

    private var mRoutes: String? = null
    private lateinit var menuManager: MenuManager
    private lateinit var mapManager: MapManager
    private var mSelectBusDialog: DialogPlus? = null
    private var mRoutesList: List<String>? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

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
    }

    private fun onStationClicked(station: String) {
        Toast.makeText(this, "$station\n\n загрузка...", Toast.LENGTH_SHORT).show()

        val dialogView = View.inflate(this, R.layout.station_view, null)
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
        DataService.loadBusStopInfo(station) {
            if (it == null) {
                Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                findAll.visibility = View.GONE
            } else {
                time.text = it.header
                val adapter = ArrayAdapter(this, R.layout.bus_complete_view, it.routes)
                list.adapter = adapter
                findAll.visibility = View.VISIBLE
                findAll.tag = it.routes.joinToString { it.route }
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

        val dialogView = View.inflate(this, R.layout.select_bus_dialog, null)
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
                    //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Let.handle(this, requestCode, permissions, grantResults)
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
    @AskPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun enableMyLocation() {
        mapManager.enableMyLocation()
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

            menuManager.startUpdate()
            mapManager.clearRoutes()
            Toast.makeText(this, "Загрузка", Toast.LENGTH_SHORT).show()
            if (!q.contains(',')) {
                DataService.loadRouteByName(this, q.trim()) {
                    runOnUiThread {
                        mapManager.clearRoutes()
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
                        menuManager.stopUpdate()
                    }
                } else {
                    Toast.makeText(this, "Не найдено маршруток", Toast.LENGTH_SHORT).show()
                }
                menuManager.stopUpdate()
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