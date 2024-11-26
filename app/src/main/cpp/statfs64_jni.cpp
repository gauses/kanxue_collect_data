#include <jni.h>
#include <string>
#include <sys/vfs.h>
#include <sstream>
#include <iomanip>

extern "C" JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getStatFsInfo(JNIEnv* env, jobject, jstring jPath) {
    const char* path = env->GetStringUTFChars(jPath, nullptr);

    struct statfs64 fs;
    int result = statfs64(path, &fs);

    env->ReleaseStringUTFChars(jPath, path);

    if (result != 0) {
        return env->NewStringUTF("Error: Unable to fetch statfs64 data");
    }

    // 获取结构体的原始内存内容
    const unsigned char* rawData = reinterpret_cast<const unsigned char*>(&fs);
    size_t structSize = sizeof(struct statfs64);

    // 将内容格式化为连续的十六进制字符串
    std::ostringstream oss;
    for (size_t i = 0; i < structSize; ++i) {
        oss << std::hex << std::setw(2) << std::setfill('0')
            << static_cast<int>(rawData[i]);
    }

    // 返回格式化的结果
    return env->NewStringUTF(oss.str().c_str());
}

