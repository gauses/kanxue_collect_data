package com.nest.kanxue.devicefingerprint

object DrmIdFetcher {

    init {
        System.loadLibrary("ndk_kanxue")
    }

    // 声明一个本地方法，以便从 C++ 层获取 DRM ID
    external fun getDrmId(): String?

    // 声明一个本地方法，以便从 C++ 层获取
    external fun readCompatibleNative(): String
    // Kotlin包装方法
    fun readCompatible(): String {
        return try {
            readCompatibleNative()
        } catch (e: Exception) {
            "Error reading compatible: ${e.message}"
        }
    }

    external fun getSystemInfo(): String?
    external fun getUname(): String?

    external fun getFileStat(path: String): String




    external fun getCnameInfoHex(): String?

    external fun getBootTime(): LongArray?

    // 定义 native 方法
    external fun getStatFsInfo(path: String): String
    external fun getStatFsInfoParse(path: String)


    //获取Vulkan信息
    external fun getVulkanInfo():String


    // 声明本地方法
//    external fun getFileSystemStats(): Map<String, StatFs64>
}