package com.app.fresy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.app.fresy.adapter.AdapterCart;
import com.app.fresy.connection.API;
import com.app.fresy.connection.RestAdapter;
import com.app.fresy.connection.param.ParamOrder;
import com.app.fresy.connection.response.RespShipMethod;
import com.app.fresy.connection.response.RespShipZone;
import com.app.fresy.data.ThisApp;
import com.app.fresy.model.Coupon;
import com.app.fresy.model.CouponOrder;
import com.app.fresy.model.Order;
import com.app.fresy.model.Payment;
import com.app.fresy.model.ProductOrder;
import com.app.fresy.model.ShippingOrder;
import com.app.fresy.model.User;
import com.app.fresy.room.AppDatabase;
import com.app.fresy.room.DAO;
import com.app.fresy.room.table.CartEntity;
import com.app.fresy.utils.Tools;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCheckout extends AppCompatActivity {

    // activity transition
    public static void navigate(Activity context, int code) {
        Intent i = new Intent(context, ActivityCheckout.class);
        context.startActivityForResult(i, code);
    }

    private View parent_view;
    private EditText et_note, et_coupon;
    private View btn_continue, progress_bar_order, lyt_coupon;
    private RadioGroup rg_ship_option;
    private MaterialButton btn_use_coupon;
    private TextView tv_ship_address, tv_bill_address, tv_payment, tv_payment_desc, tv_ship_empty;
    private TextView tv_item_fee, tv_shipping_fee, tv_total_fee, tv_total_cart;
    private TextView tv_coupon_fee;
    private RecyclerView recyclerView;
    private DAO db;
    private AdapterCart adapter;
    private Call<List<Payment>> callback_payment = null;
    private Call<Coupon> callback_coupon = null;
    private Coupon coupon = null;
    private User user = new User();

    private List<CartEntity> listCart;
    private Payment selectedPayment = null;
    private RespShipMethod selectedShip = null;
    private boolean payment_done, shipping_done;
    private API api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        parent_view = findViewById(android.R.id.content);

        db = AppDatabase.getDb(this).get();
        api = RestAdapter.createAPI();

        initToolbar();
        iniComponent();
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
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_activity_checkout);
        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
    }

    private void iniComponent() {
        btn_continue = findViewById(R.id.btn_continue);
        progress_bar_order = findViewById(R.id.progress_bar_order);
        lyt_coupon = findViewById(R.id.lyt_coupon);
        et_note = findViewById(R.id.et_note);
        et_coupon = findViewById(R.id.et_coupon);
        tv_coupon_fee = findViewById(R.id.tv_coupon_fee);
        tv_ship_empty = findViewById(R.id.tv_ship_empty);
        rg_ship_option = findViewById(R.id.rg_ship_option);
        tv_payment = findViewById(R.id.tv_payment);
        tv_payment_desc = findViewById(R.id.tv_payment_desc);
        tv_ship_address = findViewById(R.id.tv_ship_address);
        tv_bill_address = findViewById(R.id.tv_bill_address);
        tv_item_fee = findViewById(R.id.tv_item_fee);
        tv_shipping_fee = findViewById(R.id.tv_shipping_fee);
        tv_total_fee = findViewById(R.id.tv_total_fee);
        tv_total_cart = findViewById(R.id.tv_total_cart);
        btn_use_coupon = findViewById(R.id.btn_use_coupon);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new AdapterCart(this, AdapterCart.LayoutType.CHECKOUT);
        recyclerView.setAdapter(adapter);

        (findViewById(R.id.bt_edit_address)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityAddress.navigate(ActivityCheckout.this);
            }
        });
        (findViewById(R.id.tv_payment)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPaymentSelect();
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!payment_done && !shipping_done) {
                    Tools.snackBarError(ActivityCheckout.this, parent_view, R.string.loading_data);
                    return;
                }
                validateSubmitOrder();
            }
        });

        btn_use_coupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coupon == null) {
                    requestCheckCoupon();
                } else {
                    coupon = null;
                    calculateFee();
                }
            }
        });

        displayData();
        calculateFee();
        requestPayment();
        requestShippingZone();
    }

    private void displayData() {
        listCart = db.getAllCart();
        adapter.setItems(listCart);
    }

    private void calculateFee() {
        double price = 0d;
        double coupon_amount = 0d;
        double price_total = 0d;
        double shipping_fee = 0d;
        for (CartEntity c : adapter.getItem()) {
            price = price + (Double.parseDouble(c.getPrice()) * c.getAmount());
        }
        if (tv_shipping_fee.getTag() != null) {
            String shipping_str = tv_shipping_fee.getTag().toString();
            tv_shipping_fee.setText(Tools.getPrice(shipping_str));
            shipping_fee = Double.parseDouble(shipping_str);
        }

        if (coupon != null) {
            double min_amount = Double.parseDouble(coupon.minimum_amount);
            double max_amount = Double.parseDouble(coupon.maximum_amount);
            if (price < min_amount) {
                Tools.snackBarError(ActivityCheckout.this, parent_view, getString(R.string.coupon_not_met_min) + Tools.getPrice(min_amount));
                coupon = null;
                calculateFee();
            } else if (price > max_amount) {
                Tools.snackBarError(ActivityCheckout.this, parent_view, getString(R.string.coupon_reach_min) + Tools.getPrice(max_amount));
                coupon = null;
                calculateFee();
            } else {
                btn_use_coupon.setText(R.string.REMOVE);
                et_coupon.setEnabled(false);
                lyt_coupon.setEnabled(false);
                lyt_coupon.setVisibility(View.VISIBLE);
                if (coupon.type.equalsIgnoreCase("fixed_cart")) {
                    coupon_amount = Double.parseDouble(coupon.amount);
                } else if (coupon.type.equalsIgnoreCase("percent")) {
                    coupon_amount = (Double.parseDouble(coupon.amount) / 100) * price;
                }
                tv_coupon_fee.setText("-" + Tools.getPrice(coupon_amount));
            }
        } else {
            btn_use_coupon.setText(R.string.USE);
            et_coupon.setText("");
            et_coupon.setEnabled(true);
            lyt_coupon.setEnabled(true);
            lyt_coupon.setVisibility(View.GONE);
        }

        price_total = (price - coupon_amount) + shipping_fee;

        Spanned price_text = Tools.getPrice(price);
        Spanned price_total_text = Tools.getPrice(price_total);

        tv_item_fee.setText(price_text);
        tv_total_fee.setText(price_total_text);
        tv_total_cart.setText(price_total_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = ThisApp.get().getUser();
        setShippingAddress();
    }

    boolean is_address_valid = false;

    private void setShippingAddress() {
        is_address_valid = !user.shipping.first_name.trim().equals("") && !user.billing.first_name.trim().equals("");
        tv_ship_address.setText(Tools.getAddress(this, user.shipping));
        tv_bill_address.setText(Tools.getAddress(this, user.billing));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void showFailedDialog(String msg, final Retry retry) {
        showLoading(false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setIcon(Tools.tintDrawable(this, R.drawable.ic_report_outline, R.color.colorError));
        builder.setTitle("Failed");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.RETRY, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                retry.onRetry();
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

    private void requestCheckCoupon() {
        String code = et_coupon.getText().toString().trim();
        if (code.equals("")) {
            Tools.snackBarError(this, parent_view, R.string.please_input_coupon);
            return;
        }
        showLoading(true);
        callback_coupon = api.coupon(code);
        callback_coupon.enqueue(new Callback<Coupon>() {
            @Override
            public void onResponse(Call<Coupon> call, Response<Coupon> response) {
                Coupon resp = response.body();
                showLoading(false);
                if (resp != null) {
                    if (resp.id != -1 && (resp.type.equalsIgnoreCase("fixed_cart") || resp.type.equalsIgnoreCase("percent"))) {
                        Tools.snackBarSuccess(ActivityCheckout.this, parent_view, R.string.success_use_coupon);
                        coupon = resp;
                        calculateFee();
                    } else if (resp.code.equals("not_found")) {
                        Tools.snackBarSuccess(ActivityCheckout.this, parent_view, R.string.coupon_not_found);
                    } else if (resp.code.equals("expired")) {
                        Tools.snackBarSuccess(ActivityCheckout.this, parent_view, R.string.coupon_expired);
                    } else if (resp.code.equals("limit")) {
                        Tools.snackBarSuccess(ActivityCheckout.this, parent_view, R.string.coupon_limit_reached);
                    } else {
                        Tools.snackBarError(ActivityCheckout.this, parent_view, R.string.invalid_coupon);
                    }
                } else {
                    Tools.snackBarError(ActivityCheckout.this, parent_view, R.string.failed_load_coupon);
                }
            }

            @Override
            public void onFailure(Call<Coupon> call, Throwable t) {
                showLoading(false);
                Tools.snackBarError(ActivityCheckout.this, parent_view, R.string.failed_load_coupon);
            }
        });
    }

    private void requestPayment() {
        selectedPayment = null;
        payment_done = false;
        showLoading(true);
        if (callback_payment != null && callback_payment.isExecuted()) callback_payment.cancel();
        callback_payment = api.payment();
        callback_payment.enqueue(new Callback<List<Payment>>() {
            @Override
            public void onResponse(Call<List<Payment>> call, Response<List<Payment>> response) {
                List<Payment> resp = response.body();
                payment_done = true;
                showLoading(false);
                if (resp != null && resp.size() > 0) {
                    successLoadPayment(resp);
                } else {
                    failedLoadPayment();
                }
            }

            @Override
            public void onFailure(Call<List<Payment>> call, Throwable t) {
                payment_done = true;
                showLoading(false);
                failedLoadPayment();
            }
        });
    }

    private void failedLoadPayment() {
        showFailedDialog(getString(R.string.failed_load_payment), new Retry() {
            @Override
            public void onRetry() {
                requestPayment();
            }
        });
    }

    private List<Payment> payments = new ArrayList<>();
    private List<String> paymentsStr = new ArrayList<>();

    private void successLoadPayment(List<Payment> resp) {
        for (Payment p : resp) {
            if (p.enabled) {
                payments.add(p);
                paymentsStr.add(p.title);
            }
        }
        if (payments.size() == 0) {
            tv_payment.setText(R.string.no_payment);
        }
    }

    private void showPaymentSelect() {
        String[] paymentsArr = new String[paymentsStr.size()];
        paymentsArr = paymentsStr.toArray(paymentsArr);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.payment_method);
        builder.setSingleChoiceItems(paymentsArr, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedPayment = payments.get(which);
                tv_payment.setText(selectedPayment.title);
                tv_payment_desc.setText(selectedPayment.description);
                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void showLoading(boolean show) {
        if (!show && !payment_done && !shipping_done) {
            return;
        }
        (findViewById(R.id.progress_bar)).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    Call<List<RespShipZone>> callbackShipZone;
    Call<List<RespShipMethod>> callbackShipMethod;

    private void requestShippingZone() {
        showLoading(true);
        shipping_done = false;
        if (callbackShipZone != null && !callbackShipZone.isCanceled()) callbackShipZone.cancel();
        callbackShipZone = api.shippingZones();
        callbackShipZone.enqueue(new Callback<List<RespShipZone>>() {
            @Override
            public void onResponse(Call<List<RespShipZone>> call, Response<List<RespShipZone>> response) {
                List<RespShipZone> resp = response.body();
                if (resp == null) {
                    shipping_done = true;
                    showLoading(false);
                    failedLoadShipping(false);
                } else if (resp.size() == 1) {
                    shipping_done = true;
                    showLoading(false);
                    failedLoadShipping(true);
                } else {
                    requestShippingMethod(resp.get(1).id);
                }
            }

            @Override
            public void onFailure(Call<List<RespShipZone>> call, Throwable t) {
                shipping_done = true;
                showLoading(false);
                failedLoadShipping(false);
            }
        });
    }

    private void requestShippingMethod(final long id) {
        selectedShip = null;
        if (callbackShipMethod != null && !callbackShipMethod.isCanceled())
            callbackShipMethod.cancel();
        callbackShipMethod = api.shippingZonesMethods(id);
        callbackShipMethod.enqueue(new Callback<List<RespShipMethod>>() {
            @Override
            public void onResponse(Call<List<RespShipMethod>> call, Response<List<RespShipMethod>> response) {
                List<RespShipMethod> resp = response.body();
                if (resp == null) {
                    shipping_done = true;
                    failedLoadShipping(false);
                } else {
                    successLoadShipping(resp);
                }
                showLoading(false);
            }

            @Override
            public void onFailure(Call<List<RespShipMethod>> call, Throwable t) {
                shipping_done = true;
                showLoading(false);
                failedLoadShipping(false);
            }
        });
    }

    private List<RespShipMethod> shipMethods = new ArrayList<>();
    private List<String> shipStr = new ArrayList<>();

    private void successLoadShipping(List<RespShipMethod> resp) {
        for (RespShipMethod p : resp) {
            if (p.enabled && p.settings != null && p.settings.cost != null) {
                shipMethods.add(p);
                shipStr.add(p.title);
            }
        }
        if (shipMethods.size() == 0) {
            tv_ship_empty.setVisibility(View.VISIBLE);
        } else {
            tv_ship_empty.setVisibility(View.GONE);
            rg_ship_option.removeAllViews();
            for (int i = 0; i < shipMethods.size(); i++) {
                rg_ship_option.addView(getTopicRadioTemplate(i, shipMethods.get(i)));
            }
            rg_ship_option.check(-1);
            rg_ship_option.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    selectedShip = shipMethods.get(checkedId);
                    String ship_fee = selectedShip.settings.cost.value;
                    tv_shipping_fee.setTag(ship_fee);
                    calculateFee();
                }
            });
        }
    }

    private void failedLoadShipping(boolean is_empty) {
        String msg = getString(R.string.failed_load_shipping);
        if (is_empty) msg = getString(R.string.no_shipping);
        showFailedDialog(msg, new Retry() {
            @Override
            public void onRetry() {
                requestShippingZone();
            }
        });
    }

    private void validateSubmitOrder() {
        if (!is_address_valid) {
            Tools.snackBarError(this, parent_view, R.string.please_edit_address);
            return;
        }
        if (selectedShip == null) {
            Tools.snackBarError(this, parent_view, R.string.please_select_shipping);
            return;
        }
        if (selectedPayment == null) {
            Tools.snackBarError(this, parent_view, R.string.please_select_payment);
            return;
        }
        final ParamOrder paramOrder = new ParamOrder();

        paramOrder.customer_id = user.id;
        paramOrder.payment_method = selectedPayment.id;
        paramOrder.payment_method_title = selectedPayment.title;
        paramOrder.customer_note = et_note.getText().toString();
        paramOrder.status = "on-hold";

        if (!user.billing.first_name.equals("")) {
            paramOrder.billing = user.billing;
        }
        paramOrder.shipping = user.shipping;

        paramOrder.line_items = new ArrayList<>();
        for (CartEntity c : listCart) {
            ProductOrder po = new ProductOrder();
            po.product_id = c.getId();
            po.quantity = c.getAmount();
            paramOrder.line_items.add(po);
        }

        paramOrder.shipping_lines = new ArrayList<>();
        ShippingOrder shipOrder = new ShippingOrder();
        shipOrder.method_id = selectedShip.method_id;
        shipOrder.total = selectedShip.settings.cost.value;
        paramOrder.shipping_lines.add(shipOrder);

        if (coupon != null) {
            paramOrder.coupon_lines = new ArrayList<>();
            paramOrder.coupon_lines.add(new CouponOrder(coupon.code));
        }

        showOrderLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                submitOrder(paramOrder);
            }
        }, 500);
    }


    private Call<Order> callbackOrder;

    private Order order;
    private void submitOrder(ParamOrder paramOrder) {
        order = null;
        if (callbackOrder != null && !callbackOrder.isCanceled()) callbackOrder.cancel();
        callbackOrder = api.order(paramOrder);
        callbackOrder.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                order = response.body();
                showOrderLoading(false);
                if (order == null) {
                    failedSubmitOrder();
                } else {
                    // clear cart
                    db.deleteAllCart();
                    orderSuccess();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                showOrderLoading(false);
                failedSubmitOrder();
            }
        });
    }

    private void showOrderLoading(boolean show) {
        progress_bar_order.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        btn_continue.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    private void orderSuccess() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        ActivityOrderReceived.navigate(ActivityCheckout.this, order, selectedPayment);
        finish();
    }

    private void failedSubmitOrder() {
        showFailedDialog(getString(R.string.failed_order_msg), new Retry() {
            @Override
            public void onRetry() {
                validateSubmitOrder();
            }
        });
    }

    private AppCompatRadioButton getTopicRadioTemplate(int idx, RespShipMethod r) {
        String cost = " (" + Tools.getPrice(r.settings.cost.value) + ")";
        AppCompatRadioButton rb = new AppCompatRadioButton(this);
        rb.setId(idx);
        rb.setText(r.title + cost);
        rb.setMaxLines(1);
        rb.setSingleLine(true);
        rb.setEllipsize(TextUtils.TruncateAt.END);
        rb.setTextAppearance(this, R.style.TextAppearance_AppCompat_Small);
        rb.setHighlightColor(getResources().getColor(R.color.colorTextAction));
        rb.setTextColor(getResources().getColor(R.color.grey_60));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rb.setLayoutParams(params);
        rb.setPadding(0, 15, 0, 15);
        return rb;
    }

    private interface Retry {
        void onRetry();
    }

}