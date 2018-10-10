package ru.boomik.vrnbus.dto


import com.google.gson.annotations.SerializedName

class StationDto(
        @SerializedName("NAME_")
        val name: String,
        @SerializedName("ID")
        val id: Int,
        @SerializedName("LAT_")
        var lat: Double,
        @SerializedName("LON_")
        var lon : Double)