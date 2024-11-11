package com.nest.kanxue.hardwarerelated

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL10


object getHardwareRelated {

    fun getCpuCoreCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    fun getCpuMaxFreq(): String {
        val coreIndex = 0 // 读取第一个核心的最大频率
        val maxFreqPath = "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_max_freq"
        return try {
            File(maxFreqPath).readText().trim()
        } catch (e: IOException) {
            "Unavailable"
        }
    }

    fun getCpuModel(): String {
        val cpuInfoPath = "/proc/cpuinfo"
        return try {
            val cpuInfo = File(cpuInfoPath).readLines()
            cpuInfo.firstOrNull { it.contains("Hardware") }
                ?.split(":")
                ?.getOrNull(1)
                ?.trim()
                ?: "Unavailable"
        } catch (e: IOException) {
            "Unavailable"
        }
    }


    fun getMemoryInfo(context: Context): Pair<Long, Long> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val availableMemory = memoryInfo.availMem
        val totalMemory = memoryInfo.totalMem
        return Pair(availableMemory, totalMemory)
    }

    fun getSupportedAbis(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Build.SUPPORTED_ABIS
        } else {
            arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
        }
    }

    fun getScreenBrightness(context: Context): Int {
        return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
    }

    fun getScreenTimeout(context: Context): Int {
        return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 0)
    }

    fun getScreenSize(): Pair<Int, Int> {
        val metrics = Resources.getSystem().displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        return Pair(width, height)
    }

    fun getSensorsInfo(context: Context): List<Pair<String, String>> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        return sensors.map { sensor -> Pair(sensor.vendor, sensor.name) }
    }

    //内部存储（EMMC或UFS闪存）的序列号：/sys/block/mmcblk0/device/serial  （核心）
    fun readFileContent(path: String): String? {
        return try {
            File(path).readText().trim()
        } catch (e: Exception) {
            null
        }
    }


    fun getBatteryInfo(context: Context): Map<String, Any?> {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
//        val capacity = batteryIntent?.getIntExtra(BatteryManager.EXTRA_CAPACITY, -1)

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = level / scale.toFloat() * 100
        println("Battery Level: $batteryPct%")
        val capacity = batteryPct

        val temperature = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.div(10.0)
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)

        return mapOf(
            "Voltage" to voltage,
            "Capacity" to capacity,
            "Temperature" to temperature,
            "Health" to health,
            "Status" to status
        )
    }

    fun getInputDevicesInfo(): String? {
        return readFileContent("/proc/bus/input/devices")
    }



    fun getInfo(context: Context): JSONObject {

        val hardwareJSON = JSONObject()
        hardwareJSON.put("CPU Core Count" , getCpuCoreCount().toString())
        hardwareJSON.put("CPU Max Frequency (KHz)" , getCpuMaxFreq())
        hardwareJSON.put("CPU Model" , getCpuModel())

        hardwareJSON.put("Memory Available" , "${getMemoryInfo(context).first / (1024 * 1024)} MB")
        hardwareJSON.put("Memory Total" ,"${getMemoryInfo(context).second / (1024 * 1024)} MB")

        // 创建 JSONArray 并将数组元素添加进去
        val SupportedAbisJsonArray = JSONArray()
        for (item in getSupportedAbis()) {
            SupportedAbisJsonArray.put(item)
        }
        hardwareJSON.put("Supported CPU Abis" , SupportedAbisJsonArray)// CPU 支持的系统架构


        hardwareJSON.put("Screen width width" , getScreenSize().first)
        hardwareJSON.put("Screen width height" , getScreenSize().second)

        hardwareJSON.put("Screen Timeout" , getScreenTimeout(context)) //屏幕超时时间
        hardwareJSON.put("Screen Brightness" , getScreenBrightness(context)) //屏幕亮度

        hardwareJSON.put("SensorsInfo" , getSensorsInfo(context)) //传感器生产厂家和类型

        //显示设备厂商名称（GL_VENDOR）和渲染器名称（GL_RENDERER）（重要）
        hardwareJSON.put("GL_VENDOR" , GPUInfoUtil.glVersion)
        hardwareJSON.put("GL_RENDERER" , GPUInfoUtil.glRenderer)


        //内部存储（EMMC或UFS闪存）的序列号：/sys/block/mmcblk0/device/serial  （核心）
        hardwareJSON.put("storageSerial" , readFileContent("/sys/block/mmcblk0/device/serial"))

        //显示设备序列号：/sys/devices/soc0/serial_number  (核心)
        hardwareJSON.put("displaySerial" , readFileContent("/sys/devices/soc0/serial_number"))

        //内部存储SD卡的CID：/sys/block/mmcblk0/device/cid（核心）
        hardwareJSON.put("sdCardCIDSerial" , readFileContent("/sys/block/mmcblk0/device/cid"))


        //电池相关，例如电压、电池容量、电池温度、电池健康百分比、充电状态等
        val batteryMap = getBatteryInfo(context)
        hardwareJSON.put("Battery Voltage" , batteryMap.get("Voltage"))
        hardwareJSON.put("Battery Capacity" , batteryMap.get("Capacity"))
        hardwareJSON.put("Battery Temperature" , batteryMap.get("Temperature"))
        hardwareJSON.put("Battery Health" , batteryMap.get("Health"))
        hardwareJSON.put("Battery Status" , batteryMap.get("Status"))


        //input设备相关，读取/proc/bus/input/devices，获取注册的input设备信息，比如Name和Sysfs。
        hardwareJSON.put("InputDevicesInfo" , getInputDevicesInfo())

        return  hardwareJSON

    }

}