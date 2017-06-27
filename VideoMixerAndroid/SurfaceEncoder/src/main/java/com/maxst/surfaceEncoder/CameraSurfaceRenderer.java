package com.maxst.surfaceEncoder;

import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.maxst.videoPlayer.VideoPlayer;
import com.maxst.videomixer.camera.CameraJNI;
import com.maxst.videomixer.gl.RenderTexture;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Renderer object for our GLSurfaceView.
 * <p>
 * Do not call any methods here directly from another thread -- use the
 * GLSurfaceView#queueEvent() call.
 */
class CameraSurfaceRenderer implements GLSurfaceView.Renderer {
	private static final String TAG = CameraSurfaceRenderer.class.getSimpleName();
	private static final boolean VERBOSE = false;

	private static final int RECORDING_OFF = 0;
	private static final int RECORDING_ON = 1;
	private static final int RECORDING_RESUMED = 2;

	private TextureMovieEncoder mVideoEncoder;
	private File mOutputFile;

	private boolean mRecordingEnabled;
	private int mRecordingStatus;


	/**
	 * Constructs CameraSurfaceRenderer.
	 * <p>
	 * @param movieEncoder video encoder object
	 * @param outputFile output file for encoded video; forwarded to movieEncoder
	 */
	public CameraSurfaceRenderer(TextureMovieEncoder movieEncoder, File outputFile) {
		mVideoEncoder = movieEncoder;
		mOutputFile = outputFile;

		mRecordingStatus = -1;
		mRecordingEnabled = false;
	}

	/**
	 * Notifies the renderer thread that the activity is pausing.
	 * <p>
	 * For best results, call this *after* disabling Camera preview.
	 */
	public void notifyPausing() {
	}

	/**
	 * Notifies the renderer that we want to stop or start recording.
	 */
	public void changeRecordingState(boolean isRecording) {
		Log.d(TAG, "changeRecordingState: was " + mRecordingEnabled + " now " + isRecording);
		mRecordingEnabled = isRecording;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		Log.d(TAG, "onSurfaceCreated");

		// We're starting up or coming back.  Either way we've got a new EGLContext that will
		// need to be shared with the video encoder, so figure out if a recording is already
		// in progress.
		mRecordingEnabled = mVideoEncoder.isRecording();
		if (mRecordingEnabled) {
			mRecordingStatus = RECORDING_RESUMED;
		} else {
			mRecordingStatus = RECORDING_OFF;
		}

		CameraJNI.initRendering();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		Log.d(TAG, "onSurfaceChanged " + width + "x" + height);

		VideoPlayer.getInstance().updateRendering(width, height);

		CameraJNI.updateRendering(width, height);
		RenderTexture.initTargetTexture();
		RenderTexture.initFBO(width, height);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		boolean showBox = false;

		// If the recording state is changing, take care of it here.  Ideally we wouldn't
		// be doing all this in onDrawFrame(), but the EGLContext sharing with GLSurfaceView
		// makes it hard to do elsewhere.
		if (mRecordingEnabled) {
			switch (mRecordingStatus) {
				case RECORDING_OFF:
					Log.d(TAG, "START recording");
					// start recording
					mVideoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
							mOutputFile, 1280, 720, 1000000, EGL14.eglGetCurrentContext()));
					mRecordingStatus = RECORDING_ON;
					break;
				case RECORDING_RESUMED:
					Log.d(TAG, "RESUME recording");
					mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
					mRecordingStatus = RECORDING_ON;
					break;
				case RECORDING_ON:
					// yay
					break;
				default:
					throw new RuntimeException("unknown status " + mRecordingStatus);
			}
		} else {
			switch (mRecordingStatus) {
				case RECORDING_ON:
				case RECORDING_RESUMED:
					// stop recording
					Log.d(TAG, "STOP recording");
					mVideoEncoder.stopRecording();
					mRecordingStatus = RECORDING_OFF;
					break;
				case RECORDING_OFF:
					// yay
					break;
				default:
					throw new RuntimeException("unknown status " + mRecordingStatus);
			}
		}

		// Tell the video encoder thread that a new frame is available.
		// This will be ignored if we're not actually recording.

		RenderTexture.startRTT();
		CameraJNI.renderFrame();
		VideoPlayer.getInstance().update();
		RenderTexture.endRTT();

		mVideoEncoder.frameAvailable(VideoPlayer.getInstance().getSurfaceTexture());

		RenderTexture.drawTexture();
	}

	public void surfaceDestroyed() {
		notifyPausing();
		VideoPlayer.getInstance().stop();
	}

	/**
	 * Draws a red box in the corner.
	 */
	private void drawBox() {
		GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
		GLES20.glScissor(0, 0, 100, 100);
		GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
	}
}