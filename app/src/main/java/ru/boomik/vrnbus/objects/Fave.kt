package ru.boomik.vrnbus.objects

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
class Fave(val type: String, val stationId : Int, val routes: List<String>)
class FaveFull(val fave: Fave, @DrawableRes val icon : Int, val name: String)