#pragma  once

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string>

#ifdef _WIN32
#include <GLES2/gl2.h>
#include <EGL/egl.h>
#elif defined(__ANDROID__)
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "Logger.h"
#elif defined(__IOS__)
#include <OpenGLES/ES2/gl.h>
#elif defined(__MacOS__)
#include <GLUT/GLUT.h>
#endif

#define PI 3.1415926535897932384626433832795f

typedef struct
{
	GLfloat   m[4][4];
} GLMatrix;

namespace maxstAR 
{
	class GLUtil
	{
	public:
		GLUtil(void);
		~GLUtil(void);

		static void GLLoadIdentity(GLMatrix *result)
		{
			memset(result, 0x0, sizeof(GLMatrix));
			result->m[0][0] = 1.0f;
			result->m[1][1] = 1.0f;
			result->m[2][2] = 1.0f;
			result->m[3][3] = 1.0f;
		}

		static void GLMatrixMultiply(GLMatrix *result, GLMatrix *srcA, GLMatrix *srcB)
		{
			GLMatrix    tmp;
			int         i;

			for (i=0; i<4; i++)
			{
				tmp.m[i][0] =	(srcA->m[i][0] * srcB->m[0][0]) +
					(srcA->m[i][1] * srcB->m[1][0]) +
					(srcA->m[i][2] * srcB->m[2][0]) +
					(srcA->m[i][3] * srcB->m[3][0]) ;

				tmp.m[i][1] =	(srcA->m[i][0] * srcB->m[0][1]) + 
					(srcA->m[i][1] * srcB->m[1][1]) +
					(srcA->m[i][2] * srcB->m[2][1]) +
					(srcA->m[i][3] * srcB->m[3][1]) ;

				tmp.m[i][2] =	(srcA->m[i][0] * srcB->m[0][2]) + 
					(srcA->m[i][1] * srcB->m[1][2]) +
					(srcA->m[i][2] * srcB->m[2][2]) +
					(srcA->m[i][3] * srcB->m[3][2]) ;

				tmp.m[i][3] =	(srcA->m[i][0] * srcB->m[0][3]) + 
					(srcA->m[i][1] * srcB->m[1][3]) +
					(srcA->m[i][2] * srcB->m[2][3]) +
					(srcA->m[i][3] * srcB->m[3][3]) ;
			}
			memcpy(result, &tmp, sizeof(GLMatrix));
		}

		static void GLOrtho(GLMatrix *result, float left, float right, float bottom, float top, float nearZ, float farZ)
		{
			float       deltaX = right - left;
			float       deltaY = top - bottom;
			float       deltaZ = farZ - nearZ;

			GLMatrix    ortho;

			if ( (deltaX == 0.0f) || (deltaY == 0.0f) || (deltaZ == 0.0f) )
				return;

			GLLoadIdentity(&ortho);

			ortho.m[0][0] = 2.0f / deltaX;
			ortho.m[3][0] = -(right + left) / deltaX;
			ortho.m[1][1] = 2.0f / deltaY;
			ortho.m[3][1] = -(top + bottom) / deltaY;
			ortho.m[2][2] = -2.0f / deltaZ;
			ortho.m[3][2] = -(nearZ + farZ) / deltaZ;

			GLMatrixMultiply(result, &ortho, result);
		}

		static void GLRotate(GLMatrix *result, GLfloat angle, GLfloat x, GLfloat y, GLfloat z)
		{
			GLfloat sinAngle, cosAngle;
			GLfloat mag = sqrtf(x * x + y * y + z * z);

			sinAngle = sinf ( angle * PI / 180.0f );
			cosAngle = cosf ( angle * PI / 180.0f );

			if ( mag > 0.0f )
			{
				GLfloat xx, yy, zz, xy, yz, zx, xs, ys, zs;
				GLfloat oneMinusCos;
				GLMatrix rotMat;

				x /= mag;
				y /= mag;
				z /= mag;

				xx = x * x;
				yy = y * y;
				zz = z * z;
				xy = x * y;
				yz = y * z;
				zx = z * x;
				xs = x * sinAngle;
				ys = y * sinAngle;
				zs = z * sinAngle;

				oneMinusCos = 1.0f - cosAngle;
				rotMat.m[0][0] = (oneMinusCos * xx) + cosAngle;
				rotMat.m[0][1] = (oneMinusCos * xy) - zs;
				rotMat.m[0][2] = (oneMinusCos * zx) + ys;
				rotMat.m[0][3] = 0.0F; 
				rotMat.m[1][0] = (oneMinusCos * xy) + zs;
				rotMat.m[1][1] = (oneMinusCos * yy) + cosAngle;
				rotMat.m[1][2] = (oneMinusCos * yz) - xs;
				rotMat.m[1][3] = 0.0F;

				rotMat.m[2][0] = (oneMinusCos * zx) - ys;
				rotMat.m[2][1] = (oneMinusCos * yz) + xs;
				rotMat.m[2][2] = (oneMinusCos * zz) + cosAngle;
				rotMat.m[2][3] = 0.0F; 

				rotMat.m[3][0] = 0.0F;
				rotMat.m[3][1] = 0.0F;
				rotMat.m[3][2] = 0.0F;
				rotMat.m[3][3] = 1.0F;

				GLMatrixMultiply( result, &rotMat, result );
			}
		}

		static void GLScale(GLMatrix *result, GLfloat sx, GLfloat sy, GLfloat sz)
		{
			result->m[0][0] *= sx;
			result->m[0][1] *= sx;
			result->m[0][2] *= sx;
			result->m[0][3] *= sx;

			result->m[1][0] *= sy;
			result->m[1][1] *= sy;
			result->m[1][2] *= sy;
			result->m[1][3] *= sy;

			result->m[2][0] *= sz;
			result->m[2][1] *= sz;
			result->m[2][2] *= sz;
			result->m[2][3] *= sz;
		}

		static GLuint LoadShader(GLenum type, const char *shaderSrc)
		{
			GLuint shader;
			GLint compiled;

			shader = glCreateShader(type);

			if (shader == 0)
				return 0;

			glShaderSource(shader, 1, &shaderSrc, NULL);

			glCompileShader(shader);

			glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);

			if (!compiled) 
			{
				GLint infoLen = 0;
				glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);

				if (infoLen > 1)
				{
					char *infoLog = (char *)malloc(sizeof(char) * infoLen);

					glGetShaderInfoLog(shader, infoLen, NULL, infoLog);
					free(infoLog);
				}

				glDeleteShader(shader);
				return 0;
			}

			return shader;
		}


		static unsigned int CreateProgramFromBuffer(const char* vertexShaderBuffer,
											 const char* fragmentShaderBuffer)
		{
			GLuint vertexShader = LoadShader(GL_VERTEX_SHADER, vertexShaderBuffer);
			if (!vertexShader)
				return 0;	 
		
			GLuint fragmentShader = LoadShader(GL_FRAGMENT_SHADER,
												fragmentShaderBuffer);
			if (!fragmentShader)
				return 0;
		
			GLuint program = glCreateProgram();
			if (program)
			{
				glAttachShader(program, vertexShader);
				checkGlError("glAttachShader");
				
				glAttachShader(program, fragmentShader);
				checkGlError("glAttachShader");
				
				glLinkProgram(program);
				GLint linkStatus = GL_FALSE;
				glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
				
				if (linkStatus != GL_TRUE)
				{
					GLint bufLength = 0;
					glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
					if (bufLength)
					{
						char* buf = (char*) malloc(bufLength);
						if (buf)
						{
							glGetProgramInfoLog(program, bufLength, NULL, buf);
							TraceR("Could not link program: %s", buf);
							free(buf);
						}
					}
					glDeleteProgram(program);
					program = 0;
				}
			}
			return program;
		}
		
		static void checkGlError(std::string op) 
		{
			int error;
			while ((error = glGetError()) != GL_NO_ERROR) 
			{
				TraceR("%s error code : %d", op.c_str(), error);
			}
		}
	};
}
