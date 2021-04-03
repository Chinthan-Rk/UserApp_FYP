package com.app.fresy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.fresy.adapter.AdapterCart;
import com.app.fresy.data.ThisApp;
import com.app.fresy.room.AppDatabase;
import com.app.fresy.room.DAO;
import com.app.fresy.room.table.CartEntity;
import com.app.fresy.utils.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityCart extends AppCompatActivity {

    public static final int REQUEST_CHECKOUT = 5000;

    // activity transition
    public static void navigate(Activity context, int code) {
        Intent i = new Intent(context, ActivityCart.class);
        context.startActivityForResult(i, code);
    }

    private RecyclerView recyclerView;
    private DAO db;
    private AdapterCart adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = AppDatabase.getDb(this).get();

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
        actionBar.setTitle(R.string.title_activity_cart);
        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
    }


    private void iniComponent() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new AdapterCart(this, AdapterCart.LayoutType.CART);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new AdapterCart.OnItemClickListener() {
            @Override
            public void onItemClick(View view, CartEntity obj, AdapterCart.ActionType actionType, int position) {
                if (actionType == AdapterCart.ActionType.DELETE) {
                    dialogDeleteCart(obj);

                } else if (actionType == AdapterCart.ActionType.PLUS) {
                    long new_amount = obj.getAmount() + 1;
                    if (Tools.checkOutOfStockSnackBar(ActivityCart.this, obj, new_amount)) return;
                    obj.setAmount(new_amount);
                    db.updateCart(obj);

                } else if (actionType == AdapterCart.ActionType.MINUS) {
                    if (obj.getAmount() == 1) {
                        dialogDeleteCart(obj);
                    } else {
                        obj.setAmount(obj.getAmount() - 1);
                        db.updateCart(obj);
                    }
                }

                adapter.notifyItemChanged(position);
                refreshCartSheet();
            }
        });

        displayData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CHECKOUT && resultCode == RESULT_OK) {
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }
        } catch (Exception ex) {

        }
    }

    private void displayData() {
        adapter.setItems(db.getAllCart());
        refreshCartSheet();
        showNoItemView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartSheet();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshCartSheet() {
        AppCompatButton counter_badge = findViewById(R.id.counter_badge);
        AppCompatButton btn_continue = findViewById(R.id.btn_continue);
        View lyt_cart_sheet = findViewById(R.id.lyt_cart_sheet);
        TextView tv_total_cart = findViewById(R.id.tv_total_cart);
        List<CartEntity> cartEntities = db.getAllCart();
        if (cartEntities.size() == 0) {
            lyt_cart_sheet.setVisibility(View.GONE);
            return;
        }
        lyt_cart_sheet.setVisibility(View.VISIBLE);
        double price = 0d;
        long counter = 0;
        for (CartEntity c : cartEntities) {
            price = price + (Double.parseDouble(c.getPrice()) * c.getAmount());
            counter = counter + c.getAmount();
        }
        tv_total_cart.setText(Tools.getPrice(price));

        if (ThisApp.get().isLogin()) {
            btn_continue.setText(R.string.CHECK_OUT);
        } else {
            btn_continue.setText(R.string.LOGIN);
        }

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ThisApp.get().isLogin()) {
                    ActivityCheckout.navigate(ActivityCart.this, REQUEST_CHECKOUT);
                } else {
                    ActivityLogin.navigate(ActivityCart.this);
                }
            }
        });

        // animate badge
        ScaleAnimation fade_in = new ScaleAnimation(0.0f, 1f, 0.0f, 1f, 1, 0.5f, 1, 0.5f);
        fade_in.setDuration(200);
        fade_in.setFillAfter(true);
        counter_badge.startAnimation(fade_in);
        String counter_text = counter > 99 ? "99+" : counter + "";
        counter_badge.setText(counter_text);

    }

    private void dialogDeleteCart(final CartEntity c) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.content_delete_one_confirm);
        builder.setIcon(Tools.tintDrawable(this, R.drawable.ic_info_outline, R.color.colorPrimary));
        builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deleteCart(c.getId());
                displayData();
            }
        });
        builder.setNegativeButton(R.string.NO, null);
        builder.show();
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

}