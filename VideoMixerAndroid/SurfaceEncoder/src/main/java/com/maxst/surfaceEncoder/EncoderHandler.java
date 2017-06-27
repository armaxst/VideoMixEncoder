package com.maxst.surfaceEncoder;

import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Handles encoder state change requests.  The handler is created on the encoder thread.
 */
public class EncoderHandler extends Handler {

	private static final String TAG = EncoderHandler.class.getSimpleName();

	public static final int MSG_START_RECORDING = 0;
	public static final int MSG_STOP_RECORDING = 1;
	public static final int MSG_FRAME_AVAILABLE = 2;
	public static final int MSG_SET_TEXTURE_ID = 3;
	public static final int MSG_UPDATE_SHARED_CONTEXT = 4;
	public static final int MSG_QUIT = 5;

	private WeakReference<TextureMovieEncoder> mWeakEncoder;

	public EncoderHandler(TextureMovieEncoder encoder) {
		mWeakEncoder = new WeakReference<TextureMovieEncoder>(encoder);
	}

	@Override  // runs on encoder thread
	public void handleMessage(Message inputMessage) {
		int what = inputMessage.what;
		Object obj = inputMessage.obj;

		TextureMovieEncoder encoder = mWeakEncoder.get();
		if (encoder == null) {
			Log.w(TAG, "EncoderHandler.handleMessage: encoder is null");
			return;
		}

		Log.i(TAG, "Message : " + what);

		switch (what) {
			case MSG_START_RECORDING:
				encoder.handleStartRecording((TextureMovieEncoder.EncoderConfig) obj);
				break;

			case MSG_STOP_RECORDING:
				encoder.handleStopRecording();
				break;

			case MSG_FRAME_AVAILABLE:
				long timestamp = (((long) inputMessage.arg1) << 32) |
						(((long) inputMessage.arg2) & 0xffffffffL);
				encoder.handleFrameAvailable((float[]) obj, timestamp);
				break;

			case MSG_UPDATE_SHARED_CONTEXT:
				encoder.handleUpdateSharedContext((EGLContext) inputMessage.obj);
				break;

			case MSG_QUIT:
				Looper.myLooper().quit();
				break;
			default:
				throw new RuntimeException("Unhandled msg what=" + what);
		}
	}
}
