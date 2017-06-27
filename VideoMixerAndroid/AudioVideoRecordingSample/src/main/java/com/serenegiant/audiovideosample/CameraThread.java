package com.serenegiant.audiovideosample;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Thread for asynchronous operation of camera preview
 */
public class CameraThread extends Thread {
	private static final String TAG = CameraThread.class.getSimpleName();

	private static final boolean DEBUG = false;
	private static final int CAMERA_ID = 0;

	private final Object mReadyFence = new Object();
	private final WeakReference<CameraGLView> mCameraGLViewReference;
	private CameraHandler mHandler;
	private volatile boolean mIsRunning = false;
	private Camera mCamera;
	private boolean mIsFrontFace;

	public CameraThread(final CameraGLView parent) {
		super("Camera thread");
		mCameraGLViewReference = new WeakReference<CameraGLView>(parent);
	}

	public CameraHandler getHandler() {
		synchronized (mReadyFence) {
			try {
				mReadyFence.wait();
			} catch (final InterruptedException e) {
			}
		}
		return mHandler;
	}

	/**
	 * message loop
	 * prepare Looper and create Handler for this thread
	 */
	@Override
	public void run() {
		if (DEBUG) Log.d(TAG, "Camera thread start");
		Looper.prepare();
		synchronized (mReadyFence) {
			mHandler = new CameraHandler(this);
			mIsRunning = true;
			mReadyFence.notify();
		}
		Looper.loop();
		if (DEBUG) Log.d(TAG, "Camera thread finish");
		synchronized (mReadyFence) {
			mHandler = null;
			mIsRunning = false;
		}
	}

	public boolean isRunning() {
		return mIsRunning;
	}

	/**
	 * start camera preview
	 * @param width
	 * @param height
	 */
	public final void startPreview(final int width, final int height) {
		if (DEBUG) Log.v(TAG, "startPreview:");
		final CameraGLView cameraGLView = mCameraGLViewReference.get();
		if ((cameraGLView != null) && (mCamera == null)) {
			// This is a sample project so just use 0 as camera ID.
			// it is better to selecting camera is available
			try {
				mCamera = Camera.open(CAMERA_ID);
				final Camera.Parameters params = mCamera.getParameters();
				final List<String> focusModes = params.getSupportedFocusModes();
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				} else if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
					params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				} else {
					if (DEBUG) Log.i(TAG, "Camera does not support autofocus");
				}
				// let's try fastest frame rate. You will get near 60fps, but your device become hot.
				final List<int[]> supportedFpsRange = params.getSupportedPreviewFpsRange();
//					final int n = supportedFpsRange != null ? supportedFpsRange.size() : 0;
//					int[] range;
//					for (int i = 0; i < n; i++) {
//						range = supportedFpsRange.get(i);
//						Log.i(TAG, String.format("supportedFpsRange(%d)=(%d,%d)", i, range[0], range[1]));
//					}
				final int[] max_fps = supportedFpsRange.get(supportedFpsRange.size() - 1);
				Log.i(TAG, String.format("fps:%d-%d", max_fps[0], max_fps[1]));
				params.setPreviewFpsRange(max_fps[0], max_fps[1]);
				params.setRecordingHint(true);
				// request preview size
				// this is a sample project and just use fixed value
				// if you want to use other size, you also need to change the recording size.
				params.setPreviewSize(1280, 720);
/*					final Size preferedSize = params.getPreferredPreviewSizeForVideo();
					if (preferedSize != null) {
						params.setPreviewSize(preferedSize.width, preferedSize.height);
					} */
				// rotate camera preview according to the device orientation
				setRotation(params);
				mCamera.setParameters(params);
				// get the actual preview size
				final Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
				Log.i(TAG, String.format("previewSize(%d, %d)", previewSize.width, previewSize.height));
				// adjust view size with keeping the aspect ration of camera preview.
				// here is not a UI thread and we should request parent view to execute.
				cameraGLView.post(new Runnable() {
					@Override
					public void run() {
						cameraGLView.setVideoSize(previewSize.width, previewSize.height);
					}
				});
				final SurfaceTexture st = cameraGLView.getSurfaceTexture();
				st.setDefaultBufferSize(previewSize.width, previewSize.height);
				mCamera.setPreviewTexture(st);
			} catch (final IOException e) {
				Log.e(TAG, "startPreview:", e);
				if (mCamera != null) {
					mCamera.release();
					mCamera = null;
				}
			} catch (final RuntimeException e) {
				Log.e(TAG, "startPreview:", e);
				if (mCamera != null) {
					mCamera.release();
					mCamera = null;
				}
			}
			if (mCamera != null) {
				// start camera preview display
				mCamera.startPreview();
			}
		}
	}

	/**
	 * stop camera preview
	 */
	public void stopPreview() {
		if (DEBUG) Log.v(TAG, "stopPreview:");
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		final CameraGLView parent = mCameraGLViewReference.get();
		if (parent == null) return;
		parent.releaseCameraHandler();
	}

	/**
	 * rotate preview screen according to the device orientation
	 * @param params
	 */
	private final void setRotation(final Camera.Parameters params) {
		if (DEBUG) Log.v(TAG, "setRotation:");
		final CameraGLView cameraGLView = mCameraGLViewReference.get();
		if (cameraGLView == null) return;

		final Display display = ((WindowManager)cameraGLView.getContext()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		final int rotation = display.getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
		}
		// get whether the camera is front camera or back camera
		final Camera.CameraInfo info =
				new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(CAMERA_ID, info);
		mIsFrontFace = (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
		if (mIsFrontFace) {	// front camera
			degrees = (info.orientation + degrees) % 360;
			degrees = (360 - degrees) % 360;  // reverse
		} else {  // back camera
			degrees = (info.orientation - degrees + 360) % 360;
		}
		// apply rotation setting
		mCamera.setDisplayOrientation(degrees);
		cameraGLView.setRotation(degrees);
		// XXX This method fails to call and camera stops working on some devices.
//			params.setRotation(degrees);
	}

}