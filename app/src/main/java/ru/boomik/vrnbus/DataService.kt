package ru.boomik.vrnbus

import android.app.Activity
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.experimental.async
import ru.boomik.vrnbus.dto.*
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.Station
import ru.boomik.vrnbus.objects.StationOnMap
import ru.boomik.vrnbus.utils.loadJSONFromAsset
import kotlin.coroutines.experimental.suspendCoroutine


class DataService {
    companion object {

        var gson: Gson

        init {
            FuelManager.instance.basePath = Consts.API_URL
            gson = Gson()
        }


        fun setReferer(referer: String?) {
            val headers = FuelManager.instance.baseHeaders?.toMutableMap() ?: mutableMapOf()
            if (referer != null) headers["Referer"] = referer
            else if (headers.contains("Referer")) headers.remove("Referer ")
            FuelManager.instance.baseHeaders = headers
        }

        fun loadBusInfo(q: String, callback: (List<Bus>?) -> Unit) {
            Consts.API_BUS_INFO.httpGet(listOf(Pair("q", q), Pair("src", "map"))).responseObject<BusInfoDto> { request, response, result ->
                //make a GET to http://httpbin.org/get and do something with response
                Log.d("log", request.toString())
                Log.d("log", response.toString())
                val (_, error) = result
                if (error == null) {

                    try {
                        val info = result.get()
                        val busDtos = info.buses
                        callback(busDtos.filter { it.count() == 2 }.map {
                             Bus(it[0].route, it[0].number, it[1].nextStationName ?: "", it[0].lastStationTime, it[0].lastSpeed, it[0].time, it[1].lat, it[1].lon, it[0].lastLat, it[0].lastLon, .0, .0)
                        })
                    } catch (exception: Throwable) {
                        Log.e("VrnBus", "Hm..", exception)
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        }

        fun loadBusStopInfo(station: String, callback: (Station?) -> Unit) {
            Consts.API_BUS_STOP_INFO.httpGet(listOf(Pair("station", station), Pair("q", ""))).responseObject<BusStopInfoDto> { request, response, result ->
                Log.d("log", request.toString())
                Log.d("log", response.toString())
                val (_, error) = result
                if (error == null) {

                    try {
                        val info = result.get()
                        val stationObject = Station.parseDto(info, station)
                        callback(stationObject)

                    } catch (exception: Throwable) {
                        Log.e("VrnBus", "Hm..", exception)
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        }

        fun loadArrivalInfo(station: Int, callback: (Station?) -> Unit) {
            Consts.ARRIVAL.httpGet(listOf(Pair("id", station))).responseObject<ArrivalDto> { request, response, result ->
                Log.d("log", request.toString())
                Log.d("log", response.toString())
                val (_, error) = result
                if (error == null) {
                    try {
                        val info = result.get()
/*
                         //Mock
                        val busses = listOf(
                                ArrivalBusDto(BusDto(51.6751,39.20839,10,"","13","","","68","","","","","",1,1,51.6755,39.2089), 10.0, 14.0),
                                ArrivalBusDto(BusDto(51.67552,39.208759,10,"","13","","","68","","","","","",1,1,51.6755,39.2089), 10.0, 1.0),
                                ArrivalBusDto(BusDto(51.67553,39.20892,10,"","13","","","8","","","","","",1,1,51.6755,39.2089), 10.0, 14.5),
                                ArrivalBusDto(BusDto(51.67554,39.20893,10,"","13","","","68","","","","","",1,1,51.6755,39.2089), 10.0, 50.0),
                                ArrivalBusDto(BusDto(51.67555,39.20897,10,"","13","","","8","","","","","",1,1,51.6755,39.2089), 10.0, 0.45),
                                ArrivalBusDto(BusDto(51.67556,39.20893,10,"","13","","","68","","","","","",1,1,51.6755,39.2089), 10.0, 0.95),
                                ArrivalBusDto(BusDto(51.67557,39.20899,10,"","13","","","68","","","","","",1,1,51.6755,39.2089), 10.0, 9.5)
                        )
                        info.arrivalInfo.arrivalDetails.first().arrivalBuses = busses*/

                        val stationObject = Station.parseDto(info)
                        callback(stationObject)
                    } catch (exception: Throwable) {
                        Log.e("VrnBus", "Hm..", exception)
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        }

        suspend fun loadArrivalInfoAsync(station: Int) = async {
            suspendCoroutine<Station?> { cont ->
                Consts.ARRIVAL.httpGet(listOf(Pair("id", station))).responseObject<ArrivalDto> { request, response, result ->
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
            }
        }

        fun loadBusStations(activity: Activity, loaded: (List<StationOnMap>) -> Unit) {
            try {
                loadJSONFromAsset(activity, "bus_stops.json") {
                    val stations: List<StationDto> = gson.fromJson(it, object : TypeToken<List<StationDto>>() {}.type)
                    loaded(stations.filter { it.lat != 0.0 && it.lon != 0.0 }.map { StationOnMap(it.name.trim(), it.id, it.lat, it.lon) })
                }
            } catch (exception: Throwable) {
                Log.e("Hm..", exception)
            }
        }

        fun loadRoutes(activity: Activity, loaded: (List<String>?) -> Unit) {
            try {
                loadJSONFromAsset(activity, "routes.json") {
                    val routes: List<String> = gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
                    loaded(routes)
                }
            } catch (exception: Throwable) {
                Log.e("Hm..", exception)
            }
        }

        fun loadRouteByName(activity: Activity, route: String, loaded: (Route?) -> Unit) {
            try {
                loadJSONFromAsset(activity, "bus_stations.json") {

                    val routes: Map<String, List<List<Any>>> = gson.fromJson(it, object : TypeToken<Map<String, List<List<Any>>>>() {}.type)

                    val stations: List<StationOnMap>?
                    stations = if (routes.containsKey(route)) {
                        routes[route]?.map {
                            StationOnMap(it[1] as String, 0, it[2] as Double, it[3] as Double)
                        }?.toList()
                    } else null

                    if (stations != null) {
                        loaded(Route(route, stations))
                    } else loaded(null)
                }
            } catch (exception: Throwable) {
                Log.e("Hm..", exception)
                loaded(null)
            }
        }
    }
}

