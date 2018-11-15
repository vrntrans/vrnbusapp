package ru.boomik.vrnbus.managers

import android.view.MenuItem
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import com.google.android.material.navigation.NavigationView
import ru.boomik.vrnbus.DataBus
import ru.boomik.vrnbus.R
import ru.boomik.vrnbus.SettingsFragment


class MenuManager(private val activity: AppCompatActivity) : NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navigationView: NavigationView

    private lateinit var drawer: DrawerLayout

    fun initialize(nav_view: NavigationView) {
        nav_view.setNavigationItemSelectedListener(this)
        this.navigationView = nav_view
        this.drawer = navigationView.parent as androidx.drawerlayout.widget.DrawerLayout
        val trafficJamItem = nav_view.menu.findItem(R.id.traffic_jam)
        val switchView = trafficJamItem.actionView as Switch
        switchView.setOnCheckedChangeListener { _, isChecked -> DataBus.sendEvent(DataBus.Traffic, isChecked) }
        DataBus.subscribe<Boolean>(DataBus.Traffic) { switchView.isChecked = it.data ?: false }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.settings -> {
                return openSettings()
            }
            else -> false
        }
    }

    private fun openSettings(): Boolean {
        drawer.closeDrawers()
        val fragment = SettingsFragment()
        val transaction = activity.supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment)
        transaction.setTransition(TRANSIT_FRAGMENT_FADE)
        transaction.addToBackStack("settings")
        transaction.commit()
        return true
    }
}