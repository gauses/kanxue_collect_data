package com.nest.kanxue

import android.util.Log
import com.google.gson.Gson
import com.nest.kanxue.devicefingerprint.DrmIdFetcher
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.regex.Pattern

//通过stat获取文件的最近访问时间、最近修改时间、最近改变时间，Innode编号
//这个也有很多大厂用了，主要针对以下一些文件和文件目录，读取文件结构体，然后上传修改时间等信息。

data class FileStat(
    val fileName: String,

    val accessTime: String?,
    val modifyTime: String?,
    val changeTime: String?,

    val inode: String?,
    val Blocks:  String?,
    val IOBlocks:  String?,

    val Device: String?,
    val Links: String?,
    val DeviceType: String?,
    val Uid: String?,
    val Gid: String?,
    val secTime: JSONObject?,
    val exist: Boolean,

    val Size:  String?,
    val error: String?

    )
fun convertToJSONObject(user: FileStat): JSONObject {
    return JSONObject().apply {
        put("fileName", user.fileName)
        put("accessTime", user.accessTime)
        put("modifyTime", user.modifyTime)
        put("changeTime", user.changeTime)

//        put("exist", user.exist)

        put("inode", user.inode)
        put("Blocks", user.Blocks)
        put("IOBlocks", user.IOBlocks)

        put("Device", user.Device)
        put("Links", user.Links)
        put("DeviceType", user.DeviceType)
        put("Uid", user.Uid)
        put("Gid", user.Gid)
        put("secTime", user.secTime)
        put("Size", user.Size)

        put("errorMsg", user.error)


    }
}

object Stat_File_Utils {

    fun getFileStat(filePath: String): FileStat {
        var fileName: String = ""
        var accessTime: String? = null
        var modifyTime: String? = null
        var changeTime: String? = null
        var inode: String? = null
        var Blocks: String? = null
        var IOBlocks: String? = null
        var Device: String? = null
        var Links: String? = null
        var DeviceType: String? = null
        var Uid: String? = null
        var Gid: String? = null
        var secTime: JSONObject? = null
        var Size: String? = ""
        var errorMSG: String? = ""




        try {


            val process = Runtime.getRuntime().exec("stat $filePath")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            val result = StringBuilder()
            Log.d("sb" , "confirmButton result = ${reader.lines()}")



            // 读取命令输出的每一行，匹配并提取字段
            while (reader.readLine().also { line = it } != null) {
                result.append(line).append("\n")
                // 输出命令结果
                Log.d("sb" , "confirmButton result = ${result.toString()}")


                line?.let {
                    when {

                        it.startsWith("Access:") && !it.contains("Birth") -> {
                            accessTime = it.split("Access:")[1].trim()
                        }

                        it.startsWith("Modify:") -> {
                            modifyTime = it.split("Modify:")[1].trim()
                        }

                        it.startsWith("Change:") -> {
                            changeTime = it.split("Change:")[1].trim()
                        }
                    }
                }
                //解析Inode
                inode = parseInode(result.toString())
                Blocks = parseBlocks(result.toString())
                IOBlocks = parseIOBlocks(result.toString())

                Device = parseDevice(result.toString())
                Links = parseLinks(result.toString())
                DeviceType = parseDeviceType(result.toString())
                Uid = parseUid(result.toString())
                Gid = parseGid(result.toString())
                secTime = JSONObject(DrmIdFetcher.getFileStat(filePath))
                fileName = parseFile(result.toString())
                Size = parseSize(result.toString())



            }


            // 检查进程执行结果
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                // 读取错误流
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                val errorMessage = errorReader.readText()
                fileName = filePath
                errorMSG = "命令执行失败，退出码: $exitCode, 错误信息: $errorMessage"
            }

        } catch (e: Exception) {
            errorMSG = e.message ?: "Unknown error"
            Log.e("FileStatError", """
            错误类型: ${e.javaClass.simpleName}
            错误信息: ${e.message}
            文件路径: $filePath
            堆栈信息: ${Log.getStackTraceString(e)}
        """.trimIndent())

        }


        // 检查是否所有字段都成功获取，若有任何一个字段为 null，则 exist 设为 false
        val exist = accessTime != null && modifyTime != null && changeTime != null && inode != null
        return FileStat(fileName, accessTime, modifyTime, changeTime, inode, Blocks, IOBlocks,
            Device, Links, DeviceType, Uid, Gid ,secTime, exist, Size, errorMSG)
    }


    fun parseInode(statOutput: String): String? {
        // 使用正则表达式来匹配 Inode 字段
        val pattern = Pattern.compile("Inode:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseBlocks(statOutput: String): String {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Blocks:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1).trim() else ""
    }

    fun parseIOBlocks(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("IO Blocks:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseDevice(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Device:\\s*([^\\s]+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseLinks(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Links:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseDeviceType(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Device type:\\s*(\\d+,\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseUid(statOutput: String): String? {
        // 使用正则表达式来匹配 Uid 字段
        val pattern = Pattern.compile("Uid:\\s*\\(\\s*(\\d+/\\s*\\w+)\\)")
        // 解析:
        // Uid: - 匹配 Uid: 字面值
        // \\s* - 匹配任意空白字符
        // \\( - 匹配左括号
        // (\\d+/\\s*\\w+) - 捕获组：匹配数字+斜杠+空白字符+文字
        // \\) - 匹配右括号
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseGid(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Gid:\\s*\\(\\s*(\\d+/\\s*\\w+)\\)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseFile(statOutput: String): String {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("File:\\s*([^\\n]+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1).trim() else ""
    }

    fun parseSize(statOutput: String): String {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Size:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

}