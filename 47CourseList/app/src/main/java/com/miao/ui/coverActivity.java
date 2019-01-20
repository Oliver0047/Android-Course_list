package com.miao.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;

import android.widget.Button;

import com.miao.R;

/**
 * Created by Oliver0047 on 2018/5/20.
 */

public class coverActivity extends Activity {
    private Button enter_button;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enterpic);
        handler.sendEmptyMessageDelayed(0,3000);
        enter_button=(Button)findViewById(R.id.coverEnter);
        enter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enter_button.setText("Loading...");
                handler.removeMessages(0);
                goHome();
            }
        });

    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            goHome();
            super.handleMessage(msg);
        }
    };

    public void goHome(){
        Intent intent = new Intent(coverActivity.this, LoginActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putSerializable("logout","false");
        intent.putExtras(mBundle);
        startActivity(intent);
        finish();
    }
}
