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
        var lastLon: Double) {

    fun getSnippet(): String? {
        return "$nextStationName\n$number";
    }

    fun getPosition(): LatLng {
        return LatLng(lastLat, lastLon)
    }
}