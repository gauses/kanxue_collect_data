package com.nest.kanxue.devicefingerprint

object DrmIdFetcher {
    init {
        System.loadLibrary("ndk_kanxue")
    }

    // 声明一个本地方法，以便从 C++ 层获取 DRM ID
    external fun getDrmId(): ByteArray?
}