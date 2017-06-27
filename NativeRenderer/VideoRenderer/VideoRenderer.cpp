#include "stdafx.h"

#ifdef LOG_TAG
#undef  LOG_TAG
#define LOG_TAG "RenderTexture"
#else 
#define LOG_TAG "RenderTexture"
#endif

#include "VideoRenderer.h"
#include <GLUtil.h>
#include <math.h>

#include <string.h>

namespace maxstAR
{
	VideoRenderer::VideoRenderer()
	{
		indices[0] = 0;
		indices[1] = 1;
		indices[2] = 2;
		indices[3] = 2;
		indices[4] = 1;
		indices[5] = 3;

		texCoords[0] = 0.0f;
		texCoords[1] = 0.0f;

		texCoords[2] = 0.0f;
		texCoords[3] = 1.0f;
	
		texCoords[4] = 1.0f;
		texCoords[5] = 0.0f;
		
		texCoords[6] = 1.0f;
		texCoords[7] = 1.0f;
	}

	VideoRenderer::~VideoRenderer()
	{
	}

	void VideoRenderer::stop()
	{
		viewWidth = 0;
		viewHeight = 0;
	}

	int VideoRenderer::initVideoTexture()
	{
		GLuint mediaTextureID;

		glGenTextures(1, &mediaTextureID);
		glBindTexture(GL_TEXTURE_EXTERNAL_OES, mediaTextureID);
		glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);

		TraceR("mediaTextureID : %d", mediaTextureID);

		return mediaTextureID;
	}

	void VideoRenderer::initVideoPlane(int width, int height)
	{
		TraceR("width : %d height : %d viewWidth : %d viewHeight : %d", width, height, viewWidth, viewHeight);
		viewWidth = width;
		viewHeight = height;

		initVertex();
		
		GLUtil::GLLoadIdentity(&projectionMatrix);
		GLUtil::GLOrtho(&projectionMatrix, -viewWidth / 2 , viewWidth / 2, -viewHeight / 2, viewHeight / 2, 50.0, 52.0);
		GLUtil::GLLoadIdentity(&modelviewMatrix);
		modelviewMatrix.m[3][2] = -50.0f;
		GLUtil::GLMatrixMultiply(&mvpMatrix, &modelviewMatrix, &projectionMatrix);
		
		createProgram();
	}

	void VideoRenderer::createProgram()
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
		textureMatrixHandle = glGetUniformLocation(shaderProgramID, "textureMatrix");
		textureHandle		= glGetUniformLocation(shaderProgramID, "texSampler2D");

		GLUtil::checkGlError("createVideoRenderingProgram");
	}

	void VideoRenderer::initVertex()
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

	void VideoRenderer::drawVideo(int textureID, float * textureMatArray)
	{
		glUseProgram(shaderProgramID);
		GLUtil::checkGlError("glUseProgram");

		glVertexAttribPointer(vertexHandle, 2, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &vertices);
		glVertexAttribPointer(textureCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, (const GLvoid*) &texCoords[0]);
		
		glEnableVertexAttribArray(vertexHandle);
		glEnableVertexAttribArray(textureCoordHandle);

		glUniformMatrix4fv(mvpMatrixHandle, 1, GL_FALSE, (GLfloat*) &mvpMatrix.m[0]);
		glUniformMatrix4fv(textureMatrixHandle, 1, GL_FALSE, (GLfloat*) textureMatArray);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureID);
		glUniform1i(textureHandle, 0);

		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glDisableVertexAttribArray(vertexHandle);
		glDisableVertexAttribArray(textureCoordHandle);
	}
}

