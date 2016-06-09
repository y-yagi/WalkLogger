package io.github.y_yagi.walklogger.operation;

import android.app.Activity;

import java.util.Date;

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

        // TODO: remove deleteRealmIfMigration
        RealmConfiguration config = new RealmConfiguration.Builder(activity).deleteRealmIfMigrationNeeded().build();
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
}
