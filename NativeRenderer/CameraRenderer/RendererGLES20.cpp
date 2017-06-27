#include "stdafx.h"
#include "RendererGLES20.h"
#include <GLUtil.h>
#include <math.h>

#ifdef __ANDROID__
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#include <string.h>

#define ANIM_WIDTH 256
#define ANIM_HEIGHT 128

namespace maxstAR
{
	shared_ptr<Renderer> renderer = shared_ptr<RendererGLES20>();
	
	shared_ptr<Renderer> Renderer::getInstance()
	{
		if (renderer == shared_ptr<RendererGLES20>())
		{
			renderer = shared_ptr<RendererGLES20>(new RendererGLES20());
		}
		return renderer;
	}

	RendererGLES20::RendererGLES20()
	//:Renderer()
	{

	}

	RendererGLES20::~RendererGLES20()
	{
	}

	void RendererGLES20::updateRendering(int width, int height)
	{
		if (windowWidth != width || windowHeight != height)
		{
  			TraceR("width : %d height : %d windowWidth : %d windowHeight : %d", width, height, windowWidth, windowHeight);
			windowWidth = width;
			windowHeight = height;	
			createVideoRenderingProgram();
			//ovalAnimator.init(width, height);
		}
	}

	void RendererGLES20::createVideoRenderingProgram()
	{
		const char  vShader[] = 
			"uniform mat4 uMVPMatrix; \n"
			"attribute vec4 vPosition; \n"
			"attribute vec2 a_texCoord; \n"
			"varying vec2 v_texCoord; \n"
			"void main()					\n"
			"{									\n"
			"		gl_Position = uMVPMatrix * vPosition; \n"
			"		v_texCoord = a_texCoord; \n"
			"} \n";

		const char  fShader[] = 
			"precision mediump float; \n"
			"varying vec2 v_texCoord; \n"
			"uniform sampler2D camTexture; \n"
			"void main(void) \n"
			"{ \n"
			"	gl_FragColor = texture2D(camTexture, v_texCoord); \n"
			"} \n";

		GLuint vertexShader;
		GLuint fragmentShader;
		GLint linked;

		vertexShader = GLUtil::LoadShader(GL_VERTEX_SHADER, vShader);
		fragmentShader = GLUtil::LoadShader(GL_FRAGMENT_SHADER, fShader);

		programObject = glCreateProgram();

		if (programObject == 0) 
		{
			TraceR("\nRenderer : initRendering can not create program\n");
			return;
		}

		TraceR("\nRenderer : createVideoRenderingProgram %d\n", programObject);

		glAttachShader(programObject, vertexShader);
		glAttachShader(programObject, fragmentShader);

		glLinkProgram(programObject);

		glGetProgramiv(programObject, GL_LINK_STATUS, &linked);

		if (!linked)
		{
			GLint infoLen = 0;
			glGetProgramiv(programObject, GL_INFO_LOG_LENGTH, &infoLen);

			if (infoLen > 1) 
			{
				char *infoLog = (char *)malloc(sizeof(char) * infoLen);

				glGetProgramInfoLog(programObject, infoLen, NULL, infoLog);
				TraceR("Renderer : initRendering : Error linking program : \n%s\n", infoLog);

				free(infoLog);
			}

			glDeleteProgram(programObject);
			return;
		}

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		
		GLUtil::checkGlError("createVideoRenderingProgram");
	}

	void RendererGLES20::drawVideoBackground()
	{
		glUseProgram(programObject);

		GLUtil::checkGlError("glUseProgram");

		GLMatrix projectionMatrix;
		GLMatrix modelviewMatrix;
		GLMatrix mvpMatrix;

		GLuint vMvp = glGetUniformLocation(programObject, "uMVPMatrix");

		GLUtil::checkGlError("glUseProgram");

		GLUtil::GLLoadIdentity(&projectionMatrix);
		GLUtil::GLOrtho(&projectionMatrix, -windowWidth / 2 , windowWidth / 2, -windowHeight / 2, windowHeight / 2, 100.0, 102.0);
		//GLUtil::GLOrtho(&projectionMatrix, -windowWidth / 2 , windowWidth / 2, -windowHeight / 2, windowHeight / 2, -1.0, -3.0);
		//GLUtil::GLOrtho(&projectionMatrix, -windowWidth / 2.0f , windowWidth / 2.0f, -windowHeight / 2.0f, windowHeight / 2.0f, 4999, 5001);
		GLUtil::GLLoadIdentity(&modelviewMatrix);
		modelviewMatrix.m[3][2] = -101.0f;

		if (portrait)
		{
			GLUtil::GLRotate(&modelviewMatrix, 90, 0, 0, 1);
		}

		GLUtil::GLScale(&modelviewMatrix, xScaleFactor, yScaleFactor, 1.0f);

		GLUtil::GLMatrixMultiply(&mvpMatrix, &modelviewMatrix, &projectionMatrix);

		glUniformMatrix4fv(vMvp, 1, GL_FALSE, (GLfloat *)&mvpMatrix);

		GLuint vPosition = glGetAttribLocation(programObject, "vPosition");
		glVertexAttribPointer(vPosition, 2, GL_FLOAT, GL_FALSE, 0, vertices);
		//glVertexAttribPointer(vPosition, 3, GL_FLOAT, GL_FALSE, 0, vertices);

		GLuint aTexCoord = glGetAttribLocation(programObject, "a_texCoord");
		glVertexAttribPointer(aTexCoord, 2, GL_FLOAT, GL_FALSE, 0, texCoords);

		glEnableVertexAttribArray(vPosition);
		glEnableVertexAttribArray(aTexCoord);

		GLuint cameraTexture = glGetUniformLocation(programObject, "camTexture");
		glBindTexture(GL_TEXTURE_2D, texID);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		videoUploadLock();

		if (textureBuf != NULL)
		{
			if (videoColorFormat == VideoColorFormat::Rgb24)
			{
#if defined(__IOS__) || defined(__MacOS__)
				glTexSubImage2D( GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_RGB, GL_UNSIGNED_BYTE, textureBuf);
#elif defined(__ANDROID__)
				glTexSubImage2D( GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_RGB, GL_UNSIGNED_BYTE, textureBuf);
#else
				glTexSubImage2D( GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_BGR_EXT, GL_UNSIGNED_BYTE, textureBuf);
#endif
			}
			else if (videoColorFormat == VideoColorFormat::Yuv420)
			{
#ifdef USING_FAST_CV
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, textureBuf );
#elif defined(__IOS__) || defined(__MacOS__)
				glTexSubImage2D( GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL_RGB, GL_UNSIGNED_BYTE, textureBuf);
#endif		
			}
		}

		videoUploadUnlock();
		glUniform1i(cameraTexture, 0);

		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glDisableVertexAttribArray(vPosition);
		glDisableVertexAttribArray(aTexCoord);

		GLUtil::checkGlError("glUseProgram");
	}

	void RendererGLES20::renderVideoBackground( )
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

		//ovalAnimator.draw();
	}

	void RendererGLES20::initVideoTexture()
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
		texCoords[1] = 0.0f;

		texCoords[2] = 0.0f;
		texCoords[3] = (float)videoHeight / (float)texHeight;

		texCoords[4] = (float)videoWidth / (float)texWidth;
		texCoords[5] = 0;

		texCoords[6] = (float)videoWidth / (float)texWidth;
		texCoords[7] = (float)videoHeight / (float)texHeight;
		

		//glEnable(GL_TEXTURE_2D);

		GLUtil::checkGlError("glEnable(GL_TEXTURE_2D)");

		if (texID != -1)
		{
			glDeleteTextures(1, (GLuint*)&texID);
			GLUtil::checkGlError("glDeleteTextures");
		}

		glGenTextures(1, (GLuint*)&texID);
		GLUtil::checkGlError("glGenTextures");

		TraceR("initVideoTexture texWidth:%d texHeight:%d texID:%d", texWidth, texHeight, texID);

		glBindTexture(GL_TEXTURE_2D, texID);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1 );

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		if (videoColorFormat == VideoColorFormat::Rgb24)
		{
#if defined(__IOS__) || defined(__MacOS__)
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
#elif defined(__ANDROID__)
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
#else
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_BGR_EXT, GL_UNSIGNED_BYTE, NULL);
#endif
		}
		else if (videoColorFormat == VideoColorFormat::Yuv420)
		{
#ifdef USING_FAST_CV
			glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, NULL ); 
#elif defined(__IOS__) || defined(__MacOS__)
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
#endif
		}

		GLUtil::checkGlError("initVideoTexture");
	}	
}

