package ru.boomik.vrnbus

import android.annotation.SuppressLint
import ru.boomik.vrnbus.managers.MapManager
import ru.boomik.vrnbus.managers.MenuManager
import ru.boomik.vrnbus.managers.SettingsManager
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.StationOnMap
import java.security.InvalidParameterException

class DataBus {

    //region Instance
    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE: DataBus = DataBus()
    }

    init {
        initListeners()
    }
    //endregion

    companion object {
        val instance: DataBus by lazy { Holder.INSTANCE }

        const val Traffic = "Traffic"
        const val Referer = "Referer"
        const val StationClick = "StationClick"
        const val BusClick = "BusClick"
        const val BusToMap = "ToMap"
        const val Update = "Update"
        const val FavoriteRoute = "FavoriteRoute"
        const val FavoriteStation = "FavoriteStation"

        //region sendEvent
        fun <T> sendEvent(key: String, obj: T) {
            instance.getListeners<T>(key).forEach { it(obj) }
        }

        fun sendEvent(key: String) {
            instance.getEmptyListeners(key).forEach { it() }
        }
        //endregion

        //region subscribe
        fun <T> subscribe(key: String, listener: (T) -> Unit) {
            instance.getListeners<T>(key).add(listener)
        }

        fun subscribeEmpty(key: String, listener: () -> Unit) {
            instance.getEmptyListeners(key).add(listener)
        }
        //endregion
    }

    //region Managers
    private lateinit var mMenuManager: MenuManager
    private lateinit var mMapManager: MapManager
    private lateinit var mSettingsManager: SettingsManager

    fun setManager(menuManager: MenuManager) {
        mMenuManager = menuManager
    }

    fun setManager(mapManager: MapManager) {
        mMapManager = mapManager
    }

    fun setManager(settingsManager: SettingsManager) {
        mSettingsManager = settingsManager
    }
    //endregion

    //region Listeners
    private lateinit var eventsMap: Map<String, Any>

    private fun initListeners() {
        mTrafficChangedCallback = mutableListOf()
        mRefererChangedCallback = mutableListOf()
        mStationCallback = mutableListOf()
        mBusCallback = mutableListOf()
        mBusToMapCallback = mutableListOf()
        mUpdateCallback = mutableListOf()
        mFavoriteRouteCallback = mutableListOf()
        mFavoriteStationCallback = mutableListOf()

        eventsMap = mapOf<String, Any>(
                Pair(Traffic, mTrafficChangedCallback),
                Pair(Referer, mRefererChangedCallback),
                Pair(StationClick, mStationCallback),
                Pair(BusClick, mBusCallback),
                Pair(BusToMap, mBusToMapCallback),
                Pair(Update, mUpdateCallback),
                Pair(FavoriteRoute, mFavoriteRouteCallback),
                Pair(FavoriteStation, mFavoriteStationCallback)
        )
    }

    private lateinit var mTrafficChangedCallback: MutableList<(Boolean) -> Unit>
    private lateinit var mRefererChangedCallback: MutableList<(String?) -> Unit>
    private lateinit var mStationCallback: MutableList<(StationOnMap) -> Unit>
    private lateinit var mBusCallback: MutableList<(Bus) -> Unit>
    private lateinit var mBusToMapCallback: MutableList<(List<Bus>) -> Unit>
    private lateinit var mUpdateCallback: MutableList<() -> Unit>
    private lateinit var mFavoriteRouteCallback: MutableList<(Pair<String,Boolean>) -> Unit>
    private lateinit var mFavoriteStationCallback: MutableList<(Pair<Int,Boolean>) -> Unit>
    //endregion

    //region InternalMethods
    @Suppress("UNCHECKED_CAST")
    private fun <T> getListeners(key: String): MutableList<(T) -> Unit> {
        if (key.isBlank()) throw IllegalStateException("Key not be empty")
        if (!eventsMap.containsKey(key)) throw NoSuchFieldException("No listeners for this $key found")
        return eventsMap[key] as? MutableList<(T) -> Unit>
                ?: throw InvalidParameterException("Incorrect value type")
    }

    @Suppress("UNCHECKED_CAST")
    private fun getEmptyListeners(key: String): MutableList<() -> Unit> {
        if (key.isBlank()) throw IllegalStateException("Key not be empty")
        if (!eventsMap.containsKey(key)) throw NoSuchFieldException("No listeners for this $key found")
        return eventsMap[key] as? MutableList<() -> Unit>
                ?: throw InvalidParameterException("Incorrect value type")
    }
    //endregion

}