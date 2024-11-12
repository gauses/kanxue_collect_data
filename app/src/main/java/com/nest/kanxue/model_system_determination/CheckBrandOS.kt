package com.nest.kanxue.model_system_determination

import android.util.Log
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

//检测品牌机系统和设备能否对应，检测方法是：
//遍历/system/framework/下所有以.jar结尾的文件名称，与机型字符串进行比较（strcasestr比较，不区分大小写）
//例如在vivo手机内，/system/framework/下存在以下文件，文件名中包含vivo：

object CheckBrandOS {


    fun isBrandAndSystemMatched(): Boolean {
        // 获取设备品牌和制造商信息
        val brand = getSystemProperty("ro.product.brand")?.lowercase() ?: return false
        val manufacturer = getSystemProperty("ro.product.manufacturer")?.lowercase() ?: return false

        // 定义要检测的品牌名，适配为母公司品牌名称
        val brandToCheck = when {
            (brand == "redmi" || brand == "poco") && manufacturer == "xiaomi" -> "xiaomi"
            (brand == "honor" || brand == "huawei") && manufacturer == "huawei" -> "huawei"
            (brand == "galaxy" || brand == "samsung") && manufacturer == "samsung" -> "samsung"
            brand == "vivo" && manufacturer == "vivo" -> "vivo"
            brand == "oppo" && manufacturer == "oppo" -> "oppo"
            else -> brand
        }

        // 遍历 /system/framework/ 目录下所有 .jar 文件
        val frameworkDir = File("/system/framework/")
        if (!frameworkDir.exists() || !frameworkDir.isDirectory) {
            return false
        }

        // 查找包含品牌名的 .jar 文件
        frameworkDir.listFiles()?.forEach { file ->
            if (file.isFile && file.extension.equals("jar", ignoreCase = true)) {
                val fileName = file.nameWithoutExtension.lowercase()
                if (fileName.contains(brandToCheck)) {
                    return true
                }
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


}