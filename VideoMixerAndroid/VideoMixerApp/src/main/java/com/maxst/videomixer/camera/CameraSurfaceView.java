package com.maxst.videomixer.camera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements Callback {
	
	private static final String TAG = CameraSurfaceView.class.getSimpleName();

	private Camera camera;

	public CameraSurfaceView(Context context) {
		super(context);

		getHolder().addCallback(this);
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		//camera.startPreview();
		Log.i(TAG, "Start camera preview");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "Stop camera preview");
		camera = null;
		//camera.stopPreview();
	}
}