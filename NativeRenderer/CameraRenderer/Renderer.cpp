#include "stdafx.h"

#include "Renderer.h"

#include <pthread.h>
#include <memory>

#ifdef USING_FAST_CV
#include <fastcv.h>
#endif

#include "../Include/Matrices.h"
#include "../Include/Logger.h"
#include "../PThreadRumtimeLibUtils.h"

#if defined(__IOS__) || defined(__MacOS__)
#include <stdio.h>
#include <tr1/unordered_map>
#include <tr1/memory>
#endif

#ifdef _WIN32
#include "../DllMain.h"
#endif

using namespace std;

#if defined(__IOS__) || defined(__MacOS__)
using namespace tr1;
#endif

namespace maxstAR
{
	Renderer::Renderer() 
	{
		sharedBuf = 0;
		videoBuf = 0;
		uBuf = 0;
		vBuf = 0;
		textureBuf = 0;
		texID = -1;

		xScaleFactor = 1.0f;
		yScaleFactor = 1.0f;

		rendererStop = false;

#ifdef _WIN32
		PThreadRumtimeLibUtils::loadDll(thisDllFullName);
#endif

		PThreadRumtimeLibUtils::pthreadMutexInit(&videoCopyMutex, 0);
		PThreadRumtimeLibUtils::pthreadMutexInit(&videoUploadMutex, 0);

		portrait = false;

		videoFromTracker = false;	

		videoWidth = 0;
		videoHeight = 0;
		firstCameraFrameArrived = false;

		indices[0] = 0;
		indices[1] = 1;
		indices[2] = 2;
		indices[3] = 2;
		indices[4] = 1;
		indices[5] = 3;

#if 1
		texCoords[0] = 0.0f;
		texCoords[1] = 0.0f;

		texCoords[2] = 0.0f;
		texCoords[3] = 1.0f;

		texCoords[4] = 1.0f;
		texCoords[5] = 0.0f;

		texCoords[6] = 1.0f;
		texCoords[7] = 1.0f;

#else
		texCoords[0] = 0.0f;
		texCoords[1] = 1.0f;
		
		texCoords[2] = 0.0f;
		texCoords[3] = 0.0f;
		
		texCoords[4] = 1.0f;
		texCoords[5] = 1.0f;
		
		texCoords[6] = 1.0f;
		texCoords[7] = 0.0f;
#endif		

		windowWidth = 0;
		windowHeight = 0;

#ifdef __ANDROID__
		videoColorFormat = VideoColorFormat::Yuv420;
#endif
	}

	Renderer::~Renderer() 
	{
		clear();
	}

	void Renderer::setScreenOrientationPortraitMode(bool portraitMode)
	{
		portrait = portraitMode;
	}

	void Renderer::initRendering()
	{
		rendererStop = false;

		clear();
	}

	void Renderer::stopRendering() 
	{
		rendererStop = true;

		clear();
	}

	void Renderer::onNewCameraFrame(unsigned char * data, int length, int width, int height)
	{
		if (videoFromTracker) 
		{
			return;
		}

		if (rendererStop)
		{
			return;
		}

		if (firstCameraFrameArrived == false || videoWidth != width || videoHeight != height)
		{
			firstCameraFrameArrived = true;
			videoWidth = width;
			videoHeight = height;
			videoBufSize = length;

			videoCopyLock();
			videoUploadLock();

			freeBuffers();
			allocBuffers(videoWidth, videoHeight);

			videoUploadUnlock();
			videoCopyUnlock();
		}

		videoCopyLock();
		memcpy(sharedBuf, data, length);			
		videoCopyUnlock();
	}

	void Renderer::updateRendering(int width, int height)
	{
		windowWidth = width;
		windowHeight = height;
	}

	void Renderer::getVideoFrame(unsigned char * buf)
	{
		videoUploadLock();
		getTextureFromCameraFrame();
		memcpy(buf, textureBuf, videoWidth * videoHeight * 3);
		videoUploadUnlock();
	}

#ifdef USING_FAST_CV
	void Renderer::convertYuv420RGB565()
	{
		videoCopyLock();
		if (videoBuf && sharedBuf)
		{
			memcpy( videoBuf, sharedBuf, videoBufSize);
		}	
		videoCopyUnlock();

		videoUploadLock();
		if (videoBuf && textureBuf)
		{
			fcvColorYUV420toRGB565u8(
				videoBuf,
				videoWidth,
				videoHeight, 
				(uint32_t*)textureBuf );
		}	
		videoUploadUnlock();		   
	}
#endif	

	void Renderer::clear()
	{
		videoCopyLock();
		videoUploadLock();

		freeBuffers();

		xScaleFactor = 1.0f;
		yScaleFactor = 1.0f;

		videoWidth = 0;
		videoHeight = 0;

		windowWidth = 0;
		windowHeight = 0;		

		firstCameraFrameArrived = false;
		texID = -1;

		videoUploadUnlock();
		videoCopyUnlock();
	}

	void Renderer::videoCopyLock()
	{
		PThreadRumtimeLibUtils::pthreadMutexLock(&videoCopyMutex);
	}

	void Renderer::videoCopyUnlock()
	{
		PThreadRumtimeLibUtils::pthreadMutexUnlock(&videoCopyMutex);
	}

	void Renderer::videoUploadLock()
	{
		PThreadRumtimeLibUtils::pthreadMutexLock(&videoUploadMutex);
	}

	void Renderer::videoUploadUnlock()
	{
		PThreadRumtimeLibUtils::pthreadMutexUnlock(&videoUploadMutex);
	}

	void Renderer::getTextureFromCameraFrame()
	{
		if (textureBuf == NULL || sharedBuf == NULL)
		{
			return;
		}

		if (videoColorFormat == VideoColorFormat::Rgb24)
		{
			memcpy(textureBuf, sharedBuf, videoBufSize); 
		}
		else if (videoColorFormat == VideoColorFormat::Yuv420)
		{
#ifdef USING_FAST_CV		
			convertYuv420RGB565();
#endif				
		}
	}

	void Renderer::allocBuffers(int width, int height, VideoColorFormat format)
	{
		if (format == VideoColorFormat::Rgb24)
		{
			videoBuf = new unsigned char[videoBufSize];
			textureBuf = new unsigned char[videoBufSize];
		}
		else if (format == VideoColorFormat::Yuv420)
		{
			TraceR("Renderer pixelFormat Yuv420");
#ifdef USING_FAST_CV
			textureBuf = (uint8_t*) fcvMemAlloc(width * height * 2, 16);
			videoBuf = (uint8_t*)fcvMemAlloc( videoBufSize, 16 );
#else
			uBuf = new unsigned char[width * height / 4];
			vBuf = new unsigned char[width * height / 4];
			videoBuf = new unsigned char[videoBufSize];
			textureBuf = new unsigned char[width * height * 4];
#endif				
		} 
		else 
		{
			TraceR("renerer pixelFormat is null");
		}

		sharedBuf = new unsigned char[videoBufSize];
	}

	void Renderer::freeBuffers()
	{
		if (sharedBuf != 0)
		{
			delete sharedBuf;
			sharedBuf = NULL;
		}

		if (uBuf != 0)
		{
			delete uBuf;
			uBuf = NULL;
		}

		if (vBuf != 0)
		{
			delete vBuf;
			vBuf = NULL;
		}

#if defined(USING_FAST_CV)
		if (textureBuf != 0)
		{
			fcvMemFree(textureBuf);
			textureBuf = NULL;
		}

		if (videoBuf != 0)
		{
			fcvMemFree(videoBuf);
			videoBuf = NULL;
		}
#else			
		if (videoBuf != 0)
		{
			delete videoBuf;
			videoBuf = NULL;
		}			

		if (textureBuf != NULL) 
		{
			delete textureBuf;
			textureBuf = NULL;
		}
#endif
	}

	void Renderer::resetVideoBackgroundPanel()
	{
		int w = windowWidth;
		int h = windowHeight;

		if (portrait)
		{
			w = windowHeight;
			h = windowWidth;
		}

		vertices[0] = -w / 2.0f;
		vertices[1] = -h / 2.0f;
		
		vertices[2] = -w / 2.0f;
		vertices[3] = h / 2.0f;
		
		vertices[4] = w / 2.0f;
		vertices[5] = -h / 2.0f;
		
		vertices[6] = w / 2.0f;
		vertices[7] = h / 2.0f;

		float camRatio = (float)videoWidth / videoHeight;
		float windowRatio = (float)w / h;

		if (camRatio > windowRatio)
		{
			xScaleFactor = camRatio / windowRatio;
			yScaleFactor = 1.0f;
		}
		else
		{
			xScaleFactor = 1.0f;
			yScaleFactor = windowRatio / camRatio;
		}
	}
} // namespace maxstAR
