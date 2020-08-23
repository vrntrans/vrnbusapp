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
        if (data?.buses == null || data.buses.isEmpty()) return listOf()

        val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX"

        val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
        val serverDate = dateFormat.parse(data.serverTime)
        val serverCalendar = Calendar.getInstance()

        val calendarNow = Calendar.getInstance()
        serverCalendar.time = serverDate
        val timeInMills = serverCalendar.timeInMillis
        val difference = (calendarNow.timeInMillis - timeInMills) / 1000

        val busPattern = "yyyy-MM-dd'T'HH:mm:ss"
        val busDateFormat = SimpleDateFormat(busPattern, Locale("ru"))

        return data.buses.map {
            val date = busDateFormat.parse(it.lastTime)
            val calendar = Calendar.getInstance()
            calendar.time = date

            BusObject().apply {
                routeId = it.lastRouteId
                licensePlate = it.name
                lastStationTime = busDateFormat.parse(it.lastStationTime)
                lastStationId = it.lastStationId
                lastSpeed = it.lastSpeed
                averageSpeed = it.averageSpeed
                lastLatitude = it.lastLatitude
                lastLongitude = it.lastLongitude
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

    fun toStationBuses(data: ObjectOnlineForStationResponse?): List<BusObject> {
        if (data?.buses == null || data.buses.isNullOrEmpty()) return listOf()
        // return data.mapNotNull { it }.map { StationObject(it.id, it.name) }
        return listOf()
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