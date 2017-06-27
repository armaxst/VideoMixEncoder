package com.maxst.videoPlayer;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

public class VideoPlayer implements SurfaceTexture.OnFrameAvailableListener,
		OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
		OnVideoSizeChangedListener {

	private static final String TAG = VideoPlayer.class.getSimpleName();

	private boolean updateSurface = false;
	private SurfaceTexture surfaceTexture = null;
	private MediaPlayer mediaPlayer;
	private float[] stMatrix = new float[16];
	private int surfaceTextureId = -1;
	private String fileName;
	private Context context;

	private boolean initDone = false;
	private int seekPosition = 0;

	private static VideoPlayer instance = null;

	public static VideoPlayer getInstance(Context context, String fileName) {
		if (instance == null) {
			instance = new VideoPlayer(context, fileName);
		}

		if (!fileName.equals(instance.fileName)) {
			instance = new VideoPlayer(context, fileName);
		}

		return instance;
	}

	public static VideoPlayer getInstance() {
		return instance;
	}

	private VideoPlayer(Context context, String fileName) {
		Log.i(TAG, "VideoPlayer constructor");

		this.context = context;
		this.fileName = fileName;
	}

	public void updateRendering(int viewWidth, int viewHeight) {
		initVideoPlane(viewWidth, viewHeight);
	}

	public void init() {
		surfaceTextureId = initVideoTexture();

		Log.i(TAG, "surfaceTexture id : " + surfaceTextureId);

		Surface surface = null;

		synchronized (this) {
			updateSurface = false;
			if (surfaceTexture != null) {
				surfaceTexture.release();
			}

			surfaceTexture = new SurfaceTexture(surfaceTextureId);
			surfaceTexture.setOnFrameAvailableListener(this);

			surface = new Surface(surfaceTexture);
		}

		AssetFileDescriptor afd = null;

		try {
			afd = context.getAssets().openFd(fileName);
		} catch (IOException e) {
			afd = null;
		}

		try {
			mediaPlayer = new MediaPlayer();

			if (afd != null) {
				mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
				afd.close();
			} else {
				if (fileName != null && (fileName.startsWith("http://") || fileName.startsWith("https://"))) {
					Uri uri = Uri.parse(fileName);
					mediaPlayer.setDataSource(context, uri);
				} else {
					mediaPlayer.setDataSource(fileName);
				}
			}

			mediaPlayer.setSurface(surface);
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnBufferingUpdateListener(this);
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnVideoSizeChangedListener(this);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {

		}
	}

	public void update() {
		if (!initDone) {
			init();
			initDone = true;
			return;
		}

		synchronized (this) {
			if (updateSurface) {
				surfaceTexture.updateTexImage();
				surfaceTexture.getTransformMatrix(stMatrix);
				updateSurface = false;

				seekPosition = mediaPlayer.getCurrentPosition();
			}
		}

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		drawVideo(surfaceTextureId, stMatrix);
		GLES20.glDisable(GLES20.GL_BLEND);
	}

	public void stop() {
		synchronized (this) {
			Log.i(TAG, "Stop  video : " + fileName);
			updateSurface = false;
			surfaceTexture.release();
			surfaceTexture = null;
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			initDone = false;
		}
	}

	public void destroy() {
		seekPosition = 0;
	}

	public SurfaceTexture getSurfaceTexture() {
		return surfaceTexture;
	}

	synchronized public void onFrameAvailable(SurfaceTexture surface) {
		updateSurface = true;
		glSurfaceView.requestRender();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.start();
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaPlayer.seekTo(seekPosition);
		mediaPlayer.start();
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

	}

	private void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	private native int initVideoTexture();
	private native void initVideoPlane(int videoWidth, int videoHeight);
	private native void drawVideo(int destTextureID, float [] textureMatrix);

	private GLSurfaceView glSurfaceView;
	public void setGLView(GLSurfaceView mGLView) {
		this.glSurfaceView = mGLView;
	}
}
