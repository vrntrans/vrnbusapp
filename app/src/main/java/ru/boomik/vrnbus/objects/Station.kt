package ru.boomik.vrnbus.objects

import ru.boomik.vrnbus.dto.BusStopInfoDto
import java.util.*

class Station(
    var title : String,
    var header : String,
    var routes : List<Bus>
) {
    companion object {
        fun parseDto (info : BusStopInfoDto) : Station {
            val header = info.header

            val next = info.busStops.values.filter { !it.isEmpty() }.map { it.trim().split("\n").map { it.trim().split(" ") }.map { Bus(it[0], if(it.size>=2) it[1] else "") }.filter { !it.route.isEmpty() }}.flatten().toMutableList()

            val possibleRoutesText = "Возможные маршруты: "
            var index = info.text.indexOf(possibleRoutesText)
            var routes : List<Bus> = ArrayList()
            if (index>=0) {
                index+=possibleRoutesText.length
                routes = info.text.substring(index).trim().split(" ").filter { !it.isEmpty() }.map { Bus(it,"") }
            }
            routes.forEach {
                val nextBus = next.firstOrNull { bus -> it.route == bus.route }
                if (nextBus!=null) it.nextStationTime=nextBus.nextStationTime
            }
            routes.sortedBy { it.nextStationTime }
            return Station("", header, routes)
        }
    }
}
