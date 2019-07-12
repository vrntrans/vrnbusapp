package ru.boomik.vrnbus

import androidx.appcompat.app.AppCompatDelegate
import android.app.Application
import com.ironz.binaryprefs.BinaryPreferencesBuilder


class VrnBusApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val preferences = BinaryPreferencesBuilder(this).build()
        val night = preferences.getString(Consts.SETTINGS_NIGHT, null)
        setUiMode(night)
    }

    private fun setUiMode(data: String?) {
        val mode = when (data?.toIntOrNull()) {
            0 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            1 -> AppCompatDelegate.MODE_NIGHT_AUTO
            2 -> AppCompatDelegate.MODE_NIGHT_NO
            3 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(mode)
    }
}