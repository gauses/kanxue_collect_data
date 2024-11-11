package com.nest.kanxue.bootid

import java.io.BufferedReader
import java.io.InputStreamReader

//boot id
//cat命令读取/proc/sys/kernel/random/boot_id，这个也是大厂必用的一个关键的设备指纹字段。



object getBootId {

    fun getBootIdUsingCat(): String? {
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
}