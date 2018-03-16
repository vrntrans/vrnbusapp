package ru.boomik.vrnbus

import android.app.Activity
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.boomik.vrnbus.dto.BusInfoDto
import ru.boomik.vrnbus.dto.BusStopInfoDto
import ru.boomik.vrnbus.dto.StationDto
import ru.boomik.vrnbus.objects.Bus
import ru.boomik.vrnbus.objects.Station
import ru.boomik.vrnbus.utils.loadJSONFromAsset


class DataService {
    companion object {

        var gson: Gson

        init {
            FuelManager.instance.basePath = Consts.API_URL
            gson = Gson()
        }


        fun loadBusInfo(q: String, callback: (List<Bus>?) -> Unit) {

            Consts.API_BUS_INFO.httpGet(listOf(Pair("q", q))).responseObject<BusInfoDto> { request, response, result ->
                //make a GET to http://httpbin.org/get and do something with response
                Log.d("log", request.toString())
                Log.d("log", response.toString())
                val (_, error) = result
                if (error == null) {

                    try {
                        val info = result.get()
                        val busDtos = info.buses
                        callback(busDtos.filter { it.count() == 2 }.map {
                            Bus(it[0].route, it[0].number, it[1].nextStationName, it[0].lastStationTime, it[0].lastSpeed, it[0].time, it[1].lat, it[1].lon, it[0].lastLat, it[0].lastLon)
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
        fun loadBusStopInfo(station: String, callback: (String?) -> Unit) {

            Consts.API_BUS_STOP_INFO.httpGet(listOf(Pair("station", station), Pair("q",""))).responseObject<BusStopInfoDto> { request, response, result ->
                //make a GET to http://httpbin.org/get and do something with response
                Log.d("log", request.toString())
                Log.d("log", response.toString())
                val (_, error) = result
                if (error == null) {

                    try {
                        val info = result.get()
                        callback(info.text)

                    } catch (exception: Throwable) {
                        Log.e("VrnBus", "Hm..", exception)
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
        }

        fun loadBusStations(activity: Activity): List<Station>? {
            try {
                val json = loadJSONFromAsset(activity, "bus_stops.json")
                var stations: List<StationDto> = gson.fromJson(json, object : TypeToken<List<StationDto>>() {}.type)
                return stations.filter { it.lat != 0.0 && it.lon != 0.0 }.map { Station(it.name.trim(), it.lat, it.lon) }
            } catch (exception: Throwable) {
                Log.e("VrnBus", "Hm..", exception);
            }
            return null
        }
    }
}

