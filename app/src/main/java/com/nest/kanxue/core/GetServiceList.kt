package com.nest.kanxue.core

import android.content.Context
import android.hardware.input.InputManager
import android.util.Log
import com.nest.kanxue.core.ShellGetMounts.getCgroupUsingFile
import java.io.File
import java.io.FileWriter

//获取输入设备信息

object GetServiceList {

    private const val TAG = "service list"

    fun getSystemProps(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "service list"))
            process.inputStream.bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }



    fun saveServiceListToFile(context: Context, targetDir: File) {
        try {
            val props = getSystemProps()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "service.list")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "service.list信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存service.list信息失败", e)
        }
    }
}