


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


#define ALOGD(...)     __android_log_print(ANDROID_LOG_ERROR, "TAG", __VA_ARGS__)
#define ALOGE(...)     __android_log_print(ANDROID_LOG_DEBUG, "TAG", __VA_ARGS__)

#define NELEM(x) (sizeof(x)/sizeof((x)[0]))

JNIEnv *gEnv = nullptr;
static jobject gTestorObject = nullptr;

const char *gExternalStoragePath = nullptr;
volatile int isStop = 0;

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

    /*get list of all sensors in the device*/
    ASensorList sensorList;
    int numSensors = ASensorManager_getSensorList(sensorManager, &sensorList);
    std::vector<ASensorRef> sensors;
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

        if (type == -1){ // Sensor.TYPE_ALL = -1
            sensors.clear();
            for (int j = 0; j < numSensors; j++) {
                auto s = sensorList[j];
                sensors.push_back(s);
            }
            break;
        }else{
            auto s = ASensorManager_getDefaultSensor(sensorManager, type);
            if (s != nullptr) {
                sensors.push_back(s);
            }else{
                ALOGD("************Sensors Not Available %d ********************\n", type);
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
        if( ASensorEventQueue_enableSensor( eventQueue, s) != 0 ){
            ALOGE("error, cannot enable sensor %s\n", ASensor_getName(s));
        }else{
            int handle = ASensor_getHandle(s);
            // write SensorDataHead to gExternalStoragePath/sensor_type file
            int fd = open((std::string(gExternalStoragePath) + "/" + std::to_string(handle)).c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0666);

            if (fd == -1) {
                ALOGE("open file error");
                return -1;
            }

            SensorDataHead hdr;
            write(fd, &hdr, sizeof(hdr));

            close(fd);
        }
    }

    /*get events from sensors*/
    int kTimeoutMilliSecs = 10000; //wait for milliseconds before returning
    int identity = 0;

    while(isStop != 1){
        /*poll for events*/
        identity = ALooper_pollAll( kTimeoutMilliSecs, NULL, NULL, NULL );
        /* if timeout */
        if (identity != looperId)
            continue;

        /*retrieve events*/
        ASensorEvent events[128];
        memset(events, 0, 128*sizeof(ASensorEvent));

        ssize_t numEvents = ASensorEventQueue_getEvents(eventQueue, events, 128);
        if ( numEvents < 1 ){
            continue; // no pending event
        }

        for (int n = 0; n < numEvents; ++n) {
            auto event = events[n];
            ALOGD("event %d\n", event.type);
            if( event.type == ASENSOR_TYPE_ACCELEROMETER ){
                ALOGE("%d [Accelerometer] x = %f  y = %f  z = %f\n",event.sensor, event.acceleration.x, event.acceleration.y, event.acceleration.z );
            }

            if ( event.type == ASENSOR_TYPE_PROXIMITY ){
                ALOGE("%d [proximity sensor] distance :%f\n",event.sensor, event.distance );
            }
            if ( event.type == ASENSOR_TYPE_GRAVITY ){
                ALOGE("%d [gravity sensor] gravity :%f,%f,%f\n",event.sensor, event.vector.v[0], event.vector.v[1], event.vector.v[2] );
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
    for(auto s : sensors){
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
    ALOGD("ndk_testor library onload , sizeof(ASensorEvent) = %d", sizeof(ASensorEvent));
    if (vm->GetEnv(reinterpret_cast<void **>(&gEnv), JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }


    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_test_ndk_Testor_testSensor(JNIEnv *env, jobject thiz, jstring dir, jintArray sensor_type_array) {
    gTestorObject = env->NewGlobalRef(thiz);
    __android_log_print(ANDROID_LOG_DEBUG, "TAG", "NdkTestor_TestSensor called");

    // get external storage path
    jboolean isCopy = true;
    gExternalStoragePath = env->GetStringUTFChars(dir, &isCopy);
    ALOGD("external storage path: %s", gExternalStoragePath);

    // jintArray to int[]
    jint *sensorTypes = env->GetIntArrayElements(sensor_type_array, nullptr);
    int numSensorTypes = env->GetArrayLength(sensor_type_array);
    for (int i = 0; i < numSensorTypes; i++) {
        __android_log_print(ANDROID_LOG_DEBUG, "TAG", "sensor type: %d", sensorTypes[i]);
    }
    startTestSensor(sensorTypes, numSensorTypes);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_test_ndk_Testor_stop(JNIEnv *env, jobject thiz) {
    // TODO: implement stop()
    isStop = true;
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

    for (int i = 0; i < numSensors; i++) {
        auto s = sensorList[i];
        auto sensorType = ASensor_getType(s);
        if (sensorType == type) {
            auto sensorName = ASensor_getName(s);
            if (strcmp(sensorName, env->GetStringUTFChars(name, nullptr)) == 0) {
                return ASensor_getHandle(s);
            }
        }
    }
    return -1;
}