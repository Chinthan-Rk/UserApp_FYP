package com.app.fresy.fragment;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.fresy.R;
import com.app.fresy.adapter.AdapterProductOrder;
import com.app.fresy.model.BillingShipping;
import com.app.fresy.model.Order;
import com.app.fresy.model.ShippingOrder;
import com.app.fresy.utils.Tools;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentOrderDetails extends DialogFragment {

    private static final String EXTRA_OBJECT = "key.EXTRA_OBJECT";

    public FragmentOrderDetails() {
    }

    public static FragmentOrderDetails newInstance(Order order) {
        FragmentOrderDetails fragment = new FragmentOrderDetails();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_OBJECT, order);
        fragment.setArguments(args);
        return fragment;
    }

    private Order order;
    private RecyclerView recyclerView;
    private View lyt_status_failed, lyt_status_normal;
    private View root_view;
    private TextView status_failed, date_failed, tv_buyer_note;

    private String[] status_arr = new String[]{
            "pending", "on-hold", "processing", "completed"
    };

    private int[] status_dot_id = new int[]{
            R.id.dot_pending, R.id.dot_on_hold, R.id.dot_processing, R.id.dot_completed
    };

    private int[] status_date_id = new int[]{
            R.id.date_pending, R.id.date_on_hold, R.id.date_processing, R.id.date_completed
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_order_details, container, false);

        order = (Order) getArguments().getSerializable(EXTRA_OBJECT);
        initComponent();

        return root_view;
    }

    private void initComponent() {
        lyt_status_failed = root_view.findViewById(R.id.lyt_status_failed);
        status_failed = root_view.findViewById(R.id.status_failed);
        date_failed = root_view.findViewById(R.id.date_failed);
        lyt_status_failed.setVisibility(View.GONE);
        lyt_status_normal = root_view.findViewById(R.id.lyt_status_normal);
        tv_buyer_note = root_view.findViewById(R.id.tv_buyer_note);

        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setNestedScrollingEnabled(false);
        AdapterProductOrder adapter = new AdapterProductOrder(getActivity());
        recyclerView.setAdapter(adapter);

        double subtotal = 0d;
        if (order.line_items != null && order.line_items.size() > 0) {
            adapter.setItems(order.line_items);
            subtotal = Double.parseDouble(order.line_items.get(0).subtotal);
        }
        ((TextView) root_view.findViewById(R.id.tv_item_fee)).setText(Tools.getPrice(subtotal));

        String shipping_fee = "0";
        String shipping_name = getString(R.string.shipping);
        if (order.shipping_lines != null && order.shipping_lines.size() > 0) {
            ShippingOrder so = order.shipping_lines.get(0);
            shipping_fee = so.total;
            shipping_name = shipping_name + " ( " + so.method_id.replace("_", " ") + " )";
        }
        ((TextView) root_view.findViewById(R.id.tv_shipping_fee)).setText(Tools.getPrice(shipping_fee));
        ((TextView) root_view.findViewById(R.id.tv_shipping_name)).setText(shipping_name);
        ((TextView) root_view.findViewById(R.id.tv_total_fee)).setText(Tools.getPrice(order.total));
        ((TextView) root_view.findViewById(R.id.total_order_big)).setText(Tools.getPrice(order.total));
        tv_buyer_note.setVisibility(order.customer_note.equals("") ? View.GONE : View.VISIBLE);
        tv_buyer_note.setText(Tools.fromHtml(order.customer_note));
        ((TextView) root_view.findViewById(R.id.tv_order_no)).setText("#" + order.number);
        ((TextView) root_view.findViewById(R.id.date_order)).setText(Tools.getFormattedDateSimple(order.date_created));
        ((TextView) root_view.findViewById(R.id.payment)).setText(order.payment_method_title);
        ((TextView) root_view.findViewById(R.id.email_order)).setText(order.billing.email);
        setAddressText(order.shipping, ((TextView) root_view.findViewById(R.id.tv_ship_address)));
        setAddressText(order.billing, ((TextView) root_view.findViewById(R.id.tv_bill_address)));

        Double discount = Double.parseDouble(order.discount_total);
        if (discount > 0) {
            ((TextView) root_view.findViewById(R.id.tv_discount_total)).setText("-" + Tools.getPrice(discount));
            (root_view.findViewById(R.id.lyt_discount)).setVisibility(View.VISIBLE);
        } else {
            (root_view.findViewById(R.id.lyt_discount)).setVisibility(View.GONE);
        }


        (root_view.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        int status_index = getStatusIndex(order.status);
        if (status_index == -1) {
            lyt_status_failed.setVisibility(View.VISIBLE);
            lyt_status_normal.setVisibility(View.GONE);
            status_failed.setText(order.status.toUpperCase());
            date_failed.setText(Tools.getFormattedDateSimple(order.date_modified));
        } else {
            lyt_status_failed.setVisibility(View.GONE);
            TextView tv_date = root_view.findViewById(status_date_id[status_index]);
            tv_date.setText(Tools.getFormattedDateSimple(order.date_modified));
            tv_date.setVisibility(View.VISIBLE);
            ImageView img_dot = root_view.findViewById(status_dot_id[status_index]);
            img_dot.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorStatusNormal), PorterDuff.Mode.SRC_IN);
        }
    }

    private void setAddressText(BillingShipping address, TextView textView) {
        String address_text = Tools.getAddress(getActivity(), address);
        textView.setText(address_text);
    }


    private int getStatusIndex(String status) {
        int index = -1;
        for (int i = 0; i < status_arr.length; i++) {
            if (status.equalsIgnoreCase(status_arr[i])) {
                return i;
            }
        }
        return index;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

}