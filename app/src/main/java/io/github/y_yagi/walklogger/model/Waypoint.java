package io.github.y_yagi.walklogger.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by yaginuma on 17/06/20.
 */

public class Waypoint extends RealmObject {
    @PrimaryKey
    private String uuid;
    private String detail;
    private double latitude;
    private double longitude;

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
