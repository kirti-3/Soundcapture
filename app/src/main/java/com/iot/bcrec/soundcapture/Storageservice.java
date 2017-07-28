package com.iot.bcrec.soundcapture;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Storageservice extends Service {

    private File myfile;
    private String state;
    public static final String TAG="Sound";
    private BroadcastReceiver reciever= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s1= intent.getStringExtra("Data");
            String s= s1 + "," + getTime() +"\n";
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(myfile,true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fileOutputStream);

                myOutWriter.append(s);
                myOutWriter.close();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private String getTime()
    {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Log.d(TAG,"storing data");
        String formattedTime = df.format(c.getTime());
        return(formattedTime);
    }
    public Storageservice() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"storage service");
        state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state))
        {
            File Root = Environment.getExternalStorageDirectory();
            File Dir = new File(Root.getAbsolutePath()+"/NoiseD");
            Log.d(TAG,"file created");
            if(!Dir.exists()){
                Dir.mkdir();
            }
            myfile = new File(Dir,"/Data.csv");
            Log.d(TAG,"Data wrriten in "+ myfile);
            try {
                myfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        registerReceiver(reciever, new IntentFilter("SoundData"));
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(reciever);
        super.onDestroy();
    }
}
