package ru.boomik.vrnbus.managers

import android.app.Activity
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataBus


class SettingsManager {

    companion object {
        lateinit var instance: SettingsManager
            private set
    }


    private lateinit var mPreferences: Preferences

    fun initialize(activity: Activity) {
        instance = this
        this.mPreferences = BinaryPreferencesBuilder(activity.applicationContext).build()
        loadPreferences()
        DataBus.subscribe<Boolean>(DataBus.Traffic) {
            mPreferences.edit().putBoolean(Consts.SETTINGS_TRAFFIC_JAM, it.data ?: false).apply()
        }
        DataBus.subscribe<String?>(DataBus.Referer) {
            mPreferences.edit().putString(Consts.SETTINGS_REFERER, it.data).apply()
        }
        DataBus.subscribe<Pair<String, Boolean>>(DataBus.FavoriteRoute) {
            if (it.data!=null) saveStringToList(Consts.SETTINGS_FAVORITE_ROUTE, it.data!!.first, it.data!!.second)
        }
        DataBus.subscribe<Pair<Int, Boolean>>(DataBus.FavoriteStation) {
            if (it.data!=null)  saveIntToList(Consts.SETTINGS_FAVORITE_STATIONS, it.data!!.first, it.data!!.second)
        }
    }

    private fun loadPreferences() {
        val traffic = mPreferences.getBoolean(Consts.SETTINGS_TRAFFIC_JAM, false)
        val referer = mPreferences.getString(Consts.SETTINGS_REFERER, null)
        DataBus.sendEvent(DataBus.Traffic, traffic)
        DataBus.sendEvent(DataBus.Referer, referer)
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
        return if (!value.isNullOrBlank()) value?.split(";")?.map { it.toInt() } else null
    }
    private fun listOfStringToString(values : List<String>?) : String? {
        return values?.distinct()?.joinToString(";")
    }
    private fun stringToListOfString(value : String?) : List<String>? {
        return if (!value.isNullOrBlank()) value?.split(";")?.map { it } else null
    }

    //endregion
}