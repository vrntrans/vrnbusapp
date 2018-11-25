package ru.boomik.vrnbus.managers

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.async
import ru.boomik.vrnbus.DataService
import ru.boomik.vrnbus.objects.Station
import java.io.File
import java.util.*
import kotlin.coroutines.suspendCoroutine


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
       // isNeedReload(mContext)
        DataService.loadRoutes(mContext) {
            routesList = it
        }
    }

    suspend fun loadRoutes(context: Context) = async {
        suspendCoroutine<Station?> { cont ->

        }
    }

    fun isNeedReload(context : Context)  : Boolean {
        val week = 594000000
        var min3 = 180000

        val dir = context.externalCacheDir ?: context.cacheDir
        if (dir==null || !dir.exists() || dir.listFiles().isEmpty()) return true

        val cacheTime = File(dir.absolutePath+"/cache.txt")
        if (!cacheTime.exists()) return true
        val timeString = cacheTime.readLines().first()
        if (timeString.isEmpty()) return true
        val time = timeString.toLongOrNull() ?: return true
        val difference = System.currentTimeMillis() - time
        //31536000000
        //604800000
        //594000000

        return difference>week
    }

    suspend fun loadAll(context: Context) = async {
        suspendCoroutine<Station?> { cont ->

        }
    }

}