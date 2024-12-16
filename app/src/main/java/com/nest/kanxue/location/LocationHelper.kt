import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.nest.kanxue.location.LocationInfo
import org.json.JSONObject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationHelper(
    private val context: Context,
    private val onLocationUpdate: (LocationInfo) -> Unit,
    private val onSatelliteUpdate: (Map<String, Int>) -> Unit,

    private val onError: ((String) -> Unit)? = null
) {
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


//    var locationInfoJSONObject = JSONObject()

    var locationInfo = LocationInfo()
    var currentPdop: Float = 0f
    var currentHdop: Float = 0f
    var currentVdop: Float = 0f
    var currentSatelliteCount: Int = 0
    var currentSatelliteInfo: JSONObject = JSONObject()  // 添加默认值


    private val locationListener = LocationListener { location ->
        Log.d("LocationHelper", "Location update received: $location")
        Log.d("LocationHelper", "Location location.latitude: " + location.latitude)




        location.apply {
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


        onLocationUpdate(locationInfo)
    }

    // 卫星位置数据类
    private data class SatellitePosition(
        val x: Double,
        val y: Double,
        val z: Double
    )

    @RequiresApi(Build.VERSION_CODES.N)
    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            try {
                val satellites = mutableListOf<SatellitePosition>()
                val satelliteInfo = mutableMapOf(
                    "GPS" to 0,
                    "GLONASS" to 0,
                    "GALILEO" to 0,
                    "BEIDOU" to 0,
                    "QZSS" to 0,
                    "IRNSS" to 0
                )
                Log.d("LocationHelper", "status.satelliteCount: " + status.satelliteCount) //
                currentSatelliteCount =  status.satelliteCount


                for (i in 0 until status.satelliteCount) {
                    val azimuth = status.getAzimuthDegrees(i)
                    val elevation = status.getElevationDegrees(i)
                    val cn0DbHz = status.getCn0DbHz(i)
                    val usedInFix = status.usedInFix(i)

                    Log.d("LocationHelper", "status azimuth $azimuth")
                    Log.d("LocationHelper", "status elevation $elevation")
                    Log.d("LocationHelper", "status cn0DbHz $cn0DbHz")
                    Log.d("LocationHelper", "status usedInFix $usedInFix")


                    // 只处理用于定位的卫星
//                    if (usedInFix && cn0DbHz > 0) {
                    if (cn0DbHz > 0) {
                        val position = convertToCartesian(azimuth, elevation)
                        Log.d("LocationHelper", "status position $position")
                        satellites.add(position)
                    }


                    // 更新卫星计数
                    when (status.getConstellationType(i)) {
                        GnssStatus.CONSTELLATION_GPS -> //1
                            if (usedInFix) satelliteInfo["GPS"] = satelliteInfo["GPS"]!! + 1

                        GnssStatus.CONSTELLATION_SBAS -> //2
                            if (usedInFix) satelliteInfo["SBAS"] = satelliteInfo["SBAS"]!! + 1

                        GnssStatus.CONSTELLATION_GLONASS -> //3
                            if (usedInFix) satelliteInfo["GLONASS"] = satelliteInfo["GLONASS"]!! + 1

                        GnssStatus.CONSTELLATION_QZSS -> //4
                            if (usedInFix) satelliteInfo["QZSS"] = satelliteInfo["QZSS"]!! + 1


                        GnssStatus.CONSTELLATION_BEIDOU -> //5
                            if (usedInFix) satelliteInfo["BEIDOU"] = satelliteInfo["BEIDOU"]!! + 1

                        GnssStatus.CONSTELLATION_GALILEO -> //6
                            if (usedInFix) satelliteInfo["GALILEO"] = satelliteInfo["GALILEO"]!! + 1


                        GnssStatus.CONSTELLATION_IRNSS -> //7
                            if (usedInFix) satelliteInfo["IRNSS"] = satelliteInfo["IRNSS"]!! + 1
                    }
                }


                Log.d("LocationHelper", "status satelliteInfo $satelliteInfo")

                // 按照指定顺序添加键值对
                val orderedKeys = listOf("GPS", "SBAS", "GLONASS", "GALILEO", "BEIDOU", "QZSS", "IRNSS")
                orderedKeys.forEach { key ->
                    satelliteInfo[key]?.let { value ->
                        currentSatelliteInfo.put(key, value)
                    }
                }
                Log.d("LocationHelper", "currentSatelliteInfo $currentSatelliteInfo")


                // 更新卫星信息
                onSatelliteUpdate(satelliteInfo)

            } catch (e: Exception) {
                Log.e("LocationHelper", "Error processing GNSS status", e)
            }
        }
    }

    private fun convertToCartesian(azimuth: Float, elevation: Float): SatellitePosition {
        val azRad = Math.toRadians(azimuth.toDouble())
        val elRad = Math.toRadians(elevation.toDouble())

        val x = cos(elRad) * sin(azRad)
        val y = cos(elRad) * cos(azRad)
        val z = sin(elRad)

        return SatellitePosition(x, y, z)
    }

    private fun calculatePDOP(satellites: List<SatellitePosition>): Double {
        if (satellites.size < 4) {
            return 0.0
        }

        val geometryMatrix = Array(satellites.size) { DoubleArray(4) }
        for (i in satellites.indices) {
            val sat = satellites[i]
            val r = sqrt(sat.x * sat.x + sat.y * sat.y + sat.z * sat.z)
            geometryMatrix[i][0] = sat.x / r
            geometryMatrix[i][1] = sat.y / r
            geometryMatrix[i][2] = sat.z / r
            geometryMatrix[i][3] = 1.0
        }

        val gTg = multiplyMatrixTranspose(geometryMatrix)

        return try {
            sqrt(gTg[0][0] + gTg[1][1] + gTg[2][2])
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error calculating PDOP", e)
            0.0
        }
    }

    private fun multiplyMatrixTranspose(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val n = matrix[0].size
        val result = Array(n) { DoubleArray(n) }

        for (i in 0 until n) {
            for (j in 0 until n) {
                var sum = 0.0
                for (k in matrix.indices) {
                    sum += matrix[k][i] * matrix[k][j]
                }
                result[i][j] = sum
            }
        }

        return result
    }

    fun startLocationUpdates() {
        if (!checkLocationEnabled()) {
            onError?.invoke("GPS is disabled. Please enable location services.")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError?.invoke("Location permission not granted")
            return
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locationListener,
                Looper.getMainLooper()
            )

            //查看有几颗卫星
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationManager.registerGnssStatusCallback(gnssStatusCallback)
            }


            // 监听 NMEA 消息 - 获取PDOP
            locationManager.addNmeaListener(object : OnNmeaMessageListener {
                override fun onNmeaMessage(message: String, timestamp: Long) {
                    Log.d("onNmeaMessage", "message: $message")
                    // $GPGSA,A,1,,,,,,,,,,,,,140.0,99.0,99.0*35
                    if (message.startsWith("\$GPGSA")) {
                        // GPGSA 语句包含 PDOP、HDOP 和 VDOP 值


                        val parts = message.split(",")
                        if (parts.size >= 17) {
                            val pdop = parts[15].toFloatOrNull() ?: 0f
                            val hdop = parts[16].toFloatOrNull() ?: 0f
                            val vdop = parts[17].split("*")[0].toFloatOrNull() ?: 0f

                            currentPdop = pdop
                            currentVdop = vdop
                            currentHdop = hdop


                            Log.d("GNSS", "PDOP: $pdop, HDOP: $hdop, VDOP: $vdop")
                        }
                    }
                }
            })

            getLastKnownLocation()?.let {
                onLocationUpdate(it)
            }

        } catch (e: Exception) {
            Log.e("LocationHelper", "Error starting location updates", e)
            onError?.invoke("Error starting location updates: ${e.message}")
        }
    }

    private fun checkLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun getLastKnownLocation(): LocationInfo? {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val providers = locationManager.getProviders(true)
        Log.d("Location1111", "Available providers: $providers")
        providers.forEach { provider ->
            val location = locationManager.getLastKnownLocation(provider)
            Log.d("Location1111", "Provider: $provider, Location: $location")
        }

        var bestLocation: Location? = null

        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                bestLocation = location
            }
        }




        return bestLocation?.let {
            locationInfo.apply {
                locationInfo.latitude = bestLocation.latitude
                locationInfo.longitude = bestLocation.longitude
                locationInfo.altitude = bestLocation.altitude
                locationInfo.speed = bestLocation.speed
                locationInfo.speedAccuracyMetersPerSecond = bestLocation.speedAccuracyMetersPerSecond
                locationInfo.accuracy = bestLocation.accuracy
                locationInfo.verticalAccuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bestLocation.verticalAccuracyMeters
                } else {
                    0f
                }
                locationInfo.horizontalAccuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bestLocation.accuracy
                } else {
                    0f
                }

                locationInfo.pdop = currentPdop
                locationInfo.hdop = currentHdop
                locationInfo.vdop = currentVdop
                locationInfo.satelliteCount = currentSatelliteCount
                locationInfo.satelliteInfo = currentSatelliteInfo
                locationInfo.bearingAccuracyDegrees = bestLocation.bearingAccuracyDegrees

            }
        }


        return locationInfo


    }
}





