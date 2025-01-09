package com.nest.kanxue.exec

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class InstrumentationUtil {

    /**
     * Get instrumentation list
     */
    fun getInstrumentationList(): String? {
        return try {
            val process = Runtime.getRuntime().exec("pm list instrumentation")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            process.waitFor()
            output
        } catch (e: Exception) {
            Log.e("InstrumentationUtil", "Error: ${e.message}")
            null
        }
    }
}