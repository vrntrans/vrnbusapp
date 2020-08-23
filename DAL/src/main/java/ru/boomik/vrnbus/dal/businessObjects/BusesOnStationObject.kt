package ru.boomik.vrnbus.dal.businessObjects

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class BusesOnStationObject(
        var id: Int,
        var title: String,
        @Contextual
        var time: Calendar,
        var buses: List<BusObject>,
        var routeIds: List<Int>,
)