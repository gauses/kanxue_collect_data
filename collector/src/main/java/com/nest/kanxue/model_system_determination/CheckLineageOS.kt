package com.nest.kanxue.model_system_determination

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object CheckLineageOS {

    fun isLineageOS(): Boolean {
        // 1. 检查 LineageOS 特征文件
        val lineageFiles = listOf(
            "/system/framework/org.lineageos.platform.jar",
            "/system/framework/org.lineageos.hardware.jar"
        )

        val fileExists = lineageFiles.any { filePath ->
            File(filePath).exists()
        }

        // 2. 检查系统属性 ro.lineage.build.version
        val systemPropExists = getSystemProperty("ro.lineage.build.version") != null

        return fileExists || systemPropExists
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