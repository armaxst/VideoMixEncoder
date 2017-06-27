#pragma once

#ifdef __ANDROID__
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#endif

#include <GLUtil.h>

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
	uniform mat4 textureMatrix; \
	 \
	void main() \
	{ \
	   gl_Position = modelViewProjectionMatrix * vertexPosition; \
	   vec4 temp = vec4(1.0 - vertexTexCoord.x, 1.0 - vertexTexCoord.y, 1, 1); \
	   texCoord = (textureMatrix * temp).xy; \
	} \
	";
	
	
	static const char* fragmentShader = " \
	 \
	#extension GL_OES_EGL_image_external : require \n \
	 \
	precision mediump float; \
	 \
	varying vec2 texCoord; \
	 \
	uniform samplerExternalOES texSampler2D; \
	 \
	void main() \
	{ \
		vec4 tex = texture2D( texSampler2D, texCoord ); \
 		gl_FragColor = tex; \
    	if((tex.g > tex.r * 1.1) && (tex.g > tex.b * 1.1) && (tex.g > 0.2)) { \
     		gl_FragColor.a = 0.0; \
    	} \
	} \
	";

	class VideoRenderer
	{
	public:
		VideoRenderer();
		~VideoRenderer();

		int initVideoTexture();
		void initVideoPlane(int width, int height);
		void drawVideo(int destTextureID, float * textureMatArray);
		void stop();

	private:
		void createProgram();
		void initVertex();

	private :
		int	shaderProgramID;
		GLint vertexHandle;
		GLint textureCoordHandle;
		GLint mvpMatrixHandle;
		GLint textureMatrixHandle;
		GLint textureHandle;

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
