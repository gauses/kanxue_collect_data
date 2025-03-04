package com.nest.kanxue.testsh

import java.io.BufferedReader
import java.io.InputStreamReader

object testShellGetProp {


    // 方法1: 使用ProcessBuilder
//    fun getSystemProperties(): String {
//        return try {
//            val process = ProcessBuilder("sh", "-c", "/system/bin/getprop")
//                .redirectErrorStream(true)
//                .start()
//
//            val reader = BufferedReader(InputStreamReader(process.inputStream))
//            val output = StringBuilder()
//            var line: String?
//
//            while (reader.readLine().also { line = it } != null) {
//                output.append(line).append("\n")
//            }
//
//            process.waitFor()
//            output.toString()
//        } catch (e: Exception) {
//            "Error executing command: ${e.message}"
//        }
//    }

    // sh -c /system/bin/getprop
    fun getSystemProps(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "/system/bin/getprop"))
            process.inputStream.bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }


//    fun getSystemProps1(): String {
//        return try {
//            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "/system/bin/getprop"))
//            process.inputStream.bufferedReader().use { reader ->
//                reader.readText().replace("[", "").replace("]", "")
//            }
//        } catch (e: Exception) {
//            "Error: ${e.message}"
//        }
//    }



    fun getSystemProps2() {
        try {
            // 创建 ProcessBuilder 对象并设置命令
            val processBuilder = ProcessBuilder("ls", "-l", "/sdcard").apply {
                redirectErrorStream(true) // 将错误流合并到输入流
            }

            // 启动进程
            val process = processBuilder.start()

            // 获取进程的输入流
            val inputStream = process.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))

            // 读取输出
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line) // 打印每一行输出
            }

            // 等待进程结束并获取退出码
            val exitCode = process.waitFor()
            println("Process exited with code: $exitCode")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}