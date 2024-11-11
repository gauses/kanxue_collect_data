package com.nest.kanxue.devicefingerprint

import android.util.Base64
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Method

//ro.build.version.release
//ro.product.model
//ro.product.brand
//ro.boot.bootloader
//ro.build.version.securitypatch
//ro.build.version.incremental
//gsm.version.baseband
//gsm.version.ril-impl
//ro.build.fingerprint
//ro.build.description
//ro.build.product
//ro.boot.vbmeta.digest
//ro.hardware
//ro.product.name
//ro.product.board
//ro.recovery_id
//ro.expect.recovery_id
//ro.board.platform
//ro.product.manufacturer
//ro.product.device
//sys.usb.state
//ro.setupwizard.mode
//ro.build.id
//ro.build.tags
//ro.build.type
//ro.debuggable
//persist.sys.meid
//vendor.serialno
//sys.serialno
//persist.sys.wififactorymac
//ro.boot.deviceid
//ro.rpmb.board
//ro.vold.serialno
//persist.oppo.wlan.macaddress
//persist.sys.oppo.serialno
//ril.serialnumber
//ro.boot.ap_serial
//ro.boot.uniqueno
//persist.sys.oppo.opmuuid
//persist.sys.oppo.nlp.id
//persist.sys.oplus.nlp.id
//persist.sys.dcs.hash
//ro.ril.oem.sno
//ro.ril.oem.psno
//persist.vendor.sys.fp.uid
//ro.ril.miui.imei0
//ro.ril.miui.imei1
//ro.ril.oem.imei
//ro.ril.oem.meid
//persist.radio.imei
//persist.radio.imei1
//persist.radio.imei2
//persist.sys.lite.uid
//persist.radio.serialno
//vendor.boot.serialno
//persist.sys.oneplus.serialno
//ro.meizu.hardware.imei1
//ro.meizu.hardware.imei2
//ro.meizu.hardware.meid
//ro.meizu.hardware.psn
//ro.meizu.hardware.sn
//persist.radio.factory_phone_sn
//persist.radio.factory_sn
//ro.meizu.serialno
//ro.boot.psn
//ro.boot.meid
//ro.boot.imei1
//ro.boot.imei2
//ro.wifimac
//ro.wifimac_2
//ro.vendor.deviceid
//ro.isn
//ro.vendor.isn
//persist.radio.device.imei
//persist.radio.device.imei2
//persist.radio.device.meid
//persist.radio.device.meid2
//persist.asus.serialno
//sys.wifimac
//sys.bt.address
//persist.btpw.bredr
//persist.radio.imei
//persist.radio.imei2
//persist.radio.meid
//persist.radio.meid2
//ro.boot.fpd.uid
//ro.vendor.boot.serialno
//ro.boot.wifimacaddr
//persist.sys.wifi.mac
//persist.sys.wifi_mac
//sys.prop.writeimei
//ril.gm.imei
//ril.cdma.meid
//ro.boot.em.did
//ro.qchip.serialno
//ro.ril.oem.btmac
//ro.ril.oem.ifimac

object getSystemProp {

    fun getSystemProperty(propName: String): String? {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod: Method = systemProperties.getMethod("get", String::class.java)
            getMethod.invoke(null, propName) as? String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 批量获取多个属性
    fun getSystemProperties(vararg propNames: String): Map<String, String?> {
        val properties = mutableMapOf<String, String?>()
        for (propName in propNames) {
            properties[propName] = getSystemProperty(propName)
        }
        return properties
    }


    val propertyNames = arrayOf(
        "ro.build.version.release",
        "ro.product.model",
        "ro.product.brand",
        "ro.boot.bootloader",
        "ro.build.version.security_patch",
        "ro.build.version.incremental",
        "gsm.version.baseband",
        "gsm.version.ril-impl",
        "ro.build.fingerprint",
        "ro.build.description",
        "ro.build.product",
        "ro.boot.vbmeta.digest",
        "ro.hardware",
        "ro.product.name",
        "ro.product.board",
        "ro.recovery_id",
        "ro.expect.recovery_id",
        "ro.board.platform",
        "ro.product.manufacturer",
        "ro.product.device",
        "sys.usb.state",
        "ro.setupwizard.mode",
        "ro.build.id",
        "ro.build.tags",
        "ro.build.type",
        "ro.debuggable",
        "persist.sys.meid",
        "vendor.serialno",
        "sys.serialno",
        "persist.sys.wififactorymac",
        "ro.boot.deviceid",
        "ro.rpmb.board",
        "ro.vold.serialno",
        "persist.oppo.wlan.macaddress",
        "persist.sys.oppo.serialno",
        "ril.serialnumber",
        "ro.boot.ap_serial",
        "ro.boot.uniqueno",
        "persist.sys.oppo.opmuuid",
        "persist.sys.oppo.nlp.id",
        "persist.sys.oplus.nlp.id",
        "persist.sys.dcs.hash",
        "ro.ril.oem.sno",
        "ro.ril.oem.psno",
        "persist.vendor.sys.fp.uid",
        "ro.ril.miui.imei0",
        "ro.ril.miui.imei1",
        "ro.ril.oem.imei",
        "ro.ril.oem.meid",
        "persist.radio.imei",
        "persist.radio.imei1",
        "persist.radio.imei2",
        "persist.sys.lite.uid",
        "persist.radio.serialno",
        "vendor.boot.serialno",
        "persist.sys.oneplus.serialno",
        "ro.meizu.hardware.imei1",
        "ro.meizu.hardware.imei2",
        "ro.meizu.hardware.meid",
        "ro.meizu.hardware.psn",
        "ro.meizu.hardware.sn",
        "persist.radio.factory_phone_sn",
        "persist.radio.factory_sn",
        "ro.meizu.serialno",
        "ro.boot.psn",
        "ro.boot.meid",
        "ro.boot.imei1",
        "ro.boot.imei2",
        "ro.wifimac",
        "ro.wifimac_2",
        "ro.vendor.deviceid",
        "ro.isn",
        "ro.vendor.isn",
        "persist.radio.device.imei",
        "persist.radio.device.imei2",
        "persist.radio.device.meid",
        "persist.radio.device.meid2",
        "persist.asus.serialno",
        "sys.wifimac",
        "sys.bt.address",
        "persist.btpw.bredr",
        "persist.radio.imei",
        "persist.radio.imei2",
        "persist.radio.meid",
        "persist.radio.meid2",
        "ro.boot.fpd.uid",
        "ro.vendor.boot.serialno",
        "ro.boot.wifimacaddr",
        "persist.sys.wifi.mac",
        "persist.sys.wifi_mac",
        "sys.prop.writeimei",
        "ril.gm.imei",
        "ril.cdma.meid",
        "ro.boot.em.did",
        "ro.qchip.serialno",
        "ro.ril.oem.btmac",
        "ro.ril.oem.ifimac"
    )




    fun getPropertyAllInfo(): JSONObject{

        println("getPropertyAllInfo start....")


        val systemProJSON = JSONObject()

        val properties = getSystemProperties(*propertyNames)
        // 输出获取的属性
        properties.forEach { (key, value) ->
            println("$key: $value")
            systemProJSON.put(key, value)
        }


        val systemAllInfoSON = JSONObject();
        systemAllInfoSON.put("name", "systemPro") ;
        systemAllInfoSON.put("data", Base64.encodeToString(systemProJSON.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT))

        return systemAllInfoSON
    }
}