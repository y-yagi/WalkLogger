package io.github.y_yagi.walklogger.model;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by yaginuma on 16/10/01.
 */

public class LoggerState extends RealmObject{
    private Boolean pause;

    public Boolean getPause() {
        return pause;
    }

    public void setPause(Boolean pause) {
        this.pause = pause;
    }
}
