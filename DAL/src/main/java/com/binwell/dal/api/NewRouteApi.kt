package com.binwell.dal.api

import com.binwell.dal.dto.FindPathDto
import com.binwell.dal.dto.NamedEntityDto
import com.binwell.dal.dto.NewRouteWithStationsDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewRouteApi {
    /**
     * Доступно анонимно
     *
     * @return Observable&lt;List&lt;NewRouteWithStationsDto&gt;&gt;
     */
    @GET("newRoutes/withStations")
    suspend fun allActiveRoutesWithStations(): List<NewRouteWithStationsDto?>?

    /**
     * Доступно анонимно
     *
     * @return Observable&lt;List&lt;NamedEntityDto&gt;&gt;
     */
    @GET("newRoutes/busTypes")
    suspend fun busTypes(): List<NamedEntityDto?>?

    /**
     * Доступно анонимно
     *
     * @param startStationId  (optional)
     * @param endStationId  (optional)
     * @return Observable&lt;FindPathDto&gt;
     */
    @GET("newRoutes/path")
    suspend fun getPathForStations(
            @Query("startStationId") startStationId: Int?, @Query("endStationId") endStationId: Int?
    ): FindPathDto?

    /**
     * Доступно анонимно
     *
     * @return Observable&lt;List&lt;NamedEntityDto&gt;&gt;
     */
    @GET("newRoutes/statuses")
    suspend fun statuses(): List<NamedEntityDto?>?
}