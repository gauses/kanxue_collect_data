package com.nest.kanxue.core

import java.io.File
import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

//温度传感器相关

class TempFilesCopier(private val context: Context, private val targetDir: File) {
    // 源目录路径
    private val sourceDir = File("/sys/class/thermal")

    // 检查字符串是否只包含数字
    private fun isNumeric(str: String): Boolean {
        return str.matches("\\d+".toRegex())
    }

    // 递归删除目录下的所有文件和子目录
    private fun deleteDirectory(dir: File): Boolean {
        if (!dir.exists()) {
            return true
        }
        
        try {
            // 直接删除整个目录
            if (dir.deleteRecursively()) {
                Log.d("TempFilesCopier", "成功删除目录: ${dir.absolutePath}")
                return true
            } else {
                Log.e("TempFilesCopier", "无法删除目录: ${dir.absolutePath}")
                return false
            }
        } catch (e: SecurityException) {
            Log.e("TempFilesCopier", "删除目录时发生权限错误: ${dir.absolutePath}, ${e.message}")
            return false
        } catch (e: Exception) {
            Log.e("TempFilesCopier", "删除目录时发生错误: ${dir.absolutePath}, ${e.message}")
            return false
        }
    }

    // 递归复制目录
    private fun copyDirectory(sourceDir: File, targetDir: File, failedFilesArray: JSONArray): Int {
        var copiedCount = 0
        
        sourceDir.listFiles()?.forEach { sourceFile ->
            val targetFile = File(targetDir, sourceFile.name)
            
            try {
                // 创建目标目录
                if (!targetFile.exists() && !targetFile.mkdirs()) {
                    val fileJson = JSONObject().apply {
                        put("fileName", sourceFile.absolutePath)
                        put("error", "无法创建目标目录")
                    }
                    failedFilesArray.put(fileJson)
                    Log.e("TempFilesCopier", "无法创建目标目录: ${targetFile.absolutePath}")
                    return@forEach
                }

                // 获取符号链接的目标路径
                val linkTarget = try {
                    val process = Runtime.getRuntime().exec(arrayOf("readlink", "-f", sourceFile.absolutePath))
                    process.inputStream.bufferedReader().readText().trim()
                } catch (e: Exception) {
                    sourceFile.absolutePath
                }

//                // 保存符号链接信息
//                File(targetFile, "symlink_info.txt").writeText("""
//                    Original Path: ${sourceFile.absolutePath}
//                    Link Target: $linkTarget
//                    Time: ${System.currentTimeMillis()}
//                """.trimIndent())



                copiedCount++

                // 复制实际目录中的文件
                val realSourceDir = File(linkTarget)
                if (realSourceDir.exists() && realSourceDir.isDirectory) {
                    realSourceDir.listFiles()?.forEach { subFile ->
                        try {
                            if (subFile.isFile) {
                                val subTargetFile = File(targetFile, subFile.name)
                                if (!subFile.canRead()) {
                                    val fileJson = JSONObject().apply {
                                        put("fileName", subFile.absolutePath)
                                        put("error", "没有读取源文件的权限")
                                    }
                                    failedFilesArray.put(fileJson)
                                    Log.e("TempFilesCopier", "文件没有读取权限: ${subFile.absolutePath}")
                                    return@forEach
                                }

                                // 复制文件内容
                                try {
                                    // 对于特殊文件（如温度值），直接读取内容并写入
                                    val content = subFile.readText()
                                    subTargetFile.writeText(content)
                                    copiedCount++
                                } catch (e: Exception) {
                                    try {
                                        // 如果直接读取失败，尝试使用流复制
                                        copyFile(subFile, subTargetFile)
                                        copiedCount++
                                    } catch (e: Exception) {
                                        val fileJson = JSONObject().apply {
                                            put("fileName", subFile.absolutePath)
                                            put("error", "复制失败: ${e.message}")
                                        }
                                        failedFilesArray.put(fileJson)
                                        Log.e("TempFilesCopier", "复制文件失败: ${subFile.absolutePath}, ${e.message}")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            val fileJson = JSONObject().apply {
                                put("fileName", subFile.absolutePath)
                                put("error", e.message)
                            }
                            failedFilesArray.put(fileJson)
                            Log.e("TempFilesCopier", "处理文件失败: ${subFile.absolutePath}, ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                val fileJson = JSONObject().apply {
                    put("fileName", sourceFile.absolutePath)
                    put("error", e.message)
                }
                failedFilesArray.put(fileJson)
                Log.e("TempFilesCopier", "处理目录失败: ${sourceFile.absolutePath}, ${e.message}")
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


    fun copyFile(sourceFile: File, targetFile: File) {
        try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytes = input.read(buffer)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FileCopy", "复制文件失败: ${e.message}")
            throw e
        }
    }

    fun copyFiles(): String {
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

            File(targetDir.absolutePath).deleteRecursively()

//            // 检查目标目录是否为空，不为空则清空
//            if (targetDir.exists() && targetDir.listFiles().isNotEmpty()) {
//                if (!deleteDirectory(targetDir)) {
//                    resultJson.put("error", "无法清空目标目录: ${targetDir.absolutePath}")
//                    return resultJson.toString()
//                }
//                // 重新创建目录
//                if (!targetDir.mkdirs()) {
//                    resultJson.put("error", "无法重新创建目标目录: ${targetDir.absolutePath}")
//                    return resultJson.toString()
//                }
//            }

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