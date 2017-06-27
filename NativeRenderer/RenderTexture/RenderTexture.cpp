#include "stdafx.h"

#ifdef LOG_TAG
#undef  LOG_TAG
#define LOG_TAG "RenderTexture"
#else 
#define LOG_TAG "RenderTexture"
#endif

#include "RenderTexture.h"
#include <GLUtil.h>
#include <math.h>

#include <string.h>

namespace maxstAR
{
	static const char* vertexShader = " \
	  \
	attribute vec4 vertexPosition; \
	attribute vec2 vertexTexCoord; \
	 \
	varying vec2 texCoord; \
	 \
	uniform mat4 modelViewProjectionMatrix; \
	 \
	void main() \
	{ \
	   gl_Position = modelViewProjectionMatrix * vertexPosition; \
	   texCoord = vertexTexCoord; \
	} \
	";
	
	
	static const char* fragmentShader = " \
	 \
	precision mediump float; \
	 \
	varying vec2 texCoord; \
	 \
	uniform sampler2D texSampler2D; \
	 \
	void main() \
	{ \
		gl_FragColor = texture2D( texSampler2D, texCoord ); \
	} \
	";

	RenderTexture::RenderTexture()
	{
		indices[0] = 0;
		indices[1] = 1;
		indices[2] = 2;
		indices[3] = 2;
		indices[4] = 1;
		indices[5] = 3;

		texCoords[0] = 0.0f;
		texCoords[1] = 1.0f;
		
		texCoords[2] = 0.0f;
		texCoords[3] = 0.0f;
		
		texCoords[4] = 1.0f;
		texCoords[5] = 1.0f;
		
		texCoords[6] = 1.0f;
		texCoords[7] = 0.0f;

	}

	RenderTexture::~RenderTexture()
	{
	}

	int RenderTexture::initTexture()
	{
		glGenTextures(1, &textureID);
		glBindTexture(GL_TEXTURE_2D, textureID);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);		
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D, 0);

		TraceR("texture ID : %d", textureID);

		return textureID;
	}

	int RenderTexture::initFBO(int width, int height)
	{
		viewWidth = width;
		viewHeight = height;
			
	    glActiveTexture(GL_TEXTURE0);
	    glBindTexture(GL_TEXTURE_2D, textureID);
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, viewWidth, viewHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
	    
	    glGenFramebuffers(1, &fbo);
	    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
	    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
	    glClear(GL_COLOR_BUFFER_BIT);
	    glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);	

		createProgram();

		initVertex();
		
		GLUtil::GLLoadIdentity(&projectionMatrix);
		GLUtil::GLOrtho(&projectionMatrix, -viewWidth / 2 , viewWidth / 2, -viewHeight / 2, viewHeight / 2, 20.0, 22.0);
		GLUtil::GLLoadIdentity(&modelviewMatrix);
		modelviewMatrix.m[3][2] = -21.0f;
		GLUtil::GLMatrixMultiply(&mvpMatrix, &modelviewMatrix, &projectionMatrix);
	}

	void RenderTexture::createProgram()
	{
		shaderProgramID = maxstAR::GLUtil::CreateProgramFromBuffer(vertexShader, fragmentShader);

		if (shaderProgramID == 0) 
		{
			TraceR("\nRenderer : initRendering can not create program\n");
			return;
		}

		vertexHandle		= glGetAttribLocation(shaderProgramID, "vertexPosition");
		textureCoordHandle	= glGetAttribLocation(shaderProgramID, "vertexTexCoord");
		mvpMatrixHandle 	= glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
		textureHandle		= glGetUniformLocation(shaderProgramID, "texSampler2D");

		GLUtil::checkGlError("createVideoRenderingProgram");
	}

	void RenderTexture::initVertex()
	{
		int w = viewWidth;
		int h = viewHeight;

		vertices[0] = -w / 2.0f;
		vertices[1] = -h / 2.0f;
		
		vertices[2] = -w / 2.0f;
		vertices[3] = h / 2.0f;
		
		vertices[4] = w / 2.0f;
		vertices[5] = -h / 2.0f;
		
		vertices[6] = w / 2.0f;
		vertices[7] = h / 2.0f;
	}

	void RenderTexture::startRTT()
	{
		glViewport(0, 0, viewWidth, viewHeight);
		GLUtil::checkGlError("glViewport");

		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		GLUtil::checkGlError("glBindFramebuffer");
	}

	void RenderTexture::endRTT()
	{
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		GLUtil::checkGlError("glBindFramebuffer");
	}

	void RenderTexture::drawTexture()
	{
		glUseProgram(shaderProgramID);
		GLUtil::checkGlError("glUseProgram");

		glVertexAttribPointer(vertexHandle, 2, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &vertices);
		glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &texCoords[0]);
		
		glEnableVertexAttribArray(vertexHandle);
		glEnableVertexAttribArray(textureCoordHandle);

		glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE, (GLfloat*) &mvpMatrix.m[0]);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureID);
		glUniform1i(textureHandle, 0);

		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glDisableVertexAttribArray(vertexHandle);
		glDisableVertexAttribArray(textureCoordHandle);

		GLUtil::checkGlError("glUseProgram");	
	}
}

