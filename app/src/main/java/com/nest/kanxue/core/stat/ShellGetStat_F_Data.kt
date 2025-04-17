package com.nest.kanxue.core.stat

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetStat_F_Data {
    private const val TAG = "ShellGetMeminfo"

    ///proc/mounts
    fun GetStat_F_DataUsingFile(): String {
        // 处理stat -f命令
        val fsPathsToCheck = listOf(
            "/data"

        )

        var result = ""
        fsPathsToCheck.forEach { path ->
            val key = "fs_$path"  // 添加前缀以区分文件系统信息
            result = try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "stat -f $path"))
                process.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                ""
            }
        }

        return result
    }

    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = GetStat_F_DataUsingFile()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "Stat_F_data.txt")

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