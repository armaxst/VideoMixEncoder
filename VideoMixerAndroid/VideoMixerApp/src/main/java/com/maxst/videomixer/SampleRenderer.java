package com.maxst.videomixer;

import android.app.Activity;

import com.maxst.ar.BackgroundRenderer;
import com.maxst.ar.BackgroundTexture;
import com.maxst.videoplayer.VideoPlayer;

public class SampleRenderer {

	private BackgroundQuad backgroundQuad;
	private BackgroundRenderer backgroundRenderer;

	public SampleRenderer() {
	}

	public void onSurfaceCreated() {
		backgroundQuad = new BackgroundQuad();
		backgroundRenderer = BackgroundRenderer.getInstance();
		backgroundRenderer.initRendering();
	}
	public void onSurfaceChanged(int width, int height) {
		backgroundRenderer.updateRendering(width, height);
	}

	public void onDrawFrame() {
		BackgroundTexture backgroundTexture = backgroundRenderer.getBackgroundTexture();
		if (backgroundTexture == null) {
			return;
		}

		backgroundRenderer.begin(backgroundTexture);
		backgroundRenderer.renderBackgroundToTexture();
		backgroundRenderer.end();

		backgroundQuad.draw(backgroundTexture, backgroundRenderer.getBackgroundPlaneProjectionMatrix());
	}
}
