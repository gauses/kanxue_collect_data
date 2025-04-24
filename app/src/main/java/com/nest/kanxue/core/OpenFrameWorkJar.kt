package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

//获取framework.jar，读取到本地

object OpenFrameWorkJar {
    private const val TAG = "OpenFrameWorkJar"
    private const val FRAMEWORK_JAR_PATH = "/system/framework/framework.jar"

    fun copyFrameworkJar(targetDir: File): Boolean {
        try {
            val sourceFile = File(FRAMEWORK_JAR_PATH)

            val targetJarDir = File(targetDir, "framework_copy")


            // 检查源文件是否存在
            if (!sourceFile.exists()) {
                Log.e(TAG, "framework.jar not found at $FRAMEWORK_JAR_PATH")
                return false
            }

            // 检查是否有读取权限
            if (!sourceFile.canRead()) {
                Log.e(TAG, "No permission to read framework.jar")
                return false
            }

            // 确保目标目录存在
            if (!targetJarDir.exists()) {
                if (!targetJarDir.mkdirs()) {
                    Log.e(TAG, "Failed to create target directory: ${targetDir.absolutePath}")
                    return false
                }
            }

            // 创建目标文件
            val targetFile = File(targetJarDir, "framework.jar")
            
            // 复制文件
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

            Log.i(TAG, "Successfully copied framework.jar to ${targetFile.absolutePath}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error copying framework.jar: ${e.message}")
            return false
        }
    }

    fun getFrameworkJarInfo(): String {
        val sourceFile = File(FRAMEWORK_JAR_PATH)
        return buildString {
            appendLine("Framework.jar Info:")
            appendLine("Path: ${sourceFile.absolutePath}")
            appendLine("Exists: ${sourceFile.exists()}")
            appendLine("Can Read: ${sourceFile.canRead()}")
            appendLine("Size: ${if (sourceFile.exists()) sourceFile.length() else 0} bytes")
            appendLine("Last Modified: ${if (sourceFile.exists()) sourceFile.lastModified() else 0}")
        }
    }    


    
}