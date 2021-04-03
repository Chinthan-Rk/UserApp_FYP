package com.app.fresy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.app.fresy.R;
import com.app.fresy.model.Slider;
import com.app.fresy.utils.Tools;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;

public class AdapterImageSlider extends PagerAdapter {

    private Context context;
    private List<Slider> items;

    public AdapterImageSlider.OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Slider obj);
    }

    public void setOnItemClickListener(AdapterImageSlider.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterImageSlider(Context context, List<Slider> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    public Slider getItem(int pos) {
        return items.get(pos);
    }

    public void setItems(List<Slider> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Slider o = items.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_slider_image, container, false);

        ImageView image = v.findViewById(R.id.image);
        View lyt_parent = v.findViewById(R.id.lyt_parent);
        Tools.displayImage(context, image, o.image);
        lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, o);
                }
            }
        });

        (container).addView(v);

        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        (container).removeView((View) object);

    }

}