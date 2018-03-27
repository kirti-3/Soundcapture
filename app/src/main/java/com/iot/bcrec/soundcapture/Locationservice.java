package com.iot.bcrec.soundcapture;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.iot.bcrec.soundcapture.Storageservice.TAG;

public class Locationservice extends Service {

    private Intent loc;
    public FusedLocationProviderClient mFusedLocationClient;
    private double lati;
    private double longi;
    public LocationRequest mLocationRequest;
    public LocationCallback mLocationCallback;

    public Locationservice() {
    }
    @Override
    public void onCreate()
    {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mLocationCallback = new LocationCallback()
            {
                @Override
                public void onLocationResult(LocationResult location)
                {
                    super.onLocationResult(location);
                    onNewLocation(location.getLastLocation());
                }
            };
    }

    private void onNewLocation(Location location)
    {
        loc = new Intent("LocationData");
        if(location.getLatitude()==0 && location.getLongitude()==0)
        {
            getLastLocation();
        }
        else
        {
            lati = location.getLatitude();
            Log.i(TAG,"lat ="+ lati);
            longi = location.getLongitude();
            Log.i(TAG,"long ="+ longi);
            loc.putExtra("lat", String.valueOf(lati));
            loc.putExtra("long", String.valueOf(longi));
            sendBroadcast(loc);
        }

    }

    private void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLastLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>()
        {
            @Override
            public void onSuccess(Location location)
            {
                loc = new Intent("LocationData");
                if (location != null)
                {
                    lati = location.getLatitude();
                    longi = location.getLongitude();
                    loc.putExtra("lat", String.valueOf(lati));
                    loc.putExtra("long", String.valueOf(longi));
                    sendBroadcast(loc);
                }
                else
                {
                    Log.w(TAG, "Failed to get location.");
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        createLocationRequest();
        getLastLocation();
        requestLocationUpdates();
        return START_REDELIVER_INTENT;
    }

    public void requestLocationUpdates()
    {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (Exception e)
        {
            Log.e(TAG, "Lost location permission. Could not request updates. " + e);
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
