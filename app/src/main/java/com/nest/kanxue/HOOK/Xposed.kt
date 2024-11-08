package com.nest.kanxue.HOOK

import android.util.Log
import com.nest.kanxue.Stat_File_Utils
import com.nest.kanxue.Utils
import com.nest.kanxue.convertToJSONObject
import org.json.JSONObject


//2、检测Xposed特征：
//方法1：检测下列文件或文件路径是否存在：

object Xposed {

    var xposed_file_path = arrayOf(
        "/sbin/.magisk/modules/riru_lsposed",
        "/data/adb/lspd",
        "/sbin/.magisk/modules/zygisk_lsposed",
        "/sbin/.magisk/modules/riru_edxposed",
        "/data/misc/riru/modules/edxp",
        "/data/adb/riru/modules/edxp.prop",
        "/sbin/.magisk/modules/taichi",
        "/data/misc/taichi",
        "/sbin/.magisk/modules/dreamland",
        "/data/misc/riru/modules/dreamland",
        "/data/adb/riru/modules/dreamland",
        "/system/bin/app_process.orig",
        "/system/xposed.prop",
        "/system/framework/XposedBridge.jar",
        "/system/lib/",
        "libxposed_art.so",
        "/system/lib/",
        "libxposed_art.so",
        ".no_orig",
        "/system/lib64/",
        "libxposed_art.so",
        "/system/lib64/",
        "libxposed_art.so",
        ".no_orig",
        "/system/bin/app_process_zposed",
        "/system/framework/ZposedBridge.jar",
        "/system/lib/",
        "libzposed_art.so"
    )

    fun check_xposed_file_path() {
        xposed_file_path.forEach { fileName ->
            val value = Utils.checkFileExists(fileName)
            if (value) {
                Log.d("sb", "xposed_file_path fileName = " + fileName + "exists")
            }
        }
    }


}