package ru.boomik.vrnbus

import android.app.Activity
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import ru.boomik.vrnbus.dto.ArrivalDto
import ru.boomik.vrnbus.dto.BusInfoDto
import ru.boomik.vrnbus.managers.DataStorageManager
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.Station
import ru.boomik.vrnbus.objects.StationOnMap
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


object DataService {

    val gson: Gson

    init {
        FuelManager.instance.basePath = Consts.API_URL
        gson = Gson()
    }


    fun setReferer(referer: String?) {
        val headers = FuelManager.instance.baseHeaders?.toMutableMap() ?: mutableMapOf()
        if (referer != null && !referer.isBlank()) headers["Referer"] = referer
        else if (headers.contains("Referer")) headers.remove("Referer ")
        FuelManager.instance.baseHeaders = headers
    }

    fun loadBusInfo(q: String, callback: (List<Bus>?) -> Unit) {
        val query = if (q == "*") "" else q
        Thread(Runnable {
            try {
                Consts.API_BUS_INFO.httpGet(listOf(Pair("q", query), Pair("src", "map"), Pair("hide_text", ""))).responseObject<BusInfoDto> { request, response, result ->
                    //make a GET to http://httpbin.org/get and do something with response
                    Log.d("log", request.toString())
                    Log.d("log", response.toString())
                    val (_, error) = result
                    if (error == null) {

                        try {
                            val info = result.get()
                            val busDtos = info.buses
                            val pattern = "yyyy-MM-dd'T'HH:mm:ss"


                            val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
                            val serverDate = dateFormat.parse(info.time)
                            val serverCalendar = Calendar.getInstance()

                            val calendarNow = Calendar.getInstance()
                            serverCalendar.time = serverDate
                            val timeInMills = serverCalendar.timeInMillis
                            val difference = (calendarNow.timeInMillis-timeInMills)/1000



                            callback(busDtos.filter { it.count() == 2 }.map {

                                val date = dateFormat.parse(it[0].time)
                                val calendar = Calendar.getInstance()
                                calendar.time = date


                                val bus = Bus()
                                bus.route = it[0].route
                                bus.number = it[0].number
                                bus.nextStationName = it[1].nextStationName ?: ""
                                bus.lastStationTime = it[0].lastStationTime
                                bus.lastSpeed = it[0].lastSpeed
                                bus.time = calendar
                                bus.lat = it[1].lat
                                bus.lon = it[1].lon
                                bus.lastLat = it[0].lastLat
                                bus.lastLon = it[0].lastLon
                                bus.lowFloor = it[0].lowFloor == 1
                                bus.busType = it[0].busType
                                bus.timeDifference = (timeInMills - calendar.timeInMillis) / 1000
                                bus.localServerTimeDifference = difference
                                bus.init()
                                bus
                            })
                        } catch (exception: Throwable) {
                            Log.e("VrnBus", "Hm..", exception)
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }

                }
            } catch (e: Throwable) {
                callback(null)
            }
        }).start()
    }

    suspend fun loadArrivalInfoAsync(station: Int) = GlobalScope.async {
        suspendCoroutine<Station?> { cont ->
            try {
                Consts.API_ARRIVAL.httpGet(listOf(Pair("id", station))).responseObject<ArrivalDto> { request, response, result ->
                    Log.d("log", request.toString())
                    Log.d("log", response.toString())
                    val (_, error) = result
                    if (error == null) {
                        try {
                            val info = result.get()
                            val stationObject = Station.parseDto(info)
                            cont.resume(stationObject)
                        } catch (exception: Throwable) {
                            Log.e("VrnBus", "Hm..", exception)
                            cont.resumeWithException(exception)
                        }
                    } else {
                        cont.resumeWithException(error)
                    }
                }
            } catch (e: Throwable) {
                cont.resumeWithException(e)
            }
        }
    }


    fun loadRouteByName(activity: Activity, route: String, loaded: (Route?) -> Unit) {
        try {
            GlobalScope.async(Dispatchers.Main) {
                val data = DataStorageManager.loadRoutes(activity)?.first { it.route == route }
                val edges = DataStorageManager.loadRouteEdges(activity)
                data?.let {
                    if (it.allStations == null)
                    edges?.let { edges ->
                        val validEdges = edges.filter { it.edgeKey.size==2 && it.points.isNotEmpty() }
                        val route = mutableListOf<StationOnMap>()
                        val stations = data.stations


                        for ((i, first) in data.stations.withIndex()) {
                            if (stations.size<=i+1) {
                                route.add(first)
                                break
                            }
                            val second = stations[i+1]
                            val currentEdges = validEdges.firstOrNull { it.edgeKey[0] == first.id && it.edgeKey[1]==second.id }
                            if (currentEdges==null) {
                                route.add(first)
                                continue
                            }
                            route.add(first)
                            val intermediatePoints = currentEdges.points.map { point-> StationOnMap("",0, point.lat, point.lng)}
                            route.addAll(intermediatePoints)
                            data.allStations = route
                        }
                    }
                }
                loaded(data)
            }
        } catch (exception: Throwable) {
            Log.e("VrnBus", "Hm..", exception)
        }
    }
}

