//package com.example
//
//class StorageInfo {
//
//    // 确保这个方法签名与JNI函数完全匹配
//    external fun getStorageInfo(path: String): StorageStats?
//
//    fun getAllStorageInfo(): Map<String, StorageStats?> {
//        val paths = listOf(
//            "/data",
//            "/odm",
//            "/odm_dlkm",
//            "/product",
//            "/system",
//            "/system_ext",
//            "/vendor",
//            "/vendor_dlkm"
//        )
//
//        return paths.associateWith { path ->
//            try {
//                getStorageInfo(path)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
//    }
//}