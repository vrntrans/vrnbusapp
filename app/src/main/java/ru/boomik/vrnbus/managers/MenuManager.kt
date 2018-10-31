package ru.boomik.vrnbus.managers

import android.app.Activity
import android.view.MenuItem
import android.widget.Switch
import com.google.android.material.navigation.NavigationView
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.utils.alertEnterText


class MenuManager(activity: Activity) : NavigationView.OnNavigationItemSelectedListener {

    private var mActivity: Activity = activity

    private lateinit var mTrafficJamSwitchedCallback: (Boolean) -> Unit
    private lateinit var mBusClickedCallback: () -> Unit

    private lateinit var mTrafficJam: MenuItem

    private lateinit var mTrafficSwitch: Switch

    fun createOptionsMenu(nav_view: NavigationView) {
        nav_view.setNavigationItemSelectedListener(this)

        mTrafficJam = nav_view.menu.findItem(R.id.traffic_jam)
        val switchView = mTrafficJam.actionView as Switch
        switchView.setOnCheckedChangeListener { _, isChecked -> mTrafficJamSwitchedCallback(isChecked) }
        mTrafficSwitch = switchView
    }

    fun subscribeTrafficJam(callback: (Boolean) -> Unit) {
        mTrafficJamSwitchedCallback = callback
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
         return when (item.itemId) {
            R.id.referer -> {
                setReferer()
                true
            }
            else -> false
        }
    }

    private fun setReferer() {
        alertEnterText(mActivity, mActivity.getString(R.string.enter_referer)) {
            SettingsManager.instance.setReferer(it)
        }
    }

    fun setTrafficJam(traffic: Boolean) {
        mTrafficSwitch.isChecked = traffic
    }

}