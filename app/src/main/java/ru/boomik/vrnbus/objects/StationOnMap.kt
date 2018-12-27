package ru.boomik.vrnbus.objects

import com.google.android.gms.maps.model.LatLng
import ru.boomik.vrnbus.dto.StationDto


class StationOnMap(val name: String, val id: Int, var lat: Double, var lon: Double) {


    fun getSnippet(): String? {
        return null
    }

    fun getTitle(): String {
        return name
    }

    fun getPosition(): LatLng {
        return LatLng(lat, lon)
    }

    companion object {
        fun parseDto(dto: StationDto): StationOnMap {
            return StationOnMap(dto.name ?: "", dto.id?.toIntOrNull() ?: 0, dto.lat?.toDoubleOrNull() ?: .0, dto.lon?.toDoubleOrNull() ?: .0)
        }

        fun parseListDto(dtos: List<StationDto>): List<StationOnMap> {
            return dtos.filter { it.lat != null && it.lon != null && it.name != null && it.id != null }.map { parseDto(it) }
        }
    }

}