# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

project("ndk_kanxue")

# 查找 Vulkan 库
find_library(vulkan-lib vulkan)

# 包含头文件目录
include_directories(
    jsoncpp/include
)

# 添加 JsonCpp 源文件
add_library(jsoncpp STATIC
    jsoncpp/lib_json/json_reader.cpp
    jsoncpp/lib_json/json_value.cpp
    jsoncpp/lib_json/json_writer.cpp
)

# 添加源文件
add_library(${CMAKE_PROJECT_NAME} SHARED
    # 现有源文件
    sysinfo.cpp
    filestat.cpp
    cname.cpp
    statfs64_jni.cpp
    statfs64_jni_parse.cpp
    boottime.cpp
    getdrmid.cpp
    readCompatibleNative.cpp
    getdents64.cpp
    sensor.cpp
    nativegetdrmid.cpp
    Base64Utils.cpp
    # 新增DisplayCard源文件
    DisplayCard.cpp
    getvulkan.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log
        vulkan
        mediandk
        jsoncpp)
