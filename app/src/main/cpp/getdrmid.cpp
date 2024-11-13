//
// Created by Alice on 2024/11/11.
//

#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <jni.h>
#include <jni.h>

#include <dlfcn.h>
#include <string>
#include <vector>
#include <cstring>
#include <android/log.h>

#define LOG_TAG "DrmReader"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


class DrmReader {
public:
    // AMediaDrm_getPropertyByteArray 函数指针定义
    typedef int32_t (*GetPropertyByteArrayFunc)(const uint8_t* name, size_t nameLength, uint8_t* value, size_t* valueLength);

    DrmReader() : lib_handle_(nullptr), getPropertyByteArray_(nullptr) {
        init();
    }

    ~DrmReader() {
        if (lib_handle_) {
            dlclose(lib_handle_);
            lib_handle_ = nullptr;
        }
    }

    std::string getDrmId() {
        if (!lib_handle_ || !getPropertyByteArray_) {
            LOGE("Library or function not initialized");
            return "Init failed";
        }

        // 构建请求参数
        uint8_t name[34] = {0};  // 初始化为0
        name[4] = '\\';
        name[7] = 2;
        name[8] = 0xB0;
        name[9] = 0xA3;
        name[10] = 0x30;
        name[11] = 0x61;
        name[12] = 0x82;
        name[13] = 0xD0;
        name[14] = 0x90;
        name[15] = 0;  // 修正LL
        name[16] = 0x20;
        name[17] = 0x71;
        name[18] = 0x19;
        name[19] = 0x31;
        name[20] = 0xD0;
        name[21] = 0x32;
        name[22] = 0x01;
        name[23] = 0;  // 修正BL
        name[24] = 14;
        name[25] = ':';
        name[27] = 14;
        name[28] = 30;
        name[29] = 61;
        name[30] = 15;
        name[31] = 12;
        name[32] = 30;
        name[33] = 249;

        // 打印name数组内容用于调试
        LOGI("Name array content:");
        for (int i = 0; i < 34; i++) {
            LOGI("name[%d] = 0x%02x", i, name[i]);
        }

        // 准备接收缓冲区
        std::vector<uint8_t> buffer(256);
        size_t valueLength = buffer.size();

        // 调用函数获取DRM ID
        int32_t result = getPropertyByteArray_(name, sizeof(name), buffer.data(), &valueLength);
        if (result != 0) {
            LOGE("Failed to get DRM ID, error: %d", result);
            return "Get DRM ID failed";
        }

        // 转换结果为十六进制字符串
        std::string drmId;
        for (size_t i = 0; i < valueLength; i++) {
            char hex[3];
            snprintf(hex, sizeof(hex), "%02x", buffer[i]);
            drmId += hex;
        }

        return drmId;
    }

private:
    void init() {
        // 加载 libmediandk.so
        lib_handle_ = dlopen("libmediandk.so", RTLD_NOW);
        if (!lib_handle_) {
            LOGE("Failed to load libmediandk.so: %s", dlerror());
            return;
        }

        // 获取函数指针
        getPropertyByteArray_ = reinterpret_cast<GetPropertyByteArrayFunc>(
                dlsym(lib_handle_, "AMediaDrm_getPropertyByteArray"));

        if (!getPropertyByteArray_) {
            LOGE("Failed to get AMediaDrm_getPropertyByteArray: %s", dlerror());
            dlclose(lib_handle_);
            lib_handle_ = nullptr;
            return;
        }

        LOGI("Successfully initialized DrmReader");
    }

    void* lib_handle_;
    GetPropertyByteArrayFunc getPropertyByteArray_;
};


extern "C" JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getDrmId(JNIEnv *env, jobject thiz) {
    DrmReader reader;
    std::string drmId = reader.getDrmId();
    return env->NewStringUTF(drmId.c_str());

}
