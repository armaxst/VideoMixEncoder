package com.maxst.videomixer;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.maxst.ar.BackgroundRenderer;
import com.maxst.ar.CameraDevice;
import com.maxst.ar.TrackerManager;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private GLSurfaceView glSurfaceView;
	private TrackerManager trackerManager;
	private CameraDevice cameraDevice;
	private BackgroundRenderer backgroundRenderer;
	private VideoMixerRenderer videoMixerRenderer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		VideoPlayer.getInstance(this, "AsianAdult.mp4");
		glSurfaceView = new GLSurfaceView(this);
		glSurfaceView.setEGLContextClientVersion(2);

		videoMixerRenderer = new VideoMixerRenderer(this);
		glSurfaceView.setRenderer(videoMixerRenderer);

		setContentView(glSurfaceView);

		trackerManager = TrackerManager.getInstance();
		cameraDevice = CameraDevice.getInstance();
		backgroundRenderer = BackgroundRenderer.getInstance();

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		trackerManager.init(this, "FFZygliqyv5ZbGL31UJ1QBbe3J9SCTv3Iu+cynC3bh4=");
		backgroundRenderer.setScreenOrientation(getResources().getConfiguration().orientation);
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraDevice.start(0, 1280, 720);
		glSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		glSurfaceView.onPause();
		glSurfaceView.queueEvent(new Runnable() {
			@Override
			public void run() {
				backgroundRenderer.deinitRendering();
				videoMixerRenderer.onPause();
			}
		});
		cameraDevice.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		trackerManager.deinit();
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
