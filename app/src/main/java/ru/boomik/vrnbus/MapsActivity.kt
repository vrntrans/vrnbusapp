package ru.boomik.vrnbus

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import com.canelmas.let.AskPermission
import com.canelmas.let.Let
import com.google.android.gms.maps.SupportMapFragment
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.OnClickListener
import com.orhanobut.dialogplus.ViewHolder
import ru.boomik.vrnbus.ui_utils.MapManager
import ru.boomik.vrnbus.ui_utils.MenuManager


class MapsActivity : AppCompatActivity() {

    private var mRoutes: String? = null
    private lateinit var menuManager: MenuManager
    private lateinit var mapManager: MapManager
    private var mSelectBusDialog: DialogPlus? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        menuManager = MenuManager(this)
        mapManager = MapManager(this, mapFragment)

        menuManager.subscribeRefresh { onRefresh() }
        menuManager.subscribeBus { onSelectBus() }
        menuManager.subscribeQuerySubmit { onQuerySubmit(it) }

        mapManager.subscribeReady { onReady() }
    }


    private fun onSelectBus() {

        val clickListener = OnClickListener { dialog, view ->
            //        switch (view.getId()) {
            //          case R.id.header_container:
            //            Toast.makeText(MainActivity.this, "Header clicked", Toast.LENGTH_LONG).show();
            //            break;
            //          case R.id.like_it_button:
            //            Toast.makeText(MainActivity.this, "We're glad that you like it", Toast.LENGTH_LONG).show();
            //            break;
            //          case R.id.love_it_button:
            //            Toast.makeText(MainActivity.this, "We're glad that you love it", Toast.LENGTH_LONG).show();
            //            break;
            //          case R.id.footer_confirm_button:
            //            Toast.makeText(MainActivity.this, "Confirm button clicked", Toast.LENGTH_LONG).show();
            //            break;
            //          case R.id.footer_close_button:
            //            Toast.makeText(MainActivity.this, "Close button clicked", Toast.LENGTH_LONG).show();
            //            break;
            //        }
            //        dialog.dismiss();
        }

        val dialog = DialogPlus.newDialog(this)
                .setGravity(Gravity.TOP)
                .setCancelable(true)
                .setOnClickListener(clickListener)
                .setContentHolder(ViewHolder(R.layout.select_bus_dialog))
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                //.setOverlayBackgroundResource(R.drawable.corner_background)
                .setOnItemClickListener { dialog, item, view, position ->
                        Toast.makeText(this,"itemClick", Toast.LENGTH_SHORT).show()
                }  // This will enable the expand feature, (similar to android L share dialog)
                .create()
        dialog.show()
        mSelectBusDialog = dialog
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
            return
        }
        try {
            menuManager.startUpdate()
            Toast.makeText(this, "Загрузка", Toast.LENGTH_SHORT).show()
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
            }
        } catch (exception: Throwable) {
            Log.e("Hm..", exception)
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


