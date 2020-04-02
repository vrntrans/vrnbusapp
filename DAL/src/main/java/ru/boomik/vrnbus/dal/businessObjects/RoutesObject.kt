package ru.boomik.vrnbus.dal.businessObjects

data class RoutesObject(val id : Int, val name : String, val type: Int, val forward : List<Int>, val backward : List<Int>)

enum class RouteType(type : Int) {
    Direct(1),
    Circle(2)
}