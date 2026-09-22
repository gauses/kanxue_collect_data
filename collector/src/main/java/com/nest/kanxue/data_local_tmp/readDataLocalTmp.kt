package com.nest.kanxue.data_local_tmp

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.util.Log

//读取以下目录文件，上传到服务器
//[
//{"path":" /data/local/tmp/root.info"},
//{"path":"/data/local/tmp/dev.info"},
//{"path":"/data/local/tmp/socket.info"},
//{"path":"/data/local/tmp/block.info"},
//{"path":"/data/local/tmp/by-name.info"},
//{"path":"/data/local/tmp/otacerts.zip"}
//]

object readDataLocalTmp {
    private const val TAG = "readDataLocalTmp"
    private val filePaths = listOf(
        "/data/local/tmp/root.info",
        "/data/local/tmp/dev.info",
        "/data/local/tmp/socket.info",
        "/data/local/tmp/block.info",
        "/data/local/tmp/by-name.info",
        "/data/local/tmp/otacerts.zip"
    )

    /**
     * 读取指定文件的内容
     * @param filePath 文件路径
     * @return 文件内容的字节数组，如果文件不存在则返回null
     */
    fun readFile(filePath: String): ByteArray? {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.w(TAG, "文件不存在，跳过: $filePath")
                return null
            }
            file.readBytes()
        } catch (e: Exception) {
            Log.e(TAG, "读取文件失败: $filePath", e)
            null
        }
    }

    /**
     * 读取所有文件
     * @return Map<String, ByteArray?> 文件路径和对应的文件内容
     */
    fun readAllFiles(): Map<String, ByteArray?> {
        return filePaths.associateWith { readFile(it) }
    }

    /**
     * 检查文件是否存在
     * @param filePath 文件路径
     * @return Boolean 文件是否存在
     */
    fun isFileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    /**
     * 将所有文件打包成zip文件
     * @param outputZipPath 输出的zip文件路径
     * @return Boolean 是否成功创建zip文件
     */
    fun createZipFile(outputZipPath: String): Boolean {
        var successCount = 0
        var failCount = 0
        
        return try {
            ZipOutputStream(FileOutputStream(outputZipPath)).use { zipOut ->
                filePaths.forEach { filePath ->
                    val file = File(filePath)
                    if (file.exists()) {
                        try {
                            val entry = ZipEntry(file.name)
                            zipOut.putNextEntry(entry)
                            file.inputStream().use { input ->
                                input.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                            successCount++
                            Log.d(TAG, "成功添加文件到zip: ${file.name}")
                        } catch (e: Exception) {
                            failCount++
                            Log.e(TAG, "添加文件到zip失败: ${file.name}", e)
                        }
                    } else {
                        failCount++
                        Log.w(TAG, "文件不存在，跳过: $filePath")
                    }
                }
            }
            Log.i(TAG, "zip文件创建完成，成功: $successCount, 失败: $failCount")
            true
        } catch (e: Exception) {
            Log.e(TAG, "创建zip文件失败", e)
            false
        }
    }
}