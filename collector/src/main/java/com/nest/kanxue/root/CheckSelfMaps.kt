package com.nest.kanxue.root

import java.io.File

//5.检测 /proc/self/maps中的内容
//1、检测 /proc/self/maps 是否存在名为“/memfd:/jit-cache”的段（加载zygisk模块时（也就是liblspd.so)的时候会讲其名称设置为jit-cache，这样的话so的内存段在maps中就是/memfd:/jit-cache）
//2、通过检测map表是否存在匿名的并且具有可执行属性的内存判断是否存在lsposed
//3、检测栈空间[stack]的权限是否为“rw-p”


class CheckSelfMaps {

    companion object {
        /**
         * 表示maps文件中的一个内存段
         */
        data class MemorySegment(
            val startAddress: String,
            val endAddress: String,
            val permissions: String,
            val offset: String,
            val device: String,
            val inode: String,
            val pathname: String?
        )

        /**
         * 读取/proc/self/maps文件内容
         * @return 返回maps文件的所有行
         */
        private fun readMapsFile(): List<String> {
            return try {
                File("/proc/self/maps").readLines()
            } catch (e: Exception) {
                emptyList()
            }
        }

        /**
         * 解析maps文件的一行内容
         * @param line maps文件的一行
         * @return 解析后的MemorySegment对象
         */
        private fun parseMapsLine(line: String): MemorySegment? {
            // maps文件格式：address permissions offset device inode pathname
            // 例如：7f7b4c5000-7f7b4e4000 r-xp 00000000 08:11 13667 /system/lib64/libc.so
            val parts = line.trim().split("\\s+".toRegex(), limit = 6)
            return try {
                if (parts.size >= 5) {
                    val addresses = parts[0].split("-")
                    MemorySegment(
                        startAddress = addresses[0],
                        endAddress = addresses[1],
                        permissions = parts[1],
                        offset = parts[2],
                        device = parts[3],
                        inode = parts[4],
                        pathname = if (parts.size > 5) parts[5].trim() else null
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 检测是否存在jit-cache内存段
         */
        fun hasJitCacheSegment(): Boolean {
            return readMapsFile().any { line ->
                line.contains("/memfd:/jit-cache")
            }
        }

        /**
         * 检测是否存在匿名可执行内存段（可能表明存在LSPosed）
         */
        fun hasAnonymousExecutableSegment(): Boolean {
            return readMapsFile().any { line ->
                val segment = parseMapsLine(line)
                segment?.let {
                    // 匿名段通常没有pathname或pathname为"[anonymous]"
                    (it.pathname == null || it.pathname == "[anonymous]") &&
                            // 检查是否具有可执行权限 (x)
                            it.permissions.contains('x')
                } ?: false
            }
        }

        /**
         * 检查栈空间权限
         * @return 如果发现stack段权限不是rw-p返回true
         */
        fun hasAbnormalStackPermissions(): Boolean {
            return readMapsFile().any { line ->
                if (line.contains("[stack]")) {
                    val segment = parseMapsLine(line)
                    segment?.permissions != "rw-p"
                } else false
            }
        }

        /**
         * 获取详细的检查报告
         */
        fun getFullReport(): String {
            val mapsContent = readMapsFile()

            return buildString {
                appendLine("Memory Maps Analysis Report")
                appendLine("=========================")
                appendLine("1. JIT Cache Check:")
                appendLine("   Found /memfd:/jit-cache: ${hasJitCacheSegment()}")

                appendLine("\n2. Anonymous Executable Segments:")
                val anonymousExec = mapsContent.filter { line ->
                    val segment = parseMapsLine(line)
                    segment?.let {
                        (it.pathname == null || it.pathname == "[anonymous]") &&
                                it.permissions.contains('x')
                    } ?: false
                }
                appendLine("   Found ${anonymousExec.size} anonymous executable segments")
                anonymousExec.take(5).forEach { // 只显示前5个作为示例
                    appendLine("   - $it")
                }

                appendLine("\n3. Stack Permissions:")
                mapsContent.filter { it.contains("[stack]") }.forEach {
                    val segment = parseMapsLine(it)
                    appendLine("   Stack segment permissions: ${segment?.permissions}")
                }

                appendLine("\nSummary:")
                appendLine("- JIT Cache detected: ${hasJitCacheSegment()}")
                appendLine("- Anonymous executable segments detected: ${hasAnonymousExecutableSegment()}")
                appendLine("- Abnormal stack permissions: ${hasAbnormalStackPermissions()}")
            }
        }

        /**
         * 执行所有检查
         * @return 返回一个Map包含所有检查结果
         */
        fun runAllChecks(): Map<String, Boolean> {
            return mapOf(
                "hasJitCache" to hasJitCacheSegment(),
                "hasAnonymousExecutable" to hasAnonymousExecutableSegment(),
                "hasAbnormalStackPermissions" to hasAbnormalStackPermissions()
            )
        }
    }
}