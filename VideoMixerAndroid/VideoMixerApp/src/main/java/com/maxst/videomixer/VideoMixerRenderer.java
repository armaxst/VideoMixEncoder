package com.maxst.videomixer;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

import com.maxst.videoplayer.VideoPlayer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoMixerRenderer implements Renderer {

	private int surfaceWidth;
	private int surfaceHeight;
	private SampleRenderer sampleRenderer;

	private Activity activity;

	public VideoMixerRenderer(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		sampleRenderer = new SampleRenderer(activity);
		sampleRenderer.onSurfaceCreated();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		sampleRenderer.onSurfaceChanged(width, height);

		surfaceWidth = width;
		surfaceHeight = height;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight);

		sampleRenderer.onDrawFrame();
	}

	int mFrameCount = 0;

	private boolean recordThisFrame() {
		final int TARGET_FPS = 30;
		mFrameCount++;
		switch (TARGET_FPS) {
			case 60:
				return true;
			case 30:
				return (mFrameCount & 0x01) == 0;
			case 24:
// want 2 out of every 5 frames
				int mod = mFrameCount % 5;
				return mod == 0 || mod == 2;
			default:
				return true;
		}
	}

	public void onPause() {
		sampleRenderer.onPause();
	}
}
