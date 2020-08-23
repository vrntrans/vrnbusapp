package ru.boomik.vrnbus.dal.remote.services

import ru.boomik.vrnbus.dal.api.ICoddApi
import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.dal.dto.ObjectOnlineForStationResponse
import ru.boomik.vrnbus.dal.dto.ObjectOnlineResponse
import ru.boomik.vrnbus.dal.remote.BaseRemoteDataService
import ru.boomik.vrnbus.dal.remote.RequestResultWithData

class CoddDataService(private val service: ICoddApi) : BaseRemoteDataService<ICoddApi>(service) {

    suspend fun getBusesByStationId(busStationId: Int): RequestResultWithData<List<BusObject>> {
        suspend fun getBusesBySpecificStationId(): ObjectOnlineForStationResponse? {
            return service.getObjectsOnlineForStation(busStationId)
        }
        return makeRequestWithConverter(::getBusesBySpecificStationId, converters::toStationBuses)
    }

    suspend fun getBusesForRoutes(routesId: String?): RequestResultWithData<List<BusObject>> {
        suspend fun getBusesBySpecificRouteIdId(): ObjectOnlineResponse? {
            return service.getCurrentObjectsOnline(routesId)
        }
        return makeRequestWithConverter(::getBusesBySpecificRouteIdId, converters::toBuses)
    }
}