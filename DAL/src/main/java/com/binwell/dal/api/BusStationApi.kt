package com.binwell.dal.api

import com.binwell.dal.dto.BusStationDto
import retrofit2.http.GET

interface BusStationApi {
    /**
     * Доступно анонимно
     *
     * @return Observable&lt;List&lt;BusStationDto&gt;&gt;
     */
    @GET("busStations")
    suspend fun stations(): List<BusStationDto?>?
}