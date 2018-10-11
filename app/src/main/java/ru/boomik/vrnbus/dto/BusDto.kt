package ru.boomik.vrnbus.dto

import com.google.gson.annotations.SerializedName

class BusDto(
        @SerializedName("last_lat_")
        val lastLat: Double,
        @SerializedName("last_lon_")
        var lastLon: Double,
        @SerializedName("last_speed_")
        var lastSpeed: Int,
        @SerializedName("last_time_")
        var time: String,
        @SerializedName("name_")
        var number: String,
        @SerializedName("obj_id_")
        var objId: String,
        @SerializedName("proj_id_")
        var projId: String,
        @SerializedName("route_name_")
        var route: String,
        @SerializedName("type_proj")
        var typeProj: String,
        @SerializedName("last_station_time_")
        var lastStationTime: String,
        @SerializedName("bus_station_")
        var busStation: String,
        @SerializedName("address")
        var address: String,
        @SerializedName("NAME_")
        var nextStationName: String?,
        @SerializedName("ROUT_")
        var rout: Int,
        @SerializedName("CONTROL_")
        var control: Int,
        @SerializedName("LAT_")
        var lat: Double,
        @SerializedName("LON_")
        var lon: Double)