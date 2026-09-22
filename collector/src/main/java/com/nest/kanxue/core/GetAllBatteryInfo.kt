package com.nest.kanxue.core

import android.content.Context
import android.hardware.input.InputManager
import android.media.MediaCodecList
import android.util.Log
import com.nest.kanxue.core.ShellGetMounts.getCgroupUsingFile
import java.io.File
import java.io.FileWriter

//获取电池信息

object GetAllBatteryInfo {

    private const val TAG = "GetAllBatteryInfo"

    fun getBatteryInfo(context: Context): String {
        var text = "getBatteryInfo:\n"
        try {
            val powerProfile = Class.forName("com.android.internal.os.PowerProfile").getConstructor(Context::class.java).newInstance(context)
            val batteryCapacity = Class.forName("com.android.internal.os.PowerProfile")
                .getMethod("getBatteryCapacity")
                .invoke(powerProfile)

            text += "batteryCapacity: $batteryCapacity\n"
        } catch (e: Exception) {
            Log.d("SBSBSB", "getBatteryInfo Exception: ${e.message}")
            text += "Error: ${e.message}\n"
        }

        return text
    }

    fun saveAllBatteryInfoToFile(context: Context, targetDir: File) {
        try {
            val props = getBatteryInfo(context)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "电池信息-AllBatteryInfo.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "电池信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存电池信息失败", e)
        }
    }
}