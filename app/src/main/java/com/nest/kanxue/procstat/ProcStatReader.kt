package com.nest.kanxue.procstat
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

//读取/proc/stat下的所有内容

class ProcStatReader {
    data class CpuStat(
        val name: String,        // cpu名称 (cpu, cpu0, cpu1等)
        val user: Long,          // 用户态时间
        val nice: Long,          // 优先级时间
        val system: Long,        // 系统态时间
        val idle: Long,          // 空闲时间
        val iowait: Long,        // IO等待时间
        val irq: Long,           // 硬中断时间
        val softirq: Long,       // 软中断时间
        val steal: Long,         // 虚拟化相关
        val guest: Long,         // 运行虚拟CPU的时间
        val guestNice: Long      // 虚拟CPU优先级时间
    )

    companion object {
        private const val PROC_STAT_PATH = "/proc/stat"

        /**
         * 读取/proc/stat的所有内容
         * @return Map<String, String> 键为项目名，值为对应的内容
         */
        fun readProcStat(): Map<String, String> {
            val statMap = mutableMapOf<String, String>()

            try {
                File(PROC_STAT_PATH).useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.trim().split(Regex("\\s+"), limit = 2)
                        if (parts.size == 2) {
                            statMap[parts[0]] = parts[1]
                        }
                    }
                }
            } catch (e: IOException) {
                println("读取 /proc/stat 失败: ${e.message}")
            }

            return statMap
        }

        /**
         * 解析CPU统计信息
         * @return List<CpuStat> CPU统计信息列表
         */
        fun parseCpuStats(): List<CpuStat> {
            val cpuStats = mutableListOf<CpuStat>()

            try {
                File(PROC_STAT_PATH).useLines { lines ->
                    lines.filter { it.startsWith("cpu") }.forEach { line ->
                        val parts = line.trim().split(Regex("\\s+"))
                        if (parts.size >= 11) {
                            cpuStats.add(CpuStat(
                                name = parts[0],
                                user = parts[1].toLong(),
                                nice = parts[2].toLong(),
                                system = parts[3].toLong(),
                                idle = parts[4].toLong(),
                                iowait = parts[5].toLong(),
                                irq = parts[6].toLong(),
                                softirq = parts[7].toLong(),
                                steal = parts[8].toLong(),
                                guest = parts[9].toLong(),
                                guestNice = parts[10].toLong()
                            ))
                        }
                    }
                }
            } catch (e: IOException) {
                println("解析CPU统计信息失败: ${e.message}")
            }

            return cpuStats
        }

        /**
         * 格式化输出所有统计信息
         * @return String 格式化后的统计信息
         */
        fun getFormattedStats(): String {
            return buildString {
                appendLine("=== /proc/stat 内容 ===")

                // 读取所有统计信息
                val stats = readProcStat()
                stats.forEach { (key, value) ->
                    appendLine("$key: $value")
                }

                appendLine("\n=== CPU 详细信息 ===")
                // 解析CPU统计信息
                val cpuStats = parseCpuStats()
                cpuStats.forEach { stat ->
                    appendLine("${stat.name}:")
                    appendLine("  用户态时间: ${stat.user}")
                    appendLine("  优先级时间: ${stat.nice}")
                    appendLine("  系统态时间: ${stat.system}")
                    appendLine("  空闲时间: ${stat.idle}")
                    appendLine("  IO等待时间: ${stat.iowait}")
                    appendLine("  硬中断时间: ${stat.irq}")
                    appendLine("  软中断时间: ${stat.softirq}")
                    appendLine("  虚拟化偷取时间: ${stat.steal}")
                    appendLine("  虚拟CPU时间: ${stat.guest}")
                    appendLine("  虚拟CPU优先级时间: ${stat.guestNice}")
                    appendLine()
                }
            }
        }
    }
}
// 使用示例
fun main() {
    // 方法1：获取所有原始数据
    val allStats = ProcStatReader.readProcStat()
    println("原始数据:")
    allStats.forEach { (key, value) ->
        println("$key: $value")
    }

    println("\n")

    // 方法2：获取CPU详细信息
    val cpuStats = ProcStatReader.parseCpuStats()
    println("CPU统计信息:")
    cpuStats.forEach { stat ->
        println(stat)
    }

    println("\n")

    // 方法3：获取格式化的完整报告
    println(ProcStatReader.getFormattedStats())
}