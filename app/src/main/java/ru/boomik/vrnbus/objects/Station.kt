package ru.boomik.vrnbus.objects

import ru.boomik.vrnbus.dto.BusStopInfoDto
import java.util.*

class Station(
        var title: String,
        var header: String,
        var routes: List<Bus>
) {
    companion object {
        fun parseDto(info: BusStopInfoDto, station: String): Station {
            val header = info.header

            val routes = info.busStops.filter {
                it.key != station
            }.map {
                it.value
            }.map {
                it.trim().split("\n").map {
                    it.trim().split(" ")
                }.filter {
                    !it.isEmpty()
                }.map {
                    Bus(it[0], if (it.size > 2) "${it[1]} ${it[2]}" else if (it.size == 2) it[1] else "")
                }.filter {
                    !it.route.isEmpty()
                }
            }.flatten().toMutableList()

            val possibleRoutesText = "Возможные маршруты: "
            var index = info.text.indexOf(possibleRoutesText)
            var possibleRoutes: List<Bus> = ArrayList()

            if (index >= 0) {
                index += possibleRoutesText.length
                possibleRoutes = info.text.substring(index).trim().split(" ").filter { !it.isEmpty() }.map { Bus(it, "") }
            }
            possibleRoutes.forEach {
                val nextBus = routes.firstOrNull { bus -> it.route == bus.route }
                if (nextBus == null) routes.add(it)
            }
            routes.sortedBy { it.nextStationTime }
            return Station("", header, routes)
        }
    }
}
