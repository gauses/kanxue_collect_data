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
            result = "\nKernel Information: Unable to retrieve (requires root access)"
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
            e.printStackTrace()
            "unknown"
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
            e.printStackTrace()
            "unknown"
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
            e.printStackTrace()
            null
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
        return jsonObject

    }
}