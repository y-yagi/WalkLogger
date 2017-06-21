package io.github.y_yagi.walklogger.model;

import android.location.Location;

import java.util.Date;

import io.github.y_yagi.walklogger.util.DateUtil;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yaginuma on 16/05/27.
 */
public class Walk extends RealmObject {
    @PrimaryKey
    private String uuid;
    private String name;
    private int stepCount;
    private float movingDistance;
    private Date start;
    private Date end;
    public RealmList<GpsLog> gpsLogs;
    public RealmList<Waypoint> waypoints;

    public String duration() {
        String durationStr = DateUtil.formatWithTime(this.start) + " 〜 ";
        if (this.end != null) {
            durationStr += DateUtil.formatWithTime(this.end);
        }
        return  durationStr;
    }

    public float calcMovingDistance() {
        float[] apiResults = new float[3];
        float movingDistance = 0;

        for(int i = 0; i < gpsLogs.size() -1; i++)  {
            GpsLog from = gpsLogs.get(i);
            GpsLog to = gpsLogs.get(i+1);
            Location.distanceBetween(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude(), apiResults);
            movingDistance += apiResults[0];
        }
        return movingDistance;
    }

    public String info() {
        float movingDistance = getMovingDistance();
        // NOTE: If application crashed, moving distance not saved.
        if (movingDistance == 0) {
            movingDistance = calcMovingDistance();
        }

        return String.format("%.2fm", movingDistance);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public float getMovingDistance() {
        return movingDistance;
    }

    public void setMovingDistance(float movingDistance) {
        this.movingDistance = movingDistance;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
