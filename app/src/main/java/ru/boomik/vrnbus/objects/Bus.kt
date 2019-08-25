package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.floor
import android.R
import ru.boomik.vrnbus.utils.toPluralValue


/**
 * Created by boomv on 15.03.2018.
 */
enum class BusType {
    Small,
    Medium,
    Big,
    BigLowFloor,
    Trolleybus,
    Unknown
}

class Bus {
    var route: String = ""
    var number: String? = null
    var nextStationName: String? = null
    var lastStationTime: String? = null
    var lastSpeed: Int = 0
    var time: Calendar? = null
    var lat: Double = .0
    var lon: Double = .0
    var lastLat: Double = .0
    var lastLon: Double = .0
    var timeLeft: Double = Double.MAX_VALUE
    var distance: Double = .0
    var lowFloor: Boolean = false
    var busType: Int = -1


    constructor()

    constructor(route: String) {
        this.route = route
    }

    var timeToArrival: Long = 0
    var arrivalTime: Date? = null

    var type: BusType = BusType.Unknown

    fun init() {
        type = when {
            route.startsWith("Тр.") -> BusType.Trolleybus
            lowFloor -> BusType.BigLowFloor
            busType == 3 -> BusType.Medium
            busType == 4 -> BusType.Big
            busType == -1 -> BusType.Unknown
            else -> BusType.Small
        }

        timeToArrival = (System.currentTimeMillis() + timeLeft * 60 * 1000).toLong()
        if (timeLeft != Double.MAX_VALUE) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MILLISECOND, (timeLeft * 60 * 1000).toInt())
            arrivalTime = cal.time
        }
    }


    fun getSnippet(): String? {
        val station = if (nextStationName.isNullOrBlank()) "" else "Следующая остановка:\n$nextStationName\n"
        val speed = "Скорость: $lastSpeed км/ч"
        var updateTime = ""
        var gosnumber = ""

        if (!number.isNullOrBlank()) gosnumber = "\nГосномер: $number"

        if (time != null) {

            val now = Calendar.getInstance()
            val difference = (now.timeInMillis - time!!.timeInMillis) / 1000


            val date = time!!.time
            val format1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ru"))
            val seconds = toPluralValue(difference, "секунду", "секунды", "секунд")
            updateTime = "" + (if (difference < 0 || difference > 0) "\nОбновлено: $seconds назад (${format1.format(date)})" else "Обновлено: ${format1.format(date)}")
        }
        return "$station$speed$gosnumber$updateTime"
    }

    fun getPosition(): LatLng {
        return LatLng(lastLat, lastLon)
    }

    fun getAzimuth(): Double {
        val x = lat - lastLat
        val y = lon - lastLon

        return floor(atan2(y, x) * 180 / Math.PI)
    }

    override fun toString(): String {
        return "$route  $timeLeft"
    }
}