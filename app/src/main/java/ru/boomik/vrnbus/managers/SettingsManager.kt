package ru.boomik.vrnbus.managers

import android.app.Activity
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import ru.boomik.vrnbus.*


object SettingsManager {

    private lateinit var mPreferences: Preferences

    fun initialize(activity: Activity) {
        this.mPreferences = BinaryPreferencesBuilder(activity.applicationContext).build()


        DataBus.subscribe<Boolean>(DataBus.Traffic) {
            mPreferences.edit().putBoolean(Consts.SETTINGS_TRAFFIC_JAM, it.data ?: false).apply()
        }
        DataBus.subscribe<String>(Consts.SETTINGS_REFERER) {
            mPreferences.edit().putString(Consts.SETTINGS_REFERER, it.data).apply()
        }
        DataBus.subscribe<Pair<String, Boolean>>(DataBus.FavoriteRoute) {
            saveStringToList(Consts.SETTINGS_FAVORITE_ROUTE, it.data.first, it.data.second)
        }
        DataBus.subscribe<Pair<Int, Boolean>>(DataBus.FavoriteStation) {
            saveIntToList(Consts.SETTINGS_FAVORITE_STATIONS, it.data.first, it.data.second)
        }

        DataBus.subscribe<Pair<String, Boolean>>(DataBus.Settings) {
            it.data.let { data -> if(data.second:: class == Boolean::class) mPreferences.edit().putBoolean(data.first, data.second).apply() }
        }
        DataBus.subscribe<Pair<String, Int>>(DataBus.Settings) {
            it.data.let { data -> if(data.second:: class == Int::class) mPreferences.edit().putInt(data.first, data.second).apply() }
        }
        DataBus.subscribe<Pair<String, String>>(DataBus.Settings) {
            it.data.let { data -> if(data.second:: class == String::class) mPreferences.edit().putString(data.first, data.second).apply() }
        }

        setDefaultValues(activity)
        initRemoteConfig()
    }

    private var refreshRate: Double = 30.0

    private const val LAST_VERSION_CODE = "LAST_VERSION_CODE_SETTINGS"

    private fun setDefaultValues(activity: Activity) {
        val lastCode = mPreferences.getLong(LAST_VERSION_CODE, 0)
        val currentVersion = PackageInfoCompat.getLongVersionCode(activity.packageManager.getPackageInfo(activity.packageName, 0))

        if (lastCode==0L || currentVersion>lastCode) {
            mPreferences.edit().putBoolean(Consts.SETTINGS_ROTATE, true).apply()

            mPreferences.edit().putLong(LAST_VERSION_CODE, currentVersion).apply()
        }
    }

    private fun initRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.setDefaults(R.xml.remote_config_defaults)
        remoteConfig.fetch(60*60*24).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("Fetch Succeeded")
                val busRefreshRate = FirebaseRemoteConfig.getInstance().getDouble("bus_refresh_rate")
                if (busRefreshRate>0) refreshRate = busRefreshRate
                remoteConfig.activateFetched()
            } else {
                Log.e("Fetch Failed")
            }
        }

    }

    fun loadPreferences() {
        val traffic = mPreferences.getBoolean(Consts.SETTINGS_TRAFFIC_JAM, false)
        val referer = mPreferences.getString(Consts.SETTINGS_REFERER, "") ?: ""
        DataBus.sendEvent(DataBus.Traffic, traffic)
        DataBus.sendEvent(Consts.SETTINGS_REFERER, referer)
    }

    fun saveIntToList(key : String, id : Int, inFavorite : Boolean) {
        var favorites = getIntArray(key)?.toMutableList()
        if (favorites==null && inFavorite) favorites = mutableListOf()
        if (inFavorite) favorites?.add(id)
        else favorites?.remove(id)
        mPreferences.edit().putString(key, listOfIntToString(favorites)).apply()
    }

    fun saveStringToList(key : String, id : String, inFavorite : Boolean) {
        var favorites = getStringArray(key)?.toMutableList()
        if (favorites==null && inFavorite) favorites = mutableListOf()
        if (inFavorite) favorites?.add(id)
        else favorites?.remove(id)
        mPreferences.edit().putString(key, listOfStringToString(favorites)).apply()
    }


    fun getBool(key : String) : Boolean {
        return mPreferences.getBoolean(key, false)
    }
    fun geInt(key : String) : Int {
        return mPreferences.getInt(key, 0)
    }
    fun getString(key : String) : String? {
        return mPreferences.getString(key, null)
    }
    fun getIntArray(key : String) : List<Int>? {
        return stringToListOfInt(mPreferences.getString(key, null))
    }
    fun getStringArray(key : String) : List<String>? {
        return stringToListOfString(mPreferences.getString(key, null))
    }

    //region Internals
    private fun listOfIntToString(values : List<Int>?) : String? {
        return values?.distinct()?.joinToString(";")
    }
    private fun stringToListOfInt(value : String?) : List<Int>? {
        return if (!value.isNullOrBlank()) value.split(";").map { it.toInt() } else null
    }
    private fun listOfStringToString(values : List<String>?) : String? {
        return values?.distinct()?.joinToString(";")
    }
    private fun stringToListOfString(value : String?) : List<String>? {
        return if (!value.isNullOrBlank()) value.split(";").map { it } else null
    }

    //endregion
}