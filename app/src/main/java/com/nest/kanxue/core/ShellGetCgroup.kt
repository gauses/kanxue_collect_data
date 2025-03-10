package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetCgroup {
    private const val TAG = "ShellGetCgroup"

    // sh -c /proc/self/cgroup
    fun getCgroupUsingFile(): String {
        return try {
            File("/proc/self/cgroup").bufferedReader().use { it.readText() }
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

//            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val propsFile = File(targetDir, "cgroup_${timestamp}.txt")
            val propsFile = File(targetDir, "cgroup.txt")

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