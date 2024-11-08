# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.

cmake_minimum_required (VERSION 3.22)


add_library( # 库名称
        getdents64-lib
        # 共享库
        SHARED
        # 源文件
        getdents64.cpp )

find_library( # 加载 log 库
        log-lib
        log )

target_link_libraries( # 链接 log 库
        getdents64-lib
        ${log-lib} )

