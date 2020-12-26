package ru.boomik.vrnbus.objects


class Route(val name : String, var stations : List<StationOnMap>, var stationsReverse : List<StationOnMap>) {
    var allStations : List<StationOnMap>? = null
    var allStationsReverse : List<StationOnMap>? = null
}
