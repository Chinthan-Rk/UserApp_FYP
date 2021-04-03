package com.app.fresy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.data.ThisApp;
import com.app.fresy.model.Setting;
import com.app.fresy.utils.NetworkCheck;
import com.app.fresy.utils.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    private Call<Setting> callbackSetting = null;
    private API api;
    private ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        api = RestAdapter.createAPI();
        progress_bar = findViewById(R.id.progress_bar);
        startProgressBar();

        Tools.setSystemBarColor(this, android.R.color.white);
        Tools.setSystemBarLight(this);
        Tools.RTLMode(getWindow());
    }

    private void startProgressBar() {
        int progress = progress_bar.getProgress();
        progress = progress + 2;
        if (progress > progress_bar.getMax()) progress = 0;
        progress_bar.setProgress(progress);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startProgressBar();
            }
        }, 50);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startProcess();
    }

    private void startProcess() {
        if (!NetworkCheck.isConnect(this)) {
            showDialogFailed(getString(R.string.no_internet_text));
        } else {
            requestSetting();
        }
    }

    private void requestSetting() {
        if (callbackSetting != null && callbackSetting.isExecuted()) callbackSetting.cancel();
        callbackSetting = api.version(Tools.getVersionCode(this));
        callbackSetting.enqueue(new Callback<Setting>() {
            @Override
            public void onResponse(Call<Setting> call, Response<Setting> response) {
                Setting resp = response.body();
                if (resp != null) {
                    if (resp.code.equals("active")) {
                        ThisApp.get().setSetting(resp);
                        startActivityMain();
                    } else if (resp.code.equals("inactive")) {
                        showDialogOutOfDate();
                    } else if (resp.code.equals("invalid_security")) {
                        showDialogFailed(getString(R.string.failed_security_text));
                    } else {
                        onFailRequest();
                    }
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<Setting> call, Throwable t) {
                onFailRequest();
            }
        });
    }

    private void onFailRequest() {
        showDialogFailed(getString(R.string.failed_text));
    }

    private void showDialogFailed(String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setIcon(Tools.tintDrawable(this, R.drawable.ic_report_outline, R.color.colorError));
        builder.setTitle(R.string.failed);
        builder.setMessage(Tools.fromHtml(message));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.RETRY, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                retryOpenApplication();
            }
        });
        builder.show();
    }

    private void showDialogOutOfDate() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setIcon(Tools.tintDrawable(this, R.drawable.ic_info, R.color.colorError));
        builder.setTitle(R.string.info);
        builder.setMessage(R.string.msg_app_out_date);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Tools.rateAction(ActivitySplash.this);
            }
        });
        builder.show();
    }

    // make a delay to start next activity
    private void retryOpenApplication() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startProcess();
            }
        }, 2000);
    }

    private void startActivityMain() {
        Intent i = new Intent(ActivitySplash.this, ActivityMain.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onDestroy() {
        if (callbackSetting != null && !callbackSetting.isCanceled()) callbackSetting.cancel();
        super.onDestroy();
    }
}