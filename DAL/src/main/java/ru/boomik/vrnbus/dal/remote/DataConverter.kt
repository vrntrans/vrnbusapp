package ru.boomik.vrnbus.dal.remote

import ru.boomik.vrnbus.dal.businessObjects.*
import ru.boomik.vrnbus.dal.dto.*
import java.text.SimpleDateFormat
import java.util.*

class DataConverter {

    fun toStationsObject(data: List<BusStationDto?>?): List<StationObject> {
        if (data == null || data.isEmpty()) return listOf()
        var num = 0
        return data.mapNotNull { it }.map {
            StationObject(it.id, it.name, it.latitude, it.longitude, it.azimuth, num++)
        }
    }


    fun toBuses(data: ObjectOnlineResponse?): List<BusObject> {
        return toBuses(data?.buses, data?.serverTime)
    }

    private fun toBuses(buses : List<ObjectOnlineDto>?, serverTime : String?): List<BusObject> {
        if (buses == null || buses.isEmpty()) return listOf()

        val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX"

        val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
        val serverDate = dateFormat.parse(serverTime)
        val serverCalendar = Calendar.getInstance()

        val calendarNow = Calendar.getInstance()
        serverCalendar.time = serverDate
        val timeInMills = serverCalendar.timeInMillis
        val difference = (calendarNow.timeInMillis - timeInMills) / 1000

        val busPattern = "yyyy-MM-dd'T'HH:mm:ss"
        val busDateFormat = SimpleDateFormat(busPattern, Locale("ru"))

        return buses.map {

            if (it.lastTime.isBlank()) it.lastTime = "2020-11-29T15:00:57"
            if (it.lastStationTime.isNullOrBlank()) it.lastStationTime = "2020-11-29T15:00:57"

            val date = busDateFormat.parse(it.lastTime)
            val calendar = Calendar.getInstance()
            calendar.time = date

            BusObject().apply {
                licensePlate = it.name
                lastStationTime = busDateFormat.parse(it.lastStationTime)
                lastSpeed = it.lastSpeed
                routeName = it.routeName
                averageSpeed = it.averageSpeed
                lastLatitude = if (it.lastLatitude != .0) it.lastLatitude else it.latitude
                lastLongitude = if (it.lastLongitude != .0) it.lastLongitude else it.longitude
                lastTime = calendar
                azimuth = it.azimuth
                lowFloor = it.lowfloor
                localServerTimeDifference = difference
                minutesLeftToBusStop = it.minutesLeftToBusStop
                busType = when (it.carTypeId) {
                    3 -> BusObject.BusType.Medium
                    4 -> BusObject.BusType.Big
                    -1 -> BusObject.BusType.Unknown
                    else -> BusObject.BusType.Small
                }
            }
        }
    }

    fun toStationBuses(data: ObjectOnlineForStationResponse?): BusesOnStationObject? {
        if (data == null) return null
        val pattern = "yyyy-MM-dd'T'HH:mm:ss"
        val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
        val serverDate = data.serverTime
        val serverCalendar = Calendar.getInstance()
        serverCalendar.time = dateFormat.parse(serverDate)
        val buses: List<BusObject> = toBuses(data.buses, data.serverTime)
        return BusesOnStationObject(-1, "", serverCalendar, buses)
    }

    fun toRoutesObject(data: List<RouteWithStationsDto>?): List<RoutesObject> {
        if (data == null || data.isEmpty()) return listOf()
        return data.map { it }.map { RoutesObject(it.id, it.name, if (it.type == RouteWithStationsDto.TypeEnum.NUMBER_1) 1 else 2, it.forwardDirectionStations, it.backDirectionStations) }
    }


    fun toTracks(data: List<TrackDto>?): List<TrackObject> {
        if (data == null || data.isEmpty()) return listOf()
        return data.map {
            TrackObject(
                    it.startBusStationId,
                    it.endBusStationId,
                    it.trackCoordinates?.map { coordinate -> TrackCoordinatesObject(coordinate.latitude, coordinate.longitude) }
                            ?: listOf())
        }
    }
}