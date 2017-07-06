package com.example.hw.hwprogressbutton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 如果要实现平滑滚动的效果，既可以借助于 Scroller 也可以自己实现属性动画来完成的
 */

public class MainActivity extends AppCompatActivity {
    private ProgressButton mProgressButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressButton = (ProgressButton) findViewById(R.id.hwAnimatorView);
        mProgressButton.setProgressButtonListener(new ProgressButton.ProgressButtonListener() {
            @Override
            public void onClickListener() {
                mProgressButton.start();//开启动画
            }

            @Override
            public void animationFinish() {
                Toast.makeText(MainActivity.this, "自定义动画提交控件", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
