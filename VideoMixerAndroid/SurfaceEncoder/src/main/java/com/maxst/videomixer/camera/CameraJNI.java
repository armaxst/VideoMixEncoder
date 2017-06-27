package com.maxst.videomixer.camera;

/**
 * Created by Giseok on 2015-08-16.
 */
public class CameraJNI {
    public static native void setScreenOrientationPortrait(boolean isPortrait);
    public static native void initRendering();
    public static native void updateRendering(int viewWidth, int viewHeight);
    public static native void renderFrame();
    public static native float newCameraFrameAvailable(byte [] data, int length, int width, int height);
}
