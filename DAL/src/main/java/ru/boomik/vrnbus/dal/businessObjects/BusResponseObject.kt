package ru.boomik.vrnbus.dal.businessObjects

import kotlinx.serialization.Serializable

@Serializable
data class BusResponseObject(val time: String, val buses: List<BusObject>)