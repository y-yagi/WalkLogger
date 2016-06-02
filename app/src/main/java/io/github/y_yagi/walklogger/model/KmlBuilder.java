package io.github.y_yagi.walklogger.model;

/**
 * Created by yaginuma on 16/06/02.
 */
public class KmlBuilder {
    private Walk mWalk;

    public KmlBuilder(Walk walk) {
        mWalk = walk;
    }

    public String build() {
        String kml = header();
        kml += placeMark();
        kml += closeTags();
        return kml;
    }

    private String header() {
        String header = "<?xml version='1.0' encoding='UTF-8'?>\n";
        header += "<kml xmlns='http://www.opengis.net/kml/2.2'>\n";
        header += String.format("<Document>\n<name>%s</name>\n<Folder>\n", mWalk.getName());
        return header;
    }

    private String placeMark() {
        String placeMark = String.format("<Placemark><name>%s</name>", mWalk.getName());
        placeMark += "<LineString>\n<tessellate>1</tessellate>\n<coordinates>";
        for(GpsLog gpsLog : mWalk.gpsLogs) {
            // NOTE: longitude, latitude, elevation
            placeMark += String.format("%f,%f,0.0 ", gpsLog.getLongitude(), gpsLog.getLatitude());
        }
        placeMark += "</coordinates>\n</LineString>\n</Placemark>";
        return placeMark;
    }

    private String closeTags() {
        return "</Folder>\n</Document>\n</kml>";
    }

}
