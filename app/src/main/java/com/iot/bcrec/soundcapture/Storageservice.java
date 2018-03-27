package com.iot.bcrec.soundcapture;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

//import com.iot.bcrec.soundcapture.MainActivity.*;
public class Storageservice extends Service
{
    private File myfile;
    private String state;
    private String latit,longit;
    private String s;
    private String sl,d;
    public static final String TAG="Sound";

    FirebaseDatabase database;
    DatabaseReference databaseReference;


    public void submitData(String d,String t, String lat, String lg)
    {
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();
        Map<String,String> values;
        values= new HashMap<>();
        values.put("Data",d);
        values.put("Date",t);
        values.put("latitude", lat);
        values.put("longitude",lg);
        Log.i("kks","entered0");

        databaseReference.child("IMEI").child(MainActivity.imei).push().setValue(values, new DatabaseReference.CompletionListener()
        {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null) {
                    Log.i("info", "save successfully");

                } else {
                    Log.i("info","save failed");

                }
            }
        });

        databaseReference.child("RAW DATA").push().setValue(values, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            }
        });
    }



    private BroadcastReceiver reciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s1= intent.getStringExtra("Data");
            s= s1 + "," + getTime() + "," + latit + "," + longit +","+ MainActivity.sour +","+ MainActivity.dist +"\n";
           // s= s1 + "," + getTime() +"\n";
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(myfile,true);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fileOutputStream);

                myOutWriter.append(s);
                myOutWriter.close();
                fileOutputStream.close();
                //data storing on firebase

                submitData(s1,getTime(),latit,longit);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    /**private BroadcastReceiver jReceiver= new BroadcastReceiver()
     {
     @Override
     public void onReceive(Context context, Intent intent)
     {
     sl = intent.getStringExtra("source");
     d = intent.getStringExtra("distance");
     }
     };*/
    private BroadcastReceiver mReceiver= new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            latit = intent.getStringExtra("lat");
            Log.i(TAG,"lat ="+ latit);
            longit = intent.getStringExtra("long");
            Log.i(TAG,"long ="+ longit);
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


    public Storageservice()
    {

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
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
        registerReceiver(mReceiver, new IntentFilter("LocationData"));
        //registerReceiver(jReceiver,new IntentFilter("SandD"));
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(reciever);
        unregisterReceiver(mReceiver);
        //unregisterReceiver(jReceiver);
        super.onDestroy();
    }
}
