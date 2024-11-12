package com.nest.kanxue.screentoolandclick


//6.检测ro.build.tags的值，读取 /system/build.prop并检测ro.build.tags的值是否为“test-keys”

class BuildTagsChecker {

    companion object {
        private const val BUILD_TAGS_PROP = "ro.build.tags"
        private const val BUILD_PROP_PATH = "/system/build.prop"
        private const val TEST_KEYS_VALUE = "test-keys"

        /**
         * 通过System.getProperty检查build.tags
         * @return 如果值为"test-keys"返回true，如果无法读取返回null
         */
        fun checkBuildTagsFromSystem(): Boolean? {
            return try {
                val buildTags = System.getProperty(BUILD_TAGS_PROP)
                buildTags?.contains(TEST_KEYS_VALUE)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 从build.prop文件读取build.tags
         * @return 如果值为"test-keys"返回true，如果无法读取返回null
         */
        fun checkBuildTagsFromFile(): Boolean? {
            return try {
                val buildProp = java.io.File(BUILD_PROP_PATH)
                if (!buildProp.exists() || !buildProp.canRead()) {
                    return null
                }

                var result: Boolean? = null
                buildProp.useLines { lines ->
                    lines.forEach { line ->
                        if (line.startsWith(BUILD_TAGS_PROP)) {
                            val value = line.substringAfter("=").trim()
                            result = value.contains(TEST_KEYS_VALUE)
                            return@forEach
                        }
                    }
                }
                result
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 使用ProcessBuilder执行getprop命令
         * @return 如果值为"test-keys"返回true，如果无法读取返回null
         */
        fun checkBuildTagsFromGetprop(): Boolean? {
            return try {
                val process = ProcessBuilder("getprop", BUILD_TAGS_PROP)
                    .redirectErrorStream(true)
                    .start()

                val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
                if (output.isNotEmpty()) {
                    output.contains(TEST_KEYS_VALUE)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 综合检查build.tags
         * @return 检查结果的Map
         */
        fun checkBuildTags(): Map<String, Boolean?> {
            return mapOf(
                "systemProperty" to checkBuildTagsFromSystem(),
                "buildProp" to checkBuildTagsFromFile(),
                "getProp" to checkBuildTagsFromGetprop()
            )
        }

        /**
         * 获取详细的检查报告
         * @return 详细的检查报告字符串
         */
        fun getFullReport(): String {
            val results = checkBuildTags()

            return buildString {
                appendLine("Build Tags Check Report")
                appendLine("=====================")

                appendLine("\n1. System Property Check:")
                appendLine("Method: System.getProperty")
                appendLine("Result: ${formatResult(results["systemProperty"])}")

                appendLine("\n2. Build.prop File Check:")
                appendLine("File: $BUILD_PROP_PATH")
                appendLine("Result: ${formatResult(results["buildProp"])}")

                appendLine("\n3. Getprop Command Check:")
                appendLine("Command: getprop $BUILD_TAGS_PROP")
                appendLine("Result: ${formatResult(results["getProp"])}")

                appendLine("\nSummary:")
                val isTestKeys = results.values.any { it == true }
                appendLine("Device appears to be ${if (isTestKeys) "test-signed" else "release-signed"}")

                if (results.values.any { it == null }) {
                    appendLine("\nNote: Some checks failed to complete. This might indicate restricted access or security measures.")
                }
            }
        }

        private fun formatResult(result: Boolean?): String {
            return when (result) {
                true -> "test-keys detected"
                false -> "release-keys or other value"
                null -> "unable to check"
            }
        }

        /**
         * 检查是否有任何方法检测到test-keys
         * @return 如果任何方法检测到test-keys返回true
         */
        fun isTestKeysDetected(): Boolean {
            return checkBuildTags().values.any { it == true }
        }
    }
}