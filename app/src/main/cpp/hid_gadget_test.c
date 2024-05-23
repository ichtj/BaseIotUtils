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

#define TAG		"HidTools"

#define LOGI(...)	__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...)	__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGW(...)	__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define	LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
// 定义一个全局变量用于存储 Java 中的回调对象
jobject g_callbackObject;
JavaVM *g_javaVM;
bool g_monitoring = false;
int g_hidFileDescriptor = -1;

// JNI方法：开始监控HID设备
JNIEXPORT void callbackConn(JNIEnv const *env,bool connect) {// 获取Java回调方法ID
    jclass cls = (*env)->GetObjectClass(env,g_callbackObject);
    jmethodID methodID = (*env)->GetMethodID(env,cls, "connect", "(Z)V");
    if (methodID == NULL) {
        return;
    }
    // 调用Java回调方法
    (*env)->CallVoidMethod(env,g_callbackObject, methodID, connect);
}

JNIEXPORT jint JNICALL
Java_com_ichtj_basetools_hid_HidTools_sendCmds(JNIEnv *env, jclass clazz,jstring dev, jbyteArray data) {
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
        LOGD("writeData[%d] = 0x%02x", i, (unsigned char)send_buffer[i]);
    }
    // 写入数据到USB设备
    int ret = write(fd, send_buffer, length);
    LOGD("ret>>%d",ret);
    // 关闭USB设备
    close(fd);
    free(send_buffer);
    // 释放本地引用
    (*env)->ReleaseByteArrayElements(env,data, buffer, JNI_ABORT);
    return 0;
}

void printJByteArray(JNIEnv *env, jbyteArray byteArray) {
    jsize length = (*env)->GetArrayLength(env,byteArray);
    jbyte *elements = (*env)->GetByteArrayElements(env,byteArray, NULL);
    if (elements == NULL) {
        return;
    }
    for (int i = 0; i < length; ++i) {
//        LOGD("ichtj>%c", (char)elements[i]);
    }
    (*env)->ReleaseByteArrayElements(env,byteArray, elements, JNI_ABORT);
}


// JNI回调函数，用于回调数据到Java层
void callbackData(JNIEnv *env, jbyteArray data) {
    printJByteArray(env,data);
    // 获取Java回调方法ID
    jclass cls = (*env)->GetObjectClass(env,g_callbackObject);
    jmethodID methodID = (*env)->GetMethodID(env,cls, "receive", "([B)V");
    if (methodID == NULL) {
        return;
    }
    // 调用Java回调方法
    (*env)->CallVoidMethod(env,g_callbackObject, methodID, data);
    LOGD("callback data complete");
}

// 监控HID设备的线程函数
void *receiveData(void *arg) {
    JNIEnv *env;
    // 通过全局变量获取JavaVM
    (*g_javaVM)->AttachCurrentThread(g_javaVM,&env, NULL);
    while (g_monitoring) {
        char receive_buffer[1024];  // 假设接收数据的缓冲区大小为1024字节
        int ret = read(g_hidFileDescriptor, receive_buffer, sizeof(receive_buffer));
        if (ret < 0) {
            LOGE("read err");
            close(g_hidFileDescriptor);
            return NULL;
        }
        LOGD("read>> data.length>>%d",receive_buffer);
        // 将接收到的数据转换为Java字节数组
        jbyteArray result = (*env)->NewByteArray(env,ret);
        (*env)->SetByteArrayRegion(env,result, 0, ret, (jbyte *)receive_buffer);
        callbackData(env,result);
    }
    // 关闭USB设备
    close(g_hidFileDescriptor);
    // 分离当前线程
    (*g_javaVM)->DetachCurrentThread(g_javaVM);
    return NULL;
}


void JNICALL
Java_com_ichtj_basetools_hid_HidTools_init(JNIEnv *env, jobject thiz,jstring dev, jobject callback) {
    const char *filename = (*env)->GetStringUTFChars(env, dev, NULL);
    // 保存Java层的回调对象
    g_callbackObject = (*env)->NewGlobalRef(env,callback);
    // 获取JavaVM
    (*env)->GetJavaVM(env,&g_javaVM);
    // 打开HID设备文件
    g_hidFileDescriptor = open(filename, O_RDWR, 0666);
    if (g_hidFileDescriptor < 0) {
        LOGE("g_hidFileDescriptor err");
        // 失败处理
        callbackConn(env,false);
        return;
    }
    // 设置监控标志为true
    g_monitoring = true;
    // 创建一个新的pthread线程来监控HID设备
    pthread_t thread;
    pthread_create(&thread, NULL, receiveData, NULL);
    pthread_detach(thread);
    callbackConn(env,true);
}

// JNI方法：停止监控HID设备
JNIEXPORT void JNICALL
Java_com_ichtj_basetools_hid_HidTools_stopMonitoring(JNIEnv *env, jobject thiz) {
    // 设置监控标志为false
    g_monitoring = false;
    callbackConn(env,false);
    if (g_callbackObject != NULL) {
        (*env)->DeleteGlobalRef(env,g_callbackObject);
        g_callbackObject = NULL;
    }
}



