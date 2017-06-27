package com.maxst.videomixer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.maxst.videomixer.camera.CameraJNI;
import com.maxst.videomixer.camera.CameraPreview;
import com.maxst.videomixer.camera.SurfaceManager;
import com.maxst.videomixer.gl.SampleGLView;
import com.maxst.videoPlayer.VideoPlayer;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private SampleGLView glView;
	private int screenWidth;
	private int screenHeight;
	private CameraPreview cameraPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		VideoPlayer.getInstance(this, "AsianAdult.mp4");
		glView = new SampleGLView(this);

		setContentView(glView);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenWidth = displaymetrics.widthPixels;
		screenHeight = displaymetrics.heightPixels;

		boolean isPortrait = (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		CameraJNI.setScreenOrientationPortrait(isPortrait);
		cameraPreview = SurfaceManager.getInstance(this).getCameraPreview();
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraPreview.startCamera(screenWidth, screenHeight);
		glView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		glView.onPause();
		cameraPreview.stopCamera();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		SurfaceManager.release();
		VideoPlayer.getInstance().destroy();
	}

	static {
		loadLibrary("NativeRenderer");
	}

	public static boolean loadLibrary(String nLibName) {
		try {
			System.loadLibrary(nLibName);
			Log.i(TAG, "Native library lib" + nLibName + ".so loaded");
			return true;
		} catch (UnsatisfiedLinkError ulee) {
			Log.i(TAG, "The library lib" + nLibName + ".so could not be loaded");
		} catch (SecurityException se) {
			Log.i(TAG, "The library lib" + nLibName + ".so was not allowed to be loaded");
		}

		return false;
	}

}
