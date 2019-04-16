package ru.boomik.vrnbus

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng


object Consts{
    const val API_URL="https://vrnbus.herokuapp.com"
    const val API_BUS_INFO="/busmap"
    const val API_ROUTES="/bus_stations.json"
    const val API_BUS_LIST="/buslist"
    const val API_STATIONS="/bus_stops"


    const val ARRIVAL="/arrival_by_id"
    const val SETTINGS_TRAFFIC_JAM = "traffic_jam"
    const val SETTINGS_FAVORITE_ROUTE = "favorite_route"
    const val SETTINGS_FAVORITE_STATIONS = "favorite_stations"
    const val SETTINGS_NIGHT = "nightMode"
    const val SETTINGS_REFERER = "referer"
    const val SETTINGS_ZOOM = "zoomButtons"
    const val SETTINGS_OSM = "osmMap"
    const val SETTINGS_ANALYTICS = "analytics"

    const val LOCATION_PERMISSION_REQUEST = 10

    const val TILES_URL_A="http://a.tile.openstreetmap.org/{z}/{x}/{y}.png"
    const val TILES_URL_B="http://a.tile.openstreetmap.org/{z}/{x}/{y}.png"
    const val TILES_URL_C="http://a.tile.openstreetmap.org/{z}/{x}/{y}.png"
    const val TILES_URL_DARK_A="https://a.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"
    const val TILES_URL_DARK_B="https://b.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"
    const val TILES_URL_DARK_C="https://c.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"

    const val TILES_URL_STAMEN="http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg"
    const val TILES_URL_MB="http://a.tiles.mapbox.com/v3/mapbox.geography-class/{z}/{x}/{y}.png"

    const val COLOR_BUS = 0x0000FF
    const val COLOR_BUS_MEDIUM = 0xdd2a23
    const val COLOR_BUS_SMALL = 0xe9a01f
    const val COLOR_BUS_TROLLEYBUS = 0x477a25

    const val INITIAL_ZOOM = 16f
    val INITIAL_POSITION =  LatLng(51.661772, 39.202066)

}