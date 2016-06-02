package io.github.y_yagi.walklogger.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.IOException;
import java.io.OutputStream;

import io.github.y_yagi.walklogger.model.GpxBuilder;
import io.github.y_yagi.walklogger.model.KmlBuilder;
import io.github.y_yagi.walklogger.model.Walk;
import io.github.y_yagi.walklogger.util.LogUtil;
import io.realm.Realm;

/**
 * Created by yaginuma on 16/06/02.
 */
public class ExportActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private String mUuid;
    private Walk mWalk;
    private GoogleApiClient mGoogleApiClient;
    private DriveId mFolderDriveId;
    private static final String TAG = LogUtil.makeLogTag(ExportActivity.class);
    private static final String EXTRA_TRAVEL_UUID = "uuid";
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    public static void startActivity(Context context, String uuid) {
        Intent intent = new Intent(context, ExportActivity.class);
        intent.putExtra(EXTRA_TRAVEL_UUID, uuid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        mUuid = extras.getString(EXTRA_TRAVEL_UUID);
        setWalk();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mFolderDriveId = Drive.DriveApi.getRootFolder(getGoogleApiClient()).getDriveId();
        Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(driveContentsCallback);
    };


    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to read drive content");
                return;
            }
            DriveFolder folder = mFolderDriveId.asDriveFolder();
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(mWalk.getName() + ".gpx")
                    .setMimeType("application/gpx+xml")
                    .build();
            DriveContents contents = result.getDriveContents();
            try {
                OutputStream os = contents.getOutputStream();
                os.write(new GpxBuilder(mWalk).build().getBytes());
                os.close();
            } catch(IOException e) {
                Log.e(TAG, "Error while trying to create file " + e.getMessage());
                return;
            }
            folder.createFile(getGoogleApiClient(), changeSet, contents).setResultCallback(fileCallback);
        }
    };

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback =  new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to create the file");
                return;
            }
            finish();
        }
    };

    @Override
    public void onConnectionSuspended(int cause) { }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    private void setWalk() {
        Realm realm = Realm.getDefaultInstance();
        mWalk = realm.where(Walk.class).equalTo("uuid", mUuid).findFirst();
    }
}
