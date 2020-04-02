package ru.boomik.vrnbus.dal.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.boomik.vrnbus.dal.dto.*

interface ICoddApi {

    @GET("busStations")
    suspend fun stations(): List<BusStationDto?>?

    @GET("objects/online")
    suspend fun getCurrentObjectsOnline(@Query("routeIds") routeIds: String?): ObjectOnlineResponse?

    @GET("objects/online/station/{busStationId}")
    suspend fun getObjectsOnlineForStation(@Path("busStationId") busStationId: Long?): ObjectOnlineForStationResponse?

    @GET("routes/withStations")
    suspend fun allActiveRoutesWithStations():List<RouteWithStationsDto>?

    @GET("tracks")
    suspend fun tracks():List<TrackDto>?

    @GET("routes/path")
    suspend fun getPathForStations(@Query("startStationId") startStationId: Int?, @Query("endStationId") endStationId: Int?): FindPathDto?
}