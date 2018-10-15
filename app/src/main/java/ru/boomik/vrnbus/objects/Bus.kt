package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng

/**
 * Created by boomv on 15.03.2018.
 */
class Bus(
        var route: String,
        var number: String,
        var nextStationName: String,
        var lastStationTime: String,
        var lastSpeed: Int,
        var time: String,
        var lat: Double,
        var lon: Double,
        val lastLat: Double,
        val lastLon: Double,
        val timeLeft: Double,
        val distance: Double) {

    constructor(route: String) : this(route, "", "", "", 0, "", 0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, 0.0)

    var timeToArrival : Long = 0

    init {
        timeToArrival = (System.currentTimeMillis() + timeLeft*60*1000).toLong()
    }

    fun getSnippet(): String? {
        return "$nextStationName\nгос. номер: $number\nскорость: $lastSpeed"
    }

    fun getPosition(): LatLng {
        return LatLng(lastLat, lastLon)
    }

    fun getAzimuth(): Double {
        val x = lat - lastLat
        val y = lon - lastLon

        val angle = Math.floor(Math.atan2(y, x) * 180 / Math.PI)
        return Math.abs(angle - 180)
    }

    override fun toString(): String {
        return "$route  $timeLeft"
    }
}