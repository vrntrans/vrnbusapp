@file:Suppress("UNCHECKED_CAST")

package ru.boomik.vrnbus.dal.remote.services

import ru.boomik.vrnbus.dal.api.ICoddApi
import ru.boomik.vrnbus.dal.businessObjects.RoutesObject
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.dal.businessObjects.TrackObject
import ru.boomik.vrnbus.dal.dto.RouteWithStationsDto
import ru.boomik.vrnbus.dal.remote.BaseCacheableRemoteDataService
import ru.boomik.vrnbus.dal.remote.RequestResultWithData

class CoddPersistentDataService(private val service: ICoddApi, cachePath: String, cacheTime: Long) : BaseCacheableRemoteDataService<ICoddApi>(service, cachePath, cacheTime) {

    suspend fun stations(): RequestResultWithData<List<StationObject>?> {
        return makeRequestWithCacheAndConverter(service::stations, converters::toStationsObject, "stations", List::class.java as Class<List<StationObject>>)
    }

    suspend fun routes(stations : List<StationObject>): RequestResultWithData<List<RoutesObject>?> {
        fun toRoutesObject(data: List<RouteWithStationsDto>?) : List<RoutesObject> { return converters.toRoutesObject(data, stations) }
        return makeRequestWithCacheAndConverter(service::allActiveRoutesWithStations, ::toRoutesObject, "routes", List::class.java as Class<List<RoutesObject>>)
    }

    suspend fun tracks(): RequestResultWithData<List<TrackObject>?> {
        return makeRequestWithCacheAndConverter(service::tracks, converters::toTracks, "routes", List::class.java as Class<List<TrackObject>>)
    }
}