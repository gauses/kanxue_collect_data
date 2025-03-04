package com.test.ndk

import android.hardware.Sensor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
{
        "Id": 0,
        "Name": "bmi26x Accelerometer Non-wakeup",
        "Type": 1,
        "Vendor": "BOSCH",
        "Resolution": 0.0023928226437419653,
        "StringType": "android.sensor.accelerometer",
        "ReportingMode": 0,
        "MaximumRange": 78.45320129394531,
        "MaxDelay": 1000000,
        "FifoReservedEventCount": 3000,
        "HighestDirectReportRateLevel": 3,
        "FifoMaxEventCount": 300,
        "Power": 0.18000000715255737,
        "MinDelay": 1250,
        "Version": 16777479,
        "isDirectChannelTypeSupported-TYPE_HARDWARE_BUFFER": true
    }

 */
data class SensorInfo(
    val Id: Int,
    val Name: String,
    val Type: Int,
    val Vendor: String,
    val Resolution: Float,
    val StringType: String,
    val ReportingMode: Int,
    val MaximumRange: Float,
    val MaxDelay: Int,
    val FifoReservedEventCount: Int,
    val HighestDirectReportRateLevel: Int,
    val FifoMaxEventCount: Int,
    val Power: Float,
    val MinDelay: Int,
    val Version: Int,
)

class Testor {

    companion object{
        init {
            System.loadLibrary("ndk_kanxue")
        }
    }

    fun onSensorChanged(sensorType: Int, values: FloatArray) {
        Log.d("Testor","onSensorChanged: sensorType=$sensorType, values=${values.joinToString()}")
    }

    external fun getSensorHandle(name:String, type:Int):Int
    external fun testSensor(dir:String, sensorTypeArray: IntArray)
    external fun stop()

}