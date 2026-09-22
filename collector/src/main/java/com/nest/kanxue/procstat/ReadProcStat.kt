//package com.nest.kanxue.procstat
//
//
//import android.util.Log
//import java.io.BufferedReader
//import java.io.File
//import java.io.InputStreamReader
//import java.util.concurrent.TimeUnit
//
//object ReadProcStat {
//
//
//    fun getInfo() {
//        try {
//            val processBuilder = ProcessBuilder("/bin/sh", "-c", "cat /proc/stat")
//            val process = processBuilder.start()
//
//            // 设置超时时间
//            if (!process.waitFor(5, TimeUnit.SECONDS)) {
//                println("Command timed out!")
//                return
//            }
//
//            // 读取所有输出
//            val output = process.inputStream.bufferedReader().readText()
//            println("output = $output")
//
//
//            // 获取输入流
//            val reader = BufferedReader(InputStreamReader(process.inputStream))
//
//            // 打印BufferedReader状态
//            println("BufferedReader ready: ${reader.ready()}")
//
//            // 方法1：直接读取所有行
//            println("\n--- Method 1: readLines() ---")
//            val allLines = reader.readLines()
//            println("Number of lines read: ${allLines.size}")
//            allLines.forEach { println(it) }
//
//            // 如果方法1不工作，尝试方法2：手动读取每一行
//            /*
//            println("\n--- Method 2: manual reading ---")
//            var line: String?
//            while (true) {
//                line = reader.readLine()
//                if (line == null) break
//                println("Line read: $line")
//            }
//            */
//
//            // 等待进程完成
//            val exitCode = process.waitFor()
//            println("\nProcess exit code: $exitCode")
//
//            // 检查错误流
//            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
//            val errors = errorReader.readLines()
//            if (errors.isNotEmpty()) {
//                println("\nErrors:")
//                errors.forEach { println(it) }
//            }
//
//        } catch (e: Exception) {
//            println("Error executing command: ${e.message}")
//            e.printStackTrace()
//        }
//    }
//
//}