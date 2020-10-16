package com.hostcart.socialbot.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class PostGridView extends GridView {

    public PostGridView(Context context) {
        super(context);
    }

    public PostGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSpec = heightMeasureSpec;
        if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
            heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, heightSpec);
    }
}
