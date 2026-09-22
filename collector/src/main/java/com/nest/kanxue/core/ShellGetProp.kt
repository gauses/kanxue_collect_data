package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object ShellGetProp {

    // sh -c /system/bin/getprop
    fun getSystemProps(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "/system/bin/getprop"))
            process.inputStream.bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = getSystemProps()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "getprop.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d("ShellGetProp", "系统属性已保存到: ${propsFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("ShellGetProp", "保存系统属性失败: ${e.message}")
        }
    }
}