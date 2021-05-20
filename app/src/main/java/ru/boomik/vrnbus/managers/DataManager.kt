package ru.boomik.vrnbus.managers

import kotlinx.coroutines.*
import ru.boomik.vrnbus.dal.DataServices
import ru.boomik.vrnbus.dal.businessObjects.RoutesObject
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.dal.businessObjects.TrackObject
import ru.boomik.vrnbus.dal.remote.RequestResultWithData
import ru.boomik.vrnbus.dal.remote.RequestStatus
import ru.boomik.vrnbus.objects.Route

object DataManager {

    var loaded: Boolean = false
    var searchStationId: Int = -1

    var routeNames: List<String>? = null
    var stations: List<StationObject>? = null
    var routes: List<RoutesObject>? = null
    var tracks: List<TrackObject>? = null
    var stationRoutes: Map<Int, List<RoutesObject>>? = null
    val routesCalculated: MutableMap<String, Route> = mutableMapOf()

    var activeStationId: Int = 0
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate);


    suspend fun loadData(scope: CoroutineScope) {

            val stationsAsync = scope.async {DataServices.CoddPersistentDataService.stations()}
            val routesAsync = scope.async {DataServices.CoddPersistentDataService.routes()}
            val tracksAsync = scope.async {DataServices.CoddPersistentDataService.tracks()}

            val list = listOf(stationsAsync, routesAsync, tracksAsync).awaitAll()

            val stations = list[0] as RequestResultWithData<List<StationObject>?>
            val routes = list[1] as RequestResultWithData<List<RoutesObject>?>
            val tracks = list[2] as RequestResultWithData<List<TrackObject>?>

            val stationsLoaded = stations.status == RequestStatus.Ok && stations.data != null
            val routesLoaded = routes.status == RequestStatus.Ok && routes.data != null
            val tracksLoaded = tracks.status == RequestStatus.Ok && tracks.data != null
            DataManager.loaded = tracksLoaded && stationsLoaded && routesLoaded

            val routesList = routes.data?.toList()
            DataManager.routes = routesList
            DataManager.routeNames = routesList?.map { r->r.name }
            DataManager.tracks = tracks.data

            val stationRoutes = mutableMapOf<Int, List<RoutesObject>>()

            if (routesList!=null)
            stations.data?.forEach { station ->
                val stationId = station.id
                val routes = routesList.filter {  it.forward.contains(stationId) || it.backward.contains(stationId) }.toList()
                if (routes.isNotEmpty()) stationRoutes[stationId] = routes
            }
            DataManager.stationRoutes=stationRoutes.toMap()
            DataManager.stations = stations.data?.filter { stationRoutes.containsKey(it.id) }?.toList()
    }

}
