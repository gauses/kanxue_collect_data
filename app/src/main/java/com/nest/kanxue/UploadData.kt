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
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

object UploadData {
    private const val MAX_FILE_SIZE = 100 * 1024 * 1024 // 100MB
    private val isUploading = AtomicBoolean(false)

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
        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            throw IOException("Source directory does not exist or is not a directory")
        }

        TarArchiveOutputStream(GzipCompressorOutputStream(FileOutputStream(tarGzFile))).use { tarOut ->
            addFileToTar(tarOut, sourceDir, sourceDir.parentFile!!.absolutePath)
        }
    }

    // 递归添加文件和目录，保留软链
    private fun addFileToTar(tarOut: TarArchiveOutputStream, file: File, basePath: String) {
        val entryName = file.absolutePath.substring(basePath.length + 1)
        
        if (file.length() > MAX_FILE_SIZE) {
            Log.w("FileList", "文件过大，跳过: ${file.absolutePath}")
            return
        }

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
        if (!sourceFile.exists() || !sourceFile.isDirectory) {
            throw IOException("Source directory does not exist or is not a directory")
        }

        ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
            if (sourceFile.exists()) {
                sourceFile.walkTopDown().forEach { file ->
                    try {
                        // 跳过cpu目录本身和zip文件本身
                        if (file == File(sourceFile, "cpu") || file.absolutePath == outputFile) return@forEach
                        // 跳过cpu目录下的所有内容（只保留cpu.tar.gz）
                        if (file.toPath().startsWith(File(sourceFile, "cpu").toPath())) return@forEach
                        // 跳过cpu.tar.gz本身，避免重复
                        if (file.name == "cpu.tar.gz") return@forEach
                        // 排除zip文件
                        if (!file.isDirectory && !file.name.endsWith(".zip") && !file.name.toUpperCase().contains("PROFILEINSTALL")) {
                            if (file.length() > MAX_FILE_SIZE) {
                                Log.w("FileList", "文件过大，跳过: ${file.absolutePath}")
                                return@forEach
                            }
                            val entryPath = file.absolutePath.substring(sourceFile.absolutePath.length + 1)
                            val entry = ZipEntry(entryPath)
                            zipOut.putNextEntry(entry)
                            FileInputStream(file).use { input ->
                                input.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                        }
                    } catch (e: Exception) {
                        Log.e("FileList", "处理文件时出错: ${file.absolutePath}, 错误: ${e.message}")
                    }
                }
                // 额外加入cpu.tar.gz
                extraFiles.forEach { file ->
                    try {
                        if (file.exists()) {
                            if (file.length() > MAX_FILE_SIZE) {
                                Log.w("FileList", "文件过大，跳过: ${file.absolutePath}")
                                return@forEach
                            }
                            val entry = ZipEntry(file.name)
                            zipOut.putNextEntry(entry)
                            FileInputStream(file).use { input ->
                                input.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                        }
                    } catch (e: Exception) {
                        Log.e("FileList", "处理额外文件时出错: ${file.absolutePath}, 错误: ${e.message}")
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

        if (!isUploading.compareAndSet(false, true)) {
            onFailure("已有上传任务正在进行中")
            return
        }

        var zipFile: File? = null
        var cpuTarGzFile: File? = null

        try {
            Log.d("FileList", "开始遍历文件....")
            printAllFiles(externalDir)
            Log.d("FileList", "开始压缩cpu目录....")

            // 1. 先压缩cpu目录为tar.gz
            val cpuDir = File(externalDir, "cpu")
            cpuTarGzFile = File(externalDir, "cpu.tar.gz")
            createTarGzWithSymlinks(cpuDir, cpuTarGzFile)

            Log.d("FileList", "开始整体打包zip....")
            // 2. 再整体打包zip（包含cpu.tar.gz）
            val zipFilePath = "$externalDir/$allDataFileNameSuffix.zip"
            zipFile = File(zipFilePath)
            zipDirectoryWithExtraFiles(externalDir, zipFilePath, listOf(cpuTarGzFile))

            val apiService = RetrofitClient.create()
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
                            cleanupFiles(zipFile, cpuTarGzFile)
                            onSuccess()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("sb", "Upload failed with response: $errorBody")
                            cleanupFiles(zipFile, cpuTarGzFile)
                            onFailure("上传失败: 服务器返回错误 ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("sb", "Error processing response: ${e.message}")
                        cleanupFiles(zipFile, cpuTarGzFile)
                        onFailure("处理响应时发生错误: ${e.message}")
                    } finally {
                        isUploading.set(false)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("sb", "File upload error = ${t.message}")
                    cleanupFiles(zipFile, cpuTarGzFile)
                    onFailure("上传失败: ${t.message}")
                    isUploading.set(false)
                }
            })
        } catch (e: Exception) {
            Log.e("sb", "Error during zip and upload: ${e.message}")
            cleanupFiles(zipFile, cpuTarGzFile)
            onFailure("压缩或上传过程中发生错误: ${e.message}")
            isUploading.set(false)
        }
    }

    private fun cleanupFiles(zipFile: File?, cpuTarGzFile: File?) {
        try {
            zipFile?.delete()
            cpuTarGzFile?.delete()
        } catch (e: Exception) {
            Log.e("sb", "Error cleaning up files: ${e.message}")
        }
    }
}