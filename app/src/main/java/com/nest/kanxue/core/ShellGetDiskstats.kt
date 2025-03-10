package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetDiskstats {
    private const val TAG = "ShellGetDiskstats"

    // sh -c /proc/diskstats
    fun getDiskstatsUsingFile(): String {
        return try {
            File("/proc/diskstats").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }



    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = getDiskstatsUsingFile()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "diskstats.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "diskstats信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存diskstats信息失败", e)
        }
    }
}