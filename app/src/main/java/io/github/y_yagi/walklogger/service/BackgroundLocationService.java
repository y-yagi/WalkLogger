package io.github.y_yagi.walklogger.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.github.y_yagi.walklogger.R;
import io.github.y_yagi.walklogger.model.GpsLog;
import io.github.y_yagi.walklogger.model.Walk;
import io.github.y_yagi.walklogger.util.LogUtil;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by yaginuma on 16/05/27.
 */

public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = LogUtil.makeLogTag(BackgroundLocationService.class);

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private Realm mRealm;
    private NotificationManager mNotificationManager;
    private Walk mWalk;
    private boolean mFirstValue = true;

    IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mIntentService = new Intent(this, LocationUpdates.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: remove deleteRealmIfMigration
        RealmConfiguration config = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
        mRealm = Realm.getDefaultInstance();

        buildGoogleApiClient();
        saveWalk();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.recording));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.string.app_name, builder.build());
        startForeground(R.string.app_name, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(mGoogleApiClient.isConnected()) {
            return START_STICKY;
        }

        if(!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()){
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            // Do nothing. Check permission in Activity
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mPendingIntent);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged");
        // HACK: It ignored because it is often the first value values outside.
        // This is unnecessary if the first to display a Google Map.
        if (mFirstValue) {
            mFirstValue = false;
            return;
        }

        if (mWalk.isValid()) {
            mRealm.beginTransaction();
            GpsLog gpsLog = mRealm.createObject(GpsLog.class);
            gpsLog.setUuid(UUID.randomUUID().toString());
            gpsLog.setLatitude(location.getLatitude());
            gpsLog.setLongitude(location.getLongitude());
            mWalk.gpsLogs.add(gpsLog);
            mRealm.commitTransaction();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void saveWalk() {
        mRealm.beginTransaction();
        mWalk = mRealm.createObject(Walk.class);

        Date d = new Date();
        mWalk.setUuid(UUID.randomUUID().toString());
        mWalk.setStart(d);
        mWalk.setName(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d));
        mRealm.commitTransaction();
    }
}
