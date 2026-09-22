


#include <cstdlib>
#include <string>
#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <android/looper.h>
#include <android/sensor.h>
#include <vector>
#include <fcntl.h>
#include <json/json.h>
#include <sys/stat.h>
#include <atomic>
#include <algorithm>


#define ALOGD(...)     __android_log_print(ANDROID_LOG_DEBUG, "SensorCollector", __VA_ARGS__)
#define ALOGE(...)     __android_log_print(ANDROID_LOG_ERROR, "SensorCollector", __VA_ARGS__)

#define NELEM(x) (sizeof(x)/sizeof((x)[0]))

JNIEnv *gEnv = nullptr;

const char *gExternalStoragePath = nullptr;
static std::atomic<bool> isStop{false};
static constexpr int kDynamicSensorMeta = 32; // 当前 NDK 未暴露该管理事件常量

// define pack struct for sensor data

#pragma pack(push)
#pragma pack(1)

struct SensorInfo{
    char name[256];         //ASensor_getName()
    char vendor[256];       //ASensor_getVendor()
    int type;               //ASensor_getType()
    float resolution;         //ASensor_getResolution()
    int minDelay;           //ASensor_getMinDelay()
    int fifoMaxEventCount;  //ASensor_getFifoMaxEventCount()
    int ifoReservedEventCount; //ASensor_getFifoReservedEventCount()
    char stringType[256];  //ASensor_getStringType()
    int reportingMode;    //ASensor_getReportingMode()
    int isWakeUpSensor;  //ASensor_isWakeUpSensor()
    int handle; //ASensor_getHandle()
};
struct SensorDataHead {
    char reserved[256];
};


#pragma pack(pop)


int startTestSensor(const int sensorTypes[], int numSensorTypes) {
    /*create a sensor manager*/
    ASensorManager *sensorManager = ASensorManager_getInstance();
    if (sensorManager == nullptr) {
        ALOGE("sensor manager unavailable");
        return -1;
    }

    /*get list of all sensors in the device*/
    ASensorList sensorList;
    int numSensors = ASensorManager_getSensorList(sensorManager, &sensorList);
    if (numSensors <= 0) {
        ALOGE("sensor list unavailable, count=%d", numSensors);
        return -1;
    }
    std::vector<ASensorRef> sensors;
    std::vector<ASensorRef> enabledSensors;
    std::vector<int> sensorsFds;

    std::map<std::string, int> eventFileMap;


    int looperId = 1;
    ALooper * looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    ASensorEventQueue* eventQueue = ASensorManager_createEventQueue(sensorManager, looper, looperId, NULL, NULL );
    if ( eventQueue == NULL ){
        ALOGE("error, cannot create event queue\n");
        return -1;
    }


    for( int i = 0 ; i < numSensorTypes ; i++ ){
        int type = sensorTypes[i];
        // 动态连接元事件属于管理通道，不作为普通测量数据主动订阅。
        if (type <= 0 || type >= 0x10000 || type == kDynamicSensorMeta) continue;

        // 遍历完整列表，匹配所有该类型的传感器（同类型可能有多个）
        // 注意：不再支持 TYPE_ALL(-1) 特殊路径，调用方必须传具体类型列表
        for (int j = 0; j < numSensors; j++) {
            auto s = sensorList[j];
            if (ASensor_getType(s) == type &&
                std::find(sensors.begin(), sensors.end(), s) == sensors.end()) {
                sensors.push_back(s);
            }
        }
    }

//    Json::Value root;
//    Json::Value objs(Json::arrayValue);
//    for(auto s: sensors){
//        //sensor_t *tt = (sensor_t*)s;
//        SensorInfo si;
//        memset(&si, 0, sizeof(SensorInfo));
//        getSensorInfo(s, si);
//        objs.append(toJsonValue(si));
//    }

//    ALOGD("%s", objs.toStyledString().c_str());

//    {
//        int fd = open((std::string(gExternalStoragePath) + "/sensors.txt").c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0666);
//
//        if (fd == -1) {
//            ALOGE("open file error");
//            return -1;
//        }
//
//        write(fd, objs.toStyledString().c_str(), objs.toStyledString().size());
//        close(fd);
//    }

    /*turn on all available sensors*/
    for(auto s : sensors){
        if (isStop.load()) break;
        const int result = ASensorEventQueue_enableSensor(eventQueue, s);
        if(result != 0){
            ALOGE("cannot enable sensor name=%s type=%d handle=%d result=%d",
                  ASensor_getName(s), ASensor_getType(s), ASensor_getHandle(s), result);
            // Android 16: 部分受限传感器 enableSensor 失败，跳过即可，不中断流程
        }else{
            int handle = ASensor_getHandle(s);
            // write SensorDataHead to gExternalStoragePath/sensor_type file
            int fd = open((std::string(gExternalStoragePath) + "/" + std::to_string(handle)).c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0666);

            if (fd == -1) {
                ALOGE("open file error for sensor %s, skip\n", ASensor_getName(s));
                // 不 return：继续处理剩余传感器，避免 eventQueue 泄漏
                ASensorEventQueue_disableSensor(eventQueue, s);
                continue;
            }

            SensorDataHead hdr{};
            write(fd, &hdr, sizeof(hdr));

            close(fd);
            enabledSensors.push_back(s);
            ALOGD("enabled sensor name=%s type=%d handle=%d", ASensor_getName(s), ASensor_getType(s), handle);
        }
    }
    ALOGD("sensor capture: available=%d selected=%zu enabled=%zu", numSensors, sensors.size(), enabledSensors.size());

    /*get events from sensors*/
    int kTimeoutMilliSecs = 200; // 定期检查停止标志，避免停止后继续写入
    int identity = 0;

    while(!isStop.load() && !enabledSensors.empty()){
        /*poll for events*/
        identity = ALooper_pollOnce( kTimeoutMilliSecs, NULL, NULL, NULL );
        if (isStop.load()) break;
        if (identity == ALOOPER_POLL_ERROR) {
            ALOGE("sensor looper polling failed");
            break;
        }
        /* if timeout */
        if (identity != looperId)
            continue;

        /*retrieve events*/
        ASensorEvent events[128];
        memset(events, 0, 128*sizeof(ASensorEvent));

        ssize_t numEvents = ASensorEventQueue_getEvents(eventQueue, events, 128);
        if (numEvents < 0) {
            ALOGE("sensor event read failed: %zd", numEvents);
            break;
        }
        if ( numEvents == 0 ){
            continue; // no pending event
        }

        for (int n = 0; n < numEvents; ++n) {
            auto event = events[n];
            ALOGD("event %d\n", event.type);
            if( event.type == ASENSOR_TYPE_ACCELEROMETER ){
                ALOGD("%d [Accelerometer] x = %f  y = %f  z = %f\n",event.sensor, event.acceleration.x, event.acceleration.y, event.acceleration.z );
            }

            if ( event.type == ASENSOR_TYPE_PROXIMITY ){
                ALOGD("%d [proximity sensor] distance :%f\n",event.sensor, event.distance );
            }
            if ( event.type == ASENSOR_TYPE_GRAVITY ){
                ALOGD("%d [gravity sensor] gravity :%f,%f,%f\n",event.sensor, event.vector.v[0], event.vector.v[1], event.vector.v[2] );
            }

            std::string filepath = std::string(gExternalStoragePath) + "/" + std::to_string(event.sensor);
            // append to file
            int fd = open(filepath.c_str(), O_WRONLY | O_APPEND);
            if (fd != -1) {
                write(fd, &event, sizeof(event));
                close(fd);

                //get file size, and set to map
                struct stat statbuf;
                if (stat(filepath.c_str(), &statbuf) == 0) {
                    eventFileMap[filepath] = statbuf.st_size;
                }
            }

        }
    }

    /*turn off all available sensors*/
    for(auto s : enabledSensors){
        if( ASensorEventQueue_disableSensor( eventQueue, s) != 0 ){
            ALOGE("error, cannot disable sensor %s\n", ASensor_getName(s));
        }
    }

    for(auto file: eventFileMap){
        ALOGD("file: %s, size: %d, %d", file.first.c_str(), file.second, (file.second - 256) % 104);
    }

    /* free resources */
    ASensorManager_destroyEventQueue( sensorManager, eventQueue );

    return 0;
}



JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    ALOGD("sensor collector v2 loaded, sizeof(ASensorEvent) = %zu", sizeof(ASensorEvent));
    if (vm->GetEnv(reinterpret_cast<void **>(&gEnv), JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }


    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_test_ndk_Testor_testSensor(JNIEnv *env, jobject thiz, jstring dir, jintArray sensor_type_array) {
    __android_log_print(ANDROID_LOG_DEBUG, "TAG", "NdkTestor_TestSensor called");

    // get external storage path
    jboolean isCopy = true;
    gExternalStoragePath = env->GetStringUTFChars(dir, &isCopy);
    if (gExternalStoragePath == nullptr) return;
    ALOGD("external storage path: %s", gExternalStoragePath);

    // jintArray to int[]
    jint *sensorTypes = env->GetIntArrayElements(sensor_type_array, nullptr);
    if (sensorTypes == nullptr) {
        env->ReleaseStringUTFChars(dir, gExternalStoragePath);
        gExternalStoragePath = nullptr;
        return;
    }
    int numSensorTypes = env->GetArrayLength(sensor_type_array);
    for (int i = 0; i < numSensorTypes; i++) {
        __android_log_print(ANDROID_LOG_DEBUG, "TAG", "sensor type: %d", sensorTypes[i]);
    }
    startTestSensor(sensorTypes, numSensorTypes);
    env->ReleaseIntArrayElements(sensor_type_array, sensorTypes, JNI_ABORT);
    env->ReleaseStringUTFChars(dir, gExternalStoragePath);
    gExternalStoragePath = nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_test_ndk_Testor_prepare(JNIEnv *env, jobject thiz) {
    isStop.store(false);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_test_ndk_Testor_stop(JNIEnv *env, jobject thiz) {
    isStop.store(true);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_test_ndk_Testor_getSensorHandle(JNIEnv *env, jobject thiz, jstring name, jint type) {
    /*create a sensor manager*/
    ASensorManager *sensorManager = ASensorManager_getInstance();
    /*get list of all sensors in the device*/
    ASensorList sensorList;
    int numSensors = ASensorManager_getSensorList(sensorManager, &sensorList);
    ALOGD("NDK层获取到的传感器数量: %d", numSensors);

    const char *requestedName = env->GetStringUTFChars(name, nullptr);
    if (requestedName == nullptr) return -1;
    int handle = -1;

    for (int i = 0; i < numSensors; i++) {
        auto s = sensorList[i];
        auto sensorType = ASensor_getType(s);
        if (sensorType == type) {
            auto sensorName = ASensor_getName(s);
            if (strcmp(sensorName, requestedName) == 0) {
                handle = ASensor_getHandle(s);
                break;
            }
        }
    }
    env->ReleaseStringUTFChars(name, requestedName);
    return handle;
}
