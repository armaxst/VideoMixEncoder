package com.maxst.mediacodectest;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Giseok on 2015-08-19.
 */
public class CameraToMpegTest {

	private static final String TAG = "CameraToMpegTest";
	private static final boolean VERBOSE = false;           // lots of logging

	// where to put the output file (note: /sdcard requires WRITE_EXTERNAL_STORAGE permission)
	private static final File OUTPUT_DIR = Environment.getExternalStorageDirectory();

	// parameters for the encoder
	private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
	private static final int FRAME_RATE = 30;               // 30fps
	private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames
	private static final long DURATION_SEC = 8;             // 8 seconds of video

	// Fragment shader that swaps color channels around.
	private static final String SWAPPED_FRAGMENT_SHADER =
			"#extension GL_OES_EGL_image_external : require\n" +
					"precision mediump float;\n" +
					"varying vec2 vTextureCoord;\n" +
					"uniform samplerExternalOES sTexture;\n" +
					"void main() {\n" +
					"  gl_FragColor = texture2D(sTexture, vTextureCoord).gbra;\n" +
					"}\n";

	// encoder / muxer state
	private MediaCodec mEncoder;
	private CodecInputSurface mInputSurface;
	private MediaMuxer mMuxer;
	private int mTrackIndex;
	private boolean mMuxerStarted;

	// camera state
	private Camera mCamera;
	private SurfaceTextureManager mStManager;

	// allocate one of these up front so we don't need to do it every time
	private MediaCodec.BufferInfo mBufferInfo;

	/** test entry point */
	public void testEncodeCameraToMp4() throws Throwable {
		CameraToMpegWrapper.runTest(this);
	}

	/**
	 * Wraps encodeCameraToMpeg().  This is necessary because SurfaceTexture will try to use
	 * the looper in the current thread if one exists, and the CTS tests create one on the
	 * test thread.
	 *
	 * The wrapper propagates exceptions thrown by the worker thread back to the caller.
	 */
	private static class CameraToMpegWrapper implements Runnable {
		private Throwable mThrowable;
		private CameraToMpegTest mCameraToMpegTest;

		private CameraToMpegWrapper(CameraToMpegTest test) {
			mCameraToMpegTest = test;
		}

		@Override
		public void run() {
			try {
				mCameraToMpegTest.encodeCameraToMpeg();
			} catch (Throwable th) {
				mThrowable = th;
			}
		}

		/** Entry point. */
		public static void runTest(CameraToMpegTest obj) throws Throwable {
			CameraToMpegWrapper wrapper = new CameraToMpegWrapper(obj);
			Thread th = new Thread(wrapper, "codec test");
			th.start();
			th.join();
			if (wrapper.mThrowable != null) {
				throw wrapper.mThrowable;
			}
		}
	}

	/**
	 * Tests encoding of AVC video from Camera input.  The output is saved as an MP4 file.
	 */
	private void encodeCameraToMpeg() {
		// arbitrary but popular values
		int encWidth = 640;
		int encHeight = 480;
		int encBitRate = 6000000;      // Mbps
		Log.d(TAG, MIME_TYPE + " output " + encWidth + "x" + encHeight + " @" + encBitRate);

		try {
			prepareCamera(encWidth, encHeight);
			prepareEncoder(encWidth, encHeight, encBitRate);
			mInputSurface.makeCurrent();

			prepareSurfaceTexture();
			mCamera.startPreview();

			long startWhen = System.nanoTime();
			long desiredEnd = startWhen + DURATION_SEC * 1000000000L;
			SurfaceTexture st = mStManager.getSurfaceTexture();
			int frameCount = 0;

			while(System.nanoTime() < desiredEnd) {
				mStManager.drawImage();

				mInputSurface.setPresentationTime(st.getTimestamp());
				mInputSurface.swapBuffers();
			}
		} finally {
			releaseCamera();
			releaseSurfaceTexture();
		}

//		try {
//			prepareCamera(encWidth, encHeight);
//			prepareEncoder(encWidth, encHeight, encBitRate);
//			mInputSurface.makeCurrent();
//			prepareSurfaceTexture();
//
//			mCamera.startPreview();
//
//			long startWhen = System.nanoTime();
//			long desiredEnd = startWhen + DURATION_SEC * 1000000000L;
//			SurfaceTexture st = mStManager.getSurfaceTexture();
//			int frameCount = 0;
//
//			while (System.nanoTime() < desiredEnd) {
//				// Feed any pending encoder output into the muxer.
//				drainEncoder(false);
//
//				// Switch up the colors every 15 frames.  Besides demonstrating the use of
//				// fragment shaders for video editing, this provides a visual indication of
//				// the frame rate: if the camera is capturing at 15fps, the colors will change
//				// once per second.
//				if ((frameCount % 15) == 0) {
//					String fragmentShader = null;
//					if ((frameCount & 0x01) != 0) {
//						fragmentShader = SWAPPED_FRAGMENT_SHADER;
//					}
//					mStManager.changeFragmentShader(fragmentShader);
//				}
//				frameCount++;
//
//				// Acquire a new frame of input, and render it to the Surface.  If we had a
//				// GLSurfaceView we could switch EGL contexts and call drawImage() a second
//				// time to render it on screen.  The texture can be shared between contexts by
//				// passing the GLSurfaceView's EGLContext as eglCreateContext()'s share_context
//				// argument.
//				mStManager.awaitNewImage();
//				mStManager.drawImage();
//
//				// Set the presentation time stamp from the SurfaceTexture's time stamp.  This
//				// will be used by MediaMuxer to set the PTS in the video.
//				if (VERBOSE) {
//					Log.d(TAG, "present: " +
//							((st.getTimestamp() - startWhen) / 1000000.0) + "ms");
//				}
//				mInputSurface.setPresentationTime(st.getTimestamp());
//
//				// Submit it to the encoder.  The eglSwapBuffers call will block if the input
//				// is full, which would be bad if it stayed full until we dequeued an output
//				// buffer (which we can't do, since we're stuck here).  So long as we fully drain
//				// the encoder before supplying additional input, the system guarantees that we
//				// can supply another frame without blocking.
//				if (VERBOSE) Log.d(TAG, "sending frame to encoder");
//				mInputSurface.swapBuffers();
//			}
//
//			// send end-of-stream to encoder, and drain remaining output
//			drainEncoder(true);
//		} finally {
//			// release everything we grabbed
//			releaseCamera();
//			releaseEncoder();
//			releaseSurfaceTexture();
//		}
	}

	/**
	 * Configures Camera for video capture.  Sets mCamera.
	 * <p>
	 * Opens a Camera and sets parameters.  Does not start preview.
	 */
	private void prepareCamera(int encWidth, int encHeight) {
		if (mCamera != null) {
			throw new RuntimeException("camera already initialized");
		}

		Camera.CameraInfo info = new Camera.CameraInfo();

		// Try to find a front-facing camera (e.g. for videoconferencing).
		int numCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numCameras; i++) {
			Camera.getCameraInfo(i, info);
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				mCamera = Camera.open(i);
				break;
			}
		}
		if (mCamera == null) {
			Log.d(TAG, "No front-facing camera found; opening default");
			mCamera = Camera.open();    // opens first back-facing camera
		}
		if (mCamera == null) {
			throw new RuntimeException("Unable to open camera");
		}

		Camera.Parameters parms = mCamera.getParameters();

		choosePreviewSize(parms, encWidth, encHeight);
		// leave the frame rate set to default
		mCamera.setParameters(parms);

		Camera.Size size = parms.getPreviewSize();
		Log.d(TAG, "Camera preview size is " + size.width + "x" + size.height);
	}

	/**
	 * Attempts to find a preview size that matches the provided width and height (which
	 * specify the dimensions of the encoded video).  If it fails to find a match it just
	 * uses the default preview size.
	 * <p>
	 * TODO: should do a best-fit match.
	 */
	private static void choosePreviewSize(Camera.Parameters parms, int width, int height) {
		// We should make sure that the requested MPEG size is less than the preferred
		// size, and has the same aspect ratio.
		Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
		if (VERBOSE && ppsfv != null) {
			Log.d(TAG, "Camera preferred preview size for video is " +
					ppsfv.width + "x" + ppsfv.height);
		}

		for (Camera.Size size : parms.getSupportedPreviewSizes()) {
			if (size.width == width && size.height == height) {
				parms.setPreviewSize(width, height);
				return;
			}
		}

		Log.w(TAG, "Unable to set preview size to " + width + "x" + height);
		if (ppsfv != null) {
			parms.setPreviewSize(ppsfv.width, ppsfv.height);
		}
	}

	/**
	 * Stops camera preview, and releases the camera to the system.
	 */
	private void releaseCamera() {
		if (VERBOSE) Log.d(TAG, "releasing camera");
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * Configures SurfaceTexture for camera preview.  Initializes mStManager, and sets the
	 * associated SurfaceTexture as the Camera's "preview texture".
	 * <p>
	 * Configure the EGL surface that will be used for output before calling here.
	 */
	private void prepareSurfaceTexture() {
		mStManager = new SurfaceTextureManager();
		SurfaceTexture st = mStManager.getSurfaceTexture();
		try {
			mCamera.setPreviewTexture(st);
		} catch (IOException ioe) {
			throw new RuntimeException("setPreviewTexture failed", ioe);
		}
	}

	/**
	 * Releases the SurfaceTexture.
	 */
	private void releaseSurfaceTexture() {
		if (mStManager != null) {
			mStManager.release();
			mStManager = null;
		}
	}

	/**
	 * Configures encoder and muxer state, and prepares the input Surface.  Initializes
	 * mEncoder, mMuxer, mInputSurface, mBufferInfo, mTrackIndex, and mMuxerStarted.
	 */
	private void prepareEncoder(int width, int height, int bitRate) {
		mBufferInfo = new MediaCodec.BufferInfo();

		MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

		// Set some properties.  Failing to specify some of these can cause the MediaCodec
		// configure() call to throw an unhelpful exception.
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
				MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
		if (VERBOSE) Log.d(TAG, "format: " + format);

		// Create a MediaCodec encoder, and configure it with our format.  Get a Surface
		// we can use for input and wrap it with a class that handles the EGL work.
		//
		// If you want to have two EGL contexts -- one for display, one for recording --
		// you will likely want to defer instantiation of CodecInputSurface until after the
		// "display" EGL context is created, then modify the eglCreateContext call to
		// take eglGetCurrentContext() as the share_context argument.
		try {
			mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		mInputSurface = new CodecInputSurface(mEncoder.createInputSurface());
		mEncoder.start();

		// Output filename.  Ideally this would use Context.getFilesDir() rather than a
		// hard-coded output directory.
		String outputPath = new File(OUTPUT_DIR,
				"test." + width + "x" + height + ".mp4").toString();
		Log.i(TAG, "Output file is " + outputPath);


		// Create a MediaMuxer.  We can't add the video track and start() the muxer here,
		// because our MediaFormat doesn't have the Magic Goodies.  These can only be
		// obtained from the encoder after it has started processing data.
		//
		// We're not actually interested in multiplexing audio.  We just want to convert
		// the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
		try {
			mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
		} catch (IOException ioe) {
			throw new RuntimeException("MediaMuxer creation failed", ioe);
		}

		mTrackIndex = -1;
		mMuxerStarted = false;
	}

	/**
	 * Releases encoder resources.
	 */
	private void releaseEncoder() {
		if (VERBOSE) Log.d(TAG, "releasing encoder objects");
		if (mEncoder != null) {
			mEncoder.stop();
			mEncoder.release();
			mEncoder = null;
		}
		if (mInputSurface != null) {
			mInputSurface.release();
			mInputSurface = null;
		}
		if (mMuxer != null) {
			mMuxer.stop();
			mMuxer.release();
			mMuxer = null;
		}
	}

	/**
	 * Extracts all pending data from the encoder and forwards it to the muxer.
	 * <p>
	 * If endOfStream is not set, this returns when there is no more data to drain.  If it
	 * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
	 * Calling this with endOfStream set should be done once, right before stopping the muxer.
	 * <p>
	 * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
	 * not recording audio.
	 */
	private void drainEncoder(boolean endOfStream) {
		final int TIMEOUT_USEC = 10000;
		if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

		if (endOfStream) {
			if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
			mEncoder.signalEndOfInputStream();
		}

		ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
		while (true) {
			int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
			if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
				// no output available yet
				if (!endOfStream) {
					break;      // out of while
				} else {
					if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
				}
			} else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
				// not expected for an encoder
				encoderOutputBuffers = mEncoder.getOutputBuffers();
			} else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
				// should happen before receiving buffers, and should only happen once
				if (mMuxerStarted) {
					throw new RuntimeException("format changed twice");
				}
				MediaFormat newFormat = mEncoder.getOutputFormat();
				Log.d(TAG, "encoder output format changed: " + newFormat);

				// now that we have the Magic Goodies, start the muxer
				mTrackIndex = mMuxer.addTrack(newFormat);
				mMuxer.start();
				mMuxerStarted = true;
			} else if (encoderStatus < 0) {
				Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
						encoderStatus);
				// let's ignore it
			} else {
				ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
				if (encodedData == null) {
					throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
							" was null");
				}

				if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
					// The codec config data was pulled out and fed to the muxer when we got
					// the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
					if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
					mBufferInfo.size = 0;
				}

				if (mBufferInfo.size != 0) {
					if (!mMuxerStarted) {
						throw new RuntimeException("muxer hasn't started");
					}

					// adjust the ByteBuffer values to match BufferInfo (not needed?)
					encodedData.position(mBufferInfo.offset);
					encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

					mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
					if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
				}

				mEncoder.releaseOutputBuffer(encoderStatus, false);

				if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
					if (!endOfStream) {
						Log.w(TAG, "reached end of stream unexpectedly");
					} else {
						if (VERBOSE) Log.d(TAG, "end of stream reached");
					}
					break;      // out of while
				}
			}
		}
	}
}
