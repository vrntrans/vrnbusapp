package ru.boomik.vrnbus.dal.api

import retrofit2.http.GET
import ru.boomik.vrnbus.dal.dto.BusStationDto

interface BusStationApi {
    /**
     * Доступно анонимно
     *
     * @return Observable&lt;List&lt;BusStationDto&gt;&gt;
     */
    @GET("busStations")
    suspend fun stations(): List<BusStationDto?>?
}