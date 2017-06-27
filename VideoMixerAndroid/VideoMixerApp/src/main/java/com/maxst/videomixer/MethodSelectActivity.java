package com.maxst.videomixer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.maxst.viewMapper.DeclareView;
import com.maxst.viewMapper.ViewMapper;

/**
 * Created by Giseok on 2015-08-23.
 */
public class MethodSelectActivity extends Activity {

     @DeclareView(id = R.id.alpha_video_play_test, click = "clickListener")
    private Button alphaVideoPlayTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_method_select);

        ViewMapper.mapLayout(this, getWindow().getDecorView());
    }

    public View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.alpha_video_play_test:
                    startActivity(new Intent(MethodSelectActivity.this, MainActivity.class));
                    break;
            }
        }
    };
}
