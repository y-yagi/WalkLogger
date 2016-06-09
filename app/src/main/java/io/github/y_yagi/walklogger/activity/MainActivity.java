package io.github.y_yagi.walklogger.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Date;

import at.markushi.ui.CircleButton;
import io.github.y_yagi.walklogger.R;
import io.github.y_yagi.walklogger.model.Walk;
import io.github.y_yagi.walklogger.operation.MainActivityOperation;
import io.github.y_yagi.walklogger.service.BackgroundLocationService;
import io.github.y_yagi.walklogger.util.DateUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private CircleButton mRecordButton;
    private CircleButton mStopButton;
    private TextView mRecordingText;
    private MainActivityOperation mOperation;
    private Activity mActivity;
    private final int mNavPosition = 0;
    public static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        setupDrawerAndToolBar();

        mOperation = new MainActivityOperation(this);
        mActivity = this;

        mRecordButton = (CircleButton) findViewById(R.id.record_button);
        mRecordButton.setOnClickListener(this);
        mStopButton = (CircleButton) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(this);
        mRecordingText = (TextView) findViewById(R.id.recording_text);
        mRecordingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Walk walk = mOperation.getWalk();
                MapsActivity.startActivity(mActivity, walk.getUuid());
            }
        });

        if (mOperation.isRecording()) {
            mRecordButton.setVisibility(View.INVISIBLE);
            mStopButton.setVisibility(View.VISIBLE);
            mRecordingText.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOperation.term();
    }

    @Override
    public void onClick(View view) {
        if (!mOperation.isRecording()) {
            startService();
            return;
        }

        MaterialDialog.Builder materialBuilder = new MaterialDialog.Builder(this);
        materialBuilder.title(R.string.recording_finish_dialog_title);
        materialBuilder.content(R.string.recording_finish_dialog_content);
        materialBuilder.input(getString(R.string.walk_name), "", true, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog dialog, CharSequence input) {
                String walkName = input.toString();
                if (walkName.isEmpty()) {
                    walkName = DateUtil.formatWithTime(new Date());
                }
                mOperation.saveWalk(walkName);
            }
        });

        materialBuilder.positiveText(R.string.recording_finish_positive);
        materialBuilder.negativeText(R.string.recording_finish_negative);
        materialBuilder.neutralText(R.string.recording_finish_neutral);
        materialBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(MaterialDialog dialog, DialogAction which) {
                stopService();
            }
        });

        materialBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(MaterialDialog dialog, DialogAction which) {
                stopService();
                mOperation.deleteWalkData();
            }
        });
        materialBuilder.show();
    }

    private void startService() {
        Intent intent = new Intent(MainActivity.this, BackgroundLocationService.class);
        startService(intent);
        mRecordButton.setVisibility(View.INVISIBLE);
        mStopButton.setVisibility(View.VISIBLE);
        mRecordingText.setVisibility(View.VISIBLE);
    }

    private void stopService() {
        Intent intent = new Intent(MainActivity.this, BackgroundLocationService.class);
        stopService(intent);
        mRecordButton.setVisibility(View.VISIBLE);
        mStopButton.setVisibility(View.INVISIBLE);
        mRecordingText.setVisibility(View.INVISIBLE);
    }

    private void setupDrawerAndToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(mNavPosition).setChecked(true);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_record) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_walk_history) {
            Intent intent = new Intent(this, WalkHistoryActivity.class);
            startActivity(intent);
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
