#include <jni.h>
#include <sys/vfs.h>
#include <android/log.h>
#include <errno.h>
#include <string.h>

#define TAG "StatFS64_JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)


//使用JNI解析statfs64具体数据的函数
//2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D  Parsed struct statfs64 {
//    2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_type: 61267
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_bsize: 4096
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_blocks: 29048418
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_bfree: 24661509
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_bavail: 24360966
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_files: 7413760
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_ffree: 7336921
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_fsid: { val: [3190777050, 700628452] }
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_namelen: 255
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_frsize: 4096
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_flags: 1030
//            2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D      f_spare: [0, 0, 0]
//    2024-12-06 15:50:54.654 23858-23858 StatFS64_JNI            com.nest.kanxue_data                 D  }

extern "C"
JNIEXPORT void JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getStatFsInfoParse(JNIEnv* env, jobject, jstring path) {
    if (path == NULL) {
        LOGE("Path is null");
        return;
    }

    const char *nativePath = NULL;
    try {
        nativePath = env->GetStringUTFChars(path, 0);
        if (nativePath == NULL) {
            LOGE("Failed to get native path string");
            return;
        }

        struct statfs64 stats;
        memset(&stats, 0, sizeof(struct statfs64));  // 初始化结构体

        int result = statfs64(nativePath, &stats);
        if (result != 0) {
            LOGE("statfs64 failed for path %s: %s", nativePath, strerror(errno));
        } else {
            // 使用 %llu 来打印 unsigned long long 值
            LOGD("Parsed struct statfs64 {");
            LOGD("    f_type: %lu", (unsigned long)stats.f_type);
            LOGD("    f_bsize: %lu", (unsigned long)stats.f_bsize);
            LOGD("    f_blocks: %llu", (unsigned long long)stats.f_blocks);
            LOGD("    f_bfree: %llu", (unsigned long long)stats.f_bfree);
            LOGD("    f_bavail: %llu", (unsigned long long)stats.f_bavail);
            LOGD("    f_files: %llu", (unsigned long long)stats.f_files);
            LOGD("    f_ffree: %llu", (unsigned long long)stats.f_ffree);

            // 安全地访问 f_fsid
            LOGD("    f_fsid: { val: [%lu, %lu] }",
                 stats.f_fsid.__val[0],
                 stats.f_fsid.__val[1]);

            LOGD("    f_namelen: %lu", (unsigned long)stats.f_namelen);
            LOGD("    f_frsize: %lu", (unsigned long)stats.f_frsize);
            LOGD("    f_flags: %lu", (unsigned long)stats.f_flags);

            // 安全地打印数组
            LOGD("    f_spare: [%lu, %lu, %lu]",
                 (unsigned long)stats.f_spare[0],
                 (unsigned long)stats.f_spare[1],
                 (unsigned long)stats.f_spare[2]);
            LOGD("}");
        }
    } catch (...) {
        LOGE("Unknown exception occurred");
    }

    // 清理资源
    if (nativePath != NULL) {
        env->ReleaseStringUTFChars(path, nativePath);
    }
}