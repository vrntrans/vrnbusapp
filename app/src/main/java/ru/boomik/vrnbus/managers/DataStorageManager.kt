package ru.boomik.vrnbus.managers

import android.annotation.SuppressLint
import android.content.Context
import ru.boomik.vrnbus.DataService


class DataStorageManager {

    private lateinit var mContext: Context

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: DataStorageManager
            private set
    }


    fun setActivity(context: Context) {
        mContext = context
        instance = this
    }

    var routesList: List<String>? = null
    var activeStationId: Int = 0

    fun load() {
        DataService.loadRoutes(mContext) {
            routesList = it
        }
    }
}