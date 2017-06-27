package com.maxst.mediacodectest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class GLSurfaceEncodingActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		encodeThread.start();
	}

	private Thread encodeThread = new Thread() {

		@Override
		public void run() {
			super.run();

			final EncodeAndMuxTest encodeAndMuxTest = new EncodeAndMuxTest();
			encodeAndMuxTest.testEncodeVideoToMp4();

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "Surface encoding completed. Out File Name : " + encodeAndMuxTest.getOutFileName(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	};
}
