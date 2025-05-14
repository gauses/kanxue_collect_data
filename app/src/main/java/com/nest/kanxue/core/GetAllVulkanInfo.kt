package com.nest.kanxue.core

import android.content.Context
import android.hardware.input.InputManager
import android.media.MediaCodecList
import android.util.Log
import androidx.compose.runtime.MutableState
import com.nest.kanxue.core.ShellGetMounts.getCgroupUsingFile
import com.nest.kanxue.devicefingerprint.DrmIdFetcher
import java.io.File
import java.io.FileWriter

//获取电池信息

object GetAllVulkanInfo {

    private const val TAG = "GetAllVulkanInfo"

    fun getAllVulkanInfo(context: Context): String {
        var text = ""
        try {
            text =  DrmIdFetcher.getVulkanInfo()
        } catch (e: Exception) {
            Log.d("SBSBSB", "GetAllVulkanInfo Exception: ${e.message}")
            text += "Error: ${e.message}\n"
        }

        return text
    }

    fun saveAllVulkanInfoToFile(context: Context, targetDir: File) {
        try {
            val props = getAllVulkanInfo(context)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "AllVulkanInfo.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "AllVulkanInfo已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存电池信息失败", e)
        }
    }
}