package com.nest.kanxue.core

import android.content.Context
import android.hardware.input.InputManager
import android.media.MediaCodecList
import android.util.Log
import com.nest.kanxue.core.ShellGetMounts.getCgroupUsingFile
import java.io.File
import java.io.FileWriter

//获取硬解码

object GetAllCodec {

    private const val TAG = "GetAllCodec"

    fun getAllCodec(context:Context):String{
        val clist = MediaCodecList(MediaCodecList.ALL_CODECS)
        var text = ""

        for (codec in clist.codecInfos) {
            text += "Codec: ${codec.name} ${codec.isEncoder} ${codec.supportedTypes.joinToString(",")}\n"
        }

        Log.d("SBSBSB", "MediaCodecList: $text")
        return text
    }



    fun saveAllCodecToFile(context: Context, targetDir: File) {
        try {
            val props = getAllCodec(context)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "硬解码-AllCodec.txt")

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