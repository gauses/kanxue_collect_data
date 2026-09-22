package com.nest.kanxue.modifymachine

import java.io.File

//2.检查/dev/wgzs目录下的内容是否存在，若存在则获取值：
//wg.cust.config.phone.id
//wg.cust.s_android_id
//wg.cust.sys.prop.filter
//wg.cust.config.phone.imeimackey
//wg.cust.config.phone.imei
//wg.cust.config.pkg.name
//wg.cust.destUids

object CheckDevWgzs {

    private const val WGZS_DIR = "/dev/wgzs"

    private val CONFIG_FILES = mapOf(
        "phone_id" to "wg.cust.config.phone.id",
        "android_id" to "wg.cust.s_android_id",
        "prop_filter" to "wg.cust.sys.prop.filter",
        "imei_mac_key" to "wg.cust.config.phone.imeimackey",
        "imei" to "wg.cust.config.phone.imei",
        "package_name" to "wg.cust.config.pkg.name",
        "dest_uids" to "wg.cust.destUids"
    )

    /**
     * 检查 WGZS 目录是否存在
     */
    fun isWGZSExists(): Boolean {
        return try {
            File(WGZS_DIR).exists()
        } catch (e: Exception) {
            false
        }
    }

}