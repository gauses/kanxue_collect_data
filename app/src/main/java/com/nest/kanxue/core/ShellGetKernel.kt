package com.nest.kanxue.core

import android.os.Build
import android.util.Log
import com.nest.kanxue.devicefingerprint.DrmIdFetcher
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException


object ShellGetKernel {
    private const val TAG = "ShellGetKernel"


    //JNI
    fun getKernelUsingFile(): String? {
        val kernel = DrmIdFetcher.getCnameInfoHex()
        return kernel
    }

    fun saveSystemPropsToFile(targetDir: File) {
        try {
            val props = getKernelUsingFile()
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val propsFile = File(targetDir, "Kernel.txt")

            FileWriter(propsFile).use { writer ->
                writer.write(props)
            }

            Log.d(TAG, "Kernel信息已保存到: ${propsFile.absolutePath}")
            Log.d(TAG, "文件大小: ${propsFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存Kernel信息失败", e)
        }
    }
}