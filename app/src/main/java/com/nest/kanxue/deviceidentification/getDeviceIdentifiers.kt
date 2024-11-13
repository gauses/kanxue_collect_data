package com.nest.kanxue.deviceidentification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Base64
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID
import kotlin.system.measureTimeMillis

object getDeviceIdentifiers {

    fun fetchAdIdWithLatency(context: Context, callback: (String?, Long, String?,) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            var adId: String? = null
            var error = ""
            val latency = measureTimeMillis {
                try {
                    val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
                    adId = info.id
                } catch (e: Exception) {
                    error = e.message.toString()
                    e.printStackTrace()
                }
            }
            withContext(Dispatchers.Main) {
                callback(adId, latency , error)
            }
        }
    }

//    suspend fun getAdvertisingId(context: Context): String? {
//        return withContext(Dispatchers.IO) {
//            try {
//                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
//                adInfo.id
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
//    }

    fun generateDeviceUUID(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return UUID.nameUUIDFromBytes(androidId.toByteArray()).toString()
    }

    fun get_ANDROID_ID(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId
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

        DeviceIdentifiersJSON.put("uuid(ANDROID_ID生成)", generateDeviceUUID(context))
        DeviceIdentifiersJSON.put("ANDROID_ID", get_ANDROID_ID(context))

//        GlobalScope.launch {
//            val adId = getAdvertisingId(context)
//            println("Advertising ID: $adId")
//            DeviceIdentifiersJSON.put("ad_aaid", adId)
//        }
        fetchAdIdWithLatency(context) { adId, latency, error ->
            println("Ad ID: $adId")
            println("Fetch Ad ID Latency: ${latency}ms")

            DeviceIdentifiersJSON.put("advertiserId", adId)
            DeviceIdentifiersJSON.put("advertiserId_FetchTime" , latency) //应该是获取google ad的请求时间
            DeviceIdentifiersJSON.put("gaidError", error)

        }

        if(!TextUtils.isEmpty(getIMEI(context))){
            DeviceIdentifiersJSON.put("imei", ""+getIMEI(context))
        }else{
            DeviceIdentifiersJSON.put("imei", "高版本Android基本获取不到")
        }

        DeviceIdentifiersJSON.put("mdm_uuid", "不知道是什么")
        DeviceIdentifiersJSON.put("ps_imei", "不知道是什么")
        DeviceIdentifiersJSON.put("op_security_uuid", "不知道是什么")
        DeviceIdentifiersJSON.put("ai_stored_imei", "不知道是什么")

//        https://developer.android.com/reference/android/os/Build.html#getSerial()
        DeviceIdentifiersJSON.put("device_serial(Android10以后默认是Unknown)）", getDeviceSerial())

        val DeviceAllInfoSON = JSONObject();
//        DeviceAllInfoSON.put("name", "Identifiers") ;
        DeviceAllInfoSON.put("name", "设备基本标识") ;
        DeviceAllInfoSON.put("data", Base64.encodeToString(DeviceIdentifiersJSON.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))


        return DeviceAllInfoSON


    }
}