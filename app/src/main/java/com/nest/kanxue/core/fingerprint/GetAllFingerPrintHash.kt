package com.nest.kanxue.core.fingerprint

import android.content.Context
import android.media.MediaCodecList
import android.util.Log
import java.io.File
import java.io.FileWriter

//获取硬解码

object GetAllFingerPrintHash {

    private const val TAG = "GetAllFingerPrintHash"




    fun saveFingerPrintHashToFile(context: Context, SurfaceFingerprintHash: String, targetDir: File) {
        try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "保存指纹信息Hash值-(显卡,音频，ClientRect).txt")


            val GpuFingerprinter = "显卡信息 ：" + GpuFingerprinter.generateGpuFingerprint() + "\n"
            val AudioFingerprint = "声卡信息 ：" + AudioFingerprint.generateAudioFingerprint() + "\n"
            val ViewRectFingerprint = "ClientRect(基于Android View系统的实现) ：" + ViewRectFingerprint.generateRectFingerprint(context) + "\n"
            val SurfaceFingerprint = "ClientRect(基于SurfaceView和硬件加速的实现) ：" + SurfaceFingerprintHash + "\n"



            FileWriter(propsFile).use { writer ->
                writer.write(GpuFingerprinter)
                writer.write(AudioFingerprint)
                writer.write(ViewRectFingerprint)
                writer.write(SurfaceFingerprint)

            }

            Log.d(TAG, "service.list信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存service.list信息失败", e)
        }
    }
}