package com.nest.kanxue.bootid

import java.io.File
import java.io.IOException

class TunInfoReader {



    /**
     * 读取 TUN 接口的信息
     * @return 包含 TUN 信息的 Map，key 为文件名，value 为文件内容
     */
    fun readTunInfo(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            // TUN 接口目录
            val tunDir = File("/sys/class/net/tun")

            // 检查目录是否存在且可读
            if (!tunDir.exists() || !tunDir.canRead()) {
                throw IOException("无法访问 TUN 目录，请检查权限")
            }

            // 读取目录下所有文件
            tunDir.listFiles()?.forEach { file ->
                if (file.isFile && file.canRead()) {
                    try {
                        val content = file.readText().trim()
                        result[file.name] = content
                    } catch (e: IOException) {
                        result[file.name] = "读取失败: ${e.message}"
                    }
                }
            }
        } catch (e: Exception) {
            throw IOException("读取 TUN 信息失败: ${e.message}")
        }

        return result
    }

    /**
     * 读取特定的 TUN 接口属性
     * @param propertyName 属性文件名
     * @return 属性值
     */
    fun readTunProperty(propertyName: String): String {
        try {
            val file = File("/sys/class/net/tun/$propertyName")
            if (!file.exists() || !file.canRead()) {
                throw IOException("无法访问 $propertyName")
            }
            return file.readText().trim()
        } catch (e: Exception) {
            throw IOException("读取属性 $propertyName 失败: ${e.message}")
        }
    }
}
