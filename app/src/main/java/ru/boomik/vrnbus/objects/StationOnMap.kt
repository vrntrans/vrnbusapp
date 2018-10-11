package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class StationOnMap(val name: String, val id : Int, var lat: Double, var lon: Double)  {


    fun getSnippet(): String? {
        return null
    }

    fun getTitle(): String {
        return name
    }

    fun getPosition(): LatLng {
        return LatLng(lat, lon)
    }
}