package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetMeminfo {
    private const val TAG = "ShellGetMeminfo"

    ///proc/mounts
    fun getMeminfoUsingFile(): String {
        return try {
            File("/proc/meminfo").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = getMeminfoUsingFile()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "meminfo.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "meminfo信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存meminfo信息失败", e)
        }
    }
}