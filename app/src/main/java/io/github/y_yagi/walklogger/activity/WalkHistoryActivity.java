package io.github.y_yagi.walklogger.activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import io.github.y_yagi.walklogger.R;
import io.github.y_yagi.walklogger.databinding.CardWalkBinding;
import io.github.y_yagi.walklogger.model.Walk;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class WalkHistoryActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mActivity = this;
        displayWalkHistory();

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

    private void displayWalkHistory() {
        RealmResults<Walk> walks = getWalks();
        LinearLayout layout = (LinearLayout) findViewById(R.id.walk_history);
        layout.removeAllViews();
        for (Walk walk: walks) {
            CardView cardView;
            CardWalkBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.card_walk, layout, false);
            binding.setWalk(walk);
            cardView = (CardView) binding.getRoot();
            setupToolbar(cardView, walk.getUuid());
            layout.addView(cardView);
        }
    }

    private void setupToolbar(CardView cardView, final String uuid) {
        Toolbar toolbar = (Toolbar) cardView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_walk_history_card);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.item_map) {
                    MapsActivity.startActivity(mActivity, uuid);
                } else if (item.getItemId() == R.id.item_export) {
                    showExportDialog(uuid);
                } else if (item.getItemId() == R.id.item_update) {
                    Walk walk = getWalk(uuid);
                    showUpdateDialog(walk);
                } else if (item.getItemId() == R.id.item_delete) {
                    Walk walk = getWalk(uuid);
                    showDeleteDialog(walk);
                }
                return false;
            }
        });
    }

    private void showUpdateDialog(final Walk walk) {
        new MaterialDialog.Builder(this)
            .title(R.string.update_walk_dialog_title)
            .positiveText(R.string.ok)
            .negativeText(R.string.cancel)
            .input("", walk.getName(), false, new MaterialDialog.InputCallback() {
                @Override
                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    String walkName = input.toString();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    walk.setName(walkName);
                    realm.commitTransaction();
                    displayWalkHistory();
                }
            })
            .show();
    }

    private void showDeleteDialog(final Walk walk) {
        new MaterialDialog.Builder(this)
                .title(R.string.delete_walk_dialog_title)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        walk.gpsLogs.deleteAllFromRealm();
                        walk.deleteFromRealm();
                        realm.commitTransaction();
                        displayWalkHistory();
                    }
                })
                .show();
    }

    private void showExportDialog(final String uuid) {
        new MaterialDialog.Builder(this)
                .title(R.string.export_walk_dialog_title)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ExportActivity.startActivity(mActivity, uuid);
                    }
                })
                .show();
    }

    private RealmResults<Walk> getWalks() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Walk> walks = realm.where(Walk.class)
                .findAllSorted("start", Sort.DESCENDING);
        return walks;
    }

    private Walk getWalk(String uuid) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Walk.class).equalTo("uuid", uuid).findFirst();
    }

}
