#pragma once

#include <pthread.h>
#include <string>

using namespace std;

#ifdef _WIN32
typedef int (PTW32_CDECL * pthread_create_func) (pthread_t * tid,
												 const pthread_attr_t * attr,
												 void *(PTW32_CDECL *start) (void *),
												 void *arg);

typedef int (PTW32_CDECL * pthread_join_func) (pthread_t thread,
											   void **value_ptr);

typedef int (PTW32_CDECL * pthread_mutex_init_func) (pthread_mutex_t * mutex,
													 const pthread_mutexattr_t * attr);

typedef int (PTW32_CDECL * pthread_mutex_lock_func) (pthread_mutex_t * mutex);

typedef int (PTW32_CDECL * pthread_mutex_unlock_func) (pthread_mutex_t * mutex);
#else
#define pthreadCreateFunc pthread_create
#define pthreadJoinFunc pthread_join
#define pthreadMutexInitFunc pthread_mutex_init
#define pthreadMutexLockFunc pthread_mutex_lock
#define pthreadMutexUnlockFunc pthread_mutex_unlock
#endif

class PThreadRumtimeLibUtils
{
public:
	static void loadDll(wstring thisDllPath);
	static int pthreadCreate(pthread_t * tid, const pthread_attr_t * attr, void *(*start) (void *), void *arg);
	static void pthreadMutexInit(pthread_mutex_t * mutex, pthread_mutexattr_t * attr);
	static void pthreadMutexLock(pthread_mutex_t * mutex);
	static void pthreadMutexUnlock(pthread_mutex_t * mutex);
	static void pthreadJoin(pthread_t thread, void **value_ptr);

private:
#ifdef _WIN32
	static pthread_create_func pthreadCreateFunc;
	static pthread_join_func pthreadJoinFunc;
	static pthread_mutex_init_func pthreadMutexInitFunc;
	static pthread_mutex_lock_func pthreadMutexLockFunc;
	static pthread_mutex_unlock_func pthreadMutexUnlockFunc;

	static HINSTANCE pthreadVC2DllHandle;
#endif
};