//
// Created by Alice on 2024/11/26.
//
#include <jni.h>
#include <unistd.h>  // 包含uname函数
#include <sys/utsname.h>  // 包含utsname结构体
#include <sstream>
#include <iomanip>

// 将字符转换为两位十六进制字符串
std::string to_hex(char c) {
    std::stringstream ss;
    ss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(static_cast<unsigned char>(c));
    return ss.str();
}

// 将 struct utsname 转换为十六进制字符串
std::string utsname_to_hex(const struct utsname &name) {
    std::stringstream ss;
    ss << to_hex(name.sysname[0]);
    for (size_t i = 1; name.sysname[i] != '\0'; ++i) {
        ss << to_hex(name.sysname[i]);
    }
    ss << ' ';

    ss << to_hex(name.nodename[0]);
    for (size_t i = 1; name.nodename[i] != '\0'; ++i) {
        ss << to_hex(name.nodename[i]);
    }
    ss << ' ';

    ss << to_hex(name.release[0]);
    for (size_t i = 1; name.release[i] != '\0'; ++i) {
        ss << to_hex(name.release[i]);
    }
    ss << ' ';

    ss << to_hex(name.version[0]);
    for (size_t i = 1; name.version[i] != '\0'; ++i) {
        ss << to_hex(name.version[i]);
    }
    ss << ' ';

    ss << to_hex(name.machine[0]);
    for (size_t i = 1; name.machine[i] != '\0'; ++i) {
        ss << to_hex(name.machine[i]);
    }

    return ss.str();
}

// JNI 方法声明
extern "C" JNIEXPORT jstring Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getCnameInfoHex(JNIEnv *env, jobject thiz) {
    struct utsname name;
    if (uname(&name) == -1) {
        return env->NewStringUTF("Error getting system information");
    }

    std::string hex_string = utsname_to_hex(name);
    return env->NewStringUTF(hex_string.c_str());
}
