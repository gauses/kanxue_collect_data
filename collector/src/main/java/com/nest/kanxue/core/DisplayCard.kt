package com.nest.kanxue.core

import android.util.Log
import org.json.JSONObject
import java.io.File

object DisplayCard {
    private const val TAG = "DisplayCard"
    private var vulkanInstance: Long = 0

    init {
        System.loadLibrary("ndk_kanxue")
    }

    private external fun createVulkanInstance(): Long
    private external fun getPhysicalDeviceCount(instance: Long): Int
    private external fun getPhysicalDevices(instance: Long): LongArray
    private external fun getDeviceInfo(instance: Long, device: Long): String
    private external fun destroyVulkanInstance(instance: Long)

    fun getDisplayCardInfo(): JSONObject {
        val result = JSONObject()
        try {
            vulkanInstance = createVulkanInstance()
            if (vulkanInstance == 0L) {
                Log.e(TAG, "Failed to create Vulkan instance")
                return result
            }

            val deviceCount = getPhysicalDeviceCount(vulkanInstance)
            if (deviceCount <= 0) {
                Log.e(TAG, "No physical devices found")
                return result
            }

            val devices = getPhysicalDevices(vulkanInstance)
            Log.i(TAG, "getPhysicalDevices : $devices")
            val deviceArray = JSONObject()
            for (i in devices.indices) {
                val deviceInfo = getDeviceInfo(vulkanInstance, devices[i])
                deviceArray.put("device_$i", JSONObject(deviceInfo))
            }
            result.put("devices", deviceArray)
            result.put("device_count", deviceCount)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting display card info: ${e.message}")
        } finally {
            if (vulkanInstance != 0L) {
                destroyVulkanInstance(vulkanInstance)
                vulkanInstance = 0
            }
        }
        return result
    }

    fun saveDisplayCardInfo(targetDir: File) {
        try {
            val info = getDisplayCardInfo()
            val file = File(targetDir, "显卡_info.json")
            file.writeText(info.toString(4))
            Log.i(TAG, "Display card info saved to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving display card info: ${e.message}")
        }
    }

    fun printDeviceInfo() {
        try {
            val instance = createVulkanInstance()
            if (instance == 0L) {
                Log.e(TAG, "Failed to create Vulkan instance")
                return
            }

            val deviceCount = getPhysicalDeviceCount(instance)
            Log.d(TAG, "Found $deviceCount physical devices")

            val devices = getPhysicalDevices(instance)
            for (i in devices.indices) {
                val deviceInfo = getDeviceInfo(instance, devices[i])
                val json = JSONObject(deviceInfo)
                
                // 打印基本信息
                Log.d(TAG, "Device $i:")
                Log.d(TAG, "  Name: ${json.getString("deviceName")}")
                Log.d(TAG, "  Type: ${getDeviceTypeString(json.getInt("deviceType"))}")
                Log.d(TAG, "  Driver Version: ${json.getLong("driverVersion")}")
                Log.d(TAG, "  API Version: ${json.getLong("apiVersion")}")

                // 打印特性信息
                val features = json.getJSONObject("features")
                Log.d(TAG, "  Features:")
                val featureNames = features.keys()
                while (featureNames.hasNext()) {
                    val name = featureNames.next()
                    val value = features.getInt(name)
                    Log.d(TAG, "    - $name: $value")  // 打印名称和值
                }
            }

            destroyVulkanInstance(instance)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device info: ${e.message}")
        }
    }

    private fun getDeviceTypeString(type: Int): String {
        return when (type) {
            1 -> "Integrated GPU"
            2 -> "Discrete GPU"
            3 -> "Virtual GPU"
            4 -> "CPU"
            else -> "Unknown"
        }
    }
}