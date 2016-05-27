package io.github.y_yagi.walklogger.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yaginuma on 16/05/27.
 */
public class DateUtil {
    public static String format(Date d) {
        return new SimpleDateFormat("yyyy/MM/dd").format(d);
    }

    public static String formatWithTime(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
    }
}

