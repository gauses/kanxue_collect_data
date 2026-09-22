package com.nest.kanxue.screentoolandclick

import java.io.File

class SELinuxContextChecker {

    companion object {
        private const val ZYGOTE_CONTEXT = "u:r:zygote:s0"

        /**
         * 数据类用于存储进程的SELinux上下文信息
         */
        data class SELinuxInfo(
            val pid: Int,
            val context: String?,
            val isZygoteContext: Boolean,
            val error: String? = null
        )

        /**
         * 检查指定进程的SELinux安全上下文
         * @param pid 进程ID
         * @return SELinuxInfo包含检查结果
         */
        fun checkProcessContext(pid: Int): SELinuxInfo {
            val prevPath = "/proc/$pid/attr/prev"

            return try {
                val file = java.io.File(prevPath)
                if (!file.exists()) {
                    return SELinuxInfo(
                        pid = pid,
                        context = null,
                        isZygoteContext = false,
                        error = "File does not exist: $prevPath"
                    )
                }

                val context = file.readText().trim()
                SELinuxInfo(
                    pid = pid,
                    context = context,
                    isZygoteContext = context == ZYGOTE_CONTEXT
                )
            } catch (e: Exception) {
                SELinuxInfo(
                    pid = pid,
                    context = null,
                    isZygoteContext = false,
                    error = "Error reading context: ${e.message}"
                )
            }
        }

        /**
         * 检查当前进程的SELinux安全上下文
         * @return SELinuxInfo包含检查结果
         */
        fun checkCurrentProcessContext(): SELinuxInfo {
            return checkProcessContext(android.os.Process.myPid())
        }

        /**
         * 检查父进程的SELinux安全上下文
         * @return SELinuxInfo包含检查结果
         */
        fun checkParentProcessContext(): SELinuxInfo? {
            return try {
                val ppid = File("/proc/self/status")
                    .readLines()
                    .firstOrNull { it.startsWith("PPid:") }
                    ?.substringAfter("PPid:")
                    ?.trim()
                    ?.toIntOrNull()

                ppid?.let { checkProcessContext(it) }
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 通过shell命令获取SELinux状态
         * @return SELinux状态信息或null（如果无法获取）
         */
        fun getSELinuxStatus(): String? {
            return try {
                val process = Runtime.getRuntime().exec("getenforce")
                process.inputStream.bufferedReader().use { it.readText().trim() }
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 获取详细的SELinux检查报告
         * @return 详细报告字符串
         */
        fun getFullReport(): String {
            val currentProcess = checkCurrentProcessContext()
            val parentProcess = checkParentProcessContext()
            val selinuxStatus = getSELinuxStatus()

            return buildString {
                appendLine("SELinux Context Check Report")
                appendLine("==========================")

                appendLine("\n1. Current Process (PID: ${currentProcess.pid}):")
                appendLine("Context: ${currentProcess.context ?: "Unknown"}")
                appendLine("Is Zygote Context: ${currentProcess.isZygoteContext}")
                currentProcess.error?.let { appendLine("Error: $it") }

                appendLine("\n2. Parent Process:")
                if (parentProcess != null) {
                    appendLine("PID: ${parentProcess.pid}")
                    appendLine("Context: ${parentProcess.context ?: "Unknown"}")
                    appendLine("Is Zygote Context: ${parentProcess.isZygoteContext}")
                    parentProcess.error?.let { appendLine("Error: $it") }
                } else {
                    appendLine("Unable to get parent process information")
                }

                appendLine("\n3. SELinux Status:")
                appendLine("Enforcement Status: ${selinuxStatus ?: "Unable to determine"}")

                appendLine("\nSummary:")
                appendLine("- Current process has zygote context: ${currentProcess.isZygoteContext}")
                appendLine("- Parent process has zygote context: ${parentProcess?.isZygoteContext}")
                if (currentProcess.isZygoteContext || parentProcess?.isZygoteContext == true) {
                    appendLine("\nWARNING: Zygote context detected! This might indicate a security issue.")
                }
            }
        }

        /**
         * 检查是否检测到任何zygote上下文
         * @return 如果检测到zygote上下文返回true
         */
        fun isZygoteContextDetected(): Boolean {
            val current = checkCurrentProcessContext()
            val parent = checkParentProcessContext()
            return current.isZygoteContext || parent?.isZygoteContext == true
        }

        /**
         * 获取进程树中的所有SELinux上下文
         * @param maxDepth 最大搜索深度
         * @return 进程树中的所有SELinux信息
         */
        fun getProcessTreeContexts(maxDepth: Int = 5): List<SELinuxInfo> {
            val contexts = mutableListOf<SELinuxInfo>()
            var currentPid = android.os.Process.myPid()
            var depth = 0

            while (currentPid != 0 && depth < maxDepth) {
                val info = checkProcessContext(currentPid)
                contexts.add(info)

                // 获取父进程PID
                currentPid = try {
                    File("/proc/$currentPid/status")
                        .readLines()
                        .firstOrNull { it.startsWith("PPid:") }
                        ?.substringAfter("PPid:")
                        ?.trim()
                        ?.toIntOrNull() ?: 0
                } catch (e: Exception) {
                    0
                }

                depth++
            }

            return contexts
        }
    }
}