#include <jni.h>
#include <string>
#include <sys/stat.h>
#include <time.h>
#include <android/log.h>
#include <errno.h>

#define TAG "FileStatJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// 格式化时间的辅助函数
std::string formatTimeStr(time_t time) {
    char buffer[32];
    struct tm* timeinfo = localtime(&time);
    strftime(buffer, sizeof(buffer), "%Y-%m-%d %H:%M:%S", timeinfo);
    return std::string(buffer);
}

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getFileStat(JNIEnv *env, jobject /* this */, jstring path) {
    if (path == nullptr) {
        return env->NewStringUTF("Error: Input path is null");
    }

    const char *pathStr = env->GetStringUTFChars(path, nullptr);
    if (pathStr == nullptr) {
        return env->NewStringUTF("Error: Failed to get path string");
    }

    struct stat buf;
    std::string result;

    if (stat(pathStr, &buf) == 0) {
        // 获取原始时间戳
        time_t atime = buf.st_atime;
        time_t mtime = buf.st_mtime;
        time_t ctime = buf.st_ctime;

        // 获取格式化的时间字符串
        std::string atimeStr = formatTimeStr(atime);
        std::string mtimeStr = formatTimeStr(mtime);
        std::string ctimeStr = formatTimeStr(ctime);

        char buffer[2048];
        snprintf(buffer, sizeof(buffer),
                 "File: %s\n"
                 "Device ID: %ld\n"
                 "Inode: %lu\n"
                 "Mode: %o\n"
                 "Hard Links: %lu\n"
                 "User ID: %u\n"
                 "Group ID: %u\n"
                 "Device ID (special): %ld\n"
                 "Size: %lld\n"
                 "Block Size: %ld\n"
                 "Blocks: %llu\n"
                 "Access Time (raw): %ld\n"
                 "Modify Time (raw): %ld\n"
                 "Change Time (raw): %ld\n"
                 "Access Time (formatted): %s\n"
                 "Modify Time (formatted): %s\n"
                 "Change Time (formatted): %s\n",
                 pathStr,
                 (long)buf.st_dev,
                 (unsigned long)buf.st_ino,
                 (unsigned int)buf.st_mode,
                 (unsigned long)buf.st_nlink,
                 (unsigned int)buf.st_uid,
                 (unsigned int)buf.st_gid,
                 (long)buf.st_rdev,
                 (long long)buf.st_size,
                 (long)buf.st_blksize,
                 (unsigned long long)buf.st_blocks,
//                 (long)atime,
//                 (long)mtime,
//                 (long)ctime,
                 atime,
                 mtime,
                 ctime,
                 atimeStr.c_str(),
                 mtimeStr.c_str(),
                 ctimeStr.c_str()
        );

        result = std::string(buffer);
    } else {
        result = std::string("Failed to get file stat: ") + strerror(errno);
    }

    env->ReleaseStringUTFChars(path, pathStr);
    return env->NewStringUTF(result.c_str());
}

} // extern "C"