package ru.boomik.vrnbus.dal.remote.services

import ru.boomik.vrnbus.dal.api.ICoddApi
import ru.boomik.vrnbus.dal.businessObjects.RoutesObject
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.dal.businessObjects.TrackObject
import ru.boomik.vrnbus.dal.remote.BaseCacheableRemoteDataService
import ru.boomik.vrnbus.dal.remote.RequestResultWithData

class CoddPersistentDataService(private val service: ICoddApi, cachePath: String, cacheTime: Long) : BaseCacheableRemoteDataService<ICoddApi>(service, cachePath, cacheTime) {

    suspend fun stations(): RequestResultWithData<List<StationObject>?> {
        return makeRequestWithCacheAndConverter(service::stations, converters::toStationsObject, "stations")
    }

    suspend fun routes(): RequestResultWithData<List<RoutesObject>?> {
        return makeRequestWithCacheAndConverter(service::allActiveRoutesWithStations, converters::toRoutesObject, "routes")
    }

    suspend fun tracks(): RequestResultWithData<List<TrackObject>?> {
        return makeRequestWithCacheAndConverter(service::tracks, converters::toTracks, "routes")
    }
}