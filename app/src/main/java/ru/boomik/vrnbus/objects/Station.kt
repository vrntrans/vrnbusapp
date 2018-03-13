package ru.boomik.vrnbus.objects

import com.beust.klaxon.Json
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class Station(
        @Json(name = "NAME_")
        val name: String,
        @Json(name = "LAT_")
        var lat: Double,
        @Json(name = "LON_")
        var lon : Double) : ClusterItem {
    override fun getSnippet(): String? {
       return null
    }

    override fun getTitle(): String {
       return name
    }

    override fun getPosition(): LatLng {
        return LatLng(lat,lon)
    }

}