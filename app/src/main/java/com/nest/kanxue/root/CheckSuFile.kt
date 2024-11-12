package com.nest.kanxue.root

//2.检查su文件是否存在

object CheckSuFile {

    private val SU_PATHS = listOf(
        "/su/bin/su",
        "/sbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/system/xbin/su",
        "/system/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/system/bin/.ext/.su",
        "/system/etc/.installed_su_daemon",
        "/system/etc/.has_su_daemon",
        "/system/xbin/sugote",
        "/system/xbin/sugote-mksh",
        "/system/xbin/supolicy",
        "/system/etc/init.d/99SuperSUDaemon",
        "/system/.supersu",
        "/product/bin/su",
        "/apex/com.android.runtime/bin/su",
        "/apex/com.android.art/bin/su",
        "/system_ext/bin/su",
        "/system/xbin/bstk/su",
        "/system/app/SuperUser/SuperUser.apk",
        "/system/app/Superuser.apk",
        "/system/xbin/mu_bak",
        "/odm/bin/su",
        "/vendor/bin/su",
        "/vendor/xbin/su",
        "/system/bin/.ext/su",
        "/system/usr/we-need-root/su",
        "/cache/su",
        "/data/su",
        "/dev/su",
        "/system/bin/cph_su",
        "/dev/com.koushikdutta.superuser.daemon",
        "/system/xbin/daemonsu",
        "/sbin/.mianju",
        "/sbin/nvsu",
        "/system/bin/.hid/su",
        "/system/addon.d/99-magisk.sh"
    )


    /**
     * 检查所有可能的su文件路径
     * @return 返回找到的su文件路径列表
     */
    fun checkSuFiles(): List<String> {
        return SU_PATHS.filter { path ->
            val file = java.io.File(path)
            file.exists()
        }
    }






}