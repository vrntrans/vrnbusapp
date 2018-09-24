package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng

/**
 * Created by boomv on 15.03.2018.
 */
class Bus(
        var route: String,
        var number: String,
        var nextStationName: String,
        var nextStationTime: String,
        var lastStationTime: String,
        var lastSpeed: Int,
        var time: String,
        var lat: Double,
        var lon: Double,
        val lastLat: Double,
        val lastLon: Double) {

    constructor(route: String, nextStationTime: String) : this(route, "", "", nextStationTime, "", 0, "", 0.0, 0.0, 0.0, 0.0)

    fun getSnippet(): String? {
        return "$nextStationName\nгос. номер: $number\nскорость: $lastSpeed"
    }

    fun getPosition(): LatLng {
        return LatLng(lastLat, lastLon)
    }

    fun getAzimuth() : Double {
        val x = lat - lastLat
        val y = lon - lastLon

        val angle = Math.floor(Math.atan2(y, x) * 180 / Math.PI)
        return Math.abs(angle-180)
    }

    override fun toString(): String {
        return "$route  $nextStationTime"
    }
}