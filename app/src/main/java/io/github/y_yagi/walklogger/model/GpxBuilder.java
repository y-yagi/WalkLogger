package io.github.y_yagi.walklogger.model;

/**
 * Created by yaginuma on 16/06/01.
 */
public class GpxBuilder {
    private Walk mWalk;
    public GpxBuilder(Walk walk) {
        mWalk = walk;
    }

    public String build() {
        String gpx = header();
        gpx += wpt();
        gpx += trk();
        gpx += trkseg();
        for(GpsLog gpsLog : mWalk.gpsLogs) {
            gpx += trkpt(gpsLog);
        }
        gpx += closeTags();
        return gpx;
    }

    private String header() {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        header += "<gpx version=\"1.1\"\n";
        header += "creator=\"Walk Logger\"\n";
        header += "xmlns=\"http://www.topografix.com/GPX/1/1\"\n";
        header += "xmlns:topografix=\"http://www.topografix.com/GPX/Private/TopoGrafix/0/1\"\n";
        header += "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        header += "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.topografix.com/GPX/Private/TopoGrafix/0/1 http://www.topografix.com/GPX/Private/TopoGrafix/0/1/topografix.xsd\">\n";
        return header;
    }

    private String trk() {
        String trk = "<trk>\n";
        trk += String.format("<name><![CDATA[%s]]></name>\n", mWalk.getName());
        return trk;
    }

    private String trkseg() {
        return "<trkseg>\n";
    }

    private String trkpt(GpsLog gpsLog) {
        return String.format("<trkpt lat=\"%f\" lon=\"%f\"></trkpt>\n", gpsLog.getLatitude(), gpsLog.getLongitude());
    }

    private String closeTags() {
        return "</trkseg>\n</trk>\n</gpx>";
    }

    private String wpt() {
        String wpt = "";
        if (mWalk.waypoints.isEmpty()) return wpt;


        for (Waypoint waypoint: mWalk.waypoints) {
            wpt += String.format("<wpt lat=\"%f\" lon=\"%f\">\n", waypoint.getLatitude(), waypoint.getLongitude());
            wpt += String.format("<desc>%s</desc>\n", waypoint.getDetail());
            wpt += "</wpt>";
        }

        return wpt;

    }
}
