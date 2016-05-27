package io.github.y_yagi.walklogger.model;

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
    private int stepCounter;
    private float movingDistance;
    private Date start;
    private Date end;
    public RealmList<GpsLog> gpsLogs;

    public String duration() {
        return DateUtil.formatWithTime(this.start) + " 〜 " + DateUtil.formatWithTime(this.end);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStepCounter() {
        return stepCounter;
    }

    public void setStepCounter(int stepCounter) {
        this.stepCounter = stepCounter;
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
