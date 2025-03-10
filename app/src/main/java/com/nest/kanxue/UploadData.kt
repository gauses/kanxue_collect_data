package com.nest.kanxue

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.nest.kanxue.http.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object UploadData {

    fun printAllFiles(directory: String) {

        Log.d("FileList", "目录文件遍历开始")


        val dir = File(directory)
        if (dir.exists() && dir.isDirectory) {
            Log.d("FileList", "开始遍历目录: $directory")
            dir.walkTopDown().forEach { file ->
                if (!file.isDirectory) {
                    Log.d("FileList", "文件: ${file.absolutePath}")
                }
            }
            Log.d("FileList", "目录遍历完成")
        } else {
            Log.e("FileList", "目录不存在或不是一个有效的目录: $directory")
        }
    }

    private fun zipDirectory(sourceDir: String, outputFile: String) {
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
            val sourceFile = File(sourceDir)
            if (sourceFile.exists()) {
                sourceFile.walkTopDown().forEach { file ->
                    // 排除zip文件
                    // 排除profileInstalled文件
                    if (!file.isDirectory && !file.name.endsWith(".zip") && !file.name.toUpperCase().contains("profileinstall")) {
                        val entryPath = file.absolutePath.substring(sourceFile.absolutePath.length + 1)
                        val entry = ZipEntry(entryPath)
                        zipOut.putNextEntry(entry)
                        
                        FileInputStream(file).use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }

    fun upload(context: Context, externalDir: String, allDataFileNameSuffix: String, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        if (externalDir.isEmpty()) {
            Toast.makeText(context, "externalDir is empty", Toast.LENGTH_SHORT).show()
            onFailure("目录路径为空")
            return
        }

        try {
            Log.d("FileList", "开始遍历文件....")


            // 先遍历并打印所有文件
            printAllFiles(externalDir)
            Log.d("FileList", "开始压缩文件....")

            // 创建zip文件的完整路径
            val zipFilePath = "$externalDir/$allDataFileNameSuffix.zip"
            
            // 压缩文件夹
            zipDirectory(externalDir, zipFilePath)
            
            val apiService = RetrofitClient.create()
            val zipFile = File(zipFilePath)

            val requestFile = zipFile.asRequestBody("application/zip".toMediaType())
            val filePart = MultipartBody.Part.createFormData("file", zipFile.name, requestFile)
            
            val call = apiService.uploadFile(filePart)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    try {
                        Log.d("sb", "File uploaded response = " + response.code())
                        Log.d("sb", "File uploaded response = " + response.message())

                        if (response.isSuccessful) {
                            Log.d("sb", "File uploaded successfully")
                            // 上传完成后删除临时zip文件
                            zipFile.delete()
                            // 调用成功回调
                            onSuccess()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("sb", "Upload failed with response: $errorBody")
                            // 发生错误时也删除临时zip文件
                            zipFile.delete()
                            // 调用失败回调，传入错误信息
                            onFailure("上传失败: 服务器返回错误 ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("sb", "Error processing response: ${e.message}")
                        // 发生错误时也删除临时zip文件
                        zipFile.delete()
                        // 调用失败回调，传入错误信息
                        onFailure("处理响应时发生错误: ${e.message}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("sb", "File upload error = ${t.message}")
                    // 发生错误时也删除临时zip文件
                    zipFile.delete()
                    // 调用失败回调，传入错误信息
                    onFailure("上传失败: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("sb", "Error during zip and upload: ${e.message}")
            Toast.makeText(context, "压缩或上传过程中发生错误", Toast.LENGTH_SHORT).show()
            // 发生异常时调用失败回调，传入错误信息
            onFailure("压缩或上传过程中发生错误: ${e.message}")
        }
    }
}