package com.nest.kanxue.screentoolandclick

import java.io.File

//2.检查/data/local/tmp/有无以下文件或文件目录：

object CheckFileOnTmp {

    fun checkFilesInDataLocalTmp(): Boolean {
        // 要查找的文件或目录名称列表
        val targetFiles = listOf(
            "minicap.so", "minicap", "minitouch", "mini", "mini/minicap",
            "oat/arm64/scrcpy-server.odex", "vysor.pwd", "mobile_info.properties",
            "tc/mobileagent", "tc/input3.sh", "tc/mainputjar7", "com.cyjh.mobileanjian.id",
            "com.cyjh.mobileanjianen.id", "juejinAzykb/", "juejinAzykb/TouchService.jar",
            "mqc-scrcpy.jar", "uiautomator-stub.jar", "cloudtestig/cloudscreen",
            "cloudtesting/touchserver", "txysvr.apk", "yijianwanservice.apk",
            "screen-shread10x64.so", "screen-shread5x32.so", "maxpresent.jar", "libtxysvr.so"
        )

        // 基本目录路径
        val basePath = "/data/local/tmp/"

        for (fileName in targetFiles) {
            val file = File(basePath + fileName)
            // 检查文件或目录是否存在
            if (file.exists()) {
                println("检测到文件或目录: ${file.path}")
                return true
            }
        }

        // 没有找到任何目标文件或目录
        return false
    }

}