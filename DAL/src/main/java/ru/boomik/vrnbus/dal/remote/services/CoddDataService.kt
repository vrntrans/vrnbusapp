package ru.boomik.vrnbus.dal.remote.services

import ru.boomik.vrnbus.dal.api.ICoddApi
import ru.boomik.vrnbus.dal.businessObjects.BusObject
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.dal.dto.ObjectOnlineResponse
import ru.boomik.vrnbus.dal.remote.BaseRemoteDataService
import ru.boomik.vrnbus.dal.remote.RequestResultWithData

class CoddDataService(private val service: ICoddApi) : BaseRemoteDataService<ICoddApi>(service) {

    suspend fun stations(): RequestResultWithData<List<StationObject>> {
        return makeRequestWithConverter(service::stations, converters::toStationsObject)
    }

    suspend fun getBusesByStationId(routesId : String?): RequestResultWithData<List<BusObject>> {

        suspend fun getBusesByConcreteStationId(): ObjectOnlineResponse? {
            return service.getCurrentObjectsOnline(routesId)
        }

        return makeRequestWithConverter(::getBusesByConcreteStationId, converters::toBuses)
    }
}