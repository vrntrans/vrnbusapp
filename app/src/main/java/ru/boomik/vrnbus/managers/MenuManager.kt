package ru.boomik.vrnbus.managers

import android.view.MenuItem
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.SettingsFragment
import ru.boomik.vrnbus.dialogs.alertEnterText
import androidx.appcompat.app.AppCompatDelegate




class MenuManager(private val activity: AppCompatActivity) : NavigationView.OnNavigationItemSelectedListener {

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
            R.id.settings -> {
                return openSettings()
            }
            R.id.night -> {
                return nightMode()
            }
            else -> false
        }
    }

    private fun nightMode(): Boolean {
        AppCompatDelegate.setDefaultNightMode(if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            AppCompatDelegate.MODE_NIGHT_NO
        else
            AppCompatDelegate.MODE_NIGHT_YES)

        activity.recreate()
        return true
    }

    private fun openSettings(): Boolean {
        val fragment = SettingsFragment()
        val transaction = activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        return true
    }

    private fun setReferer(): Boolean {
        alertEnterText(activity, activity.getString(R.string.enter_referer)) {
            DataBus.sendEvent(DataBus.Referer, it)
        }
        return true
    }

}