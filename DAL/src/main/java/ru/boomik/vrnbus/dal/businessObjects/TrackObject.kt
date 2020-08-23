package ru.boomik.vrnbus.dal.businessObjects

import kotlinx.serialization.Serializable

@Serializable
data class TrackObject(val from: Int, val to: Int, val coords: List<TrackCoordinatesObject>)

@Serializable
data class TrackCoordinatesObject(val lat: Double, val lon: Double)