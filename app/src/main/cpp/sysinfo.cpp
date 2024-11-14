//
// Created by Alice on 2024/11/14.
//
#include <jni.h>
#include <string>
#include <sys/sysinfo.h>
#include <sys/utsname.h>
#include <android/log.h>

#define TAG "SysInfoJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getSystemInfo(JNIEnv *env, jobject /* this */) {
    struct sysinfo info;

    if (sysinfo(&info) != 0) {
        LOGE("Failed to get sysinfo");
        return env->NewStringUTF("Failed to get system info");
    }

    // Convert to MB for better readability
    const unsigned long MB = 1024 * 1024;

    char buffer[1024];
    snprintf(buffer, sizeof(buffer),
             "Total RAM: %lu MB\n"
             "Free RAM: %lu MB\n"
             "Shared RAM: %lu MB\n"
             "Buffer RAM: %lu MB\n"
             "Load Avg (1 min): %.2f\n"
             "Load Avg (5 min): %.2f\n"
             "Load Avg (15 min): %.2f\n"
             "Uptime: %lu seconds\n"
             "Processes: %d\n",
             info.totalram / MB,
             info.freeram / MB,
             info.sharedram / MB,
             info.bufferram / MB,
             (double)info.loads[0] / 65536.0,
             (double)info.loads[1] / 65536.0,
             (double)info.loads[2] / 65536.0,
             info.uptime,
             info.procs);

    LOGI("Successfully retrieved system info");
    return env->NewStringUTF(buffer);
}

JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getUname(JNIEnv *env, jobject /* this */) {
    struct utsname name;

    if (uname(&name) != 0) {
        LOGE("Failed to get uname");
        return env->NewStringUTF("Failed to get uname");
    }

    char buffer[1024];
    snprintf(buffer, sizeof(buffer),
             "System Name: %s\n"
             "Node Name: %s\n"
             "Release: %s\n"
             "Version: %s\n"
             "Machine: %s\n",
             name.sysname,
             name.nodename,
             name.release,
             name.version,
             name.machine);

    LOGI("Successfully retrieved uname info");
    return env->NewStringUTF(buffer);
}

} // extern "C"