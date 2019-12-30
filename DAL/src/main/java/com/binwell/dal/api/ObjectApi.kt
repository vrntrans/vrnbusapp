package com.binwell.dal.api

import com.binwell.dal.dto.ObjectOnlineForStationResponse
import com.binwell.dal.dto.ObjectOnlineResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ObjectApi {
    /**
     * Доступно анонимно
     *
     * @param routeIds  (optional)
     * @return Observable&lt;ObjectOnlineResponse&gt;
     */
    @GET("objects/online")
    fun getCurrentObjectsOnline(
            @Query("routeIds") routeIds: String?
    ): ObjectOnlineResponse?

    /**
     * Доступно анонимно
     *
     * @param busStationId  (required)
     * @return Observable&lt;ObjectOnlineForStationResponse&gt;
     */
    @GET("objects/online/station/{busStationId}")
    suspend fun getObjectsOnlineForStation(
            @Path("busStationId") busStationId: Long?
    ): ObjectOnlineForStationResponse?
}