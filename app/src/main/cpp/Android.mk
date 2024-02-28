LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := crashtest
LOCAL_SRC_FILES += crashtest.cpp
# 添加 C++ 标志和链接库
LOCAL_CPPFLAGS := -std=c++14
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lm -lz
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := hidtest
LOCAL_SRC_FILES += hid_gadget_test.c
# 添加 C++ 标志和链接库
LOCAL_CPPFLAGS := -std=c++14
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lm -lz
include $(BUILD_SHARED_LIBRARY)
