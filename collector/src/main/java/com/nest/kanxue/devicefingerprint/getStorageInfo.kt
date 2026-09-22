package com.nest.kanxue.devicefingerprint

import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Base64
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject


object getStorageInfo {

    fun getTotalLong(path: String): Long {
        val stat = StatFs(path)

        var totalBytes: Long = 1L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            totalBytes = stat.blockSizeLong * stat.blockCountLong
        } else {
            totalBytes = (stat.blockSize * stat.blockCount).toLong()
        }

        return totalBytes
    }


    fun getInfo(path: String): Pair<Long, Long> {
        val stat = StatFs(path)

        val totalBytes: Long
        val availableBytes: Long

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            totalBytes = stat.blockSizeLong * stat.blockCountLong
            availableBytes = stat.blockSizeLong * stat.availableBlocksLong
        } else {
            totalBytes = stat.totalBytes
            availableBytes = stat.availableBytes
        }

        return Pair(totalBytes, availableBytes)
    }


    fun getstorage_emulated_0(): JSONObject{
        val storagePath = Environment.getExternalStorageDirectory().path
        val (totalSpace, availableSpace) = getInfo(storagePath)

        println("Total Space: ${totalSpace / (1024 * 1024)} MB")
        println("Available Space: ${availableSpace / (1024 * 1024)} MB")

        val storageInfoSON = JSONObject();
//        storageInfoSON.put("name", "storage_emulated_0_space") ;
        storageInfoSON.put("name", "SD卡/storage/emulated/0下可用空间大小和总空间大小") ;

        val storageInfoArray = JSONArray();
        storageInfoArray.put(JSONObject().put("TotalSpace", "${totalSpace / (1024 * 1024)} MB"))
        storageInfoArray.put(JSONObject().put("AvailableSpace", "${availableSpace / (1024 * 1024)} MB"))
        storageInfoSON.put("data", Base64.encodeToString(Gson().toJson(Gson().fromJson(storageInfoArray.toString(), List::class.java)).toByteArray(Charsets.UTF_8), Base64.DEFAULT))

        return storageInfoSON

    }



}
