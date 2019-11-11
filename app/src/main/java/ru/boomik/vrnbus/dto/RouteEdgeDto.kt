package ru.boomik.vrnbus.dto

import com.google.gson.annotations.SerializedName

class RouteEdgeDto(@SerializedName("edge_key") val edgeKey: List<Int>, val points: List<EdgeDto>)

class EdgeDto(var lat: Double, var lng : Double)