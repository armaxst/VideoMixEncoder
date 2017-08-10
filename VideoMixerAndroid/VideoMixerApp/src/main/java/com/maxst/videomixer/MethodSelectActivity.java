package com.maxst.videomixer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MethodSelectActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_method_select);

		ButterKnife.bind(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		ButterKnife.unbind(this);
	}

	@OnClick(R.id.alpha_video_play_test)
	public void onClick() {
		startActivity(new Intent(MethodSelectActivity.this, MainActivity.class));
	}
}
