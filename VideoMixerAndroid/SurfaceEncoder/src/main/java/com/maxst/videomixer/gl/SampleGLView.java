package com.maxst.videomixer.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

public class SampleGLView extends GLSurfaceView {

	private SampleGLRenderer renderer;

	public SampleGLView(Context context) {
		super(context);

		setEGLContextClientVersion(2);
		renderer = new SampleGLRenderer();
		setRenderer(renderer);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);

		renderer.surfaceDestroyed();
	}
}
