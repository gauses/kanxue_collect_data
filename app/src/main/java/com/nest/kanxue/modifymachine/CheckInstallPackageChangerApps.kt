package com.nest.kanxue.modifymachine

import android.content.Context
import android.content.pm.PackageManager
import org.json.JSONObject

//1.检测是否安装了改机软件:

object CheckInstallPackageChangerApps {



    fun getInfo(context: Context): JSONObject{
        val jsonObject = JSONObject()
        val list = detectChangerApps(context)
        if (list.isNotEmpty()) {
            jsonObject.put("是否安装改机软件", true)
            jsonObject.put("安装改机软件列表", list)
        }else{
            jsonObject.put("是否安装改机软件", false)

        }
        return jsonObject
    }

        private val CHANGER_PACKAGES = listOf(
            "com.yztc.studio.plugin",     // 易改机
            "com.soft.apk008v",           // 008改机
            "com.uwish.app",              // 万能改机
            "zpp.wjy.xxsq",               // 新星改机
            "com.bigsing.changer",        // 改机工具
            "zap.fh.wipe",                // 变装改机
            "com.sollyu.xposed.hook.model", // 改机模型
            "com.soft.apk008Tool",        // 008工具箱
            "com.doubee.ig",              // 变量改机
            "com.variable.apkhook",       // 变量Hook
            "com.addeasy.fastest",        // 快速改机
            "com.xenice.mask",            // 面具改机
            "com.shyl.artifact",          // 神器改机
            "com.android1500.androidfaker" // 安卓改机
        )
    /**
     * 检查是否安装了改机软件
     * @return 返回检测到的改机软件包名列表
     */
    private fun detectChangerApps(context: Context): List<String> {
        val installedChangers = mutableListOf<String>()

        // 方法1：通过 PackageManager 检查
        checkWithPackageManager(context, installedChangers)
        return installedChangers.distinct()
    }

    /**
     * 使用 PackageManager 检查包名
     */
    private fun checkWithPackageManager(context: Context, result: MutableList<String>) {
        val packageManager = context.packageManager
        CHANGER_PACKAGES.forEach { packageName ->
            try {
                packageManager.getPackageInfo(packageName, 0)
                result.add(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                // 包名不存在，继续检查下一个
            }
        }
    }




}