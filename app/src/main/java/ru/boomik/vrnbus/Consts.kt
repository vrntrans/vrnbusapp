package ru.boomik.vrnbus

import com.google.android.gms.maps.model.LatLng


object Consts{
    const val API_URL="https://vrnbus.herokuapp.com"
    const val API_BUS_INFO="/busmap"
    const val ARRIVAL="/arrival_by_id"
    const val SETTINGS_TRAFFIC_JAM = "traffic_jam"
    const val SETTINGS_REFERER = "referer"
    const val SETTINGS_FAVORITE_ROUTE = "favorite_route"
    const val SETTINGS_FAVORITE_STATIONS = "favorite_stations"

    const val LOCATION_PERMISSION_REQUEST = 10


    const val INITIAL_ZOOM = 16f
    val INITIAL_POSITION =  LatLng(51.661772, 39.202066)

}