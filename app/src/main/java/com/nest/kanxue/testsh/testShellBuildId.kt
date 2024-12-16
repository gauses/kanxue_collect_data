package com.nest.kanxue.testsh

//sh -c getprop ro.system.build.id
object testShellBuildId {


    fun getSystemBuildId(): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "getprop ro.system.build.id"))
            process.inputStream.bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }


}