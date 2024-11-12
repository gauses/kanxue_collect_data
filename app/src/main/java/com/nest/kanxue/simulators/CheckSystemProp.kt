package com.nest.kanxue.simulators
import java.io.File

//2、system.prop下是否存在以下字段特征：

object CheckSystemProp {

    fun getSystemProperty(propName: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            process.inputStream.bufferedReader().use { it.readLine() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun checkEmulatorPropsWithGetprop(): Map<String, String?> {
        // 模拟器特征字段列表
        val emulatorProps = listOf(
            "init.svc.microvirtd", "bst.version", "ro.phoenix.version.code", "ro.phoenix.version.codename",
            "init.svc.droid4x", "microvirt.memu_version", "microvirt.imsi", "microvirt.simserial", "ro.px.version.build",
            "ro.phoenix.os.branch", "init.svc.su_kpbs_daemon", "init.svc.noxd", "init.svc.ttVM_x86-setup", "init.svc.xxkmsg",
            "ro.bild.remixos.version", "microvirt.mut", "init.svc.ldinit", "sys.tencent.os_version", "sys.tencent.android_id",
            "ro.genymotion.version", "init.svc.pkVM_x86-setup", "ro.andy.version", "ro.build.version.release",
            "ro.product.model", "ro.product.brand", "ro.boot.bootloader", "ro.build.version.securitypatch",
            "ro.build.version.incremental", "gsm.version.baseband", "gsm.version.ril-impl", "ro.build.fingerprint",
            "ro.build.description", "ro.build.product", "ro.boot.vbmeta.digest", "ro.hardware", "ro.product.name",
            "ro.product.board", "ro.recovery_id", "ro.expect.recovery_id", "ro.board.platform", "ro.product.manufacturer",
            "ro.product.device", "sys.usb.state", "ro.setupwizard.mode", "ro.build.id", "ro.build.tags", "ro.build.type",
            "ro.debuggable"
        )
//        val detectedFiles = mutableListOf<String>()
        val detectedProps = mutableMapOf<String, String?>()
        emulatorProps.forEach { prop ->
            val value = getSystemProperty(prop)
            if (value != null && value.isNotEmpty()) {
                detectedProps[prop] = value
                println("检测到特征字段 $prop: $value")
//                detectedFiles.add(prop)

            }
        }
        return detectedProps
    }


}