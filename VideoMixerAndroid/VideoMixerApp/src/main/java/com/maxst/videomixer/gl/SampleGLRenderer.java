package com.maxst.videomixer.gl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

import com.maxst.ar.MaxstARAPI;
import com.maxst.videoPlayer.VideoPlayer;

public class SampleGLRenderer implements Renderer {

	private int viewWidth;
	private int viewHeight;

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		MaxstARAPI.initRendering();
		RenderTexture.initTargetTexture();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		viewWidth = width;
		viewHeight = height;

		MaxstARAPI.updateRendering(viewWidth, viewHeight);
		VideoPlayer.getInstance().updateRendering(viewWidth, viewHeight);

		RenderTexture.initFBO(width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glViewport(0, 0, viewWidth, viewHeight);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		RenderTexture.startRTT();
		MaxstARAPI.renderBackground();
		VideoPlayer.getInstance().update();
		RenderTexture.endRTT();

		RenderTexture.drawTexture();
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
}
