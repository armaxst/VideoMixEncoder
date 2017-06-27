LOCAL_PATH := $(call my-dir)

MY_PROJECT_PATH := $(LOCAL_PATH)

include $(CLEAR_VARS)
NATIVE_RENDERER_PATH := $(LOCAL_PATH)/../../../NativeRenderer
include $(NATIVE_RENDERER_PATH)/Android.mk
