package com.nest.kanxue.core

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * (size - 256) % 104 === 0
 *  检查Sensor文件
 */

object CheckSensorLength {

    // 检查字符串是否只包含数字
    private fun isNumeric(str: String): Boolean {
        return str.matches("\\d+".toRegex())
    }

    fun check(targetDir: File): JSONObject {
        val resultJson = JSONObject()
        val failedFilesArray = JSONArray()
        var totalCheckedFiles = 0
        var totalDeletedFiles = 0

        try {
            if (!targetDir.exists() || !targetDir.isDirectory) {
                resultJson.put("error", "$targetDir + 目标目录不存在或不是一个有效的目录")
                return resultJson
            }

            // 只获取targetDir直接子文件，不遍历子目录
            targetDir.listFiles()?.forEach { file ->
                // 只处理：
                // 1. 是文件（不是目录）
                // 2. 文件名中不包含点号（没有后缀）
                // 3. 文件名只包含数字
                if (file.isFile && !file.name.contains(".") && isNumeric(file.name)) {
                    totalCheckedFiles++
                    val fileSize = file.length()
                    val checkResult = (fileSize - 256) % 104 === 0L
                    
                    // 只有检查结果为false的文件才添加到JSON中
                    if (!checkResult) {
                        val fileJson = JSONObject().apply {
                            put("fileName", file.name)
                            put("fileSize", fileSize)
                        }
                        failedFilesArray.put(fileJson)
                        
                        // 删除不符合条件的文件
                        try {
                            if (file.delete()) {
                                totalDeletedFiles++
                                fileJson.put("deleted", true)
                                Log.d("CheckSensor", "成功删除文件: ${file.absolutePath}")
                            } else {
                                fileJson.put("deleted", false)
                                Log.e("CheckSensor", "删除文件失败: ${file.absolutePath}")
                            }
                        } catch (e: Exception) {
                            fileJson.put("deleted", false)
                            Log.e("CheckSensor", "删除文件时发生错误: ${file.absolutePath}, ${e.message}")
                        }
                    }
                }
            }

            resultJson.put("totalCheckedFiles", totalCheckedFiles)
            resultJson.put("failedFiles", failedFilesArray)
            resultJson.put("failedCount", failedFilesArray.length())
            resultJson.put("deletedCount", totalDeletedFiles)

        } catch (e: Exception) {
            resultJson.put("error", "检查文件时发生错误: ${e.message}")
        }

        return resultJson
    }
}