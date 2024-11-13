//
// Created by Alice on 2024/11/13.
//
// device_reader.cpp
#include <fstream>
#include <string>
#include <vector>
#include <iostream>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <jni.h>

// 方法1：使用C++ fstream
std::string readCompatibleFstream() {
//    const char* path = "/sys/firmware/devicetree/base/compatible";
    const char* path = "/proc/stat";
    std::ifstream file(path, std::ios::binary);

    if (!file.is_open()) {
        return "Failed to open file";
    }

    // 读取文件内容
    std::vector<char> buffer(1024);
    file.read(buffer.data(), buffer.size());
    std::streamsize count = file.gcount();

    // 处理内容（去除null字节）
    std::string result;
    for (int i = 0; i < count; i++) {
        if (buffer[i] != '\0') {
            result += buffer[i];
        } else {
            result += '\n';  // 用换行符替换null字节
        }
    }

    file.close();
    return result;
}

// 方法2：使用底层文件操作
std::string readCompatibleLowLevel() {
//    const char* path = "/sys/firmware/devicetree/base/compatible";
    const char* path = "/proc/stat";
    int fd = open(path, O_RDONLY);

    if (fd < 0) {
        return "Failed to open file";
    }

    char buffer[1024];
    ssize_t bytes_read = read(fd, buffer, sizeof(buffer));
    close(fd);

    if (bytes_read < 0) {
        return "Failed to read file";
    }

    std::string result;
    for (ssize_t i = 0; i < bytes_read; i++) {
        if (buffer[i] != '\0') {
            result += buffer[i];
        } else {
            result += '\n';
        }
    }

    return result;
}

// JNI 接口
extern "C" JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_readCompatibleNative(
        JNIEnv* env,
        jobject /* this */) {

    std::string content = readCompatibleLowLevel();  // 或使用 readCompatibleFstream()
    return env->NewStringUTF(content.c_str());
}