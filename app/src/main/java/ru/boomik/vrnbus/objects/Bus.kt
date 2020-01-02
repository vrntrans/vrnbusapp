package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng
import ru.boomik.vrnbus.utils.toPluralValue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


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
    var timeDifference: Long = 0
    var localServerTimeDifference: Long = 0
    var azimuth: Int = 0


    constructor()

    constructor(route: String) {
        this.route = route
    }

    var timeToArrival: Long = 0
    var arrivalTime: Date? = null

    var type: BusType = BusType.Unknown

    fun init() {
        type = when {
            route.startsWith("Т") -> BusType.Trolleybus
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

        val typeString = when (type) {
            BusType.Small -> "Автобус малой вместимости\n"
            BusType.Medium -> "Автобус средней вместимости\n"
            BusType.Big -> "Автобус большой вместимости\n"
            BusType.BigLowFloor -> "Низкопольный автобус большой вместимости\n"
            BusType.Trolleybus -> "Троллейбус\n"
            else -> ""
        }

        val station = if (nextStationName.isNullOrBlank()) "" else "Следующая остановка:\n\t$nextStationName\n\n"
        val speed = "Скорость: $lastSpeed км/ч"
        var updateTime = ""
        var gosnumber = ""

        if (!number.isNullOrBlank()) gosnumber = "\nГосномер: $number"

        if (time != null) {

            var difference = timeDifference
            if (time!=null) {
                val addidionalDifference = (Calendar.getInstance().timeInMillis - time!!.timeInMillis)/1000 - localServerTimeDifference
                difference = addidionalDifference
            }


            val date = time!!.time
            val format1 = SimpleDateFormat("HH:mm:ss", Locale("ru"))
            val timeString = if (abs(difference) >=60*5) {
                toPluralValue((difference/60).toInt(), "минуту", "минуты", "минут")
            } else toPluralValue(difference, "секунду", "секунды", "секунд")

            updateTime = "" + (if (difference < 0 || difference > 0) "\nОбновлено: $timeString назад (${format1.format(date)})" else "Обновлено: ${format1.format(date)}")
        }
        return "$station$typeString$speed$gosnumber$updateTime"
    }

    fun getPosition(): LatLng {
        return LatLng(lastLat, lastLon)
    }

    fun getAzimuth(): Double {

        //val x = lat - lastLat
        //val y = lon - lastLon
        //return floor(atan2(y, x) * 180 / Math.PI)
        return azimuth.toDouble()
    }

    override fun toString(): String {
        return "$route  $timeLeft"
    }
}