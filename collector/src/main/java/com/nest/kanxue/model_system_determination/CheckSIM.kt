package com.nest.kanxue.model_system_determination

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object CheckSIM {

    fun isSimCardPresent(): Boolean {
        val simState = getSystemProperty("gsm.sim.state")
        return simState != null && simState != "ABSENT"
    }

    fun getSimOperator(c: Context): String? {
        val tm = c.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            return tm.simOperator
        } catch (e: java.lang.Exception) {
        }
        return null
    }

//    fun getSimOperator(context: Context): String? {
//
//
//        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val subscriberId = telephonyManager.subscriberId ?: return null
//        Log.d("sb" , "getSimOperator = $subscriberId")
//
//        // 根据 IMSI 前五位判断运营商
//        return when (subscriberId.take(5)) {
//            "46000", "46002", "46004", "46007", "46008" -> "中国移动"
//            "46001", "46006", "46009", "46010" -> "中国联通"
//            "46003", "46005", "46011", "46012" -> "中国电信"
//            "46015" -> "中国广电"
//            "45412", "45413", "45430" -> "中国移动（香港）"
//            "45407" -> "中国电信（香港）"
//            "45502", "45507" -> "中国电信（澳门）"
//            else -> "未知运营商"
//        }
//    }

    fun getSimICCID(context: Context): String? {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.simSerialNumber// 获取 ICCID
    }

    // 读取系统属性的方法
    fun getSystemProperty(propName: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.readLine()?.takeIf { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            null
        }
    }



}