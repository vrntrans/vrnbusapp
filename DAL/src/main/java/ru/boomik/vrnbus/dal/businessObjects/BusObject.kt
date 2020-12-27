package ru.boomik.vrnbus.dal.businessObjects

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

/*
{
  "serverTime": "2020-01-16T23:50:47.1702184+03:00",
  "buses": [
    {
      "lastStationId": 393,
      "averageSpeed": 0,
      "id": 7167,
      "name": "Т287ТК161",
      "objId": 495,
      "lastTime": "2020-01-16T23:46:05",
      "lastLongitude": 39.19441833333334,
      "lastLatitude": 51.700693333333334,
      "lastSpeed": 0,
      "projectId": 10,
      "lastStationNum": 24,
      "lastStationTime": "2020-01-16T19:53:27",
      "lastRouteId": 48,
      "carTypeId": 2,
      "azimuth": 270,
      "providerId": 12,
      "carBrandId": 311,
      "dateInserted": "2013-12-04T12:34:20",
      "objectOutput": false,
      "objectOutputDate": "2016-11-25T08:27:26",
      "phone": 5033236538,
      "yearRelease": 2014,
      "dispRouteId": 72,
      "statusName": "Активно"
    }
  ]
}
 */

@Serializable
class BusObject {
    var id: Int = 0
    var routeId: Int = 0
    var routeName: String = ""
    var minutesLeftToBusStop: Double = .0
    var licensePlate: String? = null
    @Contextual
    var lastTime: Calendar? = null
    var lastLongitude: Double = .0
    var lastLatitude: Double = .0
    var azimuth: Int = 0
    var lowFloor: Boolean = false
    var busType: BusType = BusType.Unknown
    var averageSpeed: Double = .0
    var lastSpeed: Double = .0
    @Contextual
    var lastStationTime: Date = Date()
    var lastStationId: Int = 0
    var providerId: Int = 0
    var projectId: Int = 0
    var carBrandId: Int = 0
    var nextStationName: String? = null

    var localServerTimeDifference: Long = 0

    enum class BusType {
        Small,
        Medium,
        Big,
        Trolleybus,
        Unknown
    }
}