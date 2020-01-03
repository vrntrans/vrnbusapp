package ru.boomik.vrnbus.dal.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.boomik.vrnbus.dal.dto.FindPathDto
import ru.boomik.vrnbus.dal.dto.NamedEntityDto
import ru.boomik.vrnbus.dal.dto.NewRouteWithStationsDto

interface INewRouteApi {
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