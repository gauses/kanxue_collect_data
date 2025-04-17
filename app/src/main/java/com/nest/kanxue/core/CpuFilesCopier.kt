package com.nest.kanxue.core

import java.io.File
import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CpuFilesCopier(private val context: Context, private val targetDir: File) {
    // 源目录路径
    private val sourceDir = File("/sys/devices/system/cpu")

    // 检查字符串是否只包含数字
    private fun isNumeric(str: String): Boolean {
        return str.matches("\\d+".toRegex())
    }

    // 递归删除目录下的所有文件和子目录
    private fun deleteDirectory(dir: File): Boolean {
        if (!dir.exists()) {
            return true
        }
        
        dir.listFiles()?.forEach { file ->
            try {
                if (file.isDirectory) {
                    // 递归删除子目录
                    if (!deleteDirectory(file)) {
                        return false
                    }
                } else {
                    // 删除文件
                    if (!file.delete()) {
                        Log.e("CpuFilesCopier", "无法删除文件: ${file.absolutePath}")
                        return false
                    }
                }
            } catch (e: SecurityException) {
                Log.e("CpuFilesCopier", "删除文件时发生权限错误: ${file.absolutePath}, ${e.message}")
                return false
            } catch (e: Exception) {
                Log.e("CpuFilesCopier", "删除文件时发生错误: ${file.absolutePath}, ${e.message}")
                return false
            }
        }
        
        // 删除空目录
        return dir.delete()
    }

    // 递归复制目录
    private fun copyDirectory(sourceDir: File, targetDir: File, failedFilesArray: JSONArray): Int {
        var copiedCount = 0
        
        sourceDir.listFiles()?.forEach { sourceFile ->
            val targetFile = File(targetDir, sourceFile.name)
            
            try {
                if (sourceFile.isDirectory) {
                    // 如果是目录，先创建目标目录
                    if (!targetFile.exists() && !targetFile.mkdirs()) {
                        val fileJson = JSONObject().apply {
                            put("fileName", sourceFile.absolutePath)
                            put("error", "无法创建目标目录")
                        }
                        failedFilesArray.put(fileJson)
                        Log.e("CpuFilesCopier", "无法创建目标目录: ${targetFile.absolutePath}")
                        return@forEach
                    }
                    
                    // 递归复制子目录
                    copiedCount += copyDirectory(sourceFile, targetFile, failedFilesArray)
                } else {
                    // 如果是文件，直接复制
                    if (!sourceFile.canRead()) {
                        val fileJson = JSONObject().apply {
                            put("fileName", sourceFile.absolutePath)
                            put("error", "没有读取源文件的权限")
                        }
                        failedFilesArray.put(fileJson)
                        Log.e("CpuFilesCopier", "文件没有读取权限: ${sourceFile.absolutePath}")
                        return@forEach
                    }

                    if (targetFile.exists()) {
                        val fileJson = JSONObject().apply {
                            put("fileName", sourceFile.absolutePath)
                            put("error", "目标文件已存在")
                        }
                        failedFilesArray.put(fileJson)
                        Log.e("CpuFilesCopier", "目标文件已存在: ${targetFile.absolutePath}")
                        return@forEach
                    }

                    sourceFile.copyTo(targetFile, overwrite = false)
                    copiedCount++
                    Log.d("CpuFilesCopier", "成功复制文件: ${sourceFile.absolutePath} -> ${targetFile.absolutePath}")
                }
            } catch (e: SecurityException) {
                val fileJson = JSONObject().apply {
                    put("fileName", sourceFile.absolutePath)
                    put("error", "权限错误: ${e.message}")
                }
                failedFilesArray.put(fileJson)
                Log.e("CpuFilesCopier", "复制文件时发生权限错误: ${sourceFile.absolutePath}, ${e.message}")
            } catch (e: IOException) {
                val fileJson = JSONObject().apply {
                    put("fileName", sourceFile.absolutePath)
                    put("error", "IO错误: ${e.message}")
                }
                failedFilesArray.put(fileJson)
                Log.e("CpuFilesCopier", "复制文件时发生IO错误: ${sourceFile.absolutePath}, ${e.message}")
            } catch (e: Exception) {
                val fileJson = JSONObject().apply {
                    put("fileName", sourceFile.absolutePath)
                    put("error", "未知错误: ${e.message}")
                }
                failedFilesArray.put(fileJson)
                Log.e("CpuFilesCopier", "复制文件时发生未知错误: ${sourceFile.absolutePath}, ${e.message}")
            }
        }
        
        return copiedCount
    }

    // 统计目录下的文件数量
    fun countFilesInDirectory(dir: File): Int {
        if (!dir.exists() || !dir.isDirectory) {
            return 0
        }
        val files = dir.listFiles()
        return files?.size ?: 0
    }

    fun copyCpuFiles(): String {
        val resultJson = JSONObject()
        val failedFilesArray = JSONArray()
        var totalCopiedFiles = 0
        var totalFailedFiles = 0

        try {
            // 检查源目录是否存在
            if (!sourceDir.exists()) {
                resultJson.put("error", "源目录不存在: ${sourceDir.absolutePath}")
                return resultJson.toString()
            }

            // 检查源目录是否是目录
            if (!sourceDir.isDirectory) {
                resultJson.put("error", "源路径不是目录: ${sourceDir.absolutePath}")
                return resultJson.toString()
            }

            // 检查源目录是否为空
            val sourceFiles = sourceDir.listFiles()
            if (sourceFiles == null || sourceFiles.isEmpty()) {
                resultJson.put("error", "源目录为空: ${sourceDir.absolutePath}")
                return resultJson.toString()
            }

            // 检查目标目录是否存在，不存在则创建
            if (!targetDir.exists()) {
                try {
                    if (!targetDir.mkdirs()) {
                        resultJson.put("error", "无法创建目标目录: ${targetDir.absolutePath}")
                        return resultJson.toString()
                    }
                } catch (e: SecurityException) {
                    resultJson.put("error", "没有权限创建目标目录: ${targetDir.absolutePath}, ${e.message}")
                    return resultJson.toString()
                }
            }

            // 检查目标目录是否有写入权限
            if (!targetDir.canWrite()) {
                resultJson.put("error", "没有写入目标目录的权限: ${targetDir.absolutePath}")
                return resultJson.toString()
            }

            // 检查目标目录是否是目录
            if (!targetDir.isDirectory) {
                resultJson.put("error", "目标路径不是目录: ${targetDir.absolutePath}")
                return resultJson.toString()
            }

            // 检查目标目录是否为空，不为空则清空
            val targetFiles = targetDir.listFiles()
            if (targetFiles != null && targetFiles.isNotEmpty()) {
                if (!deleteDirectory(targetDir)) {
                    resultJson.put("error", "无法清空目标目录: ${targetDir.absolutePath}")
                    return resultJson.toString()
                }
                // 重新创建目录
                if (!targetDir.mkdirs()) {
                    resultJson.put("error", "无法重新创建目标目录: ${targetDir.absolutePath}")
                    return resultJson.toString()
                }
            }

            // 递归复制整个目录结构
            totalCopiedFiles = copyDirectory(sourceDir, targetDir, failedFilesArray)
            totalFailedFiles = failedFilesArray.length()

            // 添加调试信息
            Log.d("CpuFilesCopier", "复制结果统计:")
            Log.d("CpuFilesCopier", "总文件数: ${sourceFiles.size}")
            Log.d("CpuFilesCopier", "成功复制: $totalCopiedFiles")
            Log.d("CpuFilesCopier", "失败数: $totalFailedFiles")

            resultJson.put("totalCopiedFiles", totalCopiedFiles)
            resultJson.put("failedFiles", failedFilesArray)
            resultJson.put("failedCount", totalFailedFiles)
            resultJson.put("totalFiles", sourceFiles.size)

        } catch (e: Exception) {
            resultJson.put("error", "复制文件时发生错误: ${e.message}")
            Log.e("CpuFilesCopier", "复制文件时发生错误: ${e.message}")
        }

        return resultJson.toString()
    }
}