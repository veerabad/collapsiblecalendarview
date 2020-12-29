package com.collapsiblecalendar.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class LockScrollView extends ScrollView {
    private OnSwipeTouchListener swipeTouchListener = null;

    public LockScrollView(Context context) {
        super(context);
    }

    public LockScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnSwipeTouchListener(OnSwipeTouchListener swipeTouchListener) {
        this.swipeTouchListener = swipeTouchListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (swipeTouchListener == null)
            return false;
        else
            return swipeTouchListener.onTouch(this, ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return true;
    }

}