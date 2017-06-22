package io.github.y_yagi.walklogger.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import io.github.y_yagi.walklogger.R;
import io.github.y_yagi.walklogger.model.GpsLog;
import io.github.y_yagi.walklogger.model.Walk;
import io.github.y_yagi.walklogger.model.Waypoint;
import io.realm.Realm;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String mUuid;
    private Walk mWalk;
    private static final String EXTRA_TRAVEL_UUID = "uuid";

    public static void startActivity(Context context, String uuid) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(EXTRA_TRAVEL_UUID, uuid);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        mUuid = extras.getString(EXTRA_TRAVEL_UUID);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setWalk();
        if (mWalk.gpsLogs.isEmpty()) {
            return;
        }
        GpsLog startLog = mWalk.gpsLogs.first();
        GpsLog endLog = mWalk.gpsLogs.last();
        LatLng startPoint = new LatLng(startLog.getLatitude(), startLog.getLongitude());
        LatLng endPoint = new LatLng(endLog.getLatitude(), endLog.getLongitude());

        mMap.addMarker(
                new MarkerOptions().position(startPoint).title("Start").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );
        mMap.addMarker(new MarkerOptions().position(endPoint).title("End"));
        if (mWalk.getEnd() != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endPoint, 15));
        }
        displayPolylines();
        displayWaypoints();
    }

    private void setWalk() {
        Realm realm = Realm.getDefaultInstance();
        mWalk = realm.where(Walk.class).equalTo("uuid", mUuid).findFirst();
        realm.close();
    }

    private void displayPolylines() {
        PolylineOptions rectOptions = new PolylineOptions().color(Color.RED);

        for(GpsLog gpsLog : mWalk.gpsLogs) {
            rectOptions = rectOptions.add(new LatLng(gpsLog.getLatitude(), gpsLog.getLongitude()));
        }
        mMap.addPolyline(rectOptions);
    }

    private void displayWaypoints() {
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        LatLng point;

        for(Waypoint waypoint : mWalk.waypoints) {
            point = new LatLng(waypoint.getLatitude(), waypoint.getLongitude());
            mMap.addMarker(new MarkerOptions().position(point).title(waypoint.getMemo()).icon(icon));
        }
    }
}
