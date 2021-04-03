package com.app.fresy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.fresy.adapter.AdapterNotification;
import com.app.fresy.data.Constant;
import com.app.fresy.data.SharedPref;
import com.app.fresy.room.AppDatabase;
import com.app.fresy.room.DAO;
import com.app.fresy.room.table.NotificationEntity;
import com.app.fresy.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityNotification extends AppCompatActivity {

    public static void navigate(Activity activity) {
        Intent i = new Intent(activity, ActivityNotification.class);
        activity.startActivity(i);
    }

    public View parent_view;
    private RecyclerView recyclerView;
    private DAO dao;
    private SharedPref sharedPref;

    public AdapterNotification adapter;
    static ActivityNotification activityNotification;

    public static ActivityNotification getInstance() {
        return activityNotification;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        activityNotification = this;
        dao = AppDatabase.getDb(this).get();
        sharedPref = new SharedPref(this);

        initToolbar();
        iniComponent();
        prepareAds();

        Tools.RTLMode(getWindow());
    }

    private void initToolbar() {
        ActionBar actionBar;
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_activity_notification);
        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
    }

    private void iniComponent() {
        parent_view = findViewById(android.R.id.content);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //set data and list adapter
        adapter = new AdapterNotification(this, recyclerView);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AdapterNotification.OnItemClickListener() {
            @Override
            public void onItemClick(View view, NotificationEntity obj, int pos) {
                obj.read = true;
                ActivityDialogNotification.navigate(ActivityNotification.this, obj, false, pos);
            }
        });

        startLoadMoreAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_notification, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.colorTextAction));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item_id == R.id.action_delete) {
            if (adapter.getItemCount() == 0) {
                Snackbar.make(parent_view, R.string.msg_notif_empty, Snackbar.LENGTH_SHORT).show();
                return true;
            }
            dialogDeleteConfirmation();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    public void dialogDeleteConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_delete_confirm);
        builder.setMessage(getString(R.string.content_delete_confirm) + getString(R.string.title_activity_notification));
        builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface di, int i) {
                di.dismiss();
                dao.deleteAllNotification();
                startLoadMoreAdapter();
                Snackbar.make(parent_view, R.string.delete_success, Snackbar.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    private void startLoadMoreAdapter() {
        adapter.resetListData();
        List<NotificationEntity> items = dao.getNotificationByPage(Constant.NOTIFICATION_PAGE, 0);
        adapter.insertData(items);
        showNoItemView();
        final int item_count = (int) dao.getNotificationCount();
        // detect when scroll reach bottom
        adapter.setOnLoadMoreListener(new AdapterNotification.OnLoadMoreListener() {
            @Override
            public void onLoadMore(final int current_page) {
                if (item_count > adapter.getItemCount() && current_page != 0) {
                    displayDataByPage(current_page);
                } else {
                    adapter.setLoaded();
                }
            }
        });
    }

    private void displayDataByPage(final int next_page) {
        adapter.setLoading();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<NotificationEntity> items = dao.getNotificationByPage(Constant.NOTIFICATION_PAGE, (next_page * Constant.NOTIFICATION_PAGE));
                adapter.insertData(items);
                showNoItemView();
            }
        }, 500);
    }

    private void showNoItemView() {
        View lyt_no_item = findViewById(R.id.lyt_failed);
        (findViewById(R.id.failed_retry)).setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.failed_icon)).setImageResource(R.drawable.img_no_item);
        ((TextView) findViewById(R.id.failed_message)).setText(R.string.no_item);
        if (adapter.getItemCount() == 0) {
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void prepareAds() {

    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
