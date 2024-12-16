package com.nest.kanxue.testsh

import org.json.JSONObject

object testShellSTAT {


    fun getPathStatsAsJson(): JSONObject {
        val paths = listOf(
            "/storage/emulated",
            "/storage/emulated/0",
            "/data/data",
            "/data/user",
            "/mnt",
            "/mnt/sdcard",
            "/storage/emulated/0/Android/data",
            "/storage/emulated/0/Android",
            "/storage/emulated/0/tencent",
            "/data/system/job",
            "/data/dalvik-cache",
            "/data/system/locksettings.db",
            "/data/system/inputmethod",
            "/cache",
            "/data/app-lib",
            "/data/system/recoverablekeystore.db",
            "/data/system/graphicsstats",
            "/data/system/sensor_service",
            "/data/system/last-header.txt",
            "/data/system/sync",
            "/data/system/netstats",
            "/data/media",
            "/data/system/procstats",
            "/data/system/notification_log.db",
            "/data/resource-cache",
            "/data/lost+found",
            "/data/ss",
            "/data/system/uiderrors.txt"
        )

        val fsPathsToCheck = listOf(
            "/cache",
            "/system/etc",
            "/storage/emulated"
        )

        val resultJson = JSONObject()

        // 处理普通stat命令
        paths.forEach { path ->
            val result = try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "stat $path"))
//                process.inputStream.bufferedReader().use { it.readText() }.trim()
                process.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                ""
            }
            resultJson.put(path, result)
        }

        // 处理stat -f命令
        fsPathsToCheck.forEach { path ->
            val key = "fs_$path"  // 添加前缀以区分文件系统信息
            val result = try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "stat -f $path"))
//                process.inputStream.bufferedReader().use { it.readText() }.trim()
                process.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                ""
            }
            resultJson.put(key, result)
        }

        return resultJson
    }
}