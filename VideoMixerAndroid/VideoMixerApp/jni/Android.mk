LOCAL_PATH := $(call my-dir)

VIDEO_MIXER_APP_PROJECT_PATH := $(LOCAL_PATH)

include $(CLEAR_VARS)
NATIVE_RENDERER_PATH := $(LOCAL_PATH)/../../../NativeRenderer
include $(NATIVE_RENDERER_PATH)/Android.mk

include $(CLEAR_VARS)
LOCAL_PATH := $(VIDEO_MIXER_APP_PROJECT_PATH)
MAXSTAR_PATH := $(LOCAL_PATH)/../../../3rdparty/MaxstAR
include $(MAXSTAR_PATH)/MaxstAR_prebuilt.mk
