package com.maxst.videomixer.camera;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class SurfaceManager {
	private static final String TAG = SurfaceManager.class.getSimpleName();

	private Activity activity;
	private CameraSurfaceView cameraSurfaceView;
	private CameraPreview cameraPreview;
	private GLSurfaceView glSurfaceView;

	private SurfaceManager(Activity activity) {
		this.activity = activity;
		cameraPreview = new CameraPreview();
		cameraPreview.setSurfaceManager(this);
	}

	protected void startCamera() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (cameraSurfaceView == null) {
					retrieveGLSurfaceView();
					cameraSurfaceView = new CameraSurfaceView(activity);
					cameraSurfaceView.setCamera(cameraPreview.getCamera());
					cameraSurfaceParentView.addView(cameraSurfaceView, new LayoutParams(1, 1));
					applyRenderWhenDirty(true);
				}
			}

		});
	}

	protected void stopCamera() {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (cameraSurfaceView != null) {
					cameraSurfaceParentView.removeView(cameraSurfaceView);
					cameraSurfaceView = null;
					applyRenderWhenDirty(false);
				}
			}			
		});
	}

	private ViewGroup cameraSurfaceParentView;

	private boolean retrieveGLSurfaceView() {
		try {
			if (activity == null) {
				return false;
			}

			View decorView = activity.getWindow().getDecorView();
			glSurfaceView = searchForGLSurfaceView(decorView);

			if (glSurfaceView == null) {
				cameraSurfaceParentView = (ViewGroup)decorView;
			} else {
				cameraSurfaceParentView = (ViewGroup)glSurfaceView.getParent();
			}

		}
		catch (Exception e)
		{
			return false;
		}

		return this.glSurfaceView != null;
	}	

	private GLSurfaceView searchForGLSurfaceView(View rootView) {
		GLSurfaceView result = null;
		try {
			ViewGroup rootViewGroup = (ViewGroup)rootView;

			int numChildren = rootViewGroup.getChildCount();
			for (int i = 0; i < numChildren; i++) {
				View childView = rootViewGroup.getChildAt(i);

				if ((childView instanceof GLSurfaceView)) {
					result = (GLSurfaceView)childView;
					break;
				} else if ((childView instanceof ViewGroup)) {
					result = searchForGLSurfaceView(childView);
					if (result != null) {
						break;
					}
				}
			}
		} catch (Exception e) {
			return null;
		}

		return result;
	}

	private boolean applyRenderWhenDirty(boolean renderWhenDirtyEnabled) {
		if (glSurfaceView != null) {
			glSurfaceView.setRenderMode(renderWhenDirtyEnabled ? 0 : 1);
			return true;
		}

		return false;
	}
	
	protected void requestRender() {
		if (glSurfaceView != null) {
			glSurfaceView.requestRender();
		}
	}

	private static SurfaceManager instance;

	public static SurfaceManager getInstance(Activity activity) {
		if (instance == null) {
			instance = new SurfaceManager(activity);
		}

		return instance;
	}

	public static void release() {
		instance = null;
	}

	public CameraPreview getCameraPreview() {
		return instance.cameraPreview;
	}
}
