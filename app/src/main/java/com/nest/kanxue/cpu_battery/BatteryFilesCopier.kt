package com.nest.kanxue.cpu_battery

import java.io.File
import android.os.Environment
import android.content.Context

class BatteryFilesCopier(private val context: Context) {
    /**
     * 普通方式复制（需要文件读取权限）
     */
    fun copyBatteryFiles(): String {
        return try {
            // 创建目标目录
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val batteryBackupDir = File(downloadDir, "battery_backup")
            if (!batteryBackupDir.exists()) {
                batteryBackupDir.mkdirs()
            }

            // 源目录
            val sourcePath = "/sys/class/power_supply/battery"
            val sourceDir = File(sourcePath)

            // 复制文件
            var copiedCount = 0
            var errorCount = 0
            val log = StringBuilder()

            sourceDir.walk().forEach { sourceFile ->
                try {
                    if (sourceFile.isFile) {
                        // 目标文件路径
                        val relativePath = sourceFile.absolutePath.substring(sourcePath.length)
                        val targetFile = File(batteryBackupDir, relativePath)

                        // 创建父目录
                        targetFile.parentFile?.mkdirs()

                        // 复制文件内容
                        sourceFile.inputStream().use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        log.appendLine("已复制: ${sourceFile.absolutePath}")
                        copiedCount++
                    }
                } catch (e: Exception) {
                    log.appendLine("复制失败: ${sourceFile.absolutePath}, 错误: ${e.message}")
                    errorCount++
                }
            }

            log.appendLine("\n总结:")
            log.appendLine("成功复制: $copiedCount 个文件")
            log.appendLine("失败: $errorCount 个文件")
            log.appendLine("备份目录: ${batteryBackupDir.absolutePath}")

            log.toString()

        } catch (e: Exception) {
            "复制过程出错: ${e.message}"
        }
    }
}