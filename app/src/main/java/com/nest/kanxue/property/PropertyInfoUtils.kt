package com.nest.kanxue.property

import android.util.Log
import java.io.File

/**
 * 直接复制 Android 属性服务的二进制文件 /dev/__properties__/property_info，
 * 不做任何解析，原样保存到采集目录，随 zip 一起打包上传。
 * 该文件权限通常为 0444，普通应用可直接读取，无需 root。
 */
object PropertyInfoUtils {

    private const val PROPERTY_INFO_PATH = "/dev/__properties__/property_info"

    /**
     * 将 property_info 原始文件复制到 targetDir 下（文件名保持 property_info）。
     * @return 复制成功返回目标文件，失败返回 null。
     */
    fun copyPropertyInfo(targetDir: File): File? {
        return try {
            val src = File(PROPERTY_INFO_PATH)
            if (!src.isFile || !src.canRead()) {
                Log.e("PropertyInfo", "property_info 不存在或不可读: $PROPERTY_INFO_PATH")
                return null
            }
            if (!targetDir.exists()) targetDir.mkdirs()
            val dst = File(targetDir, "property_info")
            src.inputStream().use { input ->
                dst.outputStream().use { output -> input.copyTo(output) }
            }
            Log.d("PropertyInfo", "已复制 property_info -> ${dst.absolutePath} (${dst.length()} bytes)")
            dst
        } catch (e: Exception) {
            Log.e("PropertyInfo", "复制property_info失败: ${e.message}")
            null
        }
    }
}
