package com.app.fresy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.fresy.R;
import com.app.fresy.data.Constant;
import com.app.fresy.model.Order;
import com.app.fresy.model.Product;
import com.app.fresy.room.table.CartEntity;
import com.app.fresy.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class AdapterOrderHistory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ActionType {
        NORMAL, PAY_NOW
    }

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;
    private Context ctx;
    private List<Order> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Order obj, ActionType type, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView order_no, status, date;
        public TextView product, total, payment;
        public View lyt_parent;

        public ViewHolder(View v) {
            super(v);
            order_no = v.findViewById(R.id.order_no);
            status = v.findViewById(R.id.status);
            date = v.findViewById(R.id.date);
            product = v.findViewById(R.id.product);
            total = v.findViewById(R.id.total);
            payment = v.findViewById(R.id.payment);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressViewHolder(View v) {
            super(v);
        }
    }

    public AdapterOrderHistory(Context ctx, RecyclerView view) {
        this.ctx = ctx;
        lastItemViewDetector(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
            vh = new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ProgressViewHolder) {

        } else if (holder instanceof ViewHolder) {
            final ViewHolder v = (ViewHolder) holder;
            final Order c = items.get(position);

            v.order_no.setText(ctx.getString(R.string.order_no) + " #" + c.number);
            v.date.setText(Tools.getFormattedDateSimple(c.date_modified));
            v.status.setText(Tools.getStatusOrder(ctx, c.status));
            v.total.setText(Tools.getPrice(c.total));
            v.payment.setText(c.payment_method_title);
            if (c.line_items != null && c.line_items.size() > 0) {
                v.product.setText(Tools.fromHtml(c.line_items.get(0).name));
            } else {
                v.product.setText(ctx.getString(R.string.empty_product));
            }
            v.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onOrderClick(view, c, ActionType.NORMAL, position);
                }
            });

        }

    }

    private void onOrderClick(View v, Order o, ActionType act, int pos) {
        if (onItemClickListener == null) return;
        onItemClickListener.onItemClick(v, o, act, pos);
    }

    @Override
    public int getItemViewType(int position) {
        return this.items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Order> getItem() {
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<Order> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (items.get(i) == null) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    public void insertData(List<Order> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.items.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = layoutManager.findLastVisibleItemPosition();
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            int current_page = getItemCount() / Constant.ORDER_PER_REQUEST;
                            onLoadMoreListener.onLoadMore(current_page);
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

}