package com.nest.kanxue.root

//3.检查magisk相关文件是否存在

object CheckMagiskFile {

    private val MAGISK_PATHS = listOf(
        "/cache/.disable_magisk",
        "/dev/magisk/img",
        "/sbin/.magisk",
        "/cache/magisk.log",
        "/data/adb/magisk",
        "/system/etc/init/magisk",
        "/system/etc/init/magisk.rc",
        "/data/magisk.apk"
    )

    /**
     * 检查所有可能的Magisk文件路径
     * @return 返回找到的Magisk文件路径列表
     */
    fun checkMagiskFiles(): List<String> {
        return MAGISK_PATHS.filter { path ->
            val file = java.io.File(path)
            file.exists()
        }
    }

    /**
     * 检查设备是否安装了Magisk
     * @return 如果发现任何Magisk相关文件返回true
     */
    fun hasMagiskInstalled(): Boolean {
        return checkMagiskFiles().isNotEmpty()
    }


}