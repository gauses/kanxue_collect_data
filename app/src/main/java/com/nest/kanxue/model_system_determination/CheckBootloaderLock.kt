package com.nest.kanxue.model_system_determination
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

//检测bootloader是否已解锁。

object CheckBootloaderLock {

    fun getBootloaderUnlocked(): JSONObject {
        val checks = listOf(
            "ro.boot.verifiedbootstate" to "orange",
            "ro.secureboot.lockstate" to "unlocked",
            "vendor.boot.vbmeta.device_state" to "unlocked",
            "vendor.boot.verifiedbootstate" to "orange",
            "ro.boot.vbmeta.device_state" to "unlocked",
            "ro.boot.flash.locked" to "unlocked"
        )


        var isBootloaderUnlockedJSON = JSONObject()

        for ((prop, expectedValue) in checks) {
            val actualValue = getSystemProperty(prop)?.lowercase()
            Log.d("isBootloaderUnlocked", "actualValue = $actualValue")
            isBootloaderUnlockedJSON.put(prop, actualValue)

        }

        return isBootloaderUnlockedJSON
    }

    fun isBootloaderUnlocked(): Boolean {
        // 检查系统属性的值，符合解锁条件的返回 true
        val checks = listOf(
            "ro.boot.verifiedbootstate" to "orange",
            "ro.secureboot.lockstate" to "unlocked",
            "vendor.boot.vbmeta.device_state" to "unlocked",
            "vendor.boot.verifiedbootstate" to "orange",
            "ro.boot.vbmeta.device_state" to "unlocked",
            "ro.boot.flash.locked" to "unlocked"
        )

        for ((prop, expectedValue) in checks) {
            val actualValue = getSystemProperty(prop)?.lowercase()
            Log.d("isBootloaderUnlocked", "actualValue = $actualValue")
            if (actualValue == expectedValue) {
                return true
            }
        }

        return false
    }

    // 读取系统属性的方法
    fun getSystemProperty(propName: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.readLine()?.takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun isOemUnlockAllowed(): Boolean {
        // 获取系统属性 sys.oem_unlock_allowed 的值
        val oemUnlockAllowed = getSystemProperty("sys.oem_unlock_allowed")
        // 如果属性值为 "1"，表示允许解锁 bootloader
        Log.d("isOemUnlockAllowed", "oemUnlockAllowed = $oemUnlockAllowed")
        var result = false
        if (oemUnlockAllowed == "1") {
            result = true
        }
        return result
    }


}