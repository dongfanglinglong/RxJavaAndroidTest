package com.dongfang.rx;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dongfang.rx.socket.SocekBus;
import com.dongfang.rx.socket.SocketBus;

public class MainActivity extends AppCompatActivity {


    private TextView mTextView;
    private Button mBtnStart, mBtnStop;


    private SocketBus mSocketBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textview);
        mBtnStart = (Button) findViewById(R.id.button_start);
        mBtnStop = (Button) findViewById(R.id.button_stop);

        mSocketBus = new SocketBus();


        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocketBus.start();
            }
        });


        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocketBus.stop();
            }
        });
    }
}
