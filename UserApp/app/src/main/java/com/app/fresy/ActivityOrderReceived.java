package com.app.fresy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.app.fresy.adapter.AdapterProductOrder;
import com.app.fresy.model.BillingShipping;
import com.app.fresy.model.Order;
import com.app.fresy.model.Payment;
import com.app.fresy.model.ShippingOrder;
import com.app.fresy.utils.Tools;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityOrderReceived extends AppCompatActivity {

    public static final int REQUEST_CHECKOUT = 5000;
    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";
    private static final String EXTRA_OBJECT_2 = "key.EXTRA_OBJECT2";

    // activity transition
    public static void navigate(Activity context, Order order, Payment payment) {
        Intent i = new Intent(context, ActivityOrderReceived.class);
        i.putExtra(EXTRA_OBJECT, order);
        i.putExtra(EXTRA_OBJECT_2, payment);
        context.startActivity(i);
    }

    private Order order;
    private Payment payment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_received);
        order = (Order) getIntent().getSerializableExtra(EXTRA_OBJECT);
        payment = (Payment) getIntent().getSerializableExtra(EXTRA_OBJECT_2);
        initComponent();

        Tools.setSystemBarColor(this, R.color.grey_3);
        Tools.setSystemBarLight(this);
        Tools.RTLMode(getWindow());
    }


    private void initComponent() {
        TextView tv_buyer_note = findViewById(R.id.tv_buyer_note);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        AdapterProductOrder adapter = new AdapterProductOrder(this);
        recyclerView.setAdapter(adapter);

        double subtotal = 0d;
        if (order.line_items != null && order.line_items.size() > 0) {
            adapter.setItems(order.line_items);
            subtotal = Double.parseDouble(order.line_items.get(0).subtotal);
        }
        ((TextView) findViewById(R.id.tv_item_fee)).setText(Tools.getPrice(subtotal));

        String shipping_fee = "0";
        String shipping_name = getString(R.string.shipping);
        if (order.shipping_lines != null && order.shipping_lines.size() > 0) {
            ShippingOrder so = order.shipping_lines.get(0);
            shipping_fee = so.total;
            shipping_name = shipping_name + " ( " + so.method_id.replace("_", " ") + " )";
        }
        ((TextView) findViewById(R.id.tv_shipping_fee)).setText(Tools.getPrice(shipping_fee));
        ((TextView) findViewById(R.id.tv_shipping_name)).setText(shipping_name);
        ((TextView) findViewById(R.id.tv_total_fee)).setText(Tools.getPrice(order.total));
        ((TextView) findViewById(R.id.total_order_big)).setText(Tools.getPrice(order.total));
        tv_buyer_note.setVisibility(order.customer_note.equals("") ? View.GONE : View.VISIBLE);
        tv_buyer_note.setText(Tools.fromHtml(order.customer_note));
        ((TextView) findViewById(R.id.tv_order_no)).setText("#" + order.number);
        ((TextView) findViewById(R.id.date_order)).setText(Tools.getFormattedDateSimple(order.date_created));
        ((TextView) findViewById(R.id.payment)).setText(order.payment_method_title);
        ((TextView) findViewById(R.id.email_order)).setText(order.billing.email);
        ((TextView) findViewById(R.id.instruction)).setText(payment.settings.instructions.value);
        setAddressText(order.shipping, ((TextView) findViewById(R.id.tv_ship_address)));
        setAddressText(order.billing, ((TextView) findViewById(R.id.tv_bill_address)));

        double discount = Double.parseDouble(order.discount_total);
        if (discount > 0) {
            ((TextView) findViewById(R.id.tv_discount_total)).setText("-" + Tools.getPrice(discount));
            (findViewById(R.id.lyt_discount)).setVisibility(View.VISIBLE);
        } else {
            (findViewById(R.id.lyt_discount)).setVisibility(View.GONE);
        }


        (findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setAddressText(BillingShipping address, TextView textView) {
        String address_text = Tools.getAddress(this, address);
        textView.setText(address_text);
    }

}