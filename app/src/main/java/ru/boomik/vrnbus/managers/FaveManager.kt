package ru.boomik.vrnbus.managers

import android.app.Activity
import androidx.core.view.WindowInsetsCompat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.boomik.vrnbus.dialogs.FaveParamsDialog
import ru.boomik.vrnbus.dialogs.StationInfoDialog
import ru.boomik.vrnbus.dialogs.alertQuestion
import ru.boomik.vrnbus.objects.Fave
import ru.boomik.vrnbus.objects.StationOnMap

object FaveManager {

    private lateinit var mInsets: WindowInsetsCompat
    private lateinit var mActivity: Activity
    private var faves : MutableMap<String, Fave> = mutableMapOf()

    fun checkFaveInitialized(type : String) : Boolean {
        return faves.containsKey(type)
    }

    fun faveClick(type : String, fromStation : Boolean = false) {
            if (!faves.containsKey(type)) {
                alertQuestion(mActivity, "Избранное", "Данный пункт ибранного не настроен. Настроить?", "Да", "Нет") {
                    if (it) return@alertQuestion
                    FaveParamsDialog.show(mActivity, type, mInsets)
                }
            } else {
                val fave = faves[type]!!
                val stationId = fave.stationId
                val station = DataManager.stations?.firstOrNull { s-> s.id == stationId } ?: return
                val stationOnMap = StationOnMap(station.title, stationId, station.latitude, station.longitude)
                StationInfoDialog.show(mActivity, mInsets, stationOnMap)
            }
    }
    fun initialize(activity: Activity, insets: WindowInsetsCompat) {

        mActivity = activity
        mInsets = insets
        val json = SettingsManager.getString("Favorites")

        try {
            if (json!=null) {
                val result = Json.decodeFromString<Map<String, Fave>>(json)
                faves = result.toMutableMap()
            }
        } catch (e: Throwable) {
            //ignore
        }
    }
}
