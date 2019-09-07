package ru.boomik.vrnbus.objects

import ru.boomik.vrnbus.dto.ArrivalDto
import ru.boomik.vrnbus.dto.BusStopInfoDto
import java.text.SimpleDateFormat
import java.util.*

class Station(
        var id: Int,
        var title: String,
        var time: String,
        var routes: List<Bus>,
        var buses: List<Bus>
) {
    companion object {
        fun parseDto(info: ArrivalDto): Station? {

            val time = info.arrivalInfo.header.replace("Время:", "Время обновления:")

            if (info.arrivalInfo.arrivalDetails.isEmpty()) return null

            val detail = info.arrivalInfo.arrivalDetails.first()

            val pattern = "yyyy-MM-dd'T'HH:mm:ss"


            val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
            val serverDate = dateFormat.parse(info.time)
            val serverCalendar = Calendar.getInstance()
            serverCalendar.time = serverDate
            val timeInMills = serverCalendar.timeInMillis


            val buses = detail.arrivalBuses.asSequence().map {

                val date = dateFormat.parse(it.bus.time)
                val calendar = Calendar.getInstance()
                calendar.time = date

                val bus = Bus()
                bus.route = it.bus.route
                bus.number = it.bus.number
                bus.nextStationName = it.bus.busStation
                bus.lastStationTime = it.bus.lastStationTime
                bus.lastSpeed = it.bus.lastSpeed
                bus.time = calendar
                bus.lat = detail.lat
                bus.lon = detail.lon
                bus.lastLat = it.bus.lastLat
                bus.lastLon = it.bus.lastLon
                bus.timeLeft = it.timeLeft
                bus.distance = it.distance
                bus.lowFloor = it.bus.lowFloor == 1
                bus.busType = it.bus.busType
                bus.timeDifference = (timeInMills - calendar.timeInMillis) / 1000
                bus.init()
                bus
            }.toMutableList()
            val routes = buses.toMutableList()

            val possibleRoutes: List<Bus> = detail.routes.asSequence().filter { it.isNotBlank() }.map { Bus(it) }.toList()

            possibleRoutes.forEach {
                val nextBus = routes.firstOrNull { bus -> it.route == bus.route }
                if (nextBus == null) routes.add(it)
            }
            routes.sortBy { it.timeLeft }
            return Station(detail.stopId, detail.name, time, routes, buses)
        }


        fun parseDto(info: BusStopInfoDto, station: String): Station {
            val header = info.header

            val routes = info.busStops.filter {
                it.key == station
            }.map {
                it.value
            }.map { r->
                r.trim().replace("  ", " ").split("\n").map {
                    it.trim().split(" ")
                }.filter {
                    it.isNotEmpty()
                }.map {
                    Bus(it[0])
                }.filter {
                    it.route.isNotEmpty()
                }
            }.flatten().toMutableList()

            val possibleRoutesText = "Возможные маршруты: "
            var index = info.text.indexOf(possibleRoutesText)
            var possibleRoutes: List<Bus> = ArrayList()

            if (index >= 0) {
                index += possibleRoutesText.length
                possibleRoutes = info.text.substring(index).trim().split(" ").filter { !it.isEmpty() }.map { Bus(it) }
            }
            possibleRoutes.forEach {
                val nextBus = routes.firstOrNull { bus -> it.route == bus.route }
                if (nextBus == null) routes.add(it)
            }
            routes.sortedBy { it.timeLeft }
            return Station(0, "", header, routes, routes)
        }
    }
}
