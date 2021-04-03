package com.app.fresy;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.app.fresy.data.Constant;
import com.app.fresy.data.SharedPref;
import com.app.fresy.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class ActivitySettings extends AppCompatActivity {

    // activity transition
    public static void navigate(Context context) {
        Intent i = new Intent(context, ActivitySettings.class);
        context.startActivity(i);
    }

    private SharedPref sharedPref;
    private SwitchCompat switch_push_notif, switch_vibrate, switch_image_cache;
    private View parent_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedPref = new SharedPref(this);

        initComponent();
        initToolbar();
        Tools.RTLMode(getWindow());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.title_activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
    }

    private void initComponent() {
        parent_view = findViewById(R.id.parent_view);
        switch_push_notif = findViewById(R.id.switch_push_notif);
        switch_vibrate = findViewById(R.id.switch_vibrate);
        switch_image_cache = findViewById(R.id.switch_image_cache);
        ((TextView) findViewById(R.id.build_version)).setText(Tools.getVersionName(this));

        switch_push_notif.setChecked(sharedPref.getPushNotification());
        switch_vibrate.setChecked(sharedPref.getVibration());
        switch_image_cache.setChecked(sharedPref.getImageCache());
        setRingtoneName();

        switch_push_notif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPref.setPushNotification(isChecked);
            }
        });
        switch_vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPref.setVibration(isChecked);
            }
        });
        switch_image_cache.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPref.setImageCache(isChecked);
            }
        });
    }

    public void onClickLayout(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.lyt_ringtone:
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(sharedPref.getRingtone()));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                startActivityForResult(intent, 999);
                break;
            case R.id.lyt_img_cache:
                showDialogClearImageCache();
                break;
            case R.id.lyt_contact_us:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.pref_title_contact_us), getString(R.string.developer_email));
                clipboard.setPrimaryClip(clip);
                Snackbar.make(parent_view, R.string.email_copied, Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.lyt_term_policies:
                Tools.openInAppBrowser(this, Constant.PRIVACY_POLICY_URL, false);
                break;
            case R.id.lyt_rate_this:
                Tools.rateAction(this);
                break;
            case R.id.lyt_more_app:
                Tools.openInAppBrowser(this, Constant.MORE_APP_URL, false);
                break;
            case R.id.lyt_about:
                Tools.openInAppBrowser(this, Constant.ABOUT_US_URL, false);
                break;
            case R.id.lyt_intro:
                startActivity(new Intent(this, ActivityIntro.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri == null) return;
            sharedPref.setRingtone(uri.toString());
            setRingtoneName();
        }
    }

    private void setRingtoneName() {
        ((TextView) findViewById(R.id.ringtone)).setText(sharedPref.getRingtoneName());
    }

    private void showDialogClearImageCache() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySettings.this);
        builder.setTitle(getString(R.string.dialog_confirm_title));
        builder.setMessage(getString(R.string.message_clear_image_cache));
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                Tools.clearImageCacheOnBackground(getApplicationContext());
                Snackbar.make(parent_view, getString(R.string.message_after_clear_image_cache), Snackbar.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.CANCEL, null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initComponent();
    }

}
