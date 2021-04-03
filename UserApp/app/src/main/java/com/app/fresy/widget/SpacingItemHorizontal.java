package com.app.fresy.widget;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemHorizontal extends RecyclerView.ItemDecoration {

    private int spacing;

    public SpacingItemHorizontal(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position

        if (position == 0) {
            outRect.left = 0;
            outRect.right = spacing / 2;
        } else if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.left = spacing / 2;
            outRect.right = 0;
        } else {
            outRect.left = spacing / 2;
            outRect.right = spacing / 2;
        }
    }
}
