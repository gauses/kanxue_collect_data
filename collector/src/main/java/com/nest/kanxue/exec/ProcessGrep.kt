package com.nest.kanxue.exec

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


//exec sh -c ps | grep adbd

class ProcessGrep {

    // 方法3：使用 ProcessBuilder 读取 /proc/pid/cmdline
    fun getProcessInfoViaProcPidCmdline(): String {
        return try {
            // 先获取所有进程ID
            val pids = ProcessBuilder("ls", "/proc")
                .start()
                .inputStream.bufferedReader()
                .useLines { lines ->
                    lines.filter { it.matches(Regex("\\d+")) }.toList()
                }

            // 查找包含 adbd 的进程
            pids.mapNotNull { pid ->
                try {
                    val cmdline = File("/proc/$pid/cmdline").readText()
                    if (cmdline.contains("adbd")) {
                        "PID: $pid, Cmdline: $cmdline"
                    } else null
                } catch (e: Exception) {
                    null
                }
            }.joinToString("\n")
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // 方法4：使用系统API Debug.getPss()获取进程内存信息（需要特定权限）
    @RequiresPermission(Manifest.permission.PACKAGE_USAGE_STATS)
    fun getProcessMemoryInfo(context: Context, pid: Int): String {
        val debugInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(debugInfo)
        return "PID: $pid, PSS: ${debugInfo.totalPss}kB"
    }


    // 方法1：使用 ActivityManager
    fun getProcessInfoViaActivityManager(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        return runningProcesses
            ?.filter { it.processName.contains("adbd") }
            ?.joinToString("\n") { "PID: ${it.pid}, Process: ${it.processName}" }
            ?: "No matching processes found"
    }


    // 方法2：读取 /proc 目录
    fun getProcessInfoViaProc(): String {
        try {
            val procDir = File("/proc")
            return procDir.listFiles { file ->
                file.isDirectory && file.name.matches(Regex("\\d+"))
            }?.mapNotNull { pidDir ->
                try {
                    val cmdlineFile = File(pidDir, "cmdline")
                    val cmdline = cmdlineFile.readText()
                    if (cmdline.contains("adbd")) {
                        "PID: ${pidDir.name}, Cmdline: $cmdline"
                    } else null
                } catch (e: Exception) {
                    null
                }
            }?.joinToString("\n") ?: "No matching processes found"
        } catch (e: Exception) {
            return "Error reading /proc: ${e.message}"
        }
    }

    // 另一种实现方式：直接使用 Runtime.exec()
    fun executeShellCommandAlternative(): String {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "ps | grep adbd"))
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            return output
        } catch (e: Exception) {
            return "Error executing command: ${e.message}"
        }
    }


    fun executeShellCommand(): String {
        try {
            // 创建 ProcessBuilder 来执行 ps 命令
            val psProcess = ProcessBuilder()
                .command("ps")
                .redirectErrorStream(true)
                .start()

            // 读取 ps 命令的输出
            val psOutput = psProcess.inputStream.bufferedReader().use { it.readText() }

            // 使用 Kotlin 的字符串处理功能来模拟 grep
            val filteredOutput = psOutput.lines()
                .filter { it.contains("adbd", ignoreCase = true) }
                .joinToString("\n")

            // 等待进程完成
            psProcess.waitFor()

            return filteredOutput
        } catch (e: Exception) {
            return "Error executing command: ${e.message}"
        }
    }
}