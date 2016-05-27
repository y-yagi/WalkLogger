package io.github.y_yagi.walklogger.util;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by yaginuma on 16/05/27.
 */
public class ServiceUtil {
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
