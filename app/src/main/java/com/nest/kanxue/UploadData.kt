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
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
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

    // 只用tar.gz压缩，保留软链
    fun createTarGzWithSymlinks(sourceDir: File, tarGzFile: File) {
        TarArchiveOutputStream(GzipCompressorOutputStream(FileOutputStream(tarGzFile))).use { tarOut ->
            addFileToTar(tarOut, sourceDir, sourceDir.parentFile!!.absolutePath)
        }
    }

    // 递归添加文件和目录，保留软链
    private fun addFileToTar(tarOut: TarArchiveOutputStream, file: File, basePath: String) {
        val entryName = file.absolutePath.substring(basePath.length + 1)
        if (Files.isSymbolicLink(file.toPath())) {
            val linkTarget = Files.readSymbolicLink(file.toPath()).toString()
            val entry = TarArchiveEntry(entryName, TarArchiveEntry.LF_SYMLINK)
            entry.linkName = linkTarget
            tarOut.putArchiveEntry(entry)
            tarOut.closeArchiveEntry()
        } else if (file.isDirectory) {
            val entry = TarArchiveEntry(file, entryName + "/")
            tarOut.putArchiveEntry(entry)
            tarOut.closeArchiveEntry()
            file.listFiles()?.forEach { child ->
                addFileToTar(tarOut, child, basePath)
            }
        } else if (file.isFile) {
            val entry = TarArchiveEntry(file, entryName)
            tarOut.putArchiveEntry(entry)
            file.inputStream().use { it.copyTo(tarOut) }
            tarOut.closeArchiveEntry()
        }
    }

    fun zipDirectoryWithExtraFiles(sourceDir: String, outputFile: String, extraFiles: List<File>) {
        val sourceFile = File(sourceDir)
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
            if (sourceFile.exists()) {
                sourceFile.walkTopDown().forEach { file ->
                    // 跳过cpu目录本身和zip文件本身
                    if (file == File(sourceFile, "cpu") || file.absolutePath == outputFile) return@forEach
                    // 跳过cpu目录下的所有内容（只保留cpu.tar.gz）
                    if (file.toPath().startsWith(File(sourceFile, "cpu").toPath())) return@forEach
                    // 跳过cpu.tar.gz本身，避免重复
                    if (file.name == "cpu.tar.gz") return@forEach
                    // 排除zip文件
                    if (!file.isDirectory && !file.name.endsWith(".zip") && !file.name.toUpperCase().contains("PROFILEINSTALL")) {
                        val entryPath = file.absolutePath.substring(sourceFile.absolutePath.length + 1)
                        val entry = ZipEntry(entryPath)
                        zipOut.putNextEntry(entry)
                        FileInputStream(file).use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
                // 额外加入cpu.tar.gz
                extraFiles.forEach { file ->
                    if (file.exists()) {
                        val entry = ZipEntry(file.name)
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
            printAllFiles(externalDir)
            Log.d("FileList", "开始压缩cpu目录....")

            // 1. 先压缩cpu目录为tar.gz
            val cpuDir = File(externalDir, "cpu")
            val cpuTarGzFile = File(externalDir, "cpu.tar.gz")
            createTarGzWithSymlinks(cpuDir, cpuTarGzFile)

            Log.d("FileList", "开始整体打包zip....")
            // 2. 再整体打包zip（包含cpu.tar.gz）
            val zipFilePath = "$externalDir/$allDataFileNameSuffix.zip"
            zipDirectoryWithExtraFiles(externalDir, zipFilePath, listOf(cpuTarGzFile))

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
                            // 上传完成后删除临时文件
                            zipFile.delete()
                            cpuTarGzFile.delete()
                            onSuccess()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("sb", "Upload failed with response: $errorBody")
                            zipFile.delete()
                            cpuTarGzFile.delete()
                            onFailure("上传失败: 服务器返回错误 ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("sb", "Error processing response: ${e.message}")
                        zipFile.delete()
                        cpuTarGzFile.delete()
                        onFailure("处理响应时发生错误: ${e.message}")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("sb", "File upload error = ${t.message}")
                    zipFile.delete()
                    cpuTarGzFile.delete()
                    onFailure("上传失败: ${t.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("sb", "Error during zip and upload: ${e.message}")
            onFailure("压缩或上传过程中发生错误: ${e.message}")
        }
    }
}