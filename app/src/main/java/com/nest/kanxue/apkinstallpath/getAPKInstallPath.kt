package com.nest.kanxue.apkinstallpath

import android.content.Context
import android.content.pm.PackageManager
import org.json.JSONArray
import org.json.JSONObject

object getAPKInstallPath {

    fun getInstalledAppsWithApkPath(context: Context, packageNames: List<String>): Map<String, String> {
        val packageManager = context.packageManager
        val appsWithPaths = mutableMapOf<String, String>()

        // 获取所有已安装的应用信息
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (appInfo in installedApps) {
            // 过滤出指定包名的应用
            if (packageNames.contains(appInfo.packageName)) {
                val apkPath = appInfo.sourceDir  // 获取APK的路径
                appsWithPaths[appInfo.packageName] = apkPath
            }
        }
        return appsWithPaths
    }


    fun getAPKPath(context: Context):JSONObject{

        val selectedPackageNames = listOf(
            "com.tencent.mm", //微信
            "com.android.vending" //google 市场
        )

        val APKPathJSON = JSONObject();
        val appsWithPaths = getInstalledAppsWithApkPath(context, selectedPackageNames)
        appsWithPaths.forEach { (packageName, apkPath) ->
            println("Package: $packageName, APK Path: $apkPath")
            APKPathJSON.put(packageName , apkPath)
        }
        return APKPathJSON
    }


}