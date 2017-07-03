package com.maxst.videomixer;

import android.app.Activity;

import com.maxst.ar.BackgroundRenderer;
import com.maxst.ar.BackgroundTexture;
import com.maxst.videoplayer.VideoPlayer;

public class SampleRenderer {

	private BackgroundQuad backgroundQuad;
	private BackgroundRenderer backgroundRenderer;
	private VideoQuad videoQuad;

	public SampleRenderer(Activity activity) {
		videoQuad = new VideoQuad();
		videoQuad.setScale(2, 2, 1);
		VideoPlayer videoPlayer = new VideoPlayer(activity);
		videoQuad.setVideoPlayer(videoPlayer);
		videoPlayer.openVideo("AsianAdult.mp4");
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
		BackgroundTexture backgroundTexture = backgroundRenderer.updateBackgroundTexture();
		backgroundRenderer.begin();
		backgroundRenderer.renderBackground();
		backgroundRenderer.end();

		backgroundQuad.draw(backgroundTexture, backgroundRenderer.getBackgroundPlaneProjectionMatrix());
		videoQuad.draw();
	}

	public void onPause() {
		videoQuad.destroyVideoPlayer();
	}
}
