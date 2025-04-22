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
            val file = File(targetDir, "display_card_info.json")
            file.writeText(info.toString(4))
            Log.i(TAG, "Display card info saved to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving display card info: ${e.message}")
        }
    }
}