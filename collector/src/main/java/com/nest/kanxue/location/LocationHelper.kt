package com.nest.kanxue.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject

class LocationHelper(
    private val context: Context,
    private val onLocationUpdate: (LocationInfo) -> Unit = {},
    private val onSatelliteUpdate: (Map<String, Int>) -> Unit = {},
    private val onError: ((String) -> Unit)? = null
) {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    var locationInfo = LocationInfo()
    var currentPdop: Float = 0f
    var currentHdop: Float = 0f
    var currentVdop: Float = 0f
    var currentSatelliteCount: Int = 0
    var currentSatelliteInfo: JSONObject = JSONObject()

    private var nmeaListener: OnNmeaMessageListener? = null
    private var started = false

    private val locationListener = LocationListener { location ->
        Log.d("LocationHelper", "Location update received: $location")
        applyLocation(location)
        onLocationUpdate(locationInfo)
    }

    private data class SatellitePosition(
        val x: Double,
        val y: Double,
        val z: Double
    )

    @RequiresApi(Build.VERSION_CODES.N)
    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            try {
                val satelliteInfo = mutableMapOf(
                    "GPS" to 0,
                    "SBAS" to 0,
                    "GLONASS" to 0,
                    "GALILEO" to 0,
                    "BEIDOU" to 0,
                    "QZSS" to 0,
                    "IRNSS" to 0
                )
                currentSatelliteCount = status.satelliteCount

                for (i in 0 until status.satelliteCount) {
                    when (status.getConstellationType(i)) {
                        GnssStatus.CONSTELLATION_GPS ->
                            satelliteInfo["GPS"] = satelliteInfo["GPS"]!! + 1
                        GnssStatus.CONSTELLATION_SBAS ->
                            satelliteInfo["SBAS"] = satelliteInfo["SBAS"]!! + 1
                        GnssStatus.CONSTELLATION_GLONASS ->
                            satelliteInfo["GLONASS"] = satelliteInfo["GLONASS"]!! + 1
                        GnssStatus.CONSTELLATION_GALILEO ->
                            satelliteInfo["GALILEO"] = satelliteInfo["GALILEO"]!! + 1
                        GnssStatus.CONSTELLATION_BEIDOU ->
                            satelliteInfo["BEIDOU"] = satelliteInfo["BEIDOU"]!! + 1
                        GnssStatus.CONSTELLATION_QZSS ->
                            satelliteInfo["QZSS"] = satelliteInfo["QZSS"]!! + 1
                        GnssStatus.CONSTELLATION_IRNSS ->
                            satelliteInfo["IRNSS"] = satelliteInfo["IRNSS"]!! + 1
                    }
                }

                currentSatelliteInfo = JSONObject(satelliteInfo as Map<*, *>)
                locationInfo.satelliteCount = currentSatelliteCount
                locationInfo.satelliteInfo = currentSatelliteInfo
                onSatelliteUpdate(satelliteInfo)
            } catch (e: Exception) {
                Log.e("LocationHelper", "Error processing GNSS status", e)
            }
        }
    }

    private fun applyLocation(location: Location) {
        locationInfo.latitude = location.latitude
        locationInfo.longitude = location.longitude
        locationInfo.altitude = location.altitude
        locationInfo.speed = location.speed
        locationInfo.speedAccuracyMetersPerSecond = location.speedAccuracyMetersPerSecond
        locationInfo.accuracy = location.accuracy
        locationInfo.verticalAccuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.verticalAccuracyMeters
        } else {
            0f
        }
        locationInfo.horizontalAccuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.accuracy
        } else {
            0f
        }
        locationInfo.pdop = currentPdop
        locationInfo.hdop = currentHdop
        locationInfo.vdop = currentVdop
        locationInfo.satelliteCount = currentSatelliteCount
        locationInfo.satelliteInfo = currentSatelliteInfo
        locationInfo.bearingAccuracyDegrees = location.bearingAccuracyDegrees
    }

    fun startLocationUpdates() {
        if (started) return
        if (!checkLocationEnabled()) {
            onError?.invoke("GPS is disabled. Please enable location services.")
            // 仍尝试 lastKnown
            getLastKnownLocation()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError?.invoke("Location permission not granted")
            return
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    0f,
                    locationListener,
                    Looper.getMainLooper()
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    0f,
                    locationListener,
                    Looper.getMainLooper()
                )
            }

            // 必须从有 Looper 的线程投递回调；采集线程无 Looper，统一绑主线程 Handler
            val mainHandler = Handler(Looper.getMainLooper())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationManager.registerGnssStatusCallback(gnssStatusCallback, mainHandler)
            }

            nmeaListener = OnNmeaMessageListener { message, _ ->
                if (message.startsWith("\$GPGSA") || message.startsWith("\$GNGSA")) {
                    val parts = message.split(",")
                    if (parts.size >= 17) {
                        currentPdop = parts[15].toFloatOrNull() ?: 0f
                        currentHdop = parts[16].toFloatOrNull() ?: 0f
                        currentVdop = parts[17].split("*")[0].toFloatOrNull() ?: 0f
                        locationInfo.pdop = currentPdop
                        locationInfo.hdop = currentHdop
                        locationInfo.vdop = currentVdop
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.addNmeaListener(
                    ContextCompat.getMainExecutor(context),
                    nmeaListener!!
                )
            } else {
                @Suppress("DEPRECATION")
                locationManager.addNmeaListener(nmeaListener!!, mainHandler)
            }

            getLastKnownLocation()?.let { onLocationUpdate(it) }
            started = true
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error starting location updates", e)
            onError?.invoke("Error starting location updates: ${e.message}")
        }
    }

    fun stopLocationUpdates() {
        try {
            locationManager.removeUpdates(locationListener)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
            }
            nmeaListener?.let { locationManager.removeNmeaListener(it) }
            nmeaListener = null
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error stopping location updates", e)
        } finally {
            started = false
        }
    }

    /** 输出与历史采集一致的地理位置 JSON。
     *  不再重复调用 getLastKnownLocation()：locationListener / GnssStatus 回调
     *  已在主线程持续更新 locationInfo，直接读缓存值即可，避免在子线程调用
     *  LocationManager API 导致 Android 16 部分机型崩溃。
     */
    fun toCollectJson(): JSONObject {
        return JSONObject().apply {
            put("latitude", locationInfo.latitude)
            put("longitude", locationInfo.longitude)
            put("altitude", locationInfo.altitude)
            put("speed", locationInfo.speed)
            put("speedAccuracyMetersPerSecond", locationInfo.speedAccuracyMetersPerSecond)
            put("bearingAccuracyDegrees", locationInfo.bearingAccuracyDegrees)
            put("accuracy", locationInfo.accuracy)
            put("verticalAccuracy", locationInfo.verticalAccuracy)
            put("horizontalAccuracy", locationInfo.horizontalAccuracy)
            put("satelliteInfo", locationInfo.satelliteInfo)
            put("number of satellite", locationInfo.satelliteCount)
            put("PDOP", locationInfo.pdop)
            put("HDOP", locationInfo.hdop)
            put("VDOP", locationInfo.vdop)
        }
    }

    private fun checkLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun getLastKnownLocation(): LocationInfo? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                bestLocation = location
            }
        }

        return bestLocation?.let {
            applyLocation(it)
            locationInfo
        }
    }
}
