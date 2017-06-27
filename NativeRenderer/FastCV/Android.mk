LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := prebuilt-fastCV
LOCAL_SRC_FILES := lib/libfastcv.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/inc

include $(PREBUILT_STATIC_LIBRARY)