#include <jni.h>
#include <android/log.h>
#include <vulkan/vulkan.h>
#include <string>
#include <vector>
#include <cstring>
#include "json/json.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "DisplayCard", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "DisplayCard", __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_nest_kanxue_core_DisplayCard_createVulkanInstance(JNIEnv *env, jobject thiz) {
    VkInstance instance = VK_NULL_HANDLE;
    VkApplicationInfo appInfo = {};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "DisplayCard";
    appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.pEngineName = "No Engine";
    appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.apiVersion = VK_API_VERSION_1_0;

    VkInstanceCreateInfo createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;

    VkResult result = vkCreateInstance(&createInfo, nullptr, &instance);
    if (result != VK_SUCCESS) {
        LOGE("Failed to create Vulkan instance: %d", result);
        return 0;
    }

    LOGI("Vulkan instance created successfully");
    return reinterpret_cast<jlong>(instance);
}

JNIEXPORT jint JNICALL
Java_com_nest_kanxue_core_DisplayCard_getPhysicalDeviceCount(JNIEnv *env, jobject thiz, jlong instance) {
    uint32_t deviceCount = 0;
    VkResult result = vkEnumeratePhysicalDevices(reinterpret_cast<VkInstance>(instance), &deviceCount, nullptr);
    if (result != VK_SUCCESS) {
        LOGE("Failed to enumerate physical devices: %d", result);
        return 0;
    }
    return static_cast<jint>(deviceCount);
}

JNIEXPORT jlongArray JNICALL
Java_com_nest_kanxue_core_DisplayCard_getPhysicalDevices(JNIEnv *env, jobject thiz, jlong instance) {
    uint32_t deviceCount = 0;
    vkEnumeratePhysicalDevices(reinterpret_cast<VkInstance>(instance), &deviceCount, nullptr);
    //上面调用后，deviceCount 会被更新为系统中可用的物理设备数量
    
    std::vector<VkPhysicalDevice> devices(deviceCount);
    vkEnumeratePhysicalDevices(reinterpret_cast<VkInstance>(instance), &deviceCount, devices.data());
    //上面调用后，devices 向量中就包含了所有可用的物理设备

//    第一次调用获取需要分配的内存大小
//    第二次调用获取实际的数据

    jlongArray result = env->NewLongArray(deviceCount);
    jlong *elements = env->GetLongArrayElements(result, nullptr);
    for (uint32_t i = 0; i < deviceCount; i++) {
        elements[i] = reinterpret_cast<jlong>(devices[i]);
    }
    env->ReleaseLongArrayElements(result, elements, 0);
    return result;
}

JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_core_DisplayCard_getDeviceInfo(JNIEnv *env, jobject thiz, jlong instance, jlong device) {
    VkPhysicalDevice physicalDevice = reinterpret_cast<VkPhysicalDevice>(device);
    Json::Value root;

    // 获取设备属性
    VkPhysicalDeviceProperties properties;
    vkGetPhysicalDeviceProperties(physicalDevice, &properties);
    
    root["deviceName"] = properties.deviceName;
    root["deviceType"] = properties.deviceType;
    root["driverVersion"] = properties.driverVersion;
    root["apiVersion"] = properties.apiVersion;

    // 获取设备特性
    VkPhysicalDeviceFeatures features;
    vkGetPhysicalDeviceFeatures(physicalDevice, &features);
    
    Json::Value featuresJson;
    featuresJson["robustBufferAccess"] = features.robustBufferAccess;
    featuresJson["fullDrawIndexUint32"] = features.fullDrawIndexUint32;
    featuresJson["imageCubeArray"] = features.imageCubeArray;
    featuresJson["independentBlend"] = features.independentBlend;
    featuresJson["geometryShader"] = features.geometryShader;
    featuresJson["tessellationShader"] = features.tessellationShader;
    featuresJson["sampleRateShading"] = features.sampleRateShading;
    featuresJson["dualSrcBlend"] = features.dualSrcBlend;
    featuresJson["logicOp"] = features.logicOp;
    featuresJson["multiDrawIndirect"] = features.multiDrawIndirect;
    featuresJson["drawIndirectFirstInstance"] = features.drawIndirectFirstInstance;
    featuresJson["depthClamp"] = features.depthClamp;
    featuresJson["depthBiasClamp"] = features.depthBiasClamp;
    featuresJson["fillModeNonSolid"] = features.fillModeNonSolid;
    featuresJson["depthBounds"] = features.depthBounds;
    featuresJson["wideLines"] = features.wideLines;
    featuresJson["largePoints"] = features.largePoints;
    featuresJson["alphaToOne"] = features.alphaToOne;
    featuresJson["multiViewport"] = features.multiViewport;
    featuresJson["samplerAnisotropy"] = features.samplerAnisotropy;
    featuresJson["textureCompressionETC2"] = features.textureCompressionETC2;
    featuresJson["textureCompressionASTC_LDR"] = features.textureCompressionASTC_LDR;
    featuresJson["textureCompressionBC"] = features.textureCompressionBC;
    featuresJson["occlusionQueryPrecise"] = features.occlusionQueryPrecise;
    featuresJson["pipelineStatisticsQuery"] = features.pipelineStatisticsQuery;
    featuresJson["vertexPipelineStoresAndAtomics"] = features.vertexPipelineStoresAndAtomics;
    featuresJson["fragmentStoresAndAtomics"] = features.fragmentStoresAndAtomics;
    featuresJson["shaderTessellationAndGeometryPointSize"] = features.shaderTessellationAndGeometryPointSize;
    featuresJson["shaderImageGatherExtended"] = features.shaderImageGatherExtended;
    featuresJson["shaderStorageImageExtendedFormats"] = features.shaderStorageImageExtendedFormats;
    featuresJson["shaderStorageImageMultisample"] = features.shaderStorageImageMultisample;
    featuresJson["shaderStorageImageReadWithoutFormat"] = features.shaderStorageImageReadWithoutFormat;
    featuresJson["shaderStorageImageWriteWithoutFormat"] = features.shaderStorageImageWriteWithoutFormat;
    featuresJson["shaderUniformBufferArrayDynamicIndexing"] = features.shaderUniformBufferArrayDynamicIndexing;
    featuresJson["shaderSampledImageArrayDynamicIndexing"] = features.shaderSampledImageArrayDynamicIndexing;
    featuresJson["shaderStorageBufferArrayDynamicIndexing"] = features.shaderStorageBufferArrayDynamicIndexing;
    featuresJson["shaderStorageImageArrayDynamicIndexing"] = features.shaderStorageImageArrayDynamicIndexing;
    featuresJson["shaderClipDistance"] = features.shaderClipDistance;
    featuresJson["shaderCullDistance"] = features.shaderCullDistance;
    featuresJson["shaderFloat64"] = features.shaderFloat64;
    featuresJson["shaderInt64"] = features.shaderInt64;
    featuresJson["shaderInt16"] = features.shaderInt16;
    featuresJson["shaderResourceResidency"] = features.shaderResourceResidency;
    featuresJson["shaderResourceMinLod"] = features.shaderResourceMinLod;
    featuresJson["sparseBinding"] = features.sparseBinding;
    featuresJson["sparseResidencyBuffer"] = features.sparseResidencyBuffer;
    featuresJson["sparseResidencyImage2D"] = features.sparseResidencyImage2D;
    featuresJson["sparseResidencyImage3D"] = features.sparseResidencyImage3D;
    featuresJson["sparseResidency2Samples"] = features.sparseResidency2Samples;
    featuresJson["sparseResidency4Samples"] = features.sparseResidency4Samples;
    featuresJson["sparseResidency8Samples"] = features.sparseResidency8Samples;
    featuresJson["sparseResidency16Samples"] = features.sparseResidency16Samples;
    featuresJson["sparseResidencyAliased"] = features.sparseResidencyAliased;
    featuresJson["variableMultisampleRate"] = features.variableMultisampleRate;
    featuresJson["inheritedQueries"] = features.inheritedQueries;

    root["features"] = featuresJson;

    Json::FastWriter writer;
    std::string jsonString = writer.write(root);
    return env->NewStringUTF(jsonString.c_str());
}

JNIEXPORT void JNICALL
Java_com_nest_kanxue_core_DisplayCard_destroyVulkanInstance(JNIEnv *env, jobject thiz, jlong instance) {
    vkDestroyInstance(reinterpret_cast<VkInstance>(instance), nullptr);
    LOGI("Vulkan instance destroyed");
}

} 