package com.nest.kanxue.screentoolandclick
import android.content.Context
import android.content.pm.PackageManager
object CheckInstallPackage {

    fun checkInstalledAutoClickerApps(context: Context): ArrayList<String> {
        // 模拟点击应用的包名列表

        var installedPackageList = ArrayList<String>()
        val targetPackages = listOf(
            "com.cygery.repetitouch.pro", "com.cyjh.mobileanjian", "com.touchsprite.android",
            "com.cjzs123.zhushou", "com.touchspriteent.android", "com.zidongdianji",
            "org.autojs.autojspro", "org.autojs.autojs", "com.zdanjian.zdanjian",
            "com.zdnewproject", "com.ifengwoo.zyjdkj", "com.angel.nrzs",
            "com.cyjh.mobileanjian.vip", "com.shumai.shudaxia", "fun.tooling.clicker.cn",
            "com.dianjiqi", "com.miaodong.autoactionssss", "com.mxz.wxautojiafujinderen",
            "com.touchelf.app", "com.stardust.scriptdroid", "com.adinall.autoclick",
            "com.i_cool.auto_clicker", "com.kongshan.aidianji", "com.xptech.catclicker",
            "com.tingniu.autoclick", "com.yicu.yichujifa", "com.smallyin.autoclick",
            "com.ksxkq.autoclick", "com.x2.clicker", "com.scott.autoclickhelper",
            "com.auyou.auyouwzs",
            "org.autojs.autoxjs"
        )

        // 获取 PackageManager 实例
        val packageManager = context.packageManager
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        // 检查是否有任何目标包名已安装
        for (packageInfo in installedPackages) {
            if (targetPackages.contains(packageInfo.packageName)) {
                println("检测到已安装的模拟点击应用: ${packageInfo.packageName}")
                installedPackageList.add(packageInfo.packageName)
            }
        }

        return installedPackageList
    }



    fun checkInstalledScreenshotApps(context: Context): ArrayList<String> {
        var installedPackageList = ArrayList<String>()
        // 目标截图应用的包名列表
        val targetScreenshotPackages = listOf(
            "com.github.uiautomator",
            "com.github.uiautomator2",
            "com.sigma_rt.totalcontrol",
            "com.genymobile.scrcpy"
        )

        // 获取 PackageManager 实例
        val packageManager = context.packageManager
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        // 检查是否有任何目标包名已安装
        for (packageInfo in installedPackages) {
            if (targetScreenshotPackages.contains(packageInfo.packageName)) {
                println("检测到已安装的截图应用: ${packageInfo.packageName}")
                installedPackageList.add(packageInfo.packageName)
            }
        }

        return installedPackageList
    }

}