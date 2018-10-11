package ru.boomik.vrnbus.managers

import android.app.Activity
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import ru.boomik.vrnbus.Consts


class SettingsManager(val activity: Activity, private val menuManager: MenuManager, private val mapManager: MapManager) {

    private var mPreferences: Preferences = BinaryPreferencesBuilder(activity.applicationContext).build()

    init {
        menuManager.subscribeTrafficJam {
            mPreferences.edit().putBoolean(Consts.SETTINGS_TRAFFIC_JAM, it).apply()
            mapManager.setTrafficJam(it)
        }
        loadPreferences()

    }

    private fun loadPreferences() {
        val traffic = mPreferences.getBoolean(Consts.SETTINGS_TRAFFIC_JAM,false)
        mapManager.setTrafficJam(traffic)
        menuManager.setTrafficJam(traffic)
    }
}