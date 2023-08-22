LOCAL_PATH := $(call my-dir)
# 设置支持的 ABI 架构
APP_ABI := armeabi-v7a arm64-v8a

include $(CLEAR_VARS)
LOCAL_MODULE := crashtest
LOCAL_SRC_FILES := crashtest.c
include $(BUILD_SHARED_LIBRARY)
