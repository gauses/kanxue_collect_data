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



}