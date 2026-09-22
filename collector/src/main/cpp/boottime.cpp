//
// Created by Alice on 2024/11/26.
//
#include <jni.h>
#include <time.h>
#include <unistd.h>
#include <sys/sysinfo.h>

// 获取系统启动时间
long getBootTimeSec() {
    struct sysinfo info;
    if (sysinfo(&info) != 0) {
        return -1; // 获取失败
    }
    return info.uptime;
}

// JNI 方法声明
extern "C" JNIEXPORT jlongArray JNICALL Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getBootTime(JNIEnv *env, jobject thiz) {
    // 获取系统启动时间（秒）
    long bootTimeSec = getBootTimeSec();
    if (bootTimeSec == -1) {
        return nullptr; // 返回空数组表示获取失败
    }

    // 获取当前时间（纳秒）
    struct timespec now;
    if (clock_gettime(CLOCK_BOOTTIME, &now) != 0) {
        return nullptr; // 返回空数组表示获取失败
    }

    // 计算系统启动时间（纳秒）
    long bootTimeNsec = now.tv_nsec;

    // 创建一个JLongArray并填充结果
    jlongArray result = env->NewLongArray(2);
    jlong values[2] = {bootTimeSec, bootTimeNsec};
    env->SetLongArrayRegion(result, 0, 2, values);

    return result;
}
