package com.nest.kanxue.root

object CheckAndroidPath {

    /**
     * 检查PATH环境变量中的所有路径
     * @return 返回PATH中的所有路径列表
     */
    fun getSystemPaths(): List<String> {
        return System.getenv("PATH")?.split(":") ?: emptyList()
    }

    /**
     * 检查指定路径下是否存在su文件
     * @param path 要检查的路径
     * @return 如果路径下存在su文件返回true
     */
    private fun checkSuInPath(path: String): Boolean {
        val suFile = java.io.File(path, "su")
        return suFile.exists()
    }

    /**
     * 检查PATH中所有包含su的路径
     * @return 返回包含su文件的路径列表
     */
    fun findSuInPaths(): List<String> {
        return getSystemPaths().filter { path ->
            checkSuInPath(path)
        }
    }

    /**
     * 获取详细的PATH检查结果
     * @return 返回Map，key为路径，value为是否包含su文件
     */
    fun getDetailedPathCheck(): Map<String, Boolean> {
        return getSystemPaths().associateWith { path ->
            checkSuInPath(path)
        }
    }

    /**
     * 检查PATH中是否存在su文件
     * @return 如果在任何PATH路径下发现su文件返回true
     */
    fun hasSuInPath(): Boolean {
        return findSuInPaths().isNotEmpty()
    }

    /**
     * 获取完整的检查报告，包括PATH环境变量和检查结果
     * @return 返回详细的检查报告字符串
     */
    fun getFullReport(): String {
        val pathValue = System.getenv("PATH") ?: "PATH not found"
        val paths = getSystemPaths()
        val suPaths = findSuInPaths()

        return buildString {
            appendLine("PATH Environment Variable Check Report")
            appendLine("=====================================")
            appendLine("Full PATH: $pathValue")
            appendLine("\nIndividual Paths:")
            paths.forEach { path ->
                appendLine("- $path ${if (path in suPaths) "(contains su)" else ""}")
            }
            appendLine("\nPaths containing su: ${suPaths.size}")
            suPaths.forEach { path ->
                appendLine("- $path")
            }
        }
    }
}