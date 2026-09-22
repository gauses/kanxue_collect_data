package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetMounts {
    private const val TAG = "ShellGetCgroup"

    ///proc/mounts
    fun getCgroupUsingFile(): String {
        return try {
            File("/proc/mounts").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = getCgroupUsingFile()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "mounts.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "cgroup信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存cgroup信息失败", e)
        }
    }
}