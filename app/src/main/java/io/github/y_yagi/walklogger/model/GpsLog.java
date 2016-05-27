package io.github.y_yagi.walklogger.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yaginuma on 16/05/27.
 */
public class GpsLog extends RealmObject {
    @PrimaryKey
    private String uuid;
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
