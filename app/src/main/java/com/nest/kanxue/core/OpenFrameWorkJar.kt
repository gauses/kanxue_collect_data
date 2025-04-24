package com.nest.kanxue.core

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

//获取framework.jar，读取到本地
///system/framework/framework.jar
///system/framework/services.jar
//
//
///system/framework/am.jar
///system/framework/svc.jar
///system/framework/uinput.jar
///system/framework/android.hidl.manager-V1.0-java.jar
///system/framework/abx.jar
//
///system/framework/android.hidl.base-V1.0-java.jar
///system/framework/telephony-common.jar
///system/framework/android.test.runner.jar
///system/framework/requestsync.jar
///system/framework/ims-common.jar
///system/framework/com.android.future.usb.accessory.jar
///system/framework/appwidget.jar
///system/framework/bmgr.jar
///system/framework/locksettings.jar
///system/framework/incident-helper-cmd.jar
///system/framework/telecom.jar
///system/framework/monkey.jar
///system/framework/voip-common.jar
///system/framework/com.android.mediadrm.signer.jar
///system/framework/content.jar
///system/framework/sm.jar
///system/framework/vr.jar
///system/framework/android.test.base.jar
///system/framework/javax.obex.jar
///system/framework/uiautomator.jar
///system/framework/ethernet-service.jar
///system/framework/ext.jar
///system/framework/com.android.location.provider.jar
///system/framework/com.android.media.remotedisplay.jar
///system/framework/bu.jar
///system/framework/framework-graphics.jar
//
///system/framework/org.apache.http.legacy.jar
///system/framework/android.test.mock.jar
///system/framework/hid.jar


object OpenFrameWorkJar {
    private const val TAG = "OpenFrameWorkJar"

    private val FRAMEWORK_JAR_PATHS = listOf(
        "/system/framework/framework.jar",
        "/system/framework/services.jar",
        "/system/framework/am.jar",
        "/system/framework/svc.jar",
        "/system/framework/uinput.jar",
        "/system/framework/android.hidl.manager-V1.0-java.jar",
        "/system/framework/abx.jar",
        "/system/framework/android.hidl.base-V1.0-java.jar",
        "/system/framework/telephony-common.jar",
        "/system/framework/android.test.runner.jar",
        "/system/framework/requestsync.jar",
        "/system/framework/ims-common.jar",
        "/system/framework/com.android.future.usb.accessory.jar",
        "/system/framework/appwidget.jar",
        "/system/framework/bmgr.jar",
        "/system/framework/locksettings.jar",
        "/system/framework/incident-helper-cmd.jar",
        "/system/framework/telecom.jar",
        "/system/framework/monkey.jar",
        "/system/framework/voip-common.jar",
        "/system/framework/com.android.mediadrm.signer.jar",
        "/system/framework/content.jar",
        "/system/framework/sm.jar",
        "/system/framework/vr.jar",
        "/system/framework/android.test.base.jar",
        "/system/framework/javax.obex.jar",
        "/system/framework/uiautomator.jar",
        "/system/framework/ethernet-service.jar",
        "/system/framework/ext.jar",
        "/system/framework/com.android.location.provider.jar",
        "/system/framework/com.android.media.remotedisplay.jar",
        "/system/framework/bu.jar",
        "/system/framework/framework-graphics.jar",
        "/system/framework/org.apache.http.legacy.jar",
        "/system/framework/android.test.mock.jar",
        "/system/framework/hid.jar"
    )

    fun copyFrameworkJars(targetDir: File): Boolean {
        val targetJarDir = File(targetDir, "framework_copy")
        val reportFile = File(targetJarDir, "copy_report.txt")
        val successList = mutableListOf<String>()
        val failList = mutableListOf<String>()
        
        try {
            // 确保目标目录存在
            if (!targetJarDir.exists()) {
                if (!targetJarDir.mkdirs()) {
                    Log.e(TAG, "Failed to create target directory: ${targetJarDir.absolutePath}")
                    return false
                }
            }

            // 复制每个jar文件
            for (jarPath in FRAMEWORK_JAR_PATHS) {
                try {
                    val sourceFile = File(jarPath)
                    val targetFile = File(targetJarDir, sourceFile.name)

                    if (!sourceFile.exists()) {
                        failList.add("$jarPath - File not found")
                        continue
                    }

                    if (!sourceFile.canRead()) {
                        failList.add("$jarPath - No read permission")
                        continue
                    }

                    // 复制文件
                    FileInputStream(sourceFile).use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buffer = ByteArray(8192)
                            var bytes = input.read(buffer)
                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                bytes = input.read(buffer)
                            }
                        }
                    }

                    successList.add("$jarPath - Successfully copied to ${targetFile.absolutePath}")
                    Log.i(TAG, "Successfully copied ${sourceFile.name} to ${targetFile.absolutePath}")
                } catch (e: Exception) {
                    failList.add("$jarPath - Error: ${e.message}")
                    Log.e(TAG, "Error copying $jarPath: ${e.message}")
                }
            }

            // 生成报告
            val report = buildString {
                appendLine("Framework Jars Copy Report")
                appendLine("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}")
                appendLine("\n=== Successfully Copied Files ===")
                successList.forEach { appendLine(it) }
                appendLine("\n=== Failed Files ===")
                failList.forEach { appendLine(it) }
                appendLine("\n=== File Information ===")
                FRAMEWORK_JAR_PATHS.forEach { path ->
                    val file = File(path)
                    appendLine("\nFile: $path")
                    appendLine("Exists: ${file.exists()}")
                    appendLine("Can Read: ${file.canRead()}")
                    appendLine("Can Write: ${file.canWrite()}")
                    appendLine("Can Execute: ${file.canExecute()}")
                    appendLine("Is File: ${file.isFile}")
                    appendLine("Is Directory: ${file.isDirectory}")
                    appendLine("Is Hidden: ${file.isHidden}")
                    appendLine("Absolute Path: ${file.absolutePath}")
                    appendLine("Canonical Path: ${file.canonicalPath}")
                    appendLine("Parent: ${file.parent}")
                    appendLine("Name: ${file.name}")
                    appendLine("Path: ${file.path}")
                    appendLine("Total Space: ${file.totalSpace} bytes")
                    appendLine("Free Space: ${file.freeSpace} bytes")
                    appendLine("Usable Space: ${file.usableSpace} bytes")
                    if (file.exists()) {
                        appendLine("Size: ${file.length()} bytes")
                        appendLine("Last Modified: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(file.lastModified()))}")
                        appendLine("Last Modified (timestamp): ${file.lastModified()}")
                    } else {
                        appendLine("Size: 0 bytes")
                        appendLine("Last Modified: N/A")
                    }
                }
            }

            // 写入报告文件
            reportFile.writeText(report)
            Log.i(TAG, "Copy report saved to ${reportFile.absolutePath}")

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error in copyFrameworkJars: ${e.message}")
            return false
        }
    }

    fun getFrameworkJarInfo(): String {
        val sourceFile = File(FRAMEWORK_JAR_PATHS[0])
        return buildString {
            appendLine("Framework.jar Info:")
            appendLine("Path: ${sourceFile.absolutePath}")
            appendLine("Exists: ${sourceFile.exists()}")
            appendLine("Can Read: ${sourceFile.canRead()}")
            appendLine("Size: ${if (sourceFile.exists()) sourceFile.length() else 0} bytes")
            appendLine("Last Modified: ${if (sourceFile.exists()) sourceFile.lastModified() else 0}")
        }
    }    


    
}