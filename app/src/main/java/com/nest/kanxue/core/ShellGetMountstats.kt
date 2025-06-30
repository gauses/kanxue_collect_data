package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetMountstats {
    private const val TAG = "ShellGetMountstats"



    // 备选方法：使用Runtime执行无需root的命令
    fun readCompatibleUsingCommand(): String {
        return try {
            val process = Runtime.getRuntime().exec("cat proc/self/mountstats")
            process.inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            "Error executing command: ${e.message}"
        }
    }


//    /////proc/self/mountstats
//    fun getCpuinfoUsingFile(): String {
//        return try {
//            File("proc/self/mountstats").bufferedReader().use { it.readText() }
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

            val propsFile = File(targetDir, "mountstats.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "mountstats信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存mountstats信息失败", e)
        }
    }
}