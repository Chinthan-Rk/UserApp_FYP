package com.app.fresy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.fresy.R;
import com.app.fresy.model.ProductOrder;
import com.app.fresy.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


public class AdapterProductOrder extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context ctx;
    private List<ProductOrder> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name, price_item;
        public TextView item_amount;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            price_item = v.findViewById(R.id.price_item);
            item_amount = v.findViewById(R.id.item_amount);
        }
    }

    public AdapterProductOrder(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_order, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            final ViewHolder v = (ViewHolder) holder;
            final ProductOrder c = items.get(position);
            v.name.setText(c.name);
            v.price_item.setText(Tools.getPrice(c.total));
            v.item_amount.setText(c.quantity + "");
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<ProductOrder> getItem() {
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<ProductOrder> items) {
        this.items = items;
        notifyDataSetChanged();
    }
}