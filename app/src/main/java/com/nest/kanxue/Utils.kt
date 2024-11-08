package com.nest.kanxue

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.activity.ComponentActivity.WINDOW_SERVICE
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.sqrt

object Utils {

    fun checkFileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    //获取年月日
    fun getCurrentDateTime(): String {
        // 创建日期格式，使用 "yyyy-MM-dd-HH-mm-ss" 表示年-月-日-小时-分钟-秒
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        // 获取当前时间
        val date = Date()
        // 格式化日期并返回
        return dateFormat.format(date)
    }



    fun getScreenSizeInInches(context: Context): Double {
        val metrics = DisplayMetrics()
        val wm = context
            .getSystemService(WINDOW_SERVICE) as WindowManager

        // 获取屏幕分辨率 (像素)
        val widthPixels = metrics.widthPixels
        val heightPixels = metrics.heightPixels

        // 获取屏幕的 DPI (每英寸的像素数)
        val dpi = metrics.densityDpi.toDouble()

        // 计算宽度和高度 (英寸)
        val widthInches = widthPixels / dpi
        val heightInches = heightPixels / dpi

        // 使用勾股定理计算对角线的长度 (英寸)
        return sqrt(widthInches * widthInches + heightInches * heightInches)
    }

}