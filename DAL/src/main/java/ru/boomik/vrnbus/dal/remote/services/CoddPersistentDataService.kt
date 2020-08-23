package ru.boomik.vrnbus.dal.remote.services

import ru.boomik.vrnbus.dal.api.ICoddApi
import ru.boomik.vrnbus.dal.businessObjects.RoutesObject
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.dal.businessObjects.TrackObject
import ru.boomik.vrnbus.dal.remote.BaseCacheableRemoteDataService
import ru.boomik.vrnbus.dal.remote.RequestResultWithData
import kotlin.reflect.KFunction1

class CoddPersistentDataService(private val service: ICoddApi, cachePath: String, cacheTime: Long, val logFunc: KFunction1<String, Unit>) : BaseCacheableRemoteDataService<ICoddApi>(service, cachePath, cacheTime) {

    suspend fun stations(): RequestResultWithData<List<StationObject>?> {
        return makeRequestWithCacheAndConverter(service::stations, converters::toStationsObject, "stations", 0L, false,false, logFunc)
    }

    suspend fun routes(): RequestResultWithData<List<RoutesObject>?> {
        return makeRequestWithCacheAndConverter(service::allActiveRoutesWithStations, converters::toRoutesObject, "routes", 0L, false,false, logFunc)
    }

    suspend fun tracks(): RequestResultWithData<List<TrackObject>?> {
        return makeRequestWithCacheAndConverter(service::tracks, converters::toTracks, "tracks", 0L, false,false, logFunc)
    }
}