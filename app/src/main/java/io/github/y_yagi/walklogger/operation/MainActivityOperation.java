package io.github.y_yagi.walklogger.operation;

import android.app.Activity;

import java.util.Date;
import java.util.MissingResourceException;
import java.util.UUID;

import io.github.y_yagi.walklogger.model.GpsLog;
import io.github.y_yagi.walklogger.model.LoggerState;
import io.github.y_yagi.walklogger.model.Walk;
import io.github.y_yagi.walklogger.model.Waypoint;
import io.github.y_yagi.walklogger.service.BackgroundLocationService;
import io.github.y_yagi.walklogger.util.ServiceUtil;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.Sort;

/**
 * Created by yaginuma on 16/05/29.
 */
public class MainActivityOperation {
    private Activity mActiviy;
    private Realm mRealm;

    public MainActivityOperation(Activity activity) {
        mActiviy = activity;

        Realm.init(activity);
        // TODO: remove before merge
        RealmConfiguration config = new RealmConfiguration.Builder().name("waypoint4").build();
        Realm.setDefaultConfiguration(config);
        mRealm = Realm.getDefaultInstance();
    }

    public void term() {
       mRealm.close();
    }

    public boolean isRecording() {
        return ServiceUtil.isServiceRunning(mActiviy, BackgroundLocationService.class);
    }

    public void saveWalk(String walkName) {
        Walk walk = getWalk();
        mRealm.beginTransaction();
        walk.setName(walkName);
        walk.setEnd(new Date());
        walk.setMovingDistance(walk.calcMovingDistance());
        mRealm.commitTransaction();
    }

    public void deleteWalkData() {
        Walk walk = getWalk();

        mRealm.beginTransaction();
        walk.gpsLogs.deleteAllFromRealm();
        walk.deleteFromRealm();
        mRealm.commitTransaction();
    }

    public Walk getWalk() {
        return mRealm.where(Walk.class).findAllSorted("start", Sort.DESCENDING).first();
    }

    public void pause() {
        mRealm.beginTransaction();
        LoggerState loggerState = mRealm.createObject(LoggerState.class);
        loggerState.setPause(true);
        mRealm.commitTransaction();
    }

    public void restart() {
        mRealm.beginTransaction();
        mRealm.delete(LoggerState.class);
        mRealm.commitTransaction();
    }

    public void stopService() {
        mRealm.beginTransaction();
        mRealm.delete(LoggerState.class);
        mRealm.commitTransaction();
    }

    public boolean isPaused() {
        boolean paused = false;
        LoggerState loggerState = mRealm.where(LoggerState.class).findFirst();
        if (loggerState != null && loggerState.getPause()) paused = true;
        return paused;
    }

    public void saveWaypoint(String memo) {
        Walk walk = getWalk();
        GpsLog gpsLog = walk.gpsLogs.last();
        if (gpsLog == null) {
            return;
        }

        mRealm.beginTransaction();
        String uuid = UUID.randomUUID().toString();
        Waypoint waypoint = mRealm.createObject(Waypoint.class, uuid);
        waypoint.setLatitude(gpsLog.getLatitude());
        waypoint.setLongitude(gpsLog.getLongitude());
        waypoint.setMemo(memo);
        walk.waypoints.add(waypoint);
        mRealm.commitTransaction();
    }
}
