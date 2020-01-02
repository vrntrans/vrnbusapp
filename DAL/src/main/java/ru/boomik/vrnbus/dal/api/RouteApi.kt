package ru.boomik.vrnbus.dal.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.boomik.vrnbus.dal.dto.FindPathDto
import ru.boomik.vrnbus.dal.dto.RouteWithStationsDto

interface RouteApi {
    /**
     * Доступно анонимно
     *
     * @return Observable&lt;List&lt;RouteWithStationsDto&gt;&gt;
     */
    @GET("routes/withStations")
    suspend fun allActiveRoutesWithStations():List<RouteWithStationsDto?>?

    /**
     * Доступно анонимно
     *
     * @param startStationId  (optional)
     * @param endStationId  (optional)
     * @return Observable&lt;FindPathDto&gt;
     */
    @GET("routes/path")
    suspend fun getPathForStations(
            @Query("startStationId") startStationId: Int?, @Query("endStationId") endStationId: Int?
    ): FindPathDto?
}