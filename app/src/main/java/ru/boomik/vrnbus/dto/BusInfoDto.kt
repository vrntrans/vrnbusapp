package ru.boomik.vrnbus.dto

import com.google.gson.annotations.SerializedName


class BusInfoDto(
        @SerializedName( "q")
        val query: String,
        var text: String,
        @SerializedName( "server_time")
        var time: String,
        @SerializedName( "buses")
        var buses: List<Array<BusDto>>)

