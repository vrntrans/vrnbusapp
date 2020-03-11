package ru.boomik.vrnbus.managers

import android.content.Context
import ru.boomik.vrnbus.dal.DataServices
import ru.boomik.vrnbus.dto.RouteEdgeDto
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.StationOnMap

object DataManager {

    private var loading: Boolean = false


    fun initialize(context : Context) {
        val dir = context.externalCacheDir ?: context.cacheDir
        DataServices.init(dir.absolutePath)
    }

    var routeNames: List<String>? = null
    var stations: List<StationOnMap>? = null
    var routes: List<Route>? = null
    var routeEdges: List<RouteEdgeDto>? = null
    var activeStationId: Int = 0


    suspend fun startLoadingData(callback: (Boolean) -> Unit) {
        val service = DataServices.CoddPersistentDataService;
        val stations = service.stations()
        val tracks = service.tracks()
        val routes = service.routes()
    }


}