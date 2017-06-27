#include <jni.h>
#include "RenderTexture.h"

using namespace maxstAR;

#ifdef __cplusplus
extern "C"
{
#endif

static RenderTexture renderTexture;

JNIEXPORT int JNICALL
Java_com_maxst_videomixer_gl_RenderTexture_initTargetTexture(JNIEnv* env, jobject obj)
{
	return	renderTexture.initTexture();
}

JNIEXPORT void JNICALL
Java_com_maxst_videomixer_gl_RenderTexture_initFBO(JNIEnv* env, jobject obj, jint width, jint height)
{
	renderTexture.initFBO(width, height);
}

JNIEXPORT void JNICALL
Java_com_maxst_videomixer_gl_RenderTexture_startRTT(JNIEnv* env, jobject obj)
{
	renderTexture.startRTT();
}

JNIEXPORT void JNICALL
Java_com_maxst_videomixer_gl_RenderTexture_endRTT(JNIEnv* env, jobject obj)
{
	renderTexture.endRTT();
}

JNIEXPORT void JNICALL
Java_com_maxst_videomixer_gl_RenderTexture_drawTexture(JNIEnv* env, jobject obj)
{
	renderTexture.drawTexture();
}



#ifdef __cplusplus
}
#endif
