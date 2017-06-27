#include "stdafx.h"
#include "PThreadRumtimeLibUtils.h"

#ifdef _WIN32
pthread_create_func PThreadRumtimeLibUtils::pthreadCreateFunc = NULL;
pthread_join_func PThreadRumtimeLibUtils::pthreadJoinFunc = NULL;
pthread_mutex_init_func PThreadRumtimeLibUtils::pthreadMutexInitFunc = NULL;
pthread_mutex_lock_func PThreadRumtimeLibUtils::pthreadMutexLockFunc = NULL;
pthread_mutex_unlock_func PThreadRumtimeLibUtils::pthreadMutexUnlockFunc = NULL;

HINSTANCE PThreadRumtimeLibUtils::pthreadVC2DllHandle = NULL;
#endif

void PThreadRumtimeLibUtils::loadDll(wstring dllFullName)
{
#ifdef _WIN32
	if (pthreadVC2DllHandle != NULL)
	{
		return;
	}

	wstring wstrDllFullPathDir = dllFullName.substr(0, dllFullName.find_last_of(L"\\"));
	pthreadVC2DllHandle = LoadLibrary((wstrDllFullPathDir + L"\\pthreadVC2.dll").c_str());

	pthreadMutexInitFunc = (pthread_mutex_init_func)GetProcAddress(pthreadVC2DllHandle, "pthread_mutex_init");
	pthreadCreateFunc = (pthread_create_func)GetProcAddress(pthreadVC2DllHandle, "pthread_create");
	pthreadJoinFunc = (pthread_join_func)GetProcAddress(pthreadVC2DllHandle, "pthread_join");
	pthreadMutexLockFunc = (pthread_mutex_lock_func)GetProcAddress(pthreadVC2DllHandle, "pthread_mutex_lock");
	pthreadMutexUnlockFunc = (pthread_mutex_unlock_func)GetProcAddress(pthreadVC2DllHandle, "pthread_mutex_unlock");
#endif
}

int PThreadRumtimeLibUtils::pthreadCreate(pthread_t * tid, const pthread_attr_t * attr, void *(*start) (void *), void *arg)
{
	return pthreadCreateFunc(tid, attr, start, arg);
}

void PThreadRumtimeLibUtils::pthreadMutexInit(pthread_mutex_t * mutex, pthread_mutexattr_t * attr)
{
	pthreadMutexInitFunc(mutex, attr);
}

void PThreadRumtimeLibUtils::pthreadMutexLock(pthread_mutex_t * mutex)
{
	pthreadMutexLockFunc(mutex);
}

void PThreadRumtimeLibUtils::pthreadMutexUnlock(pthread_mutex_t * mutex)
{
	pthreadMutexUnlockFunc(mutex);
}

void PThreadRumtimeLibUtils::pthreadJoin(pthread_t thread, void **value_ptr)
{
	pthreadJoinFunc(thread, value_ptr);
}