package com.nest.kanxue

import com.nest.kanxue.devicefingerprint.DrmIdFetcher
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

//通过stat获取文件的最近访问时间、最近修改时间、最近改变时间，Innode编号
//这个也有很多大厂用了，主要针对以下一些文件和文件目录，读取文件结构体，然后上传修改时间等信息。

data class FileStat(
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
    val secTime: String,
    val exist: Boolean
)
fun convertToJSONObject(user: FileStat): JSONObject {
    return JSONObject().apply {
        put("accessTime", user.accessTime)
        put("modifyTime", user.modifyTime)
        put("changeTime", user.changeTime)
        put("exist", user.exist)
        put("inode", user.inode)
        put("Blocks", user.Blocks)
        put("IOBlocks", user.IOBlocks)
        put("Device", user.Device)
        put("Links", user.Links)
        put("DeviceType", user.DeviceType)
        put("Uid", user.Uid)
        put("Gid", user.Gid)
        put("secTime", user.secTime)


    }
}

object Stat_File_Utils {

    fun getFileStat(filePath: String): FileStat {
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
        var secTime: String = ""


        try {
            val process = Runtime.getRuntime().exec("stat $filePath")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            val result = StringBuilder()

            // 读取命令输出的每一行，匹配并提取字段
            while (reader.readLine().also { line = it } != null) {
                result.append(line).append("\n")
                // 输出命令结果
                println(result.toString())

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
                secTime = DrmIdFetcher.getFileStat(filePath)


            }
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        // 检查是否所有字段都成功获取，若有任何一个字段为 null，则 exist 设为 false
        val exist = accessTime != null && modifyTime != null && changeTime != null && inode != null
        return FileStat(accessTime, modifyTime, changeTime, inode, Blocks, IOBlocks, Device, Links, DeviceType, Uid, Gid ,secTime, exist)
    }


    fun parseInode(statOutput: String): String? {
        // 使用正则表达式来匹配 Inode 字段
        val pattern = Pattern.compile("Inode:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseBlocks(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Blocks:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseIOBlocks(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("IO Blocks:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseDevice(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Device:\\s*(\\d+)")
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
        val pattern = Pattern.compile("DeviceType:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseUid(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Uid:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

    fun parseGid(statOutput: String): String? {
        // 使用正则表达式来匹配 Blocks 字段
        val pattern = Pattern.compile("Gid:\\s*(\\d+)")
        val matcher = pattern.matcher(statOutput)
        return if (matcher.find()) matcher.group(1) else ""
    }

}