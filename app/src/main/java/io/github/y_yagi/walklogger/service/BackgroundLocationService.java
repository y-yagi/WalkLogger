package io.github.y_yagi.walklogger.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crash.FirebaseCrash;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import io.github.y_yagi.walklogger.R;
import io.github.y_yagi.walklogger.activity.MainActivity;
import io.github.y_yagi.walklogger.model.GpsLog;
import io.github.y_yagi.walklogger.model.LoggerState;
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

    private boolean mFirstValue = true;
    private String mUuid;
    private SimpleDateFormat mTimeFormat;
    private Location mLastLocation;
    private int mLocationCheckCount = 0;

    IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationManager;
        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);
        mTimeFormat = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZZZZZ");

        buildGoogleApiClient();
        findOrCreateWalk();

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.notification_content));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(contentIntent);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(R.string.app_name, builder.build());
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
            FirebaseCrash.report(e);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
        if (location == null) {
            return;
        }

        // HACK: It ignored because it is often the first value values outside.
        // This is unnecessary if the first to display a Google Map.
        if (mFirstValue) {
            mFirstValue = false;
            return;
        }

        if (!isValidLocation(location)) return;
        // TODO: remove
        Log.e(TAG, "paused state: " + Boolean.toString(isPaused()));

        if (isPaused()) return;

        Realm realm = Realm.getDefaultInstance();
        Walk walk = getWalk(realm);
        if (walk != null) {
            String currentTime = mTimeFormat.format(new Date());
            realm.beginTransaction();
            GpsLog gpsLog = realm.createObject(GpsLog.class, UUID.randomUUID().toString());
            gpsLog.setLatitude(location.getLatitude());
            gpsLog.setLongitude(location.getLongitude());
            gpsLog.setTime(currentTime);
            walk.gpsLogs.add(gpsLog);
            realm.commitTransaction();
        }
        mLastLocation = location;
        realm.close();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void findOrCreateWalk() {
        Realm realm = Realm.getDefaultInstance();
        Walk walk = findAbnormalTerminationWalk(realm);
        if (walk != null) {
            mUuid = walk.getUuid();
            return;
        }

        realm.beginTransaction();
        mUuid = UUID.randomUUID().toString();
        walk = realm.createObject(Walk.class, mUuid);

        Date d = new Date();
        walk.setStart(d);
        walk.setName(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d));
        realm.commitTransaction();
    }

    private Walk findAbnormalTerminationWalk(Realm realm) {
        Date date = new Date();

        // NOTE: If data abnormally terminated within 30 minutes, continue to use.
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, -30);
        Date thirtyMinutesBack = cal.getTime();

        Walk walk = realm.where(Walk.class).greaterThan("end", thirtyMinutesBack).findFirst();
        if (walk != null && walk.getEnd() == null) {
            return walk;
        }
        return null;
    }

    private Walk getWalk(Realm realm) {
        return realm.where(Walk.class).equalTo("uuid", mUuid).findFirst();
    }

    private boolean isValidLocation(Location location) {
        if (mLastLocation == null || mLocationCheckCount > 2) {
            mLocationCheckCount = 0;
            return true;
        }

        float[] distances = new float[3];

        Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), location.getLatitude(), location.getLongitude(), distances);

        // NOTE: Deal for the case correctly value such as underground can not be acquired
        if (distances[0] > 70) {
            mLocationCheckCount += 1;
            return false;
        } else {
            return true;
        }
    }

    private boolean isPaused() {
        boolean paused = false;
        Realm realm = Realm.getDefaultInstance();

        LoggerState loggerState = realm.where(LoggerState.class).findFirst();
        if (loggerState != null && loggerState.getPause()) paused = true;

        realm.close();
        return paused;
    }
}
