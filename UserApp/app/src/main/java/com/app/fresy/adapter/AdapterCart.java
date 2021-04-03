package com.app.fresy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.fresy.R;
import com.app.fresy.room.table.CartEntity;
import com.app.fresy.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;


public class AdapterCart extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum LayoutType {
        CART, CHECKOUT
    }

    public enum ActionType {
        DELETE, PLUS, MINUS
    }

    private Context ctx;
    private LayoutType layoutType;
    private List<CartEntity> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, CartEntity obj, ActionType actionType, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name, brief, price_item;
        public TextView price_item_total, item_amount;
        public ImageView image;
        public View bt_cart_minus, bt_cart_plus, cart_delete;

        public ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            brief = v.findViewById(R.id.brief);
            price_item = v.findViewById(R.id.price_item);
            price_item_total = v.findViewById(R.id.price_item_total);
            image = v.findViewById(R.id.image);
            item_amount = v.findViewById(R.id.item_amount);
            if (layoutType == LayoutType.CART) {
                bt_cart_minus = v.findViewById(R.id.bt_cart_minus);
                bt_cart_plus = v.findViewById(R.id.bt_cart_plus);
                cart_delete = v.findViewById(R.id.cart_delete);
            }
        }
    }

    public AdapterCart(Context ctx, LayoutType layoutType) {
        this.ctx = ctx;
        this.layoutType = layoutType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (layoutType == LayoutType.CART) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
            vh = new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_checkout, parent, false);
            vh = new ViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            final ViewHolder v = (ViewHolder) holder;
            final CartEntity c = items.get(position);

            v.name.setText(c.getName());
            v.brief.setText(Tools.fromHtml(c.getShort_description()));
            v.price_item.setText(Tools.getPrice(c.getPrice()));
            v.item_amount.setText(c.getAmount() + "");

            double price = Double.parseDouble(c.getPrice()) * c.getAmount();
            v.price_item_total.setText(Tools.getPrice(price));


            if (c.getImage() != null) Tools.displayImage(ctx, v.image, c.getImage());

            if (layoutType == LayoutType.CART) {
                v.cart_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        onItemClick(view, c, ActionType.DELETE, position);
                    }
                });

                v.bt_cart_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        onItemClick(view, c, ActionType.PLUS, position);
                    }
                });

                v.bt_cart_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        onItemClick(view, c, ActionType.MINUS, position);
                    }
                });
            }

        }

    }

    private void onItemClick(View v, CartEntity c, ActionType act, int pos) {
        if (onItemClickListener == null) return;
        onItemClickListener.onItemClick(v, c, act, pos);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<CartEntity> getItem() {
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<CartEntity> items) {
        this.items = items;
        notifyDataSetChanged();
    }

}