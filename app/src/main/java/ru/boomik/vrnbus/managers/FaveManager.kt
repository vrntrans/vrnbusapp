package ru.boomik.vrnbus.managers

import android.app.Activity
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.boomik.vrnbus.Log
import ru.boomik.vrnbus.dialogs.FaveParamsDialog
import ru.boomik.vrnbus.dialogs.StationInfoDialog
import ru.boomik.vrnbus.dialogs.alertQuestion
import ru.boomik.vrnbus.objects.Fave
import ru.boomik.vrnbus.objects.FaveFull
import ru.boomik.vrnbus.objects.StationOnMap
import java.lang.ref.WeakReference

object FaveManager {

    private lateinit var mInsets: WindowInsetsCompat
    private lateinit var mActivity: WeakReference<Activity>
    private var faves : MutableMap<String, Fave> = mutableMapOf()

    fun checkFaveInitialized(type : String) : Boolean {
        return faves.containsKey(type)
    }

    fun faveClick(type : String, fromStation : Boolean = false) {
        val act = mActivity.get() ?: return
        val name = getLocalizedFaveName(type)
        val icon = getIconRes(type)
        if (!faves.containsKey(type)) {
            alertQuestion(act, "Избранное", "Данный пункт ибранного не настроен. Настроить?", "Да", "Нет") {
                if (it) return@alertQuestion
                FaveParamsDialog.show(act, type, name, icon, mInsets)
            }
        } else {
            val fave = faves[type]!!
            val stationId = fave.stationId
            val station = DataManager.stations?.firstOrNull { s -> s.id == stationId } ?: return
            val stationOnMap = StationOnMap(station.title, stationId, station.latitude, station.longitude)
            StationInfoDialog.show(act, mInsets, stationOnMap)
        }
    }

    fun getAvailableFaveForStation(id : Int) : List<FaveFull> {
        return faves.map{it.value}.filter { it.stationId == id}.map { FaveFull(it, getIconRes(it.type), getLocalizedFaveName(it.type)) }.toList()
    }

    private fun getLocalizedFaveName(type: String) : String {
        val act = mActivity.get() ?: return ""
        val res = act.resources.getIdentifier("fave_item_$type", "string", act.packageName)
        if (res<0) return ""
        return act.resources.getString(res)
    }

    private fun getIconRes(type: String): Int {
        val act = mActivity.get() ?: return -1
        return act.resources.getIdentifier("ic_fave_$type", "drawable", act.packageName)
    }

    fun initialize(activity: Activity, insets: WindowInsetsCompat) {

        mActivity = WeakReference(activity)
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

    fun destroy(activity: Activity) {
        if (::mActivity.isInitialized && mActivity.get()==activity)
            mActivity.clear()
    }

    fun save(type: String, id: Int, routes: List<String>) {
        faves[type] = Fave(type, id, routes)

        try {
                val result = Json.encodeToString(faves)
                SettingsManager.setString("Favorites", result)
            if (mActivity.get()!=null) Toast.makeText(mActivity.get(), "Избранное сохранено", Toast.LENGTH_SHORT).show()
        } catch (e: Throwable) {
            if (mActivity.get()!=null) Toast.makeText(mActivity.get(), "Избранное не сохранено: $e", Toast.LENGTH_SHORT).show()
            Log.e(e.localizedMessage, e)
            //ignore
        }
    }
}
