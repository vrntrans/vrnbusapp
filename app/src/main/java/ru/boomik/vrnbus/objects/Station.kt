package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class Station(val name: String, var lat: Double, var lon: Double) : ClusterItem {


    override fun getSnippet(): String? {
        return null
    }

    override fun getTitle(): String {
        return name
    }

    override fun getPosition(): LatLng {
        return LatLng(lat, lon)
    }
}