package com.app.fresy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.connection.param.ParamRegisterUpdate;
import com.app.fresy.connection.response.RespRegister;
import com.app.fresy.data.ThisApp;
import com.app.fresy.model.User;
import com.app.fresy.utils.NetworkCheck;
import com.app.fresy.utils.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityRegisterProfile extends AppCompatActivity {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    // activity transition
    public static void navigate(Context context, User user) {
        Intent i = new Intent(context, ActivityRegisterProfile.class);
        i.putExtra(EXTRA_OBJECT, user);
        context.startActivity(i);
    }

    private boolean is_profile;
    private User user = null;
    private ParamRegisterUpdate paramRegister = null;

    private Call<RespRegister> callbackRegister = null;
    private View parent_view;
    private AppCompatButton btn_register;
    private ProgressBar progress_bar;
    private EditText et_name, et_email, et_password, et_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile);

        user = (User) getIntent().getSerializableExtra(EXTRA_OBJECT);
        is_profile = user != null;
        initComponent();

        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
        Tools.RTLMode(getWindow());
        Tools.hideKeyboard(this);
    }

    private void initComponent() {
        parent_view = findViewById(android.R.id.content);
        btn_register = findViewById(R.id.btn_register);
        progress_bar = findViewById(R.id.progress_bar);
        et_username = findViewById(R.id.et_username);
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);

        if (is_profile) {
            findViewById(R.id.lyt_login).setVisibility(View.INVISIBLE);
            btn_register.setText(R.string.form_action_update);
            ((TextView) findViewById(R.id.page_title)).setText(R.string.title_activity_edit_profile);
            String name = user.first_name + " " + user.last_name;
            et_name.setText(name.trim());
            et_username.setText(user.username);
            et_username.setEnabled(false);
            et_email.setText(user.email);
        } else {
            findViewById(R.id.lyt_login).setVisibility(View.VISIBLE);
            btn_register.setText(R.string.form_action_register);
            ((TextView) findViewById(R.id.page_title)).setText(R.string.form_title_register);
        }

        (findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                ActivityLogin.navigate(ActivityRegisterProfile.this);
            }
        });

        (findViewById(R.id.btn_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.hideKeyboard(ActivityRegisterProfile.this);
                validateValue();
            }
        });
    }

    private void validateValue() {
        if (!is_profile) user = new User();
        paramRegister = new ParamRegisterUpdate();
        String name = et_name.getText().toString().trim();
        if (!name.equals("") && name.split(" ").length == 2) {
            String[] names = name.split(" ");
            paramRegister.first_name = names[0];
            paramRegister.last_name = names[1];
        } else {
            paramRegister.first_name = name;
        }
        paramRegister.email = et_email.getText().toString();
        paramRegister.username = et_username.getText().toString();
        paramRegister.password = et_password.getText().toString();

        if (paramRegister.email.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.profile_email_empty);
            return;
        } else if (!Tools.isEmailValid(paramRegister.email)) {
            Tools.snackBarError(this, parent_view, R.string.profile_email_invalid);
            return;
        }

        if (paramRegister.username.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.profile_username_empty);
            return;
        }

        if (!is_profile && paramRegister.password.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.profile_password_empty);
            return;
        }

        showLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestRegisterApi();
            }
        }, 500);
    }

    private void requestRegisterApi() {
        API api = RestAdapter.createAPI();
        if (is_profile) {
            callbackRegister = api.updateUser(user.id, paramRegister);
        } else {
            callbackRegister = api.register(paramRegister);
        }
        callbackRegister.enqueue(new Callback<RespRegister>() {
            @Override
            public void onResponse(Call<RespRegister> call, Response<RespRegister> response) {
                RespRegister resp = response.body();
                if (resp == null) resp = getResponse(response);
                if (resp == null) {
                    onFailRequest(null);
                } else if (resp.id != -1) {
                    updateUserData(resp.id);
                } else {
                    onFailRequest(resp.message);
                }
            }

            @Override
            public void onFailure(Call<RespRegister> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(null);
            }
        });
    }

    private void updateUserData(long id) {
        ThisApp.get().updateUserData(id, new ThisApp.LoadUserListener() {
            @Override
            public void onSuccess(User new_user) {
                showDialogSuccess(getString(R.string.register_success));
                showLoading(false);
                user = new_user;
            }

            @Override
            public void onFailed() {
                onFailRequest(null);
            }
        });
    }

    private RespRegister getResponse(Response<RespRegister> response) {
        RespRegister resp = null;
        if (response.code() != 200) {
            Gson gson = new GsonBuilder().create();
            try {
                resp = gson.fromJson(response.errorBody().string(), RespRegister.class);
            } catch (Exception e) {
            }
        }
        return resp;
    }

    private void onFailRequest(String message) {
        showLoading(false);
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

    private void showLoading(final boolean show) {
        btn_register.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        progress_bar.setVisibility(!show ? View.INVISIBLE : View.VISIBLE);
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
                if (is_profile) {
                    ThisApp.get().setUser(user);
                } else {
                    ActivityLogin.navigate(ActivityRegisterProfile.this);
                }
            }
        });
        builder.show();
    }
}
