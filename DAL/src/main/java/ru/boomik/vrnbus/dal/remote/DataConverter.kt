package ru.boomik.vrnbus.dal.remote

import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.dal.dto.BusStationDto
import ru.boomik.vrnbus.dal.dto.ObjectOnlineResponse

class DataConverter {

    fun toStationsObject(data: List<BusStationDto?>?): List<StationObject> {
        if (data == null || data.isEmpty()) return listOf()
        return data.mapNotNull { it }.map { StationObject(it.id, it.name) }
    }

    fun toBuses(data: ObjectOnlineResponse?): List<BusObject> {
        if (data?.buses == null || data.buses.isEmpty()) return listOf()
       // return data.mapNotNull { it }.map { StationObject(it.id, it.name) }
        return listOf()
    }

}