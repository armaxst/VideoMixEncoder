LOCAL_PATH := $(call my-dir)
MY_LOCAL_PATH := $(LOCAL_PATH)

include $(CLEAR_VARS)
include $(LOCAL_PATH)/FastCV/Android.mk

include $(CLEAR_VARS)
LOCAL_PATH := $(MY_LOCAL_PATH)
LOCAL_CFLAGS += -DNDEBUG -std=c++11

ifeq ($(DEBUG), yes)
LOCAL_CFLAGS += -D_DEBUG
else
$(info RELEASE mode)
endif

CAMERA_RENDERER_SRC_PATH := CameraRenderer
CAMERA_RENDERER_SRC := \
	$(CAMERA_RENDERER_SRC_PATH)/CameraJNI.cpp \
	$(CAMERA_RENDERER_SRC_PATH)/Renderer.cpp \
	$(CAMERA_RENDERER_SRC_PATH)/RendererGLES20.cpp

VIDEO_RENDERER_SRC_PATH := VideoRenderer
VIDEO_RENDERER_SRC := \
	$(VIDEO_RENDERER_SRC_PATH)/VideoPlayerJni.cpp \
	$(VIDEO_RENDERER_SRC_PATH)/VideoRenderer.cpp

RENDER_TEXTURE_SRC_PATH := RenderTexture
RENDER_TEXTURE_SRC := \
	$(RENDER_TEXTURE_SRC_PATH)/RenderTextureJNI.cpp \
	$(RENDER_TEXTURE_SRC_PATH)/RenderTexture.cpp

LOCAL_SRC_FILES += \
	PThreadRumtimeLibUtils.cpp \
	$(CAMERA_RENDERER_SRC) \
	$(VIDEO_RENDERER_SRC) \
	$(RENDER_TEXTURE_SRC)
	
LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/Include

LOCAL_MODULE := NativeRenderer

LOCAL_STATIC_LIBRARIES += cpufeatures
LOCAL_LDLIBS := -llog -landroid -lGLESv2 -lz
LOCAL_ARM_MODE := arm

LOCAL_CFLAGS += -DUSING_FAST_CV
LOCAL_STATIC_LIBRARIES += prebuilt-fastCV

include $(BUILD_SHARED_LIBRARY)

$(call import-module,cpufeatures)
