#pragma once

#ifdef __ANDROID__
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#include <GLUtil.h>

namespace maxstAR
{
	class RenderTexture
	{
	public:
		RenderTexture();
		~RenderTexture();

		int initTexture();
		int initFBO(int width, int height);
		void startRTT();
		void endRTT();
		void drawTexture();

	private:
		void createProgram();
		void initVertex();

	private :
		int	shaderProgramID;
		GLint vertexHandle;
		GLint textureCoordHandle;
		GLint mvpMatrixHandle;
		GLint textureHandle;
		GLuint textureID;
		GLuint fbo;

		GLMatrix projectionMatrix;
		GLMatrix modelviewMatrix;
		GLMatrix mvpMatrix;
		
		int viewWidth;
		int viewHeight;
		
		float vertices[2 *4];
		unsigned char indices[6];
		float texCoords[2 * 4];
	};
} // namespace maxstAR
