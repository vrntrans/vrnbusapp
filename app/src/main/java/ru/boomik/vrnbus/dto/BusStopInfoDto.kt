package ru.boomik.vrnbus.dto

import com.google.gson.annotations.SerializedName

/**
 * Created by boomv on 16.03.2018.
 */
class BusStopInfoDto(
        val header: String,
        val text: String,
        @SerializedName("bus_stops")
        val busStops: HashMap<String, String>)