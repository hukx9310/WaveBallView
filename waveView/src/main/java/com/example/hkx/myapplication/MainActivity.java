
package com.example.hkx.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    PercentBallView mPercentBallView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mPercentBallView = (PercentBallView)findViewById(R.id.pbv);
        mPercentBallView.setPercent(50);
        mPercentBallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPercentBallView.setPercent((int)(Math.random()*100));
            }
        });

    }
}
