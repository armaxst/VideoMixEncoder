package com.maxst.videomixer.camera;

import java.util.List;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;

public class CameraPreview implements PreviewCallback {
	private static final String TAG = CameraPreview.class.getSimpleName();

	private Camera camera;
	private int[] previewSize = new int[2]; // [0] : width , [1] : height
	private int screenWidth;
	private int screenHeight;
	private int bitsPerPixel;
	private boolean previewSizeFixed = false;
	private static  final int MIN_WIDTH = 1200;
	private static final int MIN_HEIGHT = 700;

	public CameraPreview() {

	}

	public Camera getCamera() {
		return camera;
	}

	private void openCamera(int windowWidth, int windowHeight) {
		if (camera == null) {
			camera = Camera.open();
		}

		screenWidth = windowWidth;
		screenHeight = windowHeight;		

		Camera.Parameters params = camera.getParameters();
		List<String> focusModes = params.getSupportedFocusModes();

		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		} else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		}

		Size size = getOptimalPreviewSize(params.getSupportedPreviewSizes());

		if (previewSizeFixed) {
			size.width = MIN_WIDTH;
			size.height = MIN_HEIGHT;
		}

		previewSize[0] = size.width;
		previewSize[1] = size.height;

		params.setPreviewSize(previewSize[0], previewSize[1]);
		params.setPreviewFormat(ImageFormat.NV21);

		PixelFormat p = new PixelFormat();
		PixelFormat.getPixelFormatInfo(params.getPreviewFormat(), p);
		bitsPerPixel = p.bitsPerPixel;
		int bufSize = (size.width * size.height * p.bitsPerPixel) / 8;

		byte[] buffer = new byte[bufSize];
		camera.addCallbackBuffer(buffer);
		buffer = new byte[bufSize];
		camera.addCallbackBuffer(buffer);
		buffer = new byte[bufSize];
		camera.addCallbackBuffer(buffer);
		buffer = new byte[bufSize];
		camera.addCallbackBuffer(buffer);

		camera.setPreviewCallbackWithBuffer(this);

		params.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);

		camera.setParameters(params);
	}

	public void startCamera(int viewWidth, int viewHeight) {
		openCamera(viewWidth, viewHeight);
		surfaceManager.startCamera();
		camera.startPreview();
	}

	public void stopCamera() {
		if (camera == null) {
			return;
		}

		camera.stopPreview();
		camera.setPreviewCallbackWithBuffer(null);
		camera.release();
		camera = null;
		surfaceManager.stopCamera();
	}	

	private Size getOptimalPreviewSize(List<Size> sizes) {
		double minRegion = Double.MAX_VALUE;
		Size optimalSize = null;

		for (Size size : sizes) {
			if (size.width >= MIN_WIDTH && size.height >= MIN_HEIGHT) {
				if (Math.abs(size.width * size.height - MIN_WIDTH * MIN_HEIGHT) < minRegion) {
					minRegion = Math.abs(size.width * size.height - MIN_WIDTH * MIN_HEIGHT);
					optimalSize = size;

					Log.i(TAG, "Preview width : " + optimalSize.width + " height : " + optimalSize.height);
				}
			}
		}

		Log.i(TAG, "Optimal Preview width : " + optimalSize.width + " height : " + optimalSize.height);
		return optimalSize;
	}	

	public int getWidth() {
		return previewSize[0];
	}

	public int getHeight() {
		return previewSize[1];
	}

	public int getBitsPerPixels() {
		return bitsPerPixel;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		CameraJNI.newCameraFrameAvailable(data, data.length, previewSize[0], previewSize[1]);
		surfaceManager.requestRender();
		camera.addCallbackBuffer(data);
	}

	private SurfaceManager surfaceManager;

	public void setSurfaceManager(SurfaceManager surfaceManager) {
		this.surfaceManager = surfaceManager; 		
	}
}
