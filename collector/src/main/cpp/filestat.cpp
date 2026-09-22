#include <jni.h>
#include <string>
#include <sys/stat.h>
#include <android/log.h>
#include <errno.h>

#define TAG "FileStatJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getFileStat(JNIEnv *env, jobject /* this */, jstring path) {
    if (path == nullptr) {
        return env->NewStringUTF("{\"error\": \"Input path is null\"}");
    }

    const char *pathStr = env->GetStringUTFChars(path, nullptr);
    if (pathStr == nullptr) {
        return env->NewStringUTF("{\"error\": \"Failed to get path string\"}");
    }

    struct stat buf;
    if (stat(pathStr, &buf) == 0) {
        char buffer[512];
        snprintf(buffer, sizeof(buffer),
                 "{"
//                 "\"atime_sec\": %ld,"
//                 "\"atime_nsec\": %u,"
//                 "\"ctime_sec\": %ld,"
//                 "\"ctime_nsec\": %u,"
//                 "\"mtime_sec\": %ld,"
//                 "\"mtime_nsec\": %u"
//                 "}",
                 "\"atime_sec\": %ld,"
                 "\"atime_nsec\": %u,"
                 "\"ctime_sec\": %ld,"
                 "\"ctime_nsec\": %u,"
                 "\"mtime_sec\": %ld,"
                 "\"mtime_nsec\": %u"
                 "}",
                 (long)buf.st_atim.tv_sec,
                 (unsigned int)buf.st_atim.tv_nsec,
                 (long)buf.st_ctim.tv_sec,
                 (unsigned int)buf.st_ctim.tv_nsec,
                 (long)buf.st_mtim.tv_sec,
                 (unsigned int)buf.st_mtim.tv_nsec
        );

        LOGI("JSON output: %s", buffer);
        env->ReleaseStringUTFChars(path, pathStr);
        return env->NewStringUTF(buffer);
    }

    env->ReleaseStringUTFChars(path, pathStr);
    return env->NewStringUTF("{\"error\": \"Failed to get file stat\"}");
}

} // extern "C"