//
// Created by ichtj on 2026/3/27.
//

#ifndef BASEIOTUTILS_LOGTOOLS_H
#define BASEIOTUTILS_LOGTOOLS_H
#define TAG "DualCameraJNI"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#endif //BASEIOTUTILS_LOGTOOLS_H
