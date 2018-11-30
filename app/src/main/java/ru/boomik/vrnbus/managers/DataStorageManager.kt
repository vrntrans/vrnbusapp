package ru.boomik.vrnbus.managers

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import ru.boomik.vrnbus.DataService
import ru.boomik.vrnbus.objects.Station
import java.io.File
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object DataStorageManager {


    var routesList: List<String>? = null
    var activeStationId: Int = 0
    private var alreadyChecked: Boolean = false

    fun load(context: Context) {
        // isNeedReload(mContext)
        DataService.loadRoutes(context) {
            routesList = it
        }
    }

    suspend fun loadRoutes(context: Context) = GlobalScope.async {

        suspendCoroutine<List<String>> { cont ->
            if (routesList != null) {
                cont.resume(routesList!!)
                return@suspendCoroutine
            }
            async {
                if (isNeedReload(context)) {
                    loadAll(context).await()
                }
            }
        }
    }


    fun isNeedReload(context: Context): Boolean {
        if (alreadyChecked) return false
        alreadyChecked = true

        val week = 594000000
        val min = 60000 * 30

        val dir = context.externalCacheDir ?: context.cacheDir
        if (dir == null || !dir.exists() || dir.listFiles().isEmpty()) return true

        val cacheTime = File(dir.absolutePath + "/cache.txt")
        if (!cacheTime.exists()) return true
        val timeString = cacheTime.readLines().first()
        if (timeString.isEmpty()) return true
        val time = timeString.toLongOrNull() ?: return true
        val difference = System.currentTimeMillis() - time
        //31536000000
        //604800000
        //594000000

        return difference > min
    }

    suspend fun loadAll(context: Context) = GlobalScope.async {
        suspendCoroutine<Station?> { cont ->

        }
    }

}