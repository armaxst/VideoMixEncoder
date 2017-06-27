LOCAL_PATH := $(call my-dir)
MY_LOCAL_PATH := $(LOCAL_PATH)

include $(CLEAR_VARS)
LOCAL_PATH := $(MY_LOCAL_PATH)
LOCAL_CFLAGS += -DNDEBUG -std=c++11

ifeq ($(DEBUG), yes)
LOCAL_CFLAGS += -D_DEBUG
else
$(info RELEASE mode)
endif

VIDEO_PLAYER_SRC := \
	AndroidVideoPlayerManager.cpp \
	VideoPlayerJni.cpp \
	VideoPlayerInterface.cpp

LOCAL_SRC_FILES += \
	$(VIDEO_PLAYER_SRC)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include

LOCAL_MODULE := VideoPlayer

LOCAL_STATIC_LIBRARIES += cpufeatures
LOCAL_LDLIBS := -llog -landroid -lGLESv2 -lz
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)