package com.nest.kanxue.cpu_battery

import java.io.File

///sys/devices/system/cpu

class CpuReader {
    companion object {
        fun readCpuDevices(): String {
            return try {
                File("/sys/devices/system/cpu").walk()
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

