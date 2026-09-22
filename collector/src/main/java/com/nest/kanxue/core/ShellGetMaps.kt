package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetMaps {
    private const val TAG = "ShellGetMaps"

    /////proc/self/maps
    fun getCpuinfoUsingFile(): String {
        return try {
            File("proc/self/maps").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = getCpuinfoUsingFile()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "maps.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "maps信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存maps信息失败", e)
        }
    }
}