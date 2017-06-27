#pragma once

#include "Renderer.h"

using namespace std;

namespace maxstAR
{
	class RendererGLES20 : public Renderer
	{
	public:
		RendererGLES20();
		~RendererGLES20();

		void updateRendering(int width, int height);
		void renderVideoBackground( );

	private :
		void createVideoRenderingProgram();
		void initVideoTexture();
		void drawVideoBackground();

	private :
		int	programObject;
	};
} // namespace maxstAR
