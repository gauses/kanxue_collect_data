package com.nest.kanxue.cpu_battery

import java.io.File

class BatteryReader {
    companion object {
        fun readBatteryInfo(): String {
            return try {
                File("/sys/class/power_supply/battery").walk()
                    .filter { it.isFile }
                    .joinToString("\n") { file ->
                        "${file.absolutePath}: ${
                            try {
                                file.readText()
                            } catch (e: Exception) {
                                "无法读取: ${e.message}"
                            }
                        }"
                    }
            } catch (e: Exception) {
                "读取失败: ${e.message}"
            }
        }
    }
}
