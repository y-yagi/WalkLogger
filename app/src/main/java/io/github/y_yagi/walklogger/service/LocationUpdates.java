package io.github.y_yagi.walklogger.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;

import io.github.y_yagi.walklogger.R;

/**
 * Created by yaginuma on 16/05/27.
 */
public class LocationUpdates extends IntentService {

    private String TAG = this.getClass().getSimpleName();

    public LocationUpdates() {
        super("WalkLogger");
    }

    public LocationUpdates(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
       Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);

       if(location != null) {
           Log.e(TAG, "onHandleIntent");
           NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
           NotificationCompat.Builder noti = new NotificationCompat.Builder(this);
           noti.setContentTitle("WalkLogger");
           noti.setContentText("Now Recording...");
           noti.setSmallIcon(R.mipmap.ic_launcher);

           notificationManager.notify(1234, noti.build());
       }
    }
}
