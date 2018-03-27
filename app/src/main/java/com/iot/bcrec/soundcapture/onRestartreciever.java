package com.iot.bcrec.soundcapture;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.util.Log;

public class onRestartreciever extends BroadcastReceiver {
    public static final String TAG="Sound";


    @Override
    public void onReceive(Context context, Intent intent) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (!SoundService.class.getName().equals(service.service.getClassName())) {
                context.startService(new Intent(context, Noiseservice.class));

                Log.i(TAG," Service restarted");
            }
            if (!Storageservice.class.getName().equals(service.service.getClassName())) {
                context.startService(new Intent(context, Storageservice.class));
            }
            if (!Locationservice.class.getName().equals(service.service.getClassName())) {
                context.startService(new Intent(context, Locationservice.class));
            }
        }
        Intent in = new Intent(context,onRestartreciever.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, in, 0);
        AlarmManager alarmmanager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmmanager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, pi);
    }
}
