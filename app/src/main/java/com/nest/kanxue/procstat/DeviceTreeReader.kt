//package com.nest.kanxue.procstat
//
//import java.io.File
//import java.io.FileInputStream
//import java.nio.charset.Charset
//
//class DeviceTreeReader {
//
//    fun readCompatible(): String {
//        val path = "/sys/firmware/devicetree/base/compatible"
//
//        return try {
//            val file = File(path)
//
//            // 检查文件是否存在和是否可读
//            if (!file.exists()) {
//                return "File does not exist: $path"
//            }
//
//            if (!file.canRead()) {
//                return "Cannot read file: $path"
//            }
//
//            // 使用FileInputStream读取文件
//            FileInputStream(file).use { inputStream ->
//                // 读取所有字节
//                val bytes = inputStream.readBytes()
//
//                // 如果是空文件
//                if (bytes.isEmpty()) {
//                    return "File is empty"
//                }
//
//                // 处理文件内容（处理可能包含的null字节）
//                bytes.takeWhile { it != 0.toByte() }
//                    .toByteArray()
//                    .toString(Charset.defaultCharset())
//            }
//
//        } catch (e: SecurityException) {
//            "Security Exception: No permission to read file"
//        } catch (e: Exception) {
//            "Error reading file: ${e.message}"
//        }
//    }
//
//    // 备选方法：使用Runtime执行无需root的命令
//    fun readCompatibleUsingCommand(): String {
//        return try {
//            val process = Runtime.getRuntime().exec("cat $path")
//            process.inputStream.bufferedReader().use { reader ->
//                reader.readText()
//            }
//        } catch (e: Exception) {
//            "Error executing command: ${e.message}"
//        }
//    }
//
//    companion object {
//        private const val path = "/sys/firmware/devicetree/base/compatible"
//    }
//}