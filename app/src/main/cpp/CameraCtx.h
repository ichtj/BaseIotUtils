//
// Created by ichtj on 2026/3/27.
//
// JNI
#include <jni.h>

// 日志
#include <android/log.h>

// Linux系统
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/mman.h>

// 线程
#include <pthread.h>

// V4L2（摄像头核心）
#include <linux/videodev2.h>

// 标准库
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#ifndef BASEIOTUTILS_CAMERACTX_H
#define BASEIOTUTILS_CAMERACTX_H


struct CameraCtx {
    int cameraId;
    int fd;

    int width;
    int height;

    bool previewRunning;

    uint8_t* previewBuf;
    uint8_t* captureBuf;

    JavaVM* jvm;
    jobject obj;

    pthread_t previewThread;
};


#endif //BASEIOTUTILS_CAMERACTX_H
