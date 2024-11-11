package com.nest.kanxue.deviceidentification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

object getDeviceIdentifiers {

    suspend fun getAdvertisingId(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                adInfo.id
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun generateDeviceUUID(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return UUID.nameUUIDFromBytes(androidId.toByteArray()).toString()
    }

    fun getIMEI(context: Context): String? {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // 检查权限
        return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.deviceId  // Android 8.0 以下使用此方法
            // 对于 Android 8.0 及更高版本
            telephonyManager.imei
        } else {
            null  // 权限未被授予时返回 null
        }
    }

    fun getDeviceSerial(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Build.getSerial()  // Android 8.0 及以上版本
            } catch (e: SecurityException) {
                null  // 权限不足时返回 null
            }
        } else {
            Build.SERIAL  // Android 8.0 以下版本
        }
    }



    fun getInfo(context: Context): JSONObject{
        val DeviceIdentifiersJSON = JSONObject()

        DeviceIdentifiersJSON.put("uuid", generateDeviceUUID(context))
        GlobalScope.launch {
            val adId = getAdvertisingId(context)
            println("Advertising ID: $adId")
            DeviceIdentifiersJSON.put("ad_aaid", adId)
        }

        DeviceIdentifiersJSON.put("imei", getIMEI(context))
        DeviceIdentifiersJSON.put("mdm_uuid", "未知")
        DeviceIdentifiersJSON.put("ps_imei", "未知")
        DeviceIdentifiersJSON.put("op_security_uuid", "未知")
        DeviceIdentifiersJSON.put("ai_stored_imei", "未知")
        DeviceIdentifiersJSON.put("device_serial", getDeviceSerial())

        val DeviceAllInfoSON = JSONObject();
        DeviceAllInfoSON.put("name", "Identifiers") ;
        DeviceAllInfoSON.put("data", Base64.encodeToString(DeviceIdentifiersJSON.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))


        return DeviceAllInfoSON


    }
}