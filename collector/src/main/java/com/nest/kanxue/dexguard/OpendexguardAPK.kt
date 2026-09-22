package com.nest.kanxue.dexguard

import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object OpendexguardAPK {

    fun copydexguardAPK(targetDir: File) {
        // 获取所有dexguard相关的APK文件路径
        val dexguardApks = mutableListOf<File>()
        val dexguardDirs = targetDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("dexguard")
        }

        // 遍历每个dexguard目录，找到APK文件
        dexguardDirs?.forEach { dir ->
            dir.listFiles { file ->
                file.isFile && file.name.endsWith(".apk")
            }?.let { apks ->
                dexguardApks.addAll(apks)
            }
        }

        // 如果找到了APK文件，开始打包
        if (dexguardApks.isNotEmpty()) {
            // 创建zip文件到Download目录
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val zipFile = File(downloadDir, "dexguard_apks.zip")
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                // 添加每个APK文件到zip中
                dexguardApks.forEach { apkFile ->
                    FileInputStream(apkFile).use { fis ->
                        val zipEntry = ZipEntry(apkFile.name)
                        zipOut.putNextEntry(zipEntry)
                        fis.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }
}