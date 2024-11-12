package com.nest.kanxue.model_system_determination

import java.io.BufferedReader
import java.io.InputStreamReader

object CheckOpenHarmony {

    fun isOpenHarmony(): Boolean {
        // 检测系统属性 ro.build.ohos.devicetype 是否存在
        val deviceType = getSystemProperty("ro.build.ohos.devicetype")
        return deviceType != null && deviceType.isNotEmpty()
    }

    // 读取系统属性的方法
    private fun getSystemProperty(propName: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.readLine()?.takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            null
        }
    }
}