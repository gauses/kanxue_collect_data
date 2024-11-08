package com.nest.kanxue

object NativeLib {

    init {
        System.loadLibrary("getdents64-lib")
    }

    // 调用 getdents64 的 JNI 方法
    external fun getDents64(fd: Int, bufferSize: Int): ByteArray?
}