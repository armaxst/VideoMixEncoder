package com.maxst.videomixer.gl;

/**
 * Created by Giseok on 2015-08-20.
 */
public class RenderTexture {
	public native static int initTargetTexture();
	public native static int initFBO(int width, int height);
	public native static void startRTT();
	public native static void endRTT();
	public native static void drawTexture();
}
