package ru.boomik.vrnbus.dto


import com.google.gson.annotations.SerializedName

class StationResultDto(
@SerializedName("result")
val result: List<StationDto>)


class StationDto(
        @SerializedName("NAME_")
        val name: String?,
        @SerializedName("ID")
        val id: String?,
        @SerializedName("LAT_")
        var lat: String?,
        @SerializedName("LON_")
        var lon : String?)