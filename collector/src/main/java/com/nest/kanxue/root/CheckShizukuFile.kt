package com.nest.kanxue.root

import java.io.File

//8.检查/data/local/tmp/有无以下文件
//shizuku
//shizuku_starter

object CheckShizukuFile {

    fun checkFilesExistence(): Boolean {
        val baseDir = "/data/local/tmp"
        val filesToCheck = listOf("shizuku", "shizuku_starter")

        try {
            val baseDirFile = File(baseDir)

            if (!baseDirFile.exists() || !baseDirFile.isDirectory) {
                println("错误: 目录 $baseDir 不存在或不是一个目录")
                return false
            }

            // 检查是否至少有一个文件存在
            val existingFiles = filesToCheck.count { fileName ->
                File(baseDir, fileName).exists()
            }

            // 如果存在的文件数量大于0（即至少有一个文件存在），返回true
            return existingFiles > 0

        } catch (e: SecurityException) {
            println("安全错误: 没有权限访问文件")
            return false
        } catch (e: Exception) {
            println("发生错误: ${e.message}")
            return false
        }
    }
}