//package com.nest.kanxue.data_misc_dg
//
//import android.content.Context
//import android.os.Environment
//import android.widget.Toast
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.io.IOException
//import java.util.zip.ZipEntry
//import java.util.zip.ZipOutputStream
//import java.io.BufferedReader
//import java.io.InputStreamReader
//
////获取data/misc/dg/下面的所有文件内容
//object GetDataMiscDg {
//
//    fun getallFiles(context: Context) {
//        try {
//            val dgDir = File("/data/misc/dg")
//            if (!dgDir.exists()) {
//                Toast.makeText(context, "目录/data/misc/dg不存在", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            // 创建Download目录
//            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            if (!downloadDir.exists()) {
//                if (!downloadDir.mkdirs()) {
//                    Toast.makeText(context, "无法创建Download目录", Toast.LENGTH_SHORT).show()
//                    return
//                }
//            }
//
//            // 使用tar命令打包文件
//            val tarFile = File(downloadDir, "dg_files.tar")
//            // 先切换到根目录，然后使用相对路径执行tar
//            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "cd / && tar -cvf ${tarFile.absolutePath} data/misc/dg"))
//
//            // 读取命令输出
//            val reader = BufferedReader(InputStreamReader(process.inputStream))
//            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
//            var fileCount = 0
//
//            // 读取标准输出（每一行代表一个文件）
//            var line: String?
//            while (reader.readLine().also { line = it } != null) {
//                fileCount++
//            }
//
//            // 读取错误输出
//            val errorBuilder = StringBuilder()
//            while (errorReader.readLine().also { line = it } != null) {
//                errorBuilder.append(line).append("\n")
//            }
//
//            // 等待命令执行完成
//            val exitCode = process.waitFor()
//
//            if (exitCode == 0) {
//                if (fileCount > 0) {
//                    // 压缩tar文件
//                    Runtime.getRuntime().exec(arrayOf("sh", "-c", "gzip ${tarFile.absolutePath}")).waitFor()
//                    val gzipFile = File("${tarFile.absolutePath}.gz")
//
//                    if (gzipFile.exists()) {
//                        Toast.makeText(context, "成功打包${fileCount}个文件到${gzipFile.absolutePath}", Toast.LENGTH_LONG).show()
//                    } else {
//                        Toast.makeText(context, "成功打包${fileCount}个文件到${tarFile.absolutePath}", Toast.LENGTH_LONG).show()
//                    }
//                } else {
//                    Toast.makeText(context, "目录为空，未打包任何文件", Toast.LENGTH_SHORT).show()
//                    tarFile.delete()
//                }
//            } else {
//                val errorMsg = errorBuilder.toString()
//                if (errorMsg.isNotEmpty()) {
//                    Toast.makeText(context, "打包失败: $errorMsg", Toast.LENGTH_LONG).show()
//                } else {
//                    Toast.makeText(context, "打包失败，退出码: $exitCode", Toast.LENGTH_LONG).show()
//                }
//                tarFile.delete()
//            }
//
//        } catch (e: SecurityException) {
//            Toast.makeText(context, "权限不足: ${e.message}", Toast.LENGTH_LONG).show()
//        } catch (e: Exception) {
//            Toast.makeText(context, "发生错误: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }
//
//}