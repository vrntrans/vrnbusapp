package ru.boomik.vrnbus.dto


import com.google.gson.annotations.SerializedName

/**
 * Created by boomv on 16.03.2018.
 */

class ArrivalDto(
        @SerializedName( "server_time")
        var time: String,
        @SerializedName("arrival_info")
        val arrivalInfo: ArrivalInfoDto)

class ArrivalInfoDto(
        val text: String,
        val header: String,
        @SerializedName("arrival_details")
        val arrivalDetails: List<ArrivalDetailsDto>)

class ArrivalDetailsDto(
        @SerializedName("bus_stop_id")
        val stopId: Int,
        @SerializedName("bus_stop_name")
        val name: String,
        var lat: Double,
        var lon: Double,
        @SerializedName("bus_routes")
        val routes: List<String>,
        @SerializedName("arrival_buses")
        var arrivalBuses: List<ArrivalBusDto>
)

class ArrivalBusDto(
        @SerializedName("bus_info")
        val bus: BusDto,
        var distance: Double,
        @SerializedName("time_left")
        var timeLeft: Double

)