#pragma once

#include <pthread.h>
#include "../include/Matrices.h"
#include <memory>

#ifdef USING_FAST_CV
#include <fastcv.h>
#endif

#if defined(__IOS__) || defined(__MacOS__)
#include <tr1/unordered_map>
#include <tr1/memory>
#endif

using namespace std;

#if defined(__IOS__) || defined(__MacOS__)
using namespace tr1;
#endif

#include "../include/Logger.h"

#define GL_BGR_EXT 0x80E0

namespace maxstAR
{
	enum VideoColorFormat
	{
		Rgb24 = 0,
		Yuv420 = 1
	};

	class Renderer
	{
	public:

		static shared_ptr<Renderer> getInstance();

		Renderer();

		virtual ~Renderer();

		virtual void setScreenOrientationPortraitMode(bool portraitMode);

		virtual void initRendering();

		virtual void stopRendering();

		virtual void onNewCameraFrame(unsigned char * data, int length, int width, int height);

		virtual void updateRendering(int width, int height);

		virtual void renderVideoBackground( ) = 0;

		virtual void getVideoFrame(unsigned char * buf);

	protected :

#ifdef USING_FAST_CV
		virtual void convertYuv420RGB565();
#endif

		virtual void drawVideoBackground() = 0;

		virtual void clear();

		virtual void videoCopyLock();

		virtual void videoCopyUnlock();

		virtual void videoUploadLock();

		virtual void videoUploadUnlock();

		virtual void getTextureFromCameraFrame();

		virtual void allocBuffers(int width, int height, VideoColorFormat format = VideoColorFormat::Yuv420);

		virtual void freeBuffers();

		virtual void initVideoTexture() = 0;

		virtual void resetVideoBackgroundPanel();

	protected :	
		static shared_ptr<Renderer> instance;

		int windowWidth;
		int windowHeight;

		bool portrait;

		bool firstCameraFrameArrived;
		int videoWidth;
		int videoHeight;
		int videoBufSize;

		int texID;

		// Store camera frame directly
		unsigned char * sharedBuf;

		// Copy sharedBuf data
		unsigned char * videoBuf;

		// U channel data of yuv420sp data
		unsigned char * uBuf;

		// V channel data of yuv420sp data
		unsigned char * vBuf;

		// RGB or RGBA or RGB565 decoded image
		unsigned char * textureBuf;

		float xScaleFactor;
		float yScaleFactor;

		bool rendererStop;

		pthread_mutex_t videoCopyMutex;
		pthread_mutex_t videoUploadMutex;		

		bool videoFromTracker;

		float vertices[2 *4];
		unsigned char indices[6];
		float texCoords[2 * 4];

		VideoColorFormat videoColorFormat;
	};
} // namespace maxstAR
