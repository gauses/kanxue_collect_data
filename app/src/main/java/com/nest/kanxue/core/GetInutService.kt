package com.nest.kanxue.core

import android.content.Context
import android.hardware.input.InputManager
import android.util.Log
import com.nest.kanxue.core.ShellGetMounts.getCgroupUsingFile
import java.io.File
import java.io.FileWriter

//获取输入设备信息

object GetInutService {

    private const val TAG = "GetInutService"

    fun getInputDevices(context: Context): String {
        val im = context.getSystemService(Context.INPUT_SERVICE) as InputManager
        val ids = im.inputDeviceIds
        val text = StringBuilder()

        for (id in ids) {
            val device = im.getInputDevice(id)
            text.append("Device ID: ${device?.id}\n")
            text.append("Name: ${device?.name}\n")
            text.append("Sources: ${device?.sources}\n")
            text.append("VendorId: ${device?.vendorId}\n")
            text.append("Descriptor: ${device?.descriptor}\n")
            text.append("Motion Range: ${device?.motionRanges}\n")
            text.append("Keyboard Type: ${device?.keyboardType}\n")
            text.append("String: ${device?.toString()}\n")
            text.append("\n") // 添加空行分隔不同设备
        }

        Log.d(TAG, "Input Devices: $text")
        return text.toString()
    }



    fun saveInutServiceToFile(context: Context, targetDir: File) {
        try {
            val props = getInputDevices(context)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "InutService.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "InutService信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存cgroup信息失败", e)
        }
    }
}