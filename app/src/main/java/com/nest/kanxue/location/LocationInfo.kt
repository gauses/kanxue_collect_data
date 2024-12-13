package com.nest.kanxue.location

import org.json.JSONObject

data class LocationInfo(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var speed: Float = 0f,
    var speedAccuracyMetersPerSecond: Float = 0f,
    var accuracy: Float = 0f,
    var verticalAccuracy: Float = 0f,
    var horizontalAccuracy: Float = 0f,
    var pdop: Float = 0f,
    var hdop: Float = 0f,
    var vdop: Float = 0f,
    var satelliteCount: Int = 0,
    var satelliteInfo: JSONObject = JSONObject(),  // 添加默认值
    var bearingAccuracyDegrees: Float = 0f
)
