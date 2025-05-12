#include <jni.h>
#include <pthread.h>
#include <string.h>
#include <stdio.h>
#include <ctype.h>
#include <fcntl.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdbool.h>
#include <jni.h>
#include <android/log.h>

#define TAG        "HidTools"

#define LOGI(...)    __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...)    __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGW(...)    __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define    LOGE(...)    __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
// 定义一个全局变量用于存储 Java 中的回调对象
jobject g_callbackObject;
JavaVM *g_javaVM;
bool g_monitoring = false;
int g_hidFileDescriptor = -1;
pthread_mutex_t g_callbackMutex = PTHREAD_MUTEX_INITIALIZER;
// 线程句柄
pthread_t g_receiveThread = 0;
pthread_t g_monitorThread = 0;

// JNI方法：开始监控HID设备
void releaseDev(JNIEnv const *env,bool isCallback);

JNIEXPORT void callbackConn(JNIEnv const *env, bool connect,bool isCallback) {// 获取Java回调方法ID
    if (isCallback){
        pthread_mutex_lock(&g_callbackMutex);
        if (g_callbackObject == NULL) {
            pthread_mutex_unlock(&g_callbackMutex);
            return;
        }
        jclass cls = (*env)->GetObjectClass(env, g_callbackObject);
        jmethodID methodID = (*env)->GetMethodID(env, cls, "connect", "(Z)V");
        if (cls == NULL || methodID == NULL) {
            pthread_mutex_unlock(&g_callbackMutex);
            return;
        }
        (*env)->CallVoidMethod(env, g_callbackObject, methodID, connect);
        pthread_mutex_unlock(&g_callbackMutex);
    }
}

JNIEXPORT jint JNICALL
Java_com_ichtj_basetools_hid_HidTools_sendCmds(JNIEnv *env, jclass clazz, jstring dev,
                                               jbyteArray data) {
    const char *filename = (*env)->GetStringUTFChars(env, dev, NULL);
    int fd = 0;
    jbyte *buffer = (*env)->GetByteArrayElements(env, data, NULL);
    jsize length = (*env)->GetArrayLength(env, data);
    if ((fd = open(filename, O_RDWR, 0666)) == -1) {
        LOGE("filename open err");
        return -1;
    }
    // 准备发送的数据
    char *send_buffer = (char *) malloc(length);
    if (!send_buffer) {
        LOGE("malloc err");
        close(fd);
        return -2;
    }
    memcpy(send_buffer, buffer, length);
    // 打印发送的数据为十六进制
    for (int i = 0; i < length; i++) {
        LOGD("writeData[%d] = 0x%02x", i, (unsigned char) send_buffer[i]);
    }
    // 写入数据到USB设备
    int ret = write(fd, send_buffer, length);
    LOGD("ret>>%d", ret);
    // 关闭USB设备
    close(fd);
    free(send_buffer);
    // 释放本地引用
    (*env)->ReleaseByteArrayElements(env, data, buffer, JNI_ABORT);
    return 0;
}

void printJByteArray(JNIEnv *env, jbyteArray byteArray) {
    jsize length = (*env)->GetArrayLength(env, byteArray);
    jbyte *elements = (*env)->GetByteArrayElements(env, byteArray, NULL);
    if (elements == NULL) {
        return;
    }
    for (int i = 0; i < length; ++i) {
//        LOGD("ichtj>%c", (char)elements[i]);
    }
    (*env)->ReleaseByteArrayElements(env, byteArray, elements, JNI_ABORT);
}


// JNI回调函数，用于回调数据到Java层
void callbackData(JNIEnv *env, jbyteArray data) {
    printJByteArray(env, data);
    // 获取Java回调方法ID
    jclass cls = (*env)->GetObjectClass(env, g_callbackObject);
    jmethodID methodID = (*env)->GetMethodID(env, cls, "receive", "([B)V");
    if (methodID == NULL) {
        return;
    }
    // 调用Java回调方法
    (*env)->CallVoidMethod(env, g_callbackObject, methodID, data);
    LOGD("callback data complete");
}

// 监控HID设备的线程函数
void *receiveData(void *arg) {
    LOGD("receiveData start>>");
    JNIEnv *env;
    // 通过全局变量获取JavaVM
    (*g_javaVM)->AttachCurrentThread(g_javaVM, &env, NULL);
    while (g_monitoring) {
        LOGD("read>> g_monitoring>>%d", g_monitoring);
        char receive_buffer[1024];  // 假设接收数据的缓冲区大小为1024字节
        int ret = read(g_hidFileDescriptor, receive_buffer, sizeof(receive_buffer));
        if (ret < 0) {
            LOGE("read err");
            close(g_hidFileDescriptor);
            return NULL;
        }
        LOGD("read>> data.length>>%d", receive_buffer);
        // 将接收到的数据转换为Java字节数组
        jbyteArray result = (*env)->NewByteArray(env, ret);
        (*env)->SetByteArrayRegion(env, result, 0, ret, (jbyte *) receive_buffer);
        callbackData(env, result);
    }
    // 关闭USB设备
    close(g_hidFileDescriptor);
    // 分离当前线程
    (*g_javaVM)->DetachCurrentThread(g_javaVM);
    LOGD("receiveData end>>");
    return NULL;
}


void *monitorHidDevice(void *arg) {
    LOGD("monitorHidDevice start>>");
    JNIEnv *env;
    (*g_javaVM)->AttachCurrentThread(g_javaVM, &env, NULL);

    while (g_monitoring) {
        sleep(1); // 每1秒检查一次
        LOGD("monitorHidDevice>>start");
        FILE *fp = popen("lsof /dev/hidg0", "r");
        if (fp == NULL) {
            LOGE("Failed to run lsof command");
            break;
        }

        char buffer[128];
        bool hasProcess = false;
        while (fgets(buffer, sizeof(buffer), fp) != NULL) {
            hasProcess = true;
            break; // 只要有一行输出，就说明设备被占用
        }
        pclose(fp);

        if (!hasProcess) {
            LOGE("No process is using /dev/hidg0, stopping monitoring");
            releaseDev(env,true);
            break;
        }
    }

    (*g_javaVM)->DetachCurrentThread(g_javaVM);
    LOGD("monitorHidDevice end>>");
    return NULL;
}



// JNI方法：停止监控HID设备
JNIEXPORT void releaseDev(JNIEnv const *env,bool isCallback) {
    g_monitoring = false;
    callbackConn(env, false,isCallback);
    pthread_mutex_lock(&g_callbackMutex);
    if (g_callbackObject != NULL) {
        (*env)->DeleteGlobalRef(env, g_callbackObject);
        g_callbackObject = NULL;
    }
    pthread_mutex_unlock(&g_callbackMutex);
    if (g_hidFileDescriptor!=-1){
        close(g_hidFileDescriptor);
    }
    g_hidFileDescriptor=-1;
    g_receiveThread=0;
    g_monitorThread=0;
    close(g_hidFileDescriptor);
    LOGE("stopMonitoring end");
}


void JNICALL
Java_com_ichtj_basetools_hid_HidTools_init(JNIEnv *env, jobject thiz, jstring dev,
                                           jobject callback) {
    LOGE("HidInit g_monitoring=%d",g_monitoring);
    // 如果已经在监控，就先释放资源
    if (g_monitoring) {
        LOGE("Already monitoring, restarting...");
        releaseDev(env,false);
    }
    const char *filename = (*env)->GetStringUTFChars(env, dev, NULL);
    // 保存Java层的回调对象
    g_callbackObject = (*env)->NewGlobalRef(env, callback);
    // 获取JavaVM
    (*env)->GetJavaVM(env, &g_javaVM);
    // 打开HID设备文件
    g_hidFileDescriptor = open(filename, O_RDWR, 0666);
    if (g_hidFileDescriptor < 0) {
        LOGE("g_hidFileDescriptor err");
        // 失败处理
        callbackConn(env, false,true);
        return;
    }
    // 设置监控标志为true
    g_monitoring = true;
    LOGE("g_receiveThread=%d",g_receiveThread);
    // 只在 `g_receiveThread` 为空时创建线程
    if (g_receiveThread == 0) {
        pthread_create(&g_receiveThread, NULL, receiveData, NULL);
    }

    // 只在 `g_monitorThread` 为空时创建线程
    LOGE("g_monitorThread=%d",g_monitorThread);
    if (g_monitorThread == 0) {
        LOGE("g_monitorThread=0");
        pthread_create(&g_monitorThread, NULL, monitorHidDevice, NULL);
    }
    callbackConn(env, true,true);
}


void JNICALL
Java_com_ichtj_basetools_hid_HidTools_stopMonitoring(JNIEnv *env, jobject thiz) {
    LOGE("stopMonitoring start");
    releaseDev(env,true);
}



