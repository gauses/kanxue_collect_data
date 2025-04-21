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

    // 保存温度信息到文件
    fun saveTemperatureInfo(targetDir: File): JSONObject {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val tempInfo = getTemperatureInfo()
        try {
            val outputFile = File(targetDir, "temperature_info.txt")
            outputFile.writeText(tempInfo.toString(2))
        } catch (e: Exception) {
            Log.e("Temperature", "保存温度信息失败: ${e.message}")
        }
        return tempInfo
    }
}