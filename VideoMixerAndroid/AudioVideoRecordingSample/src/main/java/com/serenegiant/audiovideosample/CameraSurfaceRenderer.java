package com.serenegiant.audiovideosample;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.serenegiant.encoder.MediaVideoEncoder;
import com.serenegiant.glutils.GLDrawer2D;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * GLSurfaceViewのRenderer
 */
public final class CameraSurfaceRenderer
		implements GLSurfaceView.Renderer,
		SurfaceTexture.OnFrameAvailableListener {	// API >= 11

	private static final boolean DEBUG = false;
	private static final String TAG = CameraSurfaceRenderer.class.getSimpleName();

	private final WeakReference<CameraGLView> mCameraGLViewReference;
	private SurfaceTexture mSurfaceTexture;	// API >= 11
	private int mTextureID;
	private GLDrawer2D mDrawer;
	private final float[] mStMatrix = new float[16];
	private final float[] mMvpMatrix = new float[16];
	private MediaVideoEncoder mVideoEncoder;

	public CameraSurfaceRenderer(final CameraGLView cameraGLView) {
		if (DEBUG) Log.v(TAG, "CameraSurfaceRenderer:");
		mCameraGLViewReference = new WeakReference<CameraGLView>(cameraGLView);
		Matrix.setIdentityM(mMvpMatrix, 0);
	}

	@Override
	public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
		if (DEBUG) Log.v(TAG, "onSurfaceCreated:");
		// This renderer required OES_EGL_image_external extension
		final String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);	// API >= 8
//			if (DEBUG) Log.i(TAG, "onSurfaceCreated:Gl extensions: " + extensions);

		if (!extensions.contains("OES_EGL_image_external")) {
			throw new RuntimeException("This system does not support OES_EGL_image_external.");
		}

		// create textur ID
		mTextureID = GLDrawer2D.initTex();
		// create SurfaceTexture with texture ID.
		mSurfaceTexture = new SurfaceTexture(mTextureID);
		mSurfaceTexture.setOnFrameAvailableListener(this);
		// clear screen with yellow color so that you can see rendering rectangle
		GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
		final CameraGLView cameraGLView = mCameraGLViewReference.get();
		if (cameraGLView != null) {
			cameraGLView.setHasSurface(true);
		}
		// create object for preview display
		mDrawer = new GLDrawer2D();
		mDrawer.setMatrix(mMvpMatrix, 0);
	}

	@Override
	public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
		if (DEBUG) Log.v(TAG, String.format("onSurfaceChanged:(%d,%d)", width, height));
		// if at least with or height is zero, initialization of this view is still progress.
		if ((width == 0) || (height == 0)) return;
		updateViewport();
		final CameraGLView cameraGLView = mCameraGLViewReference.get();
		if (cameraGLView != null) {
			cameraGLView.startPreview(width, height);
		}
	}

	/**
	 * when GLSurface context is soon destroyed
	 */
	public void onSurfaceDestroyed() {
		if (DEBUG) Log.v(TAG, "onSurfaceDestroyed:");
		if (mDrawer != null) {
			mDrawer.release();
			mDrawer = null;
		}
		if (mSurfaceTexture != null) {
			mSurfaceTexture.release();
			mSurfaceTexture = null;
		}
		GLDrawer2D.deleteTex(mTextureID);
	}

	public final void updateViewport() {
		final CameraGLView cameraGLView = mCameraGLViewReference.get();
		if (cameraGLView != null) {
			final int glViewWidth = cameraGLView.getWidth();
			final int glViewHeight = cameraGLView.getHeight();
			GLES20.glViewport(0, 0, glViewWidth, glViewHeight);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			final double videoWidth = cameraGLView.getVideoWidth();
			final double videoHeight = cameraGLView.getVideoHeight();
			if (videoWidth == 0 || videoHeight == 0) return;
			Matrix.setIdentityM(mMvpMatrix, 0);
			final double viewAspect = glViewWidth / (double)glViewHeight;
			Log.i(TAG, String.format("view(%d,%d)%f,video(%1.0f,%1.0f)", glViewWidth, glViewHeight, viewAspect, videoWidth, videoHeight));
			switch (cameraGLView.getScaleMode()) {
				case CameraGLView.SCALE_STRETCH_FIT:
					break;

				case CameraGLView.SCALE_KEEP_ASPECT_VIEWPORT: {
					final double req = videoWidth / videoHeight;
					int x, y;
					int width, height;
					if (viewAspect > req) {
						// if view is wider than camera image, calc width of drawing area based on view height
						y = 0;
						height = glViewHeight;
						width = (int)(req * glViewHeight);
						x = (glViewWidth - width) / 2;
					} else {
						// if view is higher than camera image, calc height of drawing area based on view width
						x = 0;
						width = glViewWidth;
						height = (int)(glViewWidth / req);
						y = (glViewHeight - height) / 2;
					}
					// set viewport to draw keeping aspect ration of camera image
					if (DEBUG) Log.v(TAG, String.format("xy(%d,%d),size(%d,%d)", x, y, width, height));
					GLES20.glViewport(x, y, width, height);
					break;
				}

				case CameraGLView.SCALE_KEEP_ASPECT:
				case CameraGLView.SCALE_CROP_CENTER: {
					final double scale_x = glViewWidth / videoWidth;
					final double scale_y = glViewHeight / videoHeight;
					final double scale = (cameraGLView.getScaleMode() == CameraGLView.SCALE_CROP_CENTER
							? Math.max(scale_x,  scale_y) : Math.min(scale_x, scale_y));
					final double width = scale * videoWidth;
					final double height = scale * videoHeight;
					Log.v(TAG, String.format("size(%1.0f,%1.0f),scale(%f,%f),mat(%f,%f)",
							width, height, scale_x, scale_y, width / glViewWidth, height / glViewHeight));
					Matrix.scaleM(mMvpMatrix, 0, (float)(width / glViewWidth), (float)(height / glViewHeight), 1.0f);
					break;
				}
			}
			if (mDrawer != null)
				mDrawer.setMatrix(mMvpMatrix, 0);
		}
	}

	private volatile boolean requestUpdateTex = false;
	private boolean flip = true;
	/**
	 * drawing to GLSurface
	 * we set renderMode to GLSurfaceView.RENDERMODE_WHEN_DIRTY,
	 * this method is only called when #requestRender is called(= when texture is required to update)
	 * if you don't set RENDERMODE_WHEN_DIRTY, this method is called at maximum 60fps
	 */
	@Override
	public void onDrawFrame(final GL10 unused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		if (requestUpdateTex) {
			requestUpdateTex = false;
			// update texture(came from camera)
			mSurfaceTexture.updateTexImage();
			// get texture matrix
			mSurfaceTexture.getTransformMatrix(mStMatrix);
		}
		// draw to preview screen
		mDrawer.draw(mTextureID, mStMatrix);
		flip = !flip;
		if (flip) {	// ~30fps
			synchronized (this) {
				if (mVideoEncoder != null) {
					// notify to capturing thread that the camera frame is available.
					mVideoEncoder.frameAvailableSoon(mStMatrix);
				}
			}
		}
	}

	@Override
	public void onFrameAvailable(final SurfaceTexture st) {
		requestUpdateTex = true;
			final CameraGLView parent = mCameraGLViewReference.get();
			if (parent != null)
				parent.requestRender();
	}

	public SurfaceTexture getSurfaceTexture() {
		return mSurfaceTexture;
	}

	public int getTextureID() {
		return mTextureID;
	}

	public void setVideoEncoder(MediaVideoEncoder encoder) {
		mVideoEncoder = encoder;
	}
}