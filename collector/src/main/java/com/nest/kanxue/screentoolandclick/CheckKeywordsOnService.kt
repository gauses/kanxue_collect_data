package com.nest.kanxue.screentoolandclick
import android.app.ActivityManager
import android.content.Context


//1.检测运行的服务中是否存在minicap、minitouch、stfservice、minitouchagent、stfagent、testminicap关键字。

object CheckKeywordsOnService {

    fun checkForScreenshotAndTouchServices(context: Context): Boolean {
        // 定义要查找的关键字列表
        val targetKeywords = listOf(
            "minicap", "minitouch", "stfservice",
            "minitouchagent", "stfagent", "testminicap"
        )

        // 获取 ActivityManager 实例
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // 遍历正在运行的服务
        val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
        for (service in runningServices) {
            // 检查服务名称中是否包含目标关键字
            val serviceName = service.service.className.lowercase()
            if (targetKeywords.any { serviceName.contains(it) }) {
                return true  // 找到匹配的关键字，返回 true
            }
        }

        // 没有找到匹配的服务，返回 false
        return false
    }

}