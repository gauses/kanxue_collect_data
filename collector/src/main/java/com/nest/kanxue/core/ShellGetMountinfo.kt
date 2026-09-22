package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetMountinfo {
    private const val TAG = "ShellGetMountinfo"

    ////proc/self/mountinfo
    fun getCpuinfoUsingFile(): String {
        return try {
            File("proc/self/mountinfo").bufferedReader().use { it.readText() }
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

            val propsFile = File(targetDir, "mountinfo.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "mountinfo信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存mountinfo信息失败", e)
        }
    }
}