package ru.boomik.vrnbus

import android.os.RemoteException
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerClient.newBuilder
import com.android.installreferrer.api.InstallReferrerStateListener
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import ru.boomik.vrnbus.dal.DataServices
import ru.boomik.vrnbus.managers.AnalyticsManager


class VrnBusApp : MultiDexApplication(), InstallReferrerStateListener {
    private lateinit var preferences: Preferences
    private lateinit var mReferrerClient: InstallReferrerClient

    override fun onCreate() {
        super.onCreate()

        preferences = BinaryPreferencesBuilder(this).build()
        val night = preferences.getString(Consts.SETTINGS_NIGHT, null)
        setUiMode(night)

        try {
            mReferrerClient = newBuilder(this).build()
            mReferrerClient.startConnection(this)
        } catch (e: Exception) {
            //ignored
        }
        DataServices.init()
    }

    override fun onInstallReferrerSetupFinished(responseCode: Int) {
        when (responseCode) {
            InstallReferrerClient.InstallReferrerResponse.OK -> {
                try {
                    val response = mReferrerClient.installReferrer
                    val referrer = response.installReferrer
                    if (referrer.isNullOrBlank()) {
                        val savedInstallReferrer = preferences.getString("install_referrer", null)
                        if (savedInstallReferrer!=referrer) {
                            AnalyticsManager.logEvent("referrer", referrer)
                            preferences.edit().putString("install_referrer", referrer).apply()
                        }
                    }
                    mReferrerClient.endConnection()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED ->
                Log.w("InstallReferrer not supported")

            InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE ->
                Log.w("Unable to connect to the service")
            else -> {
                Log.w("responseCode not found.")
            }
        }
    }

    override fun onInstallReferrerServiceDisconnected() {
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