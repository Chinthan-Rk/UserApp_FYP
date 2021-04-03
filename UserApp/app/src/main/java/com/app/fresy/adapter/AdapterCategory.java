package com.app.fresy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.fresy.R;
import com.app.fresy.model.Category;
import com.app.fresy.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class AdapterCategory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Category> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Category obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterCategory(Context context, List<Category> items) {
        this.items = items;
        ctx = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public ImageView image;
        public View indicator;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            image = v.findViewById(R.id.image);
            indicator = v.findViewById(R.id.indicator);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Category c = items.get(position);
            OriginalViewHolder v = (OriginalViewHolder) holder;
            v.name.setText(Tools.fromHtml(c.name));
            Tools.displayImageThumb(ctx, v.image, c.image.src, 0.5f);
            v.indicator.setVisibility(c.selected ? View.VISIBLE : View.INVISIBLE);
            v.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSelected(position);
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(view, c, position);
                    }
                }
            });
        }
    }

    private void setSelected(int position){
        for (Category c : items) {
            c.selected = false;
        }
        items.get(position).selected = true;
        notifyDataSetChanged();
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

}