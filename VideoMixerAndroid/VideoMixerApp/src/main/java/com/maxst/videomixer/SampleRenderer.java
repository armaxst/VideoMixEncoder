package com.maxst.videomixer;

import com.maxst.ar.BackgroundRenderer;
import com.maxst.ar.BackgroundTexture;

public class SampleRenderer {

	private BackgroundQuad backgroundQuad;
	private BackgroundRenderer backgroundRenderer;

	public void onSurfaceCreated() {
		backgroundQuad = new BackgroundQuad();
		backgroundRenderer = BackgroundRenderer.getInstance();
		backgroundRenderer.initRendering();
	}
	public void onSurfaceChanged(int width, int height) {
		backgroundRenderer.updateRendering(width, height);
	}

	public void onDrawFrame() {
		BackgroundTexture backgroundTexture = backgroundRenderer.updateBackgroundTexture();
		backgroundRenderer.begin();
		backgroundRenderer.renderBackground();
		backgroundRenderer.end();

		backgroundQuad.draw(backgroundTexture, backgroundRenderer.getBackgroundPlaneProjectionMatrix());
	}

	public void onOrientationChanged(int orientatioin) {
		backgroundRenderer.setScreenOrientation(orientatioin);
	}
}
