#pragma once

#include "Renderer.h"

namespace maxstAR
{
	class RendererGL : public Renderer
	{
	public:
		RendererGL();
		~RendererGL();

		void renderVideoBackground( );

	private :
		void initVideoTexture();
		void drawVideoBackground();
	};
} // namespace maxstAR
