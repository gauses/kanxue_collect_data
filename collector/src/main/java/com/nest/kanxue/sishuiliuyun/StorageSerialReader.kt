//package com.nest.kanxue.sishuiliuyun
//
//import java.io.File
//import java.io.IOException
//
//class StorageSerialReader {
//
//    companion object {
//        // 可能的序列号文件路径
//        private val SERIAL_PATHS = arrayOf(
//            "/sys/block/mmcblk0/device/serial",  // EMMC
//            "/sys/block/sda/device/serial",      // UFS
//            "/sys/class/block/mmcblk0/device/serial",
//            "/sys/class/block/sda/device/serial",
//            "/sys/devices/soc0/serial_number",
//            "/sys/block/mmcblk0/device/cid"
//        )
//
//        /**
//         * 方法1：直接读取文件
//         */
//        fun getStorageSerial(): String {
//            for (path in SERIAL_PATHS) {
//                try {
//                    val file = File(path)
//                    if (file.exists() && file.canRead()) {
//                        return file.readText().trim()
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//            return "unknown"
//        }
//
//        /**
//         * 方法2：使用Runtime执行shell命令
//         */
//        fun getStorageSerialWithShell(): String {
//            return try {
//                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat /sys/block/mmcblk0/device/serial"))
//                val result = process.inputStream.bufferedReader().use { it.readText() }
//                process.waitFor()
//                result.trim().takeIf { it.isNotEmpty() } ?: "unknown"
//            } catch (e: Exception) {
//                e.printStackTrace()
//                "unknown"
//            }
//        }
//
//        /**
//         * 方法3：使用ProcessBuilder执行命令
//         */
//        fun getStorageSerialWithProcessBuilder(): String {
//            return try {
//                val process = ProcessBuilder("cat", "/sys/block/mmcblk0/device/serial")
//                    .redirectErrorStream(true)
//                    .start()
//
//                val result = process.inputStream.bufferedReader().use { it.readText() }
//                process.waitFor()
//                result.trim().takeIf { it.isNotEmpty() } ?: "unknown"
//            } catch (e: Exception) {
//                e.printStackTrace()
//                "unknown"
//            }
//        }
//
//        /**
//         * 方法4：带错误处理和详细日志的实现
//         */
//        fun getStorageSerialDetailed(): StorageSerialResult {
//            val results = mutableListOf<String>()
//            var finalSerial = "unknown"
//            var errorMessage = ""
//
//            // 检查所有可能的路径
//            for (path in SERIAL_PATHS) {
//                try {
//                    val file = File(path)
//                    results.add("Checking path: $path")
//                    results.add("File exists: ${file.exists()}")
//                    results.add("Can read: ${file.canRead()}")
//
//                    if (file.exists() && file.canRead()) {
//                        val serial = file.readText().trim()
//                        if (serial.isNotEmpty()) {
//                            finalSerial = serial
//                            results.add("Successfully read serial from $path")
//                            break
//                        }
//                    }
//                } catch (e: IOException) {
//                    errorMessage = "IO Error: ${e.message}"
//                    results.add("Error reading $path: ${e.message}")
//                } catch (e: SecurityException) {
//                    errorMessage = "Security Error: ${e.message}"
//                    results.add("Security error on $path: ${e.message}")
//                } catch (e: Exception) {
//                    errorMessage = "Error: ${e.message}"
//                    results.add("Unexpected error on $path: ${e.message}")
//                }
//            }
//
//            // 如果常规方法失败，尝试使用root权限
//            if (finalSerial == "unknown") {
//                try {
//                    results.add("Trying with root permissions...")
//                    val process = Runtime.getRuntime().exec("su -c cat /sys/block/mmcblk0/device/serial")
//                    val result = process.inputStream.bufferedReader().use { it.readText() }
//                    if (result.isNotEmpty()) {
//                        finalSerial = result.trim()
//                        results.add("Successfully read serial with root permissions")
//                    }
//                } catch (e: Exception) {
//                    results.add("Root method failed: ${e.message}")
//                }
//            }
//
//            return StorageSerialResult(
//                serial = finalSerial,
//                debugInfo = results.joinToString("\n"),
//                error = errorMessage
//            )
//        }
//    }
//
//    // 结果数据类
//    data class StorageSerialResult(
//        val serial: String,
//        val debugInfo: String,
//        val error: String
//    )
//}