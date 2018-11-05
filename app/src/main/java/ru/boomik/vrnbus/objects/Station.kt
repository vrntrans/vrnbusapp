package ru.boomik.vrnbus.objects

import ru.boomik.vrnbus.dto.ArrivalDto
import ru.boomik.vrnbus.dto.BusStopInfoDto
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

            val buses = detail.arrivalBuses.asSequence().map {
                Bus(it.bus.route, it.bus.number, it.bus.busStation, it.bus.lastStationTime, it.bus.lastSpeed, it.bus.time, detail.lat, detail.lon, it.bus.lastLat, it.bus.lastLon, it.timeLeft, it.distance, it.bus.lowFloor==1, 0)
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
            }.map {
                it.trim().replace("  ", " ").split("\n").map {
                    it.trim().split(" ")
                }.filter {
                    !it.isEmpty()
                }.map {
                    Bus(it[0])
                }.filter {
                    !it.route.isEmpty()
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
