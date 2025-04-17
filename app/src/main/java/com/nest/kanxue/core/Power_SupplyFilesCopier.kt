package com.nest.kanxue.core


import java.io.File
import android.content.Context

class Power_SupplyFilesCopier(private val context: Context, private val targetDir: File) {

    fun copyPower_SupplyFiles(): String {


        return try {
            // 创建目标目录

            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            // 源目录
//            val sourcePath = "/sys/class/power_supply"
            val sourcePath = "/sys/class/power_supply/battery"
            val sourceDir = File(sourcePath)

            // 复制文件
            var copiedCount = 0
            var errorCount = 0
            val log = StringBuilder()

            sourceDir.walk().forEach { sourceFile ->
                try {
                    if (sourceFile.isFile) {
                        // 创建对应的目标文件路径
                        val relativePath = sourceFile.absolutePath.substring(sourcePath.length)
                        val targetFile = File(targetDir, relativePath)

                        // 确保目标文件的父目录存在
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
            log.appendLine("备份目录: ${targetDir.absolutePath}")

            log.toString()

        } catch (e: Exception) {
            "复制过程出错: ${e.message}"
        }
    }
}