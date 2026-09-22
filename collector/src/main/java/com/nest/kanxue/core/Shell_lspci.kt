package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object Shell_lspci {
    private const val TAG = "Shell_lspci"

    ///lspci
    fun getlspciUsingFile(): String {
        return try {
            val process = Runtime.getRuntime().exec("lspci")
            
            // 读取正常输出
            val output = process.inputStream.bufferedReader().use { it.readText() }
            // 读取错误输出
            val error = process.errorStream.bufferedReader().use { it.readText() }
            
            // 等待进程完成
            val exitCode = process.waitFor()
            
            Log.d(TAG, "命令执行完成，退出码: $exitCode")
            if (error.isNotEmpty()) {
                Log.e(TAG, "错误输出: $error")
            }
            if (output.isEmpty()) {
                Log.w(TAG, "命令输出为空")
            } else {
                Log.d(TAG, "获取到的lspci信息长度: ${output.length}")
            }
            
            output.ifEmpty { "" }
        } catch (e: Exception) {
            Log.e(TAG, "执行lspci命令失败", e)
            "Error: ${e.message}"
        }
    }

    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = getlspciUsingFile()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "lspci.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "lspci信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存lspci信息失败", e)
        }
    }
}