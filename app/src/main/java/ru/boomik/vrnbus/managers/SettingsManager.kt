package ru.boomik.vrnbus.managers

import android.annotation.SuppressLint
import android.app.Activity
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataService


class SettingsManager {

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE: SettingsManager = SettingsManager()
    }

    companion object {
        val instance: SettingsManager by lazy { Holder.INSTANCE }
    }

    private lateinit var mPreferences: Preferences
    private lateinit var mActivity: Activity
    private lateinit var mMenuManager: MenuManager
    private lateinit var mMapManager: MapManager

    fun initByActivity(activity: Activity) {
        mActivity = activity
        mPreferences = BinaryPreferencesBuilder(activity.applicationContext).build()
    }

    fun setManagers(menuManager: MenuManager, mapManager: MapManager) {
        mMenuManager = menuManager
        mMapManager = mapManager

        initialize()
    }

    private fun initialize() {
        mMenuManager.subscribeTrafficJam {
            mPreferences.edit().putBoolean(Consts.SETTINGS_TRAFFIC_JAM, it).apply()
            mMapManager.setTrafficJam(it)
        }
        loadPreferences()
    }

    private fun loadPreferences() {
        val traffic = mPreferences.getBoolean(Consts.SETTINGS_TRAFFIC_JAM, false)
        mMapManager.setTrafficJam(traffic)
        mMenuManager.setTrafficJam(traffic)

        DataService.setReferer(getReferer())
    }

    fun setReferer(referer: String?) {
        mPreferences.edit().putString(Consts.SETTINGS_REFERER, referer).apply()
        DataService.setReferer(referer)
    }

    fun getReferer(): String? {
       return mPreferences.getString(Consts.SETTINGS_REFERER, null)
    }
}