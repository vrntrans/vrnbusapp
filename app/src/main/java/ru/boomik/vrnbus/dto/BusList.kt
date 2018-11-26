package ru.boomik.vrnbus.dto

import com.google.gson.annotations.SerializedName

class BusList(
        @SerializedName("result")
        val routes: List<String>)