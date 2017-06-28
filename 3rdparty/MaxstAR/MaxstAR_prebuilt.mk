LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := MaxstAR-prebuilt
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libMaxstAR.so
include $(PREBUILT_SHARED_LIBRARY)
