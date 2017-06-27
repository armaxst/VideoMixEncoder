#pragma  once

#ifdef __ANDROID__
#include <android/log.h>
#ifndef LOG_TAG
#define  LOG_TAG "MaxstAR"
#endif

#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define  LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)

#define TraceR(...) LOGI(__VA_ARGS__)

#ifdef _DEBUG
#define TraceD(...) LOGI(__VA_ARGS__)
#else
#define TraceD(...)
#endif

#elif defined(_WIN32)
extern void TraceR(char* szFormat, ...);
extern void TraceD(char* szFormat, ...);

#else
#define TraceR(...) printf(__VA_ARGS__);
#define TraceD(...) printf(__VA_ARGS__);
#endif
