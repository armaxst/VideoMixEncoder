package com.serenegiant.audiovideosample;

/**
 * Created by Giseok on 2015-08-26.
 */

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Handler class for asynchronous camera operation
 */
public class CameraHandler extends Handler {

	private static final boolean DEBUG = false;
	private static final String TAG = CameraHandler.class.getSimpleName();

	private static final int MSG_PREVIEW_START = 1;
	private static final int MSG_PREVIEW_STOP = 2;
	private CameraThread mThread;

	public CameraHandler(final CameraThread thread) {
		mThread = thread;
	}

	public void startPreview(final int width, final int height) {
		sendMessage(obtainMessage(MSG_PREVIEW_START, width, height));
	}

	/**
	 * request to stop camera preview
	 * @param needWait need to wait for stopping camera preview
	 */
	public void stopPreview(final boolean needWait) {
		synchronized (this) {
			sendEmptyMessage(MSG_PREVIEW_STOP);
			if (needWait && mThread.isRunning()) {
				try {
					if (DEBUG) Log.d(TAG, "wait for terminating of camera thread");
					wait();
				} catch (final InterruptedException e) {
				}
			}
		}
	}

	/**
	 * message handler for camera thread
	 */
	@Override
	public void handleMessage(final Message msg) {
		switch (msg.what) {
			case MSG_PREVIEW_START:
				mThread.startPreview(msg.arg1, msg.arg2);
				break;

			case MSG_PREVIEW_STOP:
				mThread.stopPreview();
				synchronized (this) {
					notifyAll();
				}
				Looper.myLooper().quit();
				mThread = null;
				break;
			default:
				throw new RuntimeException("unknown message:what=" + msg.what);
		}
	}
}