package com.nest.kanxue.exec

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader


//exec sh -c pm path com.tencent.mm
//exec sh -c pm path com.xiaomi.market
//exec sh -c pm path com.android.vending

class ShellCommandExecutor {

    /**
     * Execute shell command and return the output
     */
    fun executeShellCommand(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            process.waitFor()
            output
        } catch (e: Exception) {
            Log.e("ShellExecutor", "Error executing command: ${e.message}")
            null
        }
    }

    /**
     * Get the package path
     */
    fun getPackagePath(packageName: String): String? {
        return executeShellCommand("pm path $packageName")
    }
}