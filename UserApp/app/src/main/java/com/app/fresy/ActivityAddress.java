package com.app.fresy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.connection.param.ParamRegisterUpdate;
import com.app.fresy.connection.response.RespRegister;
import com.app.fresy.connection.response.RespState;
import com.app.fresy.data.AppConfig;
import com.app.fresy.data.ThisApp;
import com.app.fresy.model.BillingShipping;
import com.app.fresy.model.State;
import com.app.fresy.model.User;
import com.app.fresy.utils.NetworkCheck;
import com.app.fresy.utils.Tools;
import com.app.fresy.utils.ViewAnimation;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityAddress extends AppCompatActivity {

    // activity transition
    public static void navigate(Context context) {
        Intent i = new Intent(context, ActivityAddress.class);
        context.startActivity(i);
    }

    private View parent_view;
    public EditText bill_first_name, bill_last_name, bill_company;
    public EditText bill_country, bill_address_1, bill_address_2, bill_city, bill_postcode;
    public EditText bill_email, bill_phone;
    public TextView bill_state;
    public TextView tv_bill_address;
    public View lyt_expand_bill;
    public AppCompatButton btn_update_bill;
    public ImageButton bt_toggle_bill;


    public EditText ship_first_name, ship_last_name, ship_company;
    public EditText ship_country, ship_address_1, ship_address_2, ship_city, ship_postcode;
    public TextView ship_state;
    public TextView tv_ship_address;
    public View lyt_expand_ship;
    public AppCompatButton btn_update_ship;
    public ImageButton bt_toggle_ship;

    private Call<RespRegister> callback = null;
    private Call<RespState> callbackState = null;
    private User user = new User();
    private RespState respState = null;
    private String[] states = new String[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        parent_view = findViewById(android.R.id.content);
        user = ThisApp.get().getUser();

        initBillingComponent();
        initShippingComponent();
        requestState();

        initToolbar();
        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
        Tools.RTLMode(getWindow());
        Tools.hideKeyboard(this);
    }

    private void initToolbar() {
        ActionBar actionBar;
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_activity_address);
        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
    }

    private void initBillingComponent() {
        lyt_expand_bill = findViewById(R.id.lyt_expand_bill);
        lyt_expand_bill.setVisibility(View.GONE);
        tv_bill_address = findViewById(R.id.tv_bill_address);
        tv_bill_address.setVisibility(View.VISIBLE);
        bt_toggle_bill = findViewById(R.id.bt_toggle_bill);
        bt_toggle_bill.setRotation(0);

        bill_first_name = findViewById(R.id.bill_first_name);
        bill_last_name = findViewById(R.id.bill_last_name);
        bill_company = findViewById(R.id.bill_company);
        bill_country = findViewById(R.id.bill_country);
        bill_address_1 = findViewById(R.id.bill_address_1);
        bill_address_2 = findViewById(R.id.bill_address_2);
        bill_city = findViewById(R.id.bill_city);
        bill_state = findViewById(R.id.bill_state);
        bill_postcode = findViewById(R.id.bill_postcode);
        bill_email = findViewById(R.id.bill_email);
        bill_phone = findViewById(R.id.bill_phone);

        btn_update_bill = findViewById(R.id.btn_update_bill);

        BillingShipping billing = user.billing;
        bill_first_name.setText(billing.first_name);
        bill_last_name.setText(billing.last_name);
        bill_company.setText(billing.company);
        bill_country.setText(AppConfig.COUNTRY_NAME);
        bill_country.setTag(AppConfig.COUNTRY_CODE);
        bill_address_1.setText(billing.address_1);
        bill_address_2.setText(billing.address_2);
        bill_city.setText(billing.city);
        bill_postcode.setText(billing.postcode);
        bill_email.setText(billing.email);
        bill_phone.setText(billing.phone);

        btn_update_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateValueBilling();
            }
        });

        bill_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStateSelect(bill_state);
            }
        });

        bt_toggle_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLayoutForm(v, lyt_expand_bill, tv_bill_address);
            }
        });
    }

    private void initShippingComponent() {
        lyt_expand_ship = findViewById(R.id.lyt_expand_ship);
        lyt_expand_ship.setVisibility(View.GONE);
        tv_ship_address = findViewById(R.id.tv_ship_address);
        tv_ship_address.setVisibility(View.VISIBLE);
        bt_toggle_ship = findViewById(R.id.bt_toggle_ship);
        bt_toggle_ship.setRotation(0);

        ship_first_name = findViewById(R.id.ship_first_name);
        ship_last_name = findViewById(R.id.ship_last_name);
        ship_company = findViewById(R.id.ship_company);
        ship_country = findViewById(R.id.ship_country);
        ship_address_1 = findViewById(R.id.ship_address_1);
        ship_address_2 = findViewById(R.id.ship_address_2);
        ship_city = findViewById(R.id.ship_city);
        ship_state = findViewById(R.id.ship_state);
        ship_postcode = findViewById(R.id.ship_postcode);

        btn_update_ship = findViewById(R.id.btn_update_ship);

        BillingShipping shipping = user.shipping;
        ship_first_name.setText(shipping.first_name);
        ship_last_name.setText(shipping.last_name);
        ship_company.setText(shipping.company);
        ship_country.setText(AppConfig.COUNTRY_NAME);
        ship_country.setTag(AppConfig.COUNTRY_CODE);
        ship_address_1.setText(shipping.address_1);
        ship_address_2.setText(shipping.address_2);
        ship_city.setText(shipping.city);
        ship_postcode.setText(shipping.postcode);

        btn_update_ship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateValueShipping();
            }
        });

        ship_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStateSelect(ship_state);
            }
        });

        bt_toggle_ship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLayoutForm(v, lyt_expand_ship, tv_ship_address);
            }
        });
    }

    private void validateValueBilling() {
        Tools.hideKeyboard(this);
        final BillingShipping bill = new BillingShipping();
        bill.first_name = bill_first_name.getText().toString().trim();
        bill.last_name = bill_last_name.getText().toString().trim();
        bill.company = bill_company.getText().toString().trim();
        bill.country = bill_country.getTag().toString();
        bill.address_1 = bill_address_1.getText().toString().trim();
        bill.address_2 = bill_address_2.getText().toString().trim();
        bill.city = bill_city.getText().toString().trim();
        if (bill_state.getTag() != null) bill.state = bill_state.getTag().toString();
        bill.postcode = bill_postcode.getText().toString().trim();
        bill.email = bill_email.getText().toString().trim();
        bill.phone = bill_phone.getText().toString().trim();

        if (bill.first_name.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_first_name);
            return;
        }
        if (bill.last_name.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_last_name);
            return;
        }
        if (bill.address_1.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_address);
            return;
        }
        if (bill.city.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_city);
            return;
        }
        if (bill.state == null || bill.state.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_province);
            return;
        }
        if (bill.postcode.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_postcode);
            return;
        }

        if (bill.email.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_email);
            return;
        } else if (!Tools.isEmailValid(bill.email)) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_email_invalid);
            return;
        }
        if (bill.phone.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_phone);
            return;
        }

        showLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestUpdateBillingShipping(bill, null);
            }
        }, 200);
    }

    private void validateValueShipping() {
        Tools.hideKeyboard(this);
        final BillingShipping ship = new BillingShipping();
        ship.first_name = ship_first_name.getText().toString().trim();
        ship.last_name = ship_last_name.getText().toString().trim();
        ship.company = ship_company.getText().toString().trim();
        ship.country = ship_country.getTag().toString();
        ship.address_1 = ship_address_1.getText().toString().trim();
        ship.address_2 = ship_address_2.getText().toString().trim();
        ship.city = ship_city.getText().toString().trim();
        if (ship_state.getTag() != null) ship.state = ship_state.getTag().toString();
        ship.postcode = ship_postcode.getText().toString().trim();

        if (ship.first_name.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_first_name);
            return;
        }
        if (ship.last_name.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_last_name);
            return;
        }
        if (ship.address_1.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_street);
            return;
        }
        if (ship.city.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_city);
            return;
        }
        if (ship.state == null || ship.state.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_province);
            return;
        }
        if (ship.postcode.trim().equals("")) {
            Tools.snackBarError(this, parent_view, R.string.msg_failed_postcode);
            return;
        }

        showLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestUpdateBillingShipping(null, ship);
            }
        }, 200);
    }

    private void requestUpdateBillingShipping(BillingShipping bill, BillingShipping ship) {
        final String msg = bill != null ? getString(R.string.billing_address) : getString(R.string.shipping_address);
        final ParamRegisterUpdate paramRegister = new ParamRegisterUpdate();
        paramRegister.billing = bill;
        paramRegister.shipping = ship;
        API api = RestAdapter.createAPI();
        callback = api.updateUser(user.id, paramRegister);
        callback.enqueue(new Callback<RespRegister>() {
            @Override
            public void onResponse(Call<RespRegister> call, Response<RespRegister> response) {
                RespRegister resp = response.body();
                if (resp == null) {
                    onFailRequest(null);
                } else if (resp.id != -1) {
                    showDialogSuccess(msg + " " + getString(R.string.updated));
                    updateUserData();
                } else {
                    onFailRequest(resp.message);
                }
                showLoading(false);
            }

            @Override
            public void onFailure(Call<RespRegister> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(null);
                showLoading(false);
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
        builder.setTitle("Failed");
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
                onResume();
            }
        });
        builder.show();
    }

    private void updateUserData() {
        ThisApp.get().updateUserData(user.id, new ThisApp.LoadUserListener() {
            @Override
            public void onSuccess(User new_user) {
                user = new_user;
                initBillingComponent();
                initShippingComponent();
                requestState();
            }

            @Override
            public void onFailed() {
            }
        });
    }

    private void showStateSelect(final TextView text_view) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.state_province);
        builder.setSingleChoiceItems(states, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                State s = respState.states.get(which);
                text_view.setText(s.name);
                text_view.setTag(s.code);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showLoading(boolean show) {
        (findViewById(R.id.progress_bar)).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = ThisApp.get().getUser();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callback != null && callback.isExecuted()) callback.cancel();
        if (callbackState != null && callbackState.isExecuted()) callbackState.cancel();
    }

    private void requestState() {
        showLoading(true);
        if (callbackState != null && callbackState.isExecuted()) callbackState.cancel();
        API api = RestAdapter.createAPI();
        callbackState = api.state(AppConfig.COUNTRY_CODE);
        callbackState.enqueue(new Callback<RespState>() {
            @Override
            public void onResponse(Call<RespState> call, Response<RespState> response) {
                respState = response.body();
                showLoading(false);
                if (respState != null && respState.states.size() > 0) {
                    successLoadState();
                } else {
                    failedLoadState();
                }
            }

            @Override
            public void onFailure(Call<RespState> call, Throwable t) {
                showLoading(false);
            }
        });
    }

    private void successLoadState() {
        states = new String[respState.states.size()];
        for (int i = 0; i < states.length; i++) {
            states[i] = respState.states.get(i).name;
        }
        setBillAddressText();
        setShipAddressText();
    }

    private State getStateByCode(String code) {
        for (int i = 0; i < states.length; i++) {
            State s = respState.states.get(i);
            if (code.equals(s.code)) return s;
        }
        return null;
    }

    private void setBillAddressText() {
        if (!user.billing.state.equals("")) {
            State s = getStateByCode(user.billing.state);
            if (s != null) {
                bill_state.setText(s.name);
                bill_state.setTag(s.code);
            }
        }
        if (bill_first_name.getText().toString().trim().equals("")) {
            tv_bill_address.setText(R.string.address_not_set);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(bill_first_name.getText()).append(" ");
        sb.append(bill_last_name.getText()).append("\n");
        if(!bill_company.getText().toString().trim().equals("")){
            sb.append(bill_company.getText()).append(", ");
        }
        sb.append(bill_address_1.getText()).append(", ");
        if(!bill_address_1.getText().toString().trim().equals("")){
            sb.append(bill_address_1.getText()).append(", ");
        }
        sb.append(bill_city.getText()).append(", ");
        sb.append(bill_state.getText()).append(", ").append("\n");
        sb.append(bill_postcode.getText()).append("\n");
        sb.append(bill_phone.getText()).append("\n");
        sb.append(bill_email.getText());
        tv_bill_address.setText(sb.toString());
    }

    private void setShipAddressText() {
        if (!user.shipping.state.equals("")) {
            State s = getStateByCode(user.shipping.state);
            if (s != null) {
                ship_state.setText(s.name);
                ship_state.setTag(s.code);
            }
        }
        if (ship_first_name.getText().toString().trim().equals("")) {
            tv_ship_address.setText(R.string.address_not_set);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ship_first_name.getText()).append(" ");
        sb.append(ship_last_name.getText()).append("\n");
        if(!ship_company.getText().toString().trim().equals("")){
            sb.append(ship_company.getText()).append(", ");
        }
        sb.append(ship_address_1.getText()).append(", ");
        if(!ship_address_2.getText().toString().trim().equals("")){
            sb.append(ship_address_2.getText()).append(", ");
        }
        sb.append(ship_city.getText()).append(", ");
        sb.append(ship_state.getText()).append(", ").append("\n");
        sb.append(ship_postcode.getText());
        tv_ship_address.setText(sb.toString());
    }

    private void failedLoadState() {
        showLoading(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setIcon(Tools.tintDrawable(this, R.drawable.ic_report_outline, R.color.colorError));
        builder.setTitle(R.string.failed);
        builder.setMessage(R.string.failed_load_state);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.RETRY, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestState();
            }
        });
        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    private boolean toggleLayoutForm(View button, View layout, final View tv) {
        boolean show;
        if (button.getRotation() == 0) {
            button.animate().setDuration(200).rotation(180);
            show = true;
        } else {
            button.animate().setDuration(200).rotation(0);
            show = false;
        }
        if (show) {
            ViewAnimation.expand(layout, new ViewAnimation.AnimListener() {
                @Override
                public void onFinish() {
                    tv.setVisibility(View.GONE);
                }
            });
        } else {
            ViewAnimation.collapse(layout, new ViewAnimation.AnimListener() {
                @Override
                public void onFinish() {
                    tv.setVisibility(View.VISIBLE);
                }
            });
        }
        return show;
    }
}