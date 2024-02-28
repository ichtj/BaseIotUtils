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
#include "log.h"

// 定义一个全局变量用于存储 Java 中的回调对象
jobject g_callbackObject;
JavaVM *g_javaVM;
bool g_monitoring = false;
int g_hidFileDescriptor = -1;

JNIEXPORT jint JNICALL
Java_com_wave_1chtj_example_hid_HidTools_sendCmds(JNIEnv *env, jclass clazz, jbyteArray data) {
    const char *filename = NULL;
    int fd = 0;
    jbyte *buffer = (*env)->GetByteArrayElements(env, data, NULL);
    jsize length = (*env)->GetArrayLength(env, data);

    filename = "/dev/hidg0";
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
    LOGD("writeData>>%s",send_buffer);
    // 写入数据到USB设备
    int ret = write(fd, send_buffer, length);
    if (ret < 0) {
        LOGE("write err");
    }
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
        LOGD("ichtj>%c", (char)elements[i]);
    }
    (*env)->ReleaseByteArrayElements(env,byteArray, elements, JNI_ABORT);
}


// JNI回调函数，用于回调数据到Java层
void sendDataToJava(JNIEnv *env, jbyteArray data) {
    printJByteArray(env,data);
    // 获取Java回调方法ID
    jclass cls = (*env)->GetObjectClass(env,g_callbackObject);
    jmethodID methodID = (*env)->GetMethodID(env,cls, "receive", "([B)V");
    if (methodID == NULL) {
        return;
    }
    // 调用Java回调方法
    (*env)->CallVoidMethod(env,g_callbackObject, methodID, data);
}

// 监控HID设备的线程函数
void *receiveData(void *arg) {
    JNIEnv *env;
    // 通过全局变量获取JavaVM
    (*g_javaVM)->AttachCurrentThread(g_javaVM,&env, NULL);
    while (g_monitoring) {
        // 读取数据
        char receive_buffer[200];  // 假设接收数据的缓冲区大小为1024字节
        int ret = read(g_hidFileDescriptor, receive_buffer, sizeof(receive_buffer));
        if (ret < 0) {
            perror("read");
            close(g_hidFileDescriptor);
            return NULL;
        }

        // 将接收到的数据转换为Java字节数组
        jbyteArray result = (*env)->NewByteArray(env,ret);
        (*env)->SetByteArrayRegion(env,result, 0, ret, (jbyte *)receive_buffer);
        sendDataToJava(env,result);
    }
    // 关闭USB设备
    close(g_hidFileDescriptor);
    // 分离当前线程
    (*g_javaVM)->DetachCurrentThread(g_javaVM);
    return NULL;
}

// JNI方法：开始监控HID设备
JNIEXPORT void JNICALL
Java_com_wave_1chtj_example_hid_HidTools_startMonitoring(JNIEnv *env, jobject thiz, jobject callback) {
    // 保存Java层的回调对象
    g_callbackObject = (*env)->NewGlobalRef(env,callback);
    // 获取JavaVM
    (*env)->GetJavaVM(env,&g_javaVM);
    // 打开HID设备文件
    g_hidFileDescriptor = open("/dev/hidg0", O_RDWR);
    if (g_hidFileDescriptor < 0) {
        LOGE("g_hidFileDescriptor err");
        // 失败处理
        return;
    }
    // 设置监控标志为true
    g_monitoring = true;
    // 创建一个新的pthread线程来监控HID设备
    pthread_t thread;
    pthread_create(&thread, NULL, receiveData, NULL);
    pthread_detach(thread);
}

// JNI方法：停止监控HID设备
JNIEXPORT void JNICALL
Java_com_wave_1chtj_example_hid_HidTools_stopMonitoring(JNIEnv *env, jobject thiz) {
    // 设置监控标志为false
    g_monitoring = false;
}



