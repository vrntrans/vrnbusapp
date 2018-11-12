package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * Created by boomv on 15.03.2018.
 */
enum class BusType {
    Small,
    Medium,
    Big,
    BigLowFloor,
    Trolleybus
}

class Bus {
    var route: String = ""
    var number: String? = null
    var nextStationName: String? = null
    var lastStationTime: String? = null
    var lastSpeed: Int = 0
    var time: String? = null
    var lat: Double = .0
    var lon: Double = .0
    var lastLat: Double = .0
    var lastLon: Double = .0
    var timeLeft: Double = Double.MAX_VALUE
    var distance: Double = .0
    var lowFloor: Boolean = false
    var busType: Int = -1


    constructor()

    constructor(route: String){
        this.route = route
    }
    var timeToArrival : Long = 0
    var arrivalTime : Date? = null

    var type: BusType

    init {
        type = when {
            route.startsWith("Тр.") -> BusType.Trolleybus
            lowFloor -> BusType.BigLowFloor
            busType==3 -> BusType.Medium
            busType==4 -> BusType.Big
            else -> BusType.Small
        }

        timeToArrival = (System.currentTimeMillis() + timeLeft*60*1000).toLong()
        if (timeLeft!=Double.MAX_VALUE) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MILLISECOND, (timeLeft * 60 * 1000).toInt())
            arrivalTime = cal.time
        }
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

        return Math.floor(Math.atan2(y, x) * 180 / Math.PI)
    }

    override fun toString(): String {
        return "$route  $timeLeft"
    }
}