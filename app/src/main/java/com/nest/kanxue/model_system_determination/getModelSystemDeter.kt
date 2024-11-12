package com.nest.kanxue.model_system_determination

import android.content.Context
import org.json.JSONObject

object getModelSystemDeter {

    fun getInfo(context: Context) : JSONObject{
        val modelSystemDeterJSON = JSONObject()
        modelSystemDeterJSON.put("isLineageOS" , CheckLineageOS.isLineageOS())
        modelSystemDeterJSON.put("isOpenHarmony" , CheckOpenHarmony.isOpenHarmony())
        modelSystemDeterJSON.put("系统和设备品牌匹配" , CheckBrandOS.isBrandAndSystemMatched())
        modelSystemDeterJSON.put("是否插入SIM卡" , CheckSIM.isSimCardPresent())
        modelSystemDeterJSON.put("SIM卡运营商" , ""+CheckSIM.getSimOperator(context))

        //在 Android 10 及以上系统中，通过普通应用获取 simSerialNumber 受到限制。
//        modelSystemDeterJSON.put("SIM卡ICCID" , ""+CheckSIM.getSimICCID(context))

        return modelSystemDeterJSON



    }
}