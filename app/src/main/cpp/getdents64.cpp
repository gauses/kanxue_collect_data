////
//// Created by Alice on 2024/11/8.
////
//
//#include <jni.h>
//#include <unistd.h>
//#include <sys/syscall.h>
//#include <jni.h>
//#include <jni.h>
//
//// 使用 getdents64 的 syscall 调用
////extern "C" JNIEXPORT jbyteArray JNICALL
//extern "C"
//JNIEXPORT jbyteArray
//
//Java_com_nest_kanxue_NativeLib_getDents64(JNIEnv *env, jobject, jint fd, jint bufferSize) {
//    // 创建一个缓冲区
//    char *buffer = new char[bufferSize];
//    int nread = syscall(SYS_getdents64, fd, buffer, bufferSize);
//
//    if (nread == -1) {
//        delete[] buffer;
//        return nullptr; // 如果读取失败，返回 null
//    }
//
//    // 将缓冲区内容转换为 byteArray 以返回给 Java
//    jbyteArray result = env->NewByteArray(nread);
//    env->SetByteArrayRegion(result, 0, nread, reinterpret_cast<jbyte*>(buffer));
//
//    delete[] buffer;
//    return result;
//}
//
