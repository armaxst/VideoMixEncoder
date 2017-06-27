#include "stdafx.h"

#if defined(_WIN32) || defined(_WIN64)
#include <glut.h>

#elif defined(__ANDROID__)
#include <GLES/gl.h>
#include <GLES/glext.h>
#elif defined(__IOS__)
#import <OpenGLES/ES1/gl.h>
#import <OpenGLES/ES1/glext.h>
#elif defined(__MacOS__)
#include  <GLUT/GLUT.h>
#endif

#include <string>

#include "RendererGL.h"
#include "../Include/Logger.h"

namespace maxstAR
{
	static shared_ptr<RendererGL> renderer = shared_ptr<RendererGL>();

	shared_ptr<Renderer> Renderer::getInstance()
	{
		if (renderer == shared_ptr<RendererGL>())
		{
			renderer = shared_ptr<RendererGL>(new RendererGL());
		}

		return renderer;
	}

	RendererGL::RendererGL()
		//:Renderer()
	{
	}

	RendererGL::~RendererGL()
	{
	}

	void RendererGL::renderVideoBackground( )
	{
		if (firstCameraFrameArrived == false)
		{
			return;
		}

		if (texID == -1)
		{
			initVideoTexture();
		}

		resetVideoBackgroundPanel();
		getTextureFromCameraFrame();
		drawVideoBackground();
	}

	void RendererGL::drawVideoBackground()
	{
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glOrtho(-windowWidth / 2, windowWidth / 2, -windowHeight / 2, windowHeight / 2, -1.0, 1.0);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		if (portrait)
		{
			glRotatef(-90, 0, 0, 1);
		}

		glScalef(xScaleFactor, yScaleFactor, 1.0f);

		glDisable(GL_DEPTH_TEST);

		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(2, GL_FLOAT, 0, vertices);

		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, texID);

		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glTexCoordPointer(2, GL_FLOAT, 0, texCoords);

		videoUploadLock();

		if (textureBuf != NULL)
		{
			if (videoColorFormat == VideoColorFormat::Rgb24)
			{
				glTexSubImage2D( GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_BGR_EXT, GL_UNSIGNED_BYTE, textureBuf);
			}
			else if (videoColorFormat == VideoColorFormat::Yuv420)
			{
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_RGB, GL_UNSIGNED_BYTE, textureBuf );
			}
		}

		videoUploadUnlock();

		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, indices);

		glDisableClientState(GL_VERTEX_ARRAY);
		glDisable(GL_TEXTURE_2D);

		glEnable(GL_DEPTH_TEST);
	}

	void RendererGL::initVideoTexture()
	{
		int texWidth = 1;
		int texHeight = 1;

		while(texWidth < videoWidth)
		{
			texWidth <<= 1;
		}

		while(texHeight < videoHeight)
		{
			texHeight <<= 1;
		}

		texCoords[0] = 0.0f;
		texCoords[1] = (float)videoHeight / (float)texHeight;

		texCoords[2] = 0.0f;
		texCoords[3] = 0.0f;
		
		texCoords[4] = (float)videoWidth / (float)texWidth;
		texCoords[5] = (float)videoHeight / (float)texHeight;
		
		texCoords[6] = (float)videoWidth / (float)texWidth;
		texCoords[7] = 0;

		glEnable(GL_TEXTURE_2D);

		glGenTextures(1, (GLuint*)&texID);

		glBindTexture(GL_TEXTURE_2D, texID);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1 );

		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glTexCoordPointer(2, GL_FLOAT, 0, texCoords);

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		if (videoColorFormat == VideoColorFormat::Rgb24)
		{
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_BGR_EXT, GL_UNSIGNED_BYTE, 0);
		}
		else if (videoColorFormat == VideoColorFormat::Yuv420)
		{
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);
		}
	}
}

