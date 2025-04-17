//
// Created by Alice on 2024/11/11.
//

#include <jni.h>
#include <string>
#include <android/log.h>
#include <dlfcn.h>
#include <vector>
#include <cstring>
#include <media/NdkMediaDrm.h>
#include "Base64Utils.h"

#define LOG_TAG "DrmReader"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getDrmId(JNIEnv *env, jobject thiz) {
    const uint8_t uuid[] = {0xed,0xef,0x8b,0xa9,0x79,0xd6,0x4a,0xce,
                            0xa3,0xc8,0x27,0xdc,0xd5,0x1d,0x21,0xed
    };
    AMediaDrm *mediaDrm = AMediaDrm_createByUUID(uuid);
    if (!mediaDrm) {
        return env->NewStringUTF("");  // 返回空字符串表示错误
    }

    // 获取 deviceUniqueId
    AMediaDrmByteArray aMediaDrmByteArray;
    media_status_t status = AMediaDrm_getPropertyByteArray(mediaDrm, PROPERTY_DEVICE_UNIQUE_ID, &aMediaDrmByteArray);
    LOGI("DRM_status: %d", status);

    std::string result;

    if (status == AMEDIA_OK) {
        result = Base64Utils::Encode((uint8_t *)aMediaDrmByteArray.ptr, aMediaDrmByteArray.length);
    }

    

    AMediaDrm_release(mediaDrm);


    return env->NewStringUTF(result.c_str());
}
