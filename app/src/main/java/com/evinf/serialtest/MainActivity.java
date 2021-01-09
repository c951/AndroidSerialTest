package com.evinf.serialtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android_serialport_api.SerialPort;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    protected SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;

    EditText editTextSend;
    TextView textViewRecv;

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];

                    if (mInputStream == null) {
                        return;
                    }

                    size = mInputStream.read(buffer);

                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextSend = findViewById(R.id.edt_send);
        textViewRecv = findViewById(R.id.txt_recv);

        try {
            mSerialPort = new SerialPort(new File("/dev/ttysWK3"), 115200, 0);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();
            ReadThread mReadThread = new ReadThread();
            mReadThread.start();
        } catch (Exception e) {
            Log.e(TAG, "串口打开失败 " + e);
            e.printStackTrace();
        }

        Button btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            try {
                mOutputStream.write(editTextSend.getText().toString().getBytes());
            } catch (IOException e) {
                Log.e(TAG, "发送失败" + e);
                e.printStackTrace();
            }
        });

    }
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(() -> {
            String recInfo = new String(buffer, 0, size);
            textViewRecv.append(recInfo);
        });
    }
}