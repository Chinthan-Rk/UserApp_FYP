package com.app.fresy;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.connection.param.ParamLogin;
import com.app.fresy.connection.response.RespForgotPass;
import com.app.fresy.connection.response.RespLogin;
import com.app.fresy.data.ThisApp;
import com.app.fresy.model.User;
import com.app.fresy.utils.NetworkCheck;
import com.app.fresy.utils.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogin extends AppCompatActivity {

    // activity transition
    public static void navigate(Context context) {
        Intent i = new Intent(context, ActivityLogin.class);
        context.startActivity(i);
    }

    private View parent_view;
    private EditText et_email, et_password;
    private AppCompatButton btn_login;
    private ProgressBar progress_bar;

    private Call<RespLogin> callback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initComponent();

        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
        Tools.RTLMode(getWindow());
        Tools.hideKeyboard(this);
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);
        progress_bar = findViewById(R.id.progress_bar);
        btn_login = findViewById(R.id.btn_login);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        (findViewById(R.id.show_pass)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(!v.isActivated());
                if (v.isActivated()) {
                    et_password.setTransformationMethod(null);
                } else {
                    et_password.setTransformationMethod(new PasswordTransformationMethod());
                }
                et_password.setSelection(et_password.getText().length());
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.hideKeyboard(ActivityLogin.this);
                validateValue(et_email.getText().toString().trim(), et_password.getText().toString().trim());
            }
        });

        (findViewById(R.id.register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                ActivityRegisterProfile.navigate(ActivityLogin.this, null);
            }
        });
        (findViewById(R.id.btn_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        (findViewById(R.id.forgot_password)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogLostPassword();
            }
        });

        (findViewById(R.id.btn_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void validateValue(final String email, final String password) {
        if (email.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.login_email_warning);
            return;
        }

        if (password.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.login_password_warning);
            return;
        }

        showLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestLogin(email, password);
            }
        }, 300);
    }

    private void requestLogin(String email, String password) {
        API api = RestAdapter.createAPI();
        callback = api.login(new ParamLogin(email, password));
        callback.enqueue(new Callback<RespLogin>() {
            @Override
            public void onResponse(Call<RespLogin> call, Response<RespLogin> response) {
                RespLogin resp = response.body();
                if (resp == null) {
                    onFailRequest(null);
                    showLoading(false);
                } else if (resp.message != null) {
                    onFailRequest(resp.message);
                    showLoading(false);
                } else {
                    updateUserData(resp.ID);
                }
            }

            @Override
            public void onFailure(Call<RespLogin> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(null);
                showLoading(false);
            }
        });
    }

    private void updateUserData(long id) {
        ThisApp.get().updateUserData(id, new ThisApp.LoadUserListener() {
            @Override
            public void onSuccess(User user) {
                ThisApp.get().setUser(user);
                showDialogSuccess(getString(R.string.login_success_info));
                showLoading(false);
            }

            @Override
            public void onFailed() {
                showLoading(false);
                onFailRequest(null);
            }
        });
    }

    private void onFailRequest(String message) {
        if (NetworkCheck.isConnect(this)) {
            if (TextUtils.isEmpty(message)) {
                showDialogFailed(getString(R.string.failed_text));
            } else {
                showDialogFailed(message);
            }
        } else {
            showDialogFailed(getString(R.string.no_internet_text));
        }
    }

    private void showDialogFailed(String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setIcon(Tools.tintDrawable(this, R.drawable.ic_report_outline, R.color.colorError));
        builder.setTitle(R.string.failed);
        builder.setMessage(Tools.fromHtml(message));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.OK, null);
        builder.show();
    }

    private void showDialogSuccess(String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setIcon(Tools.tintDrawable(this, R.drawable.ic_check_outline, R.color.colorPrimary));
        builder.setTitle(Tools.fromHtml(message));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private void showLoading(final boolean show) {
        btn_login.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        progress_bar.setVisibility(!show ? View.INVISIBLE : View.VISIBLE);
    }

    private void showDialogLostPassword() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_lost_password);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);

        final View progress_loading = dialog.findViewById(R.id.progress_loading);
        final View btn_close = dialog.findViewById(R.id.btn_close);
        final View bt_submit = dialog.findViewById(R.id.bt_submit);
        final EditText et_email = dialog.findViewById(R.id.et_email);
        final TextView tv_message = dialog.findViewById(R.id.tv_message);
        tv_message.setVisibility(View.INVISIBLE);

        final ForgotPasswordListener listener = new ForgotPasswordListener() {
            @Override
            public void onFinish(RespForgotPass resp) {
                bt_submit.setVisibility(View.VISIBLE);
                btn_close.setVisibility(View.VISIBLE);
                tv_message.setVisibility(View.VISIBLE);
                progress_loading.setVisibility(View.GONE);
                et_email.setEnabled(true);
                String message = "";
                if (NetworkCheck.isConnect(ActivityLogin.this)) {
                    if (resp == null) {
                        message = getString(R.string.failed_text);
                    } else {
                        message = resp.message;
                    }
                } else {
                    message = getString(R.string.no_internet_text);
                }
                tv_message.setText(message);
            }
        };

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_str = et_email.getText().toString();
                if (email_str.equals("")) {
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText(R.string.login_email_warning);
                    return;
                }
                et_email.setEnabled(false);
                tv_message.setVisibility(View.INVISIBLE);
                bt_submit.setVisibility(View.INVISIBLE);
                btn_close.setVisibility(View.INVISIBLE);
                progress_loading.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        forgotPasswordAction(et_email.getText().toString(), listener);
                    }
                }, 1000);
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void forgotPasswordAction(String user_mail, final ForgotPasswordListener listener) {
        API api = RestAdapter.createAPI();
        Call<RespForgotPass> callbackCallRating = api.forgotPassword(user_mail);
        callbackCallRating.enqueue(new Callback<RespForgotPass>() {
            @Override
            public void onResponse(Call<RespForgotPass> call, Response<RespForgotPass> response) {
                listener.onFinish(response.body());
            }

            @Override
            public void onFailure(Call<RespForgotPass> call, Throwable t) {
                listener.onFinish(null);
            }

        });
    }

    private interface ForgotPasswordListener {
        void onFinish(RespForgotPass resp);
    }
}
