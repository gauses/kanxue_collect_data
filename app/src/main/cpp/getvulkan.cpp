//
// Created by Alice on 2025/5/14.
//

#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>
#include <jni.h>
#include <string>
#include <sstream>
#include <android/log.h>
#include <iomanip>

// 将pipeline cache UUID转换为十六进制字符串
std::string pipelineCacheUUIDToHexString(const uint8_t* uuid) {
    std::ostringstream oss;
    oss << std::hex << std::setfill('0');
    for (int i = 0; i < VK_UUID_SIZE; ++i) {
        oss << std::setw(2) << static_cast<int>(uuid[i]);
        if (i < VK_UUID_SIZE - 1) {
            oss << "-";
        }
    }
    return oss.str();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getVulkanInfo(JNIEnv *env, jobject thiz) {
    // Initialize Vulkan
    VkInstance instance;
    VkInstanceCreateInfo createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;

    VkResult result = vkCreateInstance(&createInfo, nullptr, &instance);
    if (result != VK_SUCCESS) {
        return env->NewStringUTF("Failed to create Vulkan instance");
    }

    // Convert version to string
    std::ostringstream oss;
    // Get Vulkan version
    uint32_t apiVersion;
    vkEnumerateInstanceVersion(&apiVersion);


    oss << "Vulkan API Version: " << apiVersion;

    // 2. 枚举物理设备（通常取第一个）
    uint32_t deviceCount = 0;
    vkEnumeratePhysicalDevices(instance, &deviceCount, nullptr);
    VkPhysicalDevice* devices = new VkPhysicalDevice[deviceCount];
    vkEnumeratePhysicalDevices(instance, &deviceCount, devices);

    VkPhysicalDevice physicalDevice = VK_NULL_HANDLE;

    for(int i = 0; i < deviceCount; ++i){
        physicalDevice = devices[i];
        if (physicalDevice == VK_NULL_HANDLE) {
            continue;
        }
        VkPhysicalDeviceProperties deviceProps{};
        // 3. 获取设备属性
        vkGetPhysicalDeviceProperties(physicalDevice, &deviceProps);

        oss << "\nDevice Name: " << deviceProps.deviceName;
        oss << "\nAPI Version: " << deviceProps.apiVersion ;
        oss << "\nDriver Version: " << deviceProps.driverVersion;
        oss << "\nVendor ID: " << deviceProps.vendorID;
        oss << "\nDevice ID: " << deviceProps.deviceID;
        oss << "\nDevice Type: " << deviceProps.deviceType;

        oss << "\nPipeline Cache UUID: " << pipelineCacheUUIDToHexString(deviceProps.pipelineCacheUUID);

        VkPhysicalDeviceMemoryProperties memProperties;
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, &memProperties);

        oss << "\nMemory Types: ";
        for (uint32_t i = 0; i < memProperties.memoryTypeCount; ++i) {
            oss << "\n  Type " << i << ": "
                << "Heap Index: " << memProperties.memoryTypes[i].heapIndex
                << ", Property Flags: " << memProperties.memoryTypes[i].propertyFlags;
        }
        oss << "\nMemory Heaps: ";
        for (uint32_t i = 0; i < memProperties.memoryHeapCount; ++i) {
            oss << "\n  Heap " << i << ": "
                << "Size: " << memProperties.memoryHeaps[i].size
                << ", Flags: " << memProperties.memoryHeaps[i].flags;
        }

        VkDeviceSize deviceLocalMemorySize = 0;
        for (uint32_t i = 0; i < memProperties.memoryHeapCount; ++i) {
            if (memProperties.memoryHeaps[i].flags & VK_MEMORY_HEAP_DEVICE_LOCAL_BIT) {
                deviceLocalMemorySize += memProperties.memoryHeaps[i].size;
            }
        }
        oss << "\nTotal Device Local Memory Size: " << deviceLocalMemorySize  << " Bytes";
    }

    // Clean up
    vkDestroyInstance(instance, nullptr);

    return env->NewStringUTF(oss.str().c_str());
}