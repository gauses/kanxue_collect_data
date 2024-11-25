package com.nest.kanxue.bootid

import android.util.Log
import com.nest.kanxue.devicefingerprint.DrmIdFetcher
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

//boot id
//cat命令读取/proc/sys/kernel/random/boot_id，这个也是大厂必用的一个关键的设备指纹字段。


object getBootId {


    fun getFileContentUsingFile(path: String): String {
        return try {
            File(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "${e.message}  "
        }
    }




    fun getARPUsingFile(): String {
        return try {
            File("/proc/fs/ext4").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "${e.message}  "
        }
    }





    fun getCgroupUsingFile(): String {
        return try {
            File("/proc/self/cgroup").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "${e.message}  "
        }
    }


    fun getUnameUsingCat(): String {
        var result = ""
        try {
            val process = Runtime.getRuntime().exec("uname -a")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val unameOutput = reader.readText()
            if (unameOutput.isNotEmpty()) {
                result =  unameOutput
            }
        } catch (e: Exception) {
            "${e.message}  "
        }

        return result

    }




    fun getMeminfoUsingCat(): String {
        return try {
            val process = ProcessBuilder("cat", "/proc/meminfo")
                .redirectErrorStream(true)
                .start()

            val result = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            result.trim().takeIf { it.isNotEmpty() } ?: "unknown"
        } catch (e: Exception) {
            "${e.message}  "
        }
    }


     fun getMountsUsingCat(): String? {
        return try {
            val process = ProcessBuilder("cat", "/proc/self/mounts")
                .redirectErrorStream(true)
                .start()

            val result = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            result.trim().takeIf { it.isNotEmpty() } ?: "unknown"
        } catch (e: Exception) {
            "${e.message}  "
        }
    }


    private fun getBootIdUsingCat(): String? {
        return try {
            val process = ProcessBuilder("cat", "/proc/sys/kernel/random/boot_id").start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val bootId = reader.readLine().trim()
            reader.close()
            process.destroy()
            bootId
        } catch (e: Exception) {
            "${e.message}  "
        }
    }


    fun getInfo(): JSONObject{
        var jsonObject = JSONObject()
        jsonObject.put("/proc/sys/kernel/random/boot_id", getBootIdUsingCat())
        jsonObject.put("/proc/self/mounts", getMountsUsingCat())
        jsonObject.put("/proc/meminfo", getMeminfoUsingCat())
        jsonObject.put("uname -a", getUnameUsingCat())
        jsonObject.put("SystemInfo", DrmIdFetcher.getSystemInfo())
        jsonObject.put("Uname", DrmIdFetcher.getUname())
        jsonObject.put("/proc/self/cgroup", getCgroupUsingFile())
        jsonObject.put("/proc/self/net/arp", getCgroupUsingFile())

        return jsonObject

    }
}