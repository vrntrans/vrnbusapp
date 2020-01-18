package ru.boomik.vrnbus.dal.businessObjects

data class TrackObject(val from : Int, val to : Int, val coords : List<TrackCoordinatesObject>)

data class TrackCoordinatesObject(val lat:Double, val lon : Double)