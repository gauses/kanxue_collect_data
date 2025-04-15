# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.

cmake_minimum_required(VERSION 3.22.1)


project("ndk_kanxue")


include_directories(jsoncpp/include)

add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        SHARED
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
        jsoncpp/lib_json/json_reader.cpp
        jsoncpp/lib_json/json_value.cpp
        jsoncpp/lib_json/json_writer.cpp
)

target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log
        mediandk)

