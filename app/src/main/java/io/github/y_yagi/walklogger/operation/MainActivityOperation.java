package io.github.y_yagi.walklogger.operation;

import android.app.Activity;

import java.util.Date;
import java.util.MissingResourceException;

import io.github.y_yagi.walklogger.model.LoggerState;
import io.github.y_yagi.walklogger.model.Walk;
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
        // TODO: remove deleteRealmIfMigration
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
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
}
