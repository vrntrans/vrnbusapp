package ru.boomik.vrnbus.managers

import android.app.Activity
import android.view.MenuItem
import android.widget.Switch
import com.google.android.material.navigation.NavigationView
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.dialogs.alertEnterText


class MenuManager(private val activity: Activity) : NavigationView.OnNavigationItemSelectedListener {

    fun initialize(nav_view: NavigationView) {
        nav_view.setNavigationItemSelectedListener(this)

        val trafficJamItem = nav_view.menu.findItem(R.id.traffic_jam)
        val switchView = trafficJamItem.actionView as Switch
        switchView.setOnCheckedChangeListener { _, isChecked -> DataBus.sendEvent(DataBus.Traffic, isChecked) }
        DataBus.subscribe<Boolean>(DataBus.Traffic) { switchView.isChecked = it.data ?: false }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.referer -> {
                return setReferer()
            }
            else -> false
        }
    }

    private fun setReferer(): Boolean {
        alertEnterText(activity, activity.getString(R.string.enter_referer)) {
            DataBus.sendEvent(DataBus.Referer, it)
        }
        return true
    }

}