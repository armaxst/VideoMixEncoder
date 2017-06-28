/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maxst.surfaceEncoder;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.maxst.ar.MaxstARAPI;
import com.maxst.videoPlayer.VideoPlayer;

import java.io.File;

public class CameraCaptureActivity extends Activity {
    private static final String TAG = CameraCaptureActivity.class.getSimpleName();

    private GLSurfaceView mGLView;
    private CameraSurfaceRenderer mRenderer;

    private int screenWidth;
    private int screenHeight;

    // this is static so it survives activity restarts
    private static TextureMovieEncoder sVideoEncoder = new TextureMovieEncoder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_capture);

        VideoPlayer.getInstance(this, Environment.getExternalStorageDirectory().getAbsolutePath() + "/SurfaceEncoder/" + "hobi_150909.mp4");

        new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SurfaceEncoder").mkdir();

        File outputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SurfaceEncoder", String.valueOf(System.currentTimeMillis()) + ".mp4");
        TextView fileText = (TextView) findViewById(R.id.cameraOutputFile_text);
        fileText.setText(outputFile.toString());

        // Define a handler that receives camera-control messages from other threads.  All calls
        // to Camera must be made on the same thread.  Note we create this before the renderer
        // thread, so we know the fully-constructed object will be visible.
        mRecordingEnabled = sVideoEncoder.isRecording();

        // Configure the GLSurfaceView.  This will start the Renderer thread, with an
        // appropriate EGL context.
        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        mRenderer = new CameraSurfaceRenderer(sVideoEncoder, outputFile);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        VideoPlayer.getInstance().setGLView(mGLView);

        findViewById(R.id.toggleRecording_button).setOnClickListener(clickListener);

        Log.d(TAG, "onCreate complete: " + this);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenWidth = displaymetrics.widthPixels;
        screenHeight = displaymetrics.heightPixels;

        MaxstARAPI.setScreenOrientation(getResources().getConfiguration().orientation);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume -- acquiring camera");
        super.onResume();
        updateControls();
        MaxstARAPI.startCamera(0, 1280, 720);

        mGLView.onResume();
        Log.d(TAG, "onResume complete: " + this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause -- releasing camera");
        super.onPause();
        MaxstARAPI.stopCamera();
        mGLView.onPause();
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                // notify the renderer that we want to change the encoder's state
                mRenderer.surfaceDestroyed();
                MaxstARAPI.deinitRendering();
            }
        });
        Log.d(TAG, "onPause complete");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        MaxstARAPI.deinit();
        VideoPlayer.getInstance().destroy();
    }

    public View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            mRecordingEnabled = !mRecordingEnabled;

            mGLView.queueEvent(new Runnable() {
                @Override public void run() {
                    // notify the renderer that we want to change the encoder's state
                    mRenderer.changeRecordingState(mRecordingEnabled);
                }
            });
            updateControls();
        }
    };

    private boolean mRecordingEnabled = false;

    /**
     * Updates the on-screen controls to reflect the current state of the app.
     */
    private void updateControls() {
        Button toggleRelease = (Button) findViewById(R.id.toggleRecording_button);
        int id = mRecordingEnabled ?
                R.string.toggleRecordingOff : R.string.toggleRecordingOn;
        toggleRelease.setText(id);
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

