package com.nest.kanxue.devicefingerprint

import android.media.MediaDrm
import android.util.Base64
import java.util.UUID

object getDrmId {

    fun retrieveDrmId() {
        val drmId: ByteArray? = DrmIdFetcher.getDrmId()

//        println("DRM ID: " + Base64.encodeToString(drmId, Base64.DEFAULT))


        if (drmId != null) {
            val drmIdString = drmId.joinToString("") { "%02x".format(it) }  // 转换为十六进制字符串
            println("DRM ID: $drmIdString")
        } else {
            println("Failed to retrieve DRM ID.")
        }
    }

    // println("DRMID111: " + Base64.encodeToString(getDrmId.getDrmId(), Base64.DEFAULT))
    fun getDrmId(): ByteArray? {
        return try {
            // 使用 Widevine 的 UUID 作为 DRM 方案（适用于大多数 Android 设备）
            val widevineUUID = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")

            // 创建 MediaDrm 实例
            val mediaDrm = MediaDrm(widevineUUID)

            // 获取 DRM 设备唯一 ID
            mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)



        } catch (e: Exception) {
            // 处理异常，通常发生在设备不支持指定的 DRM 方案时
            e.printStackTrace()
            null
        }
    }
}