package com.nest.kanxue.checkenvironment

import java.io.File
import java.io.IOException

//1、检测frida的特征：
//进程/proc/self/fd/目录，寻找是否存在关键字 linjector
///data/local/tmp/下是否存在re.frida.server

object checkfrida {

    fun containsLinjectorInFd(): Boolean {
        val fdDir = File("/proc/self/fd/")
        if (!fdDir.exists() || !fdDir.isDirectory) {
            return false
        }

        try {
            val fdFiles = fdDir.listFiles() ?: return false
            for (fdFile in fdFiles) {
                try {
                    val canonicalPath = fdFile.canonicalPath
                    if (canonicalPath.contains("linjector", ignoreCase = true)) {
                        return true
                    }
                } catch (e: IOException) {
                    // 无法解析符号链接，忽略并继续
                    continue
                }
            }
        } catch (e: SecurityException) {
            // 无权限访问 /proc/self/fd，处理异常
            e.printStackTrace()
        }

        return false
    }

    fun isFridaServerPresent(): Boolean {
        val fridaServerFile = File("/data/local/tmp/re.frida.server")
        return fridaServerFile.exists()
    }
}