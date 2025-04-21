package com.nest.kanxue.core

import android.content.Context
import android.os.BatteryManager
import android.content.Context.BATTERY_SERVICE
import android.util.Log
import com.nest.kanxue.core.Shell_lsusb.getlsusbUsingFile
import java.io.File
import java.io.FileWriter

// 获取电池相关信息
//charge_full=4518000 - 微安时（μAh）为单位
//charge_full_design=4521000
//cycle_count=112
//current_max=
//voltage_max=
//voltage_min=
//temp=30
//technology=
object Batteryutils {
    private const val TAG = "BatteryUtils"

    fun getAllBatteryInfo(context: Context, targetDir: File): Map<String, String> {
        val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        val batteryStatus = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))

        // 获取电池容量（μAh）
        val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        Log.d(TAG, "chargeCounter (μAh): $chargeCounter")

        // 获取电池容量（百分比）
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        Log.d(TAG, "capacity (%): $capacity")

        // 计算实际容量（mAh）
        val actualCapacity = if (chargeCounter != -1 && capacity != -1 && capacity != 0) {
            // 将微安时(μAh)转换为毫安时(mAh)
            (chargeCounter * 100.0 / capacity).toInt() * 1000
        } else {
            -1
        }
        Log.d(TAG, "actualCapacity: $actualCapacity")

        // 获取当前电压（mV）
        val currentVoltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        Log.d(TAG, "currentVoltage: $currentVoltage")

        // 获取当前电流（mA）
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        Log.d(TAG, "currentNow: $currentNow")

        // 获取能量计数器（nWh）
        val energyCounter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
        Log.d(TAG, "energyCounter: $energyCounter")

        // 获取平均电流（mA）
        val currentAverage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        Log.d(TAG, "currentAverage: $currentAverage")

        val batteryInfo = mapOf(
            "charge_full" to actualCapacity.toString(),
            "charge_full_design" to estimateDesignCapacity(batteryManager, chargeCounter, capacity, currentVoltage).toString(),
            "cycle_count" to "", // 循环次数无法获取
            "current_max" to currentNow.toString(),
            "voltage_max" to "", // 最大电压无法获取
            "voltage_min" to "", // 最小电压无法获取
            "voltage_current" to currentVoltage.toString(),
            "temp" to getBatteryTemperature(batteryStatus).toString(),
            "technology" to getBatteryTechnology(batteryStatus),
            "capacity_percent" to capacity.toString(),
            "energy_counter" to energyCounter.toString(),
            "current_average" to currentAverage.toString()
        )

        // 保存电池信息到文件
        try {
            if (targetDir != null) {
                saveBatteryInfoToFile(targetDir, batteryInfo)
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存电池信息失败", e)
        }

        return batteryInfo
    }

    private fun estimateDesignCapacity(
        batteryManager: BatteryManager,
        chargeCounter: Int,
        capacity: Int,
        currentVoltage: Int
    ): Int {
        // 方法1：使用充电计数器和当前电量百分比估算
        if (chargeCounter != -1 && capacity != -1 && capacity != 0) {
            Log.d(TAG, "Input values:")
            Log.d(TAG, "chargeCounter (μAh): $chargeCounter")
            Log.d(TAG, "capacity (%): $capacity")
            
            // 计算设计容量（微安时）
            // 公式：设计容量 = (当前容量 * 100) / 当前百分比
            val designCapacity = (chargeCounter.toLong() * 100L) / capacity.toLong()
            Log.d(TAG, "Design capacity (μAh): $designCapacity")
            
            // 转换为微安时（乘以1000）
            val result = designCapacity * 1000L
            Log.d(TAG, "Final result (μAh): $result")
            
            return result.toInt()
        }

        Log.d(TAG, "Method failed, returning -1")
        return -1
    }

    private fun getBatteryTemperature(batteryStatus: android.content.Intent?): Int {
        return batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
    }

    private fun getBatteryTechnology(batteryStatus: android.content.Intent?): String {
        return batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""
    }

    // 获取电池状态信息
    fun getBatteryStatusInfo(context: Context): Map<String, String> {
        val batteryStatus = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))

        return mapOf(
            "level" to (batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)?.toString() ?: "-1"),
            "scale" to (batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)?.toString() ?: "-1"),
            "status" to getBatteryStatusString(batteryStatus),
            "health" to getBatteryHealthString(batteryStatus),
            "plugged" to getPluggedStatusString(batteryStatus),
            "voltage" to (batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.toString() ?: "-1")
        )
    }

    private fun getBatteryStatusString(batteryStatus: android.content.Intent?): String {
        return when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
            BatteryManager.BATTERY_STATUS_FULL -> "已充满"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
            else -> "未知"
        }
    }

    private fun getBatteryHealthString(batteryStatus: android.content.Intent?): String {
        return when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "良好"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "过热"
            BatteryManager.BATTERY_HEALTH_DEAD -> "已损坏"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "过压"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "未知故障"
            else -> "未知"
        }
    }

    private fun getPluggedStatusString(batteryStatus: android.content.Intent?): String {
        return when (batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_AC -> "交流电源"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
            else -> "未充电"
        }
    }

    private fun saveBatteryInfoToFile(targetDir: File, batteryInfo: Map<String, String>) {
        try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val batteryFile = File(targetDir, "BatteryInfo.txt")
            FileWriter(batteryFile).use { writer ->
                writer.write("=== 电池信息 ===\n")
                writer.write("获取时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}\n\n")
                
                batteryInfo.forEach { (key, value) ->
                    writer.write("$key = $value\n")
                }
            }

            Log.d(TAG, "电池信息已保存到: ${batteryFile.absolutePath}")
            Log.d(TAG, "文件大小: ${batteryFile.length()} 字节")
        } catch (e: Exception) {
            Log.e(TAG, "保存电池信息失败", e)
        }
    }


}
