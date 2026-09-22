package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileWriter

object Shell_AM_GetConfig {

    // sh -c /system/bin/getprop
    fun getSystemProps(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "am get-config"))
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

            val propsFile = File(targetDir, "am_get-config.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d("ShellGetProp", "系统属性已保存到: ${propsFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("ShellGetProp", "保存系统属性失败: ${e.message}")
        }
    }
}