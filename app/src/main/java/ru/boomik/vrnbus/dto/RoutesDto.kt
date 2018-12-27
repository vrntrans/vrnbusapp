package ru.boomik.vrnbus.dto

import com.google.gson.annotations.SerializedName

class RoutesDto(
        @SerializedName("result")
        val result: List<String>)
