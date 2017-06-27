package com.maxst.mediacodectest;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Giseok on 2015-08-23.
 */
public class CameraEncodingTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            new CameraToMpegTest().testEncodeCameraToMp4();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
