package com.nest.kanxue.root

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

object CheckInstallPackageRoot {

    private val targetPackages = setOf(
        "com.topjohnwu.magisk",
        "eu.chainfire.supersu",
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.fox2code.mmm",
        "io.github.vvb2060.magisk",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.smedialink.oneclickroot",
        "com.zhiqupk.root.global",
        "com.alephzain.framaroot",
        "io.github.huskydg.magisk",
        "me.weishu.kernelsu"
    )


//    fun checkPackages(): List<String> {
//        val foundPackages = mutableListOf<String>()
//        try {
//            val dataDir = File("/data/data")
//            if (dataDir.exists() && dataDir.isDirectory) {
//                dataDir.list()?.forEach { fileName ->
//                    if (targetPackages.contains(fileName)) {
//                        foundPackages.add(fileName)
//                    }
//                }
//            }
//        } catch (e: SecurityException) {
//            println("无法访问 /data/data 目录: ${e.message}")
//        } catch (e: Exception) {
//            println("检查过程中发生错误: ${e.message}")
//        }
//        return foundPackages
//    }


    fun checkPackages(context: Context): List<String> {
        val foundPackages = mutableListOf<String>()


        // 获取 PackageManager 实例
        val packageManager = context.packageManager
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        // 检查是否有任何目标包名已安装
        for (packageInfo in installedPackages) {
            if (targetPackages.contains(packageInfo.packageName)) {
                println("检测到已安装的Root应用: ${packageInfo.packageName}")
                foundPackages.add(packageInfo.packageName)
            }
        }



        return foundPackages
    }





}