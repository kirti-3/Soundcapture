package com.iot.bcrec.soundcapture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    Button startbtn,stopbtn;
    EditText e1;
    Intent sound;
    Intent storage;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String Result = intent.getStringExtra("Data");
            e1.setText(Result);
        }
    };

    public void onRecord(View view){
        startService(storage);
        startService(sound);
        startbtn.setEnabled(false);
        stopbtn.setEnabled(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sound = new Intent(this, SoundService.class);
        storage = new Intent(this, Storageservice.class);
        e1=(EditText) findViewById(R.id.editText);
        startbtn = (Button) findViewById(R.id.button);
        stopbtn = (Button) findViewById(R.id.button2);
        startbtn.setEnabled(true);
        stopbtn.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,new IntentFilter("SoundData"));
    }
    public void onEnd(View view){
        stopService(sound);
        stopService(storage);
        startbtn.setEnabled(true);
        stopbtn.setEnabled(false);
    }
}
