package com.iot.bcrec.soundcapture;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button startbtn,stopbtn;
    TelephonyManager tm;
    public static String imei;
    EditText e1;
    Intent sound;
    Intent storage;
    Intent location;
    Intent j;

    public static final int PERMISSION = 0;
    public static final String TAG="Sound";
    private Spinner sourcespinner,distancespinner,timeintervalspinner;
    public static String sour, dist,time1;
    private boolean state = false;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    private SharedPreferences permissionStatus;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);


         switch (item.getItemId())
         {
             case R.id.googleMap:
                 Intent i =new Intent(this,MapsActivity.class);
                 startActivity(i);
                 return true;
              default:
                  return false;

         }


    }


    BroadcastReceiver sReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String Result = intent.getStringExtra("Data1");
            state = intent.getBooleanExtra("button",false);
            if(state){
                /**stopService(sound);
                stopService(storage);
                stopService(location);*/
                startbtn.setEnabled(true);

            }

            e1.setText(Result);


        }
    };

    public void onRecord(View view)
    {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
        startService(storage);
        startService(sound);
        startService(location);
        sendBroadcast(j);

            ComponentName receive = new ComponentName(this, onRestartreciever.class);
            PackageManager pm = this.getPackageManager();

            pm.setComponentEnabledSetting(receive,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);


            startbtn.setEnabled(false);
        stopbtn.setEnabled(true);
        }else{
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

    }

    private void getPermission() {
        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
                        PackageManager.PERMISSION_GRANTED))
        {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            ActivityCompat.requestPermissions(this, new String[]
                    { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE}, PERMISSION);
            ActivityCompat.requestPermissions(this, new String[]
                    { Manifest.permission.WRITE_EXTERNAL_STORAGE }, PERMISSION_WRITE_EXTERNAL_STORAGE);


        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imei = tm.getImei(1);
            } else {
                imei = tm.getDeviceId();
                }
        }
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                    if(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)
                            ==PackageManager.PERMISSION_GRANTED)
                    // Permission granted
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        imei = tm.getImei(1);
                        Log.i("3rd IMEIinfo", imei);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child("USERS").push().setValue(imei);
                    } else {
                        imei = tm.getDeviceId();
                        Log.i("4thIMEIinfo ",imei);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        databaseReference.child("USERS").push().setValue(imei);
                    }
                }else {
                    // Permission denied
                    Log.i(TAG,"Permission Denied");
                }
                break;
            default:
                break;
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);



        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        sound = new Intent(this, Noiseservice.class);
        storage = new Intent(this, Storageservice.class);
        location = new Intent(this, Locationservice.class);
        j = new Intent("SandD");
        e1=(EditText) findViewById(R.id.editText);
        startbtn = (Button) findViewById(R.id.button);
        stopbtn = (Button) findViewById(R.id.button2);
        sourcespinner=(Spinner)findViewById(R.id.spinner);
        sourcespinner.setOnItemSelectedListener(this);
        distancespinner=(Spinner)findViewById(R.id.spinner2);
        distancespinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Source, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourcespinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.Distance, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distancespinner.setAdapter(adapter2);




        startbtn.setEnabled(true);
        stopbtn.setEnabled(false);
        getPermission();


        if(isMyServiceRunning(Noiseservice.class)){
            startbtn.setEnabled(false);
            stopbtn.setEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(sReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(sReceiver,new IntentFilter("SoundData1"));
    }
    public void onEnd(View view){


        ComponentName receiver = new ComponentName(this, onRestartreciever.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        stopService(sound);
        stopService(storage);
        stopService(location);

        startbtn.setEnabled(true);
        stopbtn.setEnabled(false);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.spinner)
        {
            sour = parent.getItemAtPosition(i).toString();
            j.putExtra("source", sour);
            Log.i(TAG,sour);//do this

        }
        else if(spinner.getId() == R.id.spinner2)
        {
             dist= parent.getItemAtPosition(i).toString();
            j.putExtra("distance",dist);
            Log.i(TAG,dist);//do this

        }

    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        sour = "N/A";
        dist = "N/A";

        j.putExtra("source", sour);
        Log.i(TAG,sour);

        j.putExtra("distance",dist);
        Log.i(TAG,dist);




    }
}
