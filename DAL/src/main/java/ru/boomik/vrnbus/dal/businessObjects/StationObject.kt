package ru.boomik.vrnbus.dal.businessObjects

data class StationObject(
        var id: Int,
        var title: String,
        var latitude: Double,
        var longitude: Double,
        var azimuth: Int,
        var num : Int
)