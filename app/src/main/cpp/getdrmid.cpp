//
// Created by Alice on 2024/11/11.
//

#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <jni.h>
#include <jni.h>

#define LOG_TAG "DRM-ID"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

//extern "C" JNIEXPORT jbyteArray JNICALL


extern "C"
JNIEXPORT jbyteArray
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getDrmId(JNIEnv *env, jobject thiz) {
    void* handle = dlopen("libmediandk.so", RTLD_LAZY);
    if (!handle) {
        LOGI("Failed to load libmediandk.so");
        return nullptr;
    }

    using GetPropertyByteArrayFunc = int (*)(const char* name, uint8_t* buf, size_t* bufSize);
    auto getPropertyByteArray = reinterpret_cast<GetPropertyByteArrayFunc>(dlsym(handle, "getPropertyByteArray"));

    if (!getPropertyByteArray) {
        LOGI("Failed to locate getPropertyByteArray");
        dlclose(handle);
        return nullptr;
    }

    const char* drmPropertyName = "drm.id";
    uint8_t drmIdBuffer[128];
    size_t drmIdBufferSize = sizeof(drmIdBuffer);

    int result = getPropertyByteArray(drmPropertyName, drmIdBuffer, &drmIdBufferSize);

    if (result != 0) {
        LOGI("getPropertyByteArray failed with error code: %d", result);
        dlclose(handle);
        return nullptr;
    }

    jbyteArray drmIdArray = env->NewByteArray(static_cast<jsize>(drmIdBufferSize));
    env->SetByteArrayRegion(drmIdArray, 0, static_cast<jsize>(drmIdBufferSize), reinterpret_cast<jbyte*>(drmIdBuffer));

    dlclose(handle);

    return drmIdArray;

}
