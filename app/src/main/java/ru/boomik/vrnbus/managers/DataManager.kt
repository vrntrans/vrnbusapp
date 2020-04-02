package ru.boomik.vrnbus.managers

import android.content.Context
import ru.boomik.vrnbus.dal.DataServices
import ru.boomik.vrnbus.dal.businessObjects.RoutesObject
import ru.boomik.vrnbus.dal.businessObjects.StationObject
import ru.boomik.vrnbus.dal.businessObjects.TrackObject
import ru.boomik.vrnbus.dto.RouteEdgeDto
import ru.boomik.vrnbus.objects.Route
import ru.boomik.vrnbus.objects.StationOnMap

object DataManager {

    var searchStationId: Int = -1

    var routeNames: List<String>? = null
    var stations: List<StationObject>? = null
    var routes: List<RoutesObject>? = null
    var tracks: List<TrackObject>? = null

    var activeStationId: Int = 0


}