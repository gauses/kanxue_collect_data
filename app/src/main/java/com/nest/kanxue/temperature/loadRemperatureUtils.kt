package com.nest.kanxue.temperature

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

//加载温度传感器
object loadRemperatureUtils {

    private const val THERMAL_ZONE_PATH = "/sys/class/thermal"

    fun copyThermalDirectory(targetDir: File): JSONObject {
        val result = JSONObject()
        val failedFiles = JSONArray()
        var copiedCount = 0
        
        try {
            val thermalDir = File(THERMAL_ZONE_PATH)
            if (!thermalDir.exists() || !thermalDir.isDirectory) {
                result.put("error", "Thermal directory not found")
                return result
            }

            // 确保目标目录存在
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            // 创建thermal目录
            val thermalTargetDir = File(targetDir, "thermal")
            if (!thermalTargetDir.exists()) {
                thermalTargetDir.mkdirs()
            }

            thermalDir.listFiles()?.forEach { sourceFile ->
                try {
                    if (sourceFile.isDirectory) {
                        // 处理cooling_device和thermal_zone目录
                        val deviceTargetDir = File(thermalTargetDir, sourceFile.name)
                        deviceTargetDir.mkdirs()

                        // 复制目录下的文件
                        sourceFile.listFiles()?.forEach { subFile ->
                            try {
                                if (subFile.isFile) {
                                    val targetFile = File(deviceTargetDir, subFile.name)
                                    copyFileContent(subFile, targetFile)
                                    copiedCount++
                                }
                            } catch (e: Exception) {
                                Log.e("ThermalCopy", "复制文件失败: ${subFile.absolutePath}, 错误: ${e.message}")
                                failedFiles.put(subFile.absolutePath)
                            }
                        }

                        // 保存符号链接信息
                        val linkTarget = try {
                            val process = Runtime.getRuntime().exec(arrayOf("readlink", "-f", sourceFile.absolutePath))
                            process.inputStream.bufferedReader().readText().trim()
                        } catch (e: Exception) {
                            sourceFile.absolutePath
                        }
                        File(deviceTargetDir, "symlink_target.txt").writeText(linkTarget)
                    }
                } catch (e: Exception) {
                    Log.e("ThermalCopy", "处理目录失败: ${sourceFile.absolutePath}, 错误: ${e.message}")
                    failedFiles.put(sourceFile.absolutePath)
                }
            }

            result.put("copiedFiles", copiedCount)
            result.put("failedFiles", failedFiles)
            result.put("status", "success")

        } catch (e: Exception) {
            Log.e("ThermalCopy", "复制thermal目录失败: ${e.message}")
            result.put("error", e.message)
            result.put("status", "failed")
        }

        return result
    }

    private fun copyFileContent(sourceFile: File, targetFile: File) {
        try {
            // 对于特殊文件（如温度值），直接读取内容并写入
            val content = sourceFile.readText()
            targetFile.writeText(content)
        } catch (e: Exception) {
            try {
                // 如果直接读取失败，尝试使用流复制
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e("ThermalCopy", "复制文件内容失败: ${sourceFile.absolutePath}, 错误: ${e.message}")
                throw e
            }
        }
    }

    fun getTemperatureInfo(): JSONObject {
        val result = JSONObject()
        try {
            val thermalDir = File(THERMAL_ZONE_PATH)
            if (!thermalDir.exists() || !thermalDir.isDirectory) {
                return result
            }

            // 遍历所有thermal_zone目录
            thermalDir.listFiles()?.forEach { zoneDir ->
//                if (zoneDir.isDirectory && zoneDir.name.startsWith("thermal_zone")) {
                if (zoneDir.isDirectory) {
                    // 读取温度传感器类型
                    val typeFile = File(zoneDir, "type")
                    val tempFile = File(zoneDir, "temp")
                    
                    if (typeFile.exists() && tempFile.exists()) {
                        val type = typeFile.readText().trim()
                        // 温度值通常以毫摄氏度为单位，需要转换为摄氏度
                        val temp = tempFile.readText().trim().toInt() / 1000.0
                        
                        result.put(type, String.format("%.1f°C", temp))
                        Log.d("Temperature", "$type: ${String.format("%.1f°C", temp)}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Temperature", "读取温度信息失败: ${e.message}")
        }
        return result
    }

    // 获取特定温度传感器的值
    fun getSpecificTemperature(sensorType: String): Double {
        try {
            val thermalDir = File(THERMAL_ZONE_PATH)
            thermalDir.listFiles()?.forEach { zoneDir ->
                if (zoneDir.isDirectory && zoneDir.name.startsWith("thermal_zone")) {
                    val typeFile = File(zoneDir, "type")
                    if (typeFile.exists() && typeFile.readText().trim() == sensorType) {
                        val tempFile = File(zoneDir, "temp")
                        if (tempFile.exists()) {
                            return tempFile.readText().trim().toInt() / 1000.0
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Temperature", "读取温度失败: ${e.message}")
        }
        return -1.0
    }

    /** 安全读取文件内容，读不到返回 null（非普通文件/不存在/无权限/空/异常）。 */
    private fun readFileSafe(f: File): String? {
        return try {
            if (f.isFile && f.canRead()) f.readText().trim().takeIf { it.isNotEmpty() } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 忠实镜像 /sys/class/thermal 下每个 thermal_zone 的全部字段。
     * 目录里有什么文件就采什么（原样字符串），不推断 AIDL type、不做值域过滤。
     * 每个条目额外带 "zone"（温区目录名，如 thermal_zone0）便于定位；
     * 其中 sysfs 原生的 "type" 字段即传感器名（如 cpu0-silver-usr）。
     */
    fun getThermalSensorsDetailed(): JSONObject {
        val root = JSONObject()
        val sensors = JSONArray()
        try {
            File(THERMAL_ZONE_PATH).listFiles()
                ?.filter { it.isDirectory && it.name.startsWith("thermal_zone") }
                ?.sortedBy { it.name.removePrefix("thermal_zone").toIntOrNull() ?: Int.MAX_VALUE }
                ?.forEach { zoneDir ->
                    val entry = JSONObject()
                    entry.put("zone", zoneDir.name)
                    zoneDir.listFiles()
                        ?.sortedBy { it.name }
                        ?.forEach { f ->
                            readFileSafe(f)?.let { v -> entry.put(f.name, v) }
                        }
                    // 至少读到了 zone 之外的字段才收录
                    if (entry.length() > 1) sensors.put(entry)
                }
        } catch (e: Exception) {
            Log.e("Temperature", "采集thermal详细数据失败: ${e.message}")
        }
        root.put("sensors", sensors)
        return root
    }

    // 采集 thermal 全部字段并保存到 temperature_info.txt
    fun saveTemperatureInfo(targetDir: File): JSONObject {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val tempInfo = getThermalSensorsDetailed()
        try {
            val outputFile = File(targetDir, "temperature_info.txt")
            outputFile.writeText(tempInfo.toString(2))
        } catch (e: Exception) {
            Log.e("Temperature", "保存温度信息失败: ${e.message}")
        }
        return tempInfo
    }
}