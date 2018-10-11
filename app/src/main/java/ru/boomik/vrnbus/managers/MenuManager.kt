package ru.boomik.vrnbus.managers

import android.app.Activity
import android.view.MenuItem
import android.widget.Switch
import com.google.android.material.navigation.NavigationView
import ru.boomik.vrnbus.Log
import ru.boomik.vrnbus.R


class MenuManager(activity: Activity) {


    private var mActivity: Activity = activity

    private lateinit var mTrafficJamSwitchedCallback: (Boolean) -> Unit
    private lateinit var mBusClickedCallback: () -> Unit

    private lateinit var mTrafficJam: MenuItem

    private lateinit var mTrafficSwitch: Switch

    fun createOptionsMenu(nav_view: NavigationView) {
        mTrafficJam = nav_view.menu.findItem(R.id.traffic_jam)
        val switchView = mTrafficJam.actionView as Switch
        switchView.setOnCheckedChangeListener { _, isChecked -> mTrafficJamSwitchedCallback(isChecked) }
        mTrafficSwitch = switchView
    }

    fun subscribeTrafficJam(callback: (Boolean) -> Unit) {
        mTrafficJamSwitchedCallback = callback
    }


    fun optionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                true
            }
            R.id.select_bus -> {
                mBusClickedCallback()
                true
            }
            else -> false
        }
    }

    fun setTrafficJam(traffic: Boolean) {
        mTrafficSwitch.isChecked = traffic
    }

}