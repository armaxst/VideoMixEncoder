package com.maxst.videomixer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.maxst.ar.MaxstARAPI;
import com.maxst.videomixer.gl.SampleGLView;
import com.maxst.videoPlayer.VideoPlayer;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private SampleGLView glView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		VideoPlayer.getInstance(this, "AsianAdult.mp4");
		glView = new SampleGLView(this);

		setContentView(glView);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		MaxstARAPI.init(this, "FFZygliqyv5ZbGL31UJ1QBbe3J9SCTv3Iu+cynC3bh4=");
		MaxstARAPI.setScreenOrientation(getResources().getConfiguration().orientation);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MaxstARAPI.startCamera(0, 1280, 720);
		glView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		glView.onPause();
		glView.queueEvent(new Runnable() {
			@Override
			public void run() {
				VideoPlayer.getInstance().stop();
			}
		});
		MaxstARAPI.stopCamera();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		glView.queueEvent(new Runnable() {
			@Override
			public void run() {
				MaxstARAPI.deinitRendering();
				MaxstARAPI.deinit();
				VideoPlayer.getInstance().destroy();
			}
		});
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
