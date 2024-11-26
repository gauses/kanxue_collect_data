#include <jni.h>
#include <unistd.h>     // 包含 uname 函数
#include <sys/utsname.h> // 包含 utsname 结构体
#include <sstream>
#include <iomanip>

// 将 struct utsname 转换为连续的十六进制字符串
std::string utsname_to_hex(const struct utsname &name) {
    const unsigned char *rawData = reinterpret_cast<const unsigned char *>(&name);
    size_t structSize = sizeof(struct utsname);

    std::stringstream ss;

    // 遍历结构体的字节内容并转换为十六进制
    for (size_t i = 0; i < structSize; ++i) {
        ss << std::hex << std::setw(2) << std::setfill('0') << static_cast<int>(rawData[i]);
    }

    return ss.str();
}

// JNI 方法声明
extern "C" JNIEXPORT jstring JNICALL
Java_com_nest_kanxue_devicefingerprint_DrmIdFetcher_getCnameInfoHex(JNIEnv *env, jobject thiz) {
    struct utsname name;
    if (uname(&name) == -1) {
        return env->NewStringUTF("Error: Unable to get system information");
    }

    std::string hex_string = utsname_to_hex(name);
    return env->NewStringUTF(hex_string.c_str());
}
