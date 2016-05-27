package io.github.y_yagi.walklogger.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.github.y_yagi.walklogger.model.GpsLog;
import io.github.y_yagi.walklogger.model.Walk;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by yaginuma on 16/05/27.
 */

public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "BLS";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private Realm mRealm;
    private Walk mWalk;

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

        mRealm = Realm.getDefaultInstance();

        buildGoogleApiClient();
        saveWalk();
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
        mRealm.beginTransaction();
        GpsLog gpsLog = mRealm.createObject(GpsLog.class);
        gpsLog.setUuid(UUID.randomUUID().toString());
        gpsLog.setLatitude(location.getLatitude());
        gpsLog.setLongitude(location.getLongitude());
        mWalk.gpsLogs.add(gpsLog);
        mRealm.commitTransaction();
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

        mRealm.beginTransaction();
        mWalk.setEnd(new Date());
        mRealm.commitTransaction();
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
