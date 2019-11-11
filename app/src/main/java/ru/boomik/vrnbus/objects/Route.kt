package ru.boomik.vrnbus.objects


class Route(val route : String, var stations : List<StationOnMap>) {
    var allStations : List<StationOnMap>? = null
}
