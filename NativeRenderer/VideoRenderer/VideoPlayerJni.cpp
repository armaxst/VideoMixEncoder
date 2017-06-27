#include <jni.h>
#include "VideoRenderer.h"

using namespace maxstAR;

#ifdef __cplusplus
extern "C"
{
#endif

static VideoRenderer videoRenderer;

JNIEXPORT int JNICALL
Java_com_maxst_videoPlayer_VideoPlayer_initVideoTexture(JNIEnv* env, jobject obj)
{
	return	videoRenderer.initVideoTexture();
}

JNIEXPORT void JNICALL
Java_com_maxst_videoPlayer_VideoPlayer_initVideoPlane(JNIEnv* env, jobject obj, jint width, jint height)
{
	videoRenderer.initVideoPlane(width, height);
}

JNIEXPORT void JNICALL
Java_com_maxst_videoPlayer_VideoPlayer_drawVideo(JNIEnv* env, jobject obj, jint surfaceTextureID, jfloatArray textureMat)
{
	float *textureMatArray = env->GetFloatArrayElements(textureMat, 0);
	videoRenderer.drawVideo(surfaceTextureID, textureMatArray);
	env->ReleaseFloatArrayElements(textureMat, textureMatArray, 0);
}


#ifdef __cplusplus
}
#endif
