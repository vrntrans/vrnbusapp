package ru.boomik.vrnbus.managers

import android.content.Context
import com.github.kittinunf.fuel.core.FuelManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import ru.boomik.vrnbus.Consts
import ru.boomik.vrnbus.DataService
import ru.boomik.vrnbus.Log
import ru.boomik.vrnbus.dto.RouteEdgeDto
import ru.boomik.vrnbus.dto.RoutesDto
import ru.boomik.vrnbus.dto.StationResultDto
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.loadStringFromFile
import ru.boomik.vrnbus.utils.loadStringFromNetworkAsync
import ru.boomik.vrnbus.utils.saveStringToFile
import java.io.File

object DataStorageManager {

    private var loading: Boolean = false
    private const val routeNamesFileName = "routeNames.json"
    private const val routesFileName = "routes.json"
    private const val stationsFileName = "stations.json"
    private const val routeEdgesFileName = "routeEdges.json"


    private lateinit var cacheDir: File

    private lateinit var routeNamesFile: File
    private lateinit var routesFile: File
    private lateinit var stationsFile: File
    private lateinit var routeEdgesFile: File
    private lateinit var cacheTimeFile: File

    var routeNames: List<String>? = null
    var stations: List<StationOnMap>? = null
    var routes: List<Route>? = null
    var routeEdges: List<RouteEdgeDto>? = null
    var activeStationId: Int = 0

    private var alreadyChecked: Boolean = false
    private var needReload: Boolean = false

    private var filesExist: Boolean = false
    var searchStationId: Int = -1

    val gson: Gson = Gson()


    suspend fun loadRouteNames(context: Context): List<String>? {
        load(context)
        return routeNames
    }

    suspend fun loadBusStations(context: Context): List<StationOnMap>? {
        load(context)
        return stations
    }

    suspend fun loadRoutes(context: Context): List<Route>? {
        load(context)
        return routes
    }
    suspend fun loadRouteEdges(context: Context): List<RouteEdgeDto>? {
        load(context)
        return routeEdges
    }


    private fun initCache(context: Context) {
        val dir = context.externalCacheDir ?: context.cacheDir
        val dataDir = File(dir.absolutePath + "/" + "data")
        if (!dataDir.exists())
            dataDir.mkdirs()
        cacheDir = dataDir
        routeNamesFile = File(dataDir.absolutePath + "/" + routeNamesFileName)
        routesFile = File(dataDir.absolutePath + "/" + routesFileName)
        stationsFile = File(dataDir.absolutePath + "/" + stationsFileName)
        routeEdgesFile = File(dataDir.absolutePath + "/" + routeEdgesFileName)
        cacheTimeFile = File(dataDir.absolutePath + "/cache_time.txt")
    }


    suspend fun load(context: Context): Boolean {
        Log.e("Loaded check")
        initCache(context)
        needReload = isNeedReload()
        filesExist = checkFiles()
        val initialized =
                        (routeNames?.isNotEmpty() ?: false) &&
                        (stations?.isNotEmpty() ?: false) &&
                        (routes?.isNotEmpty() ?: false) &&
                        (routeEdges?.isNotEmpty() ?: false)


        if (!needReload && filesExist && initialized) return true
        Log.e("Loaded start")

        if (loading) return false
        loading = true

        var ok = false
        try {
            loadFiles()
            ok = true
        } catch (e: Throwable) {
            Log.e("something went wrong", e)
        }
        if (!ok)
            try {
                loadFiles()
                ok = true
            } catch (e: Throwable) {
                Log.e("something went wrong", e)
            }

        Log.e("Loaded done")
        loading = false
        return ok
    }


    private fun checkFiles(): Boolean {
        return routeNamesFile.exists() && routesFile.exists() && stationsFile.exists() && routeEdgesFile.exists()
    }


    fun prepareForReload() {
        alreadyChecked=false
        try {
            cacheDir.deleteRecursively()
        } catch (e: Throwable) {

        }
    }

    private fun isNeedReload(): Boolean {
        if (alreadyChecked) return false
        alreadyChecked = true

        val week = 594000000

        if (!cacheDir.exists() || cacheDir.listFiles().isEmpty()) return true
        if (!cacheTimeFile.exists()) return true
        val timeString = cacheTimeFile.readLines().first()
        if (timeString.isEmpty()) return true
        val time = timeString.toLongOrNull() ?: return true
        val difference = System.currentTimeMillis() - time

        return difference > week
    }


    private suspend fun loadFiles() = withContext(Dispatchers.IO) {
        if (!needReload && filesExist)
            loadFromSaved()
        else loadFromNetwork()
    }

    private suspend fun loadFromNetwork() = withContext(Dispatchers.IO) {

        FuelManager.instance.basePath = Consts.API_URL

        val routeNames = async { loadNamesRoutesNetwork() }
        val routes = async { loadRoutesNetwork() }
        val stations = async { loadStationsNetwork() }
        val routeEdges = async { loadRouteEdgesNetwork() }
        listOf(routeNames, routes, stations, routeEdges).awaitAll()
        saveStringToFile(cacheTimeFile, System.currentTimeMillis().toString())
    }


    private suspend fun loadFromSaved() = withContext(Dispatchers.IO) {
        val routeNames = async { loadNamesRoutes() }
        val routes = async { loadRouteNames() }
        val stations = async { loadStations() }
        val routeEdges = async { loadRouteEdges() }
        listOf(routeNames, routes, stations, routeEdges).awaitAll()
    }


    private suspend fun loadNamesRoutesNetwork() {
        val result = loadStringFromNetworkAsync(Consts.API_BUS_LIST).await()
        result?.let { data ->
            val routes: RoutesDto = gson.fromJson(data, object : TypeToken<RoutesDto>() {}.type)
            routeNames = routes.result
            saveStringToFile(routeNamesFile, data)

        }
    }

    private suspend fun loadStationsNetwork() {
        val result = loadStringFromNetworkAsync(Consts.API_STATIONS).await()
        result?.let { data ->
            val stationsResult: StationResultDto = DataService.gson.fromJson(data, object : TypeToken<StationResultDto>() {}.type)
            stations = StationOnMap.parseListDto(stationsResult.result)
            saveStringToFile(stationsFile, data)
        }
    }

    private suspend fun loadRoutesNetwork() {
        val result = loadStringFromNetworkAsync(Consts.API_ROUTES).await()
        result?.let { data ->
            val routesParsed: Map<String, List<List<Any>>> = gson.fromJson(data, object : TypeToken<Map<String, List<List<Any>>>>() {}.type)
            routes = routesParsed.map {
                Route(it.key, it.value.map { point ->
                    StationOnMap(point[1] as String, (point[6] as Double).toInt(), point[2] as Double, point[3] as Double)
                })
            }
            saveStringToFile(routesFile, data)
        }
    }

    private suspend fun loadRouteEdgesNetwork() {
        val result = loadStringFromNetworkAsync(Consts.API_ROUTE_EDGES).await()
        result?.let { data ->
            val edgesResult: List<RouteEdgeDto> = DataService.gson.fromJson(data, object : TypeToken<List<RouteEdgeDto>>() {}.type)
            routeEdges = edgesResult
            saveStringToFile(routeEdgesFile, data)
        }
    }

    private suspend fun loadNamesRoutes() = withContext(Dispatchers.IO) {
        val data = loadStringFromFile(routeNamesFile)
        val routes: RoutesDto = gson.fromJson(data, object : TypeToken<RoutesDto>() {}.type)
        routeNames = routes.result
    }

    private suspend fun loadRouteNames() = withContext(Dispatchers.IO) {
        val data = loadStringFromFile(routesFile)
        val routesParsed: Map<String, List<List<Any>>> = gson.fromJson(data, object : TypeToken<Map<String, List<List<Any>>>>() {}.type)
        routes = routesParsed.map {
            Route(it.key, it.value.map { point ->
                StationOnMap(point[1] as String, (point[6] as Double).toInt(), point[2] as Double, point[3] as Double)
            })
        }
    }

    private suspend fun loadStations() = withContext(Dispatchers.IO) {
        val data = loadStringFromFile(stationsFile)
        val stationsResult: StationResultDto = gson.fromJson(data, object : TypeToken<StationResultDto>() {}.type)
        stations = StationOnMap.parseListDto(stationsResult.result)
    }

    private suspend fun loadRouteEdges() = withContext(Dispatchers.IO) {
        val data = loadStringFromFile(routeEdgesFile)
        val edgesResult: List<RouteEdgeDto> = gson.fromJson(data, object : TypeToken<List<RouteEdgeDto>>() {}.type)
        routeEdges = edgesResult
    }



}