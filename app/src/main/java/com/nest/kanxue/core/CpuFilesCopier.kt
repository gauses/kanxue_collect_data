package com.nest.kanxue.core

import java.io.File
import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream
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
        
        try {
            // 直接删除整个目录
            if (dir.deleteRecursively()) {
                Log.d("CpuFilesCopier", "成功删除目录: ${dir.absolutePath}")
                return true
            } else {
                Log.e("CpuFilesCopier", "无法删除目录: ${dir.absolutePath}")
                return false
            }
        } catch (e: SecurityException) {
            Log.e("CpuFilesCopier", "删除目录时发生权限错误: ${dir.absolutePath}, ${e.message}")
            return false
        } catch (e: Exception) {
            Log.e("CpuFilesCopier", "删除目录时发生错误: ${dir.absolutePath}, ${e.message}")
            return false
        }
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

//                    sourceFile.copyTo(targetFile, overwrite = false)
                    copyFile(sourceFile, targetFile)

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

    // 递归统计目录下的所有文件数量
    private fun countAllFiles(dir: File): Int {
        if (!dir.exists() || !dir.isDirectory) {
            return 0
        }
        var count = 0
        dir.walk().forEach { file ->
            if (file.isFile) {
                count++
            }
        }
        return count
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

            // 统计源目录的文件总数
            val totalSourceFiles = countAllFiles(sourceDir)
            Log.d("CpuFilesCopier", "源目录文件总数: $totalSourceFiles")

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

            // 清空目标目录
            File(targetDir.absolutePath).deleteRecursively()
            targetDir.mkdirs()

            // 使用cp -rp命令复制文件，保留权限和时间戳
            val command = arrayOf("sh", "-c", "cp -rdp ${sourceDir.absolutePath}/ ${targetDir.absolutePath}/ 2>/dev/null")
            Log.d("CpuFilesCopier", "执行命令: ${command.joinToString(" ")}")
            Log.d("CpuFilesCopier", "目录文件")
            val process = Runtime.getRuntime().exec(command)
            Log.d("CpuFilesCopier", "进程已启动")
            
            // 在后台读取输出流，防止阻塞
            val outputThread = Thread {
                try {
                    val outputStream = process.inputStream.bufferedReader().readText()
                    Log.d("CpuFilesCopier", "命令输出: $outputStream")
                } catch (e: Exception) {
                    Log.e("CpuFilesCopier", "读取输出流失败: ${e.message}")
                }
            }
            outputThread.start()
            
            // 等待进程完成
            val exitCode = process.waitFor()
            Log.d("CpuFilesCopier", "进程退出码: $exitCode")

            // 即使有权限错误，只要命令执行完成就视为成功
            if (exitCode >= 0) {
                // 复制成功，统计文件数量
                totalCopiedFiles = countAllFiles(targetDir)
                resultJson.put("totalCopiedFiles", totalCopiedFiles)
                resultJson.put("totalSourceFiles", totalSourceFiles)
                resultJson.put("status", "success")
                resultJson.put("message", "文件复制完成（部分文件因权限问题未复制）")
                Log.d("CpuFilesCopier", "目标目录文件总数: $totalCopiedFiles")
                
                // 检查目录结构
                Log.d("CpuFilesCopier", "检查目录结构:")
                targetDir.walk().forEach { file ->
                    if (file.isDirectory) {
                        val fileCount = file.listFiles()?.size ?: 0
                        Log.d("CpuFilesCopier", "目录: ${file.absolutePath}, 文件数量: $fileCount")
                    }
                }
            } else {
                resultJson.put("error", "命令执行失败")
                resultJson.put("status", "failed")
            }

        } catch (e: Exception) {
            resultJson.put("error", "复制文件时发生错误: ${e.message}")
            Log.e("CpuFilesCopier", "复制文件时发生错误: ${e.message}")
        }

        return resultJson.toString()
    }
}