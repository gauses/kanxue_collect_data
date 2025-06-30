package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetEnviron {
    private const val TAG = "ShellGetEnviron"


    // 备选方法：使用Runtime执行无需root的命令
    fun readCompatibleUsingCommand(): String {
        return try {
            val process = Runtime.getRuntime().exec("cat proc/self/environ")
            process.inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            "Error executing command: ${e.message}"
        }
    }

//    /////proc/self/environ
//    fun getCpuinfoUsingFile(): String {
//        return try {
//            File("proc/self/environ").bufferedReader().use { it.readText() }
//        } catch (e: Exception) {
//            ""
//        }
//    }

    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = readCompatibleUsingCommand()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "environ.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "environ信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存environ信息失败", e)
        }
    }
}