#include <jni.h>
#include <stdlib.h>
#include <string>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include "Renderer.h"
#include "../Include/Logger.h"

using namespace maxstAR;

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	TraceR("JNI_OnLoad");
	
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL Java_com_maxst_videomixer_camera_CameraJNI_setScreenOrientationPortrait
(JNIEnv * env, jclass thiz, jboolean portraitMode)
{
	TraceR("Java_com_maxst_videomixer_CameraJNI_setScreenOrientationPortrait");
	Renderer::getInstance()->setScreenOrientationPortraitMode(portraitMode);
}

JNIEXPORT void JNICALL Java_com_maxst_videomixer_camera_CameraJNI_initRendering
(JNIEnv * env, jclass thiz)
{
	TraceR("Java_com_maxst_videomixer_CameraJNI_initRendering");
	Renderer::getInstance()->initRendering();
}

static int windowWidth;
static int windowHeight;

JNIEXPORT void JNICALL Java_com_maxst_videomixer_camera_CameraJNI_updateRendering
(JNIEnv * env, jclass obj, jint width, jint height)
{
	Renderer::getInstance()->updateRendering(width, height);
	
	TraceR("Java_com_maxst_videomixer_CameraJNI_updateRendering");

	windowWidth = width;
	windowHeight = height;
}

JNIEXPORT void JNICALL Java_com_maxst_videomixer_camera_CameraJNI_renderFrame
(JNIEnv * env, jclass obj)
{
	glViewport(0, 0, windowWidth, windowHeight);
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);		
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	

	Renderer::getInstance()->renderVideoBackground();
}

JNIEXPORT void JNICALL Java_com_maxst_videomixer_camera_CameraJNI_newCameraFrameAvailable
(JNIEnv * env, jclass thiz, jbyteArray camImage, jint length, jint width, jint height)
{
	float percent = 0;
	jbyte* nativeBytes = (jbyte *)env->GetPrimitiveArrayCritical(camImage, 0);
	if (nativeBytes == NULL) 
	{
		return;
	}

	Renderer::getInstance()->onNewCameraFrame((unsigned char *)nativeBytes, length, width, height);
	env-> ReleasePrimitiveArrayCritical(camImage, nativeBytes, JNI_ABORT);
}

#ifdef __cplusplus
}
#endif

