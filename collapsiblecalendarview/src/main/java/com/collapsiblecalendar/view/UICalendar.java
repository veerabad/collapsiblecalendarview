package com.collapsiblecalendar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.collapsiblecalendar.R;

import java.util.Locale;

public abstract class UICalendar extends ScrollView {

    // Day of Week
    public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;
    // State
    public static final int STATE_EXPANDED = 0;
    public static final int STATE_COLLAPSED = 1;
    public static final int STATE_PROCESSING = 2;

    protected LayoutInflater mInflater;
    protected TextView mTxtTitle;
    protected TableLayout mTableHead;
    protected LockScrollView mScrollViewBody;
    protected TableLayout mTableBody;
    protected RelativeLayout mLayoutBtnGroupMonth;
    protected RelativeLayout mLayoutBtnGroupWeek;
    protected ImageView mBtnPrevMonth;
    protected ImageView mBtnNextMonth;
    protected ImageView mBtnPrevWeek;
    protected ImageView mBtnNextWeek;
    protected ExpandIconView expandIconView;
    protected ImageView mTodayIcon;
    protected String datePattern = "MMMM";
    protected int firstDayOfWeek = SUNDAY;
    protected int state = STATE_COLLAPSED;
    protected int textColor = Color.BLACK;
    protected int todayItemTextColor = Color.BLACK;
    protected int selectedItemTextColor = Color.WHITE;
    protected Drawable todayItemBackgroundDrawable;
    protected Drawable selectedItemBackgroundDrawable;
    protected int eventColor = Color.BLACK;
    // UI
    private LinearLayout mLayoutRoot;
    private LinearLayout clEntireTextView;
    // Attributes
    private boolean isShowWeek = true;
    private boolean hideArrow = true;
    private int primaryColor = Color.WHITE;
    /**
     * This can be used to defined the left icon drawable other than predefined icon
     */
    private Drawable buttonLeftDrawable;
    /**
     * This can be used to set the drawable for the right icon, other than predefined icon
     */
    private Drawable buttonRightDrawable;
    private int mButtonLeftDrawableTintColor = Color.BLACK;
    private int mButtonRightDrawableTintColor = Color.BLACK;
    private int mExpandIconColor = Color.BLACK;

    public UICalendar(Context context) {
        this(context, null, 0);
    }

    public UICalendar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UICalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mInflater = LayoutInflater.from(context);

        // load rootView from xml
        View rootView = mInflater.inflate(R.layout.widget_collapsible_calendarview, this, true);

        // init UI
        mLayoutRoot = rootView.findViewById(R.id.layout_root);
        mTxtTitle = rootView.findViewById(R.id.txt_title);
        mTodayIcon = rootView.findViewById(R.id.today_icon);
        mTableHead = rootView.findViewById(R.id.table_head);
        mTableBody = rootView.findViewById(R.id.table_body);
        mLayoutBtnGroupMonth = rootView.findViewById(R.id.layout_btn_group_month);
        mLayoutBtnGroupWeek = rootView.findViewById(R.id.layout_btn_group_week);
        mBtnPrevMonth = rootView.findViewById(R.id.btn_prev_month);
        mBtnNextMonth = rootView.findViewById(R.id.btn_next_month);
        mBtnPrevWeek = rootView.findViewById(R.id.btn_prev_week);
        mBtnNextWeek = rootView.findViewById(R.id.btn_next_week);
        mScrollViewBody = rootView.findViewById(R.id.scroll_view_body);
        expandIconView = rootView.findViewById(R.id.expandIcon);
        clEntireTextView = rootView.findViewById(R.id.cl_title);
        clEntireTextView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return expandIconView.performClick();
            }
        });
        mLayoutRoot.setOnTouchListener(getSwipe(context));
        mScrollViewBody.setOnTouchListener(getSwipe(context));
        mScrollViewBody.setOnSwipeTouchListener(getSwipe(context));
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.UICalendar, defStyleAttr, 0);
        setAttributes(attributes);
        attributes.recycle();
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public void setShowWeek(boolean showWeek) {
        this.isShowWeek = showWeek;
        mTableHead.setVisibility(showWeek ? View.VISIBLE : View.GONE);
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
        reload();
    }

    public void setHideArrow(boolean hideArrow) {
        this.hideArrow = hideArrow;
        hideButton();
    }

    protected int getState() {
        return state;
    }

    protected void setState(int state) {
        this.state = state;
        if (this.state == STATE_EXPANDED) {
            mLayoutBtnGroupMonth.setVisibility(View.VISIBLE);
            mLayoutBtnGroupWeek.setVisibility(View.GONE);
        }
        if (this.state == STATE_COLLAPSED) {
            mLayoutBtnGroupMonth.setVisibility(View.GONE);
            mLayoutBtnGroupWeek.setVisibility(View.VISIBLE);
        }
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        redraw();
        mTxtTitle.setTextColor(this.textColor);
    }

    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
        redraw();
        mLayoutRoot.setBackgroundColor(this.primaryColor);
    }

    public void setTodayItemTextColor(int todayItemTextColor) {
        this.todayItemTextColor = todayItemTextColor;
        redraw();
    }

    public void setTodayItemBackgroundDrawable(Drawable todayItemBackgroundDrawable) {
        this.todayItemBackgroundDrawable = todayItemBackgroundDrawable;
        redraw();
    }

    public void setSelectedItemTextColor(int selectedItemTextColor) {
        this.selectedItemTextColor = selectedItemTextColor;
        redraw();
    }

    public void setSelectedItemBackgroundDrawable(Drawable selectedItemBackground) {
        this.selectedItemBackgroundDrawable = selectedItemBackground;
        redraw();
    }

    public void setButtonLeftDrawable(Drawable buttonLeftDrawable) {
        this.buttonLeftDrawable = buttonLeftDrawable;
        mBtnPrevMonth.setImageDrawable(buttonLeftDrawable);
        mBtnPrevWeek.setImageDrawable(buttonLeftDrawable);
    }

    public void setButtonRightDrawable(Drawable buttonRightDrawable) {
        this.buttonRightDrawable = buttonRightDrawable;
        mBtnNextMonth.setImageDrawable(buttonRightDrawable);
        mBtnNextWeek.setImageDrawable(buttonRightDrawable);
    }

    public void setEventColor(int eventColor) {
        this.eventColor = eventColor;
        redraw();
    }

    public OnSwipeTouchListener getSwipe(Context context) {

        return new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeTop() {
                if (state == STATE_EXPANDED)
                    expandIconView.performClick();
            }

            @Override
            public void onSwipeLeft() {
                if (state == STATE_COLLAPSED) {
                    mBtnNextWeek.performClick();
                } else if (state == STATE_EXPANDED) {
                    mBtnNextMonth.performClick();
                }
            }

            @Override
            public void onSwipeRight() {
                if (state == STATE_COLLAPSED) {
                    mBtnPrevWeek.performClick();
                } else if (state == STATE_EXPANDED) {
                    mBtnPrevMonth.performClick();
                }
            }

            @Override
            public void onSwipeBottom() {
                if (state == STATE_COLLAPSED)
                    expandIconView.performClick();
            }
        };
    }

    public Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }

    public void hideButton() {
        mBtnNextWeek.setVisibility(View.GONE);
        mBtnPrevWeek.setVisibility(View.GONE);
        mBtnNextMonth.setVisibility(View.GONE);
        mBtnPrevMonth.setVisibility(View.GONE);
    }

    public void setAttributes(TypedArray attrs) {
        // set attributes by the values from XML
        //setStyle(attrs.getInt(R.styleable.UICalendar_style, mStyle));
        isShowWeek = attrs.getBoolean(R.styleable.UICalendar_showWeek, isShowWeek);
        firstDayOfWeek = attrs.getInt(R.styleable.UICalendar_firstDayOfWeek, firstDayOfWeek);
        hideArrow = attrs.getBoolean(R.styleable.UICalendar_hideArrows, hideArrow);

        datePattern = datePattern == null ? attrs.getString(R.styleable.UICalendar_datePattern) : datePattern;
        state = attrs.getInt(R.styleable.UICalendar_state, state);
        setState(state);
        textColor = attrs.getColor(R.styleable.UICalendar_textColor, textColor);
        primaryColor = attrs.getColor(R.styleable.UICalendar_primaryColor, primaryColor);

        eventColor = attrs.getColor(R.styleable.UICalendar_eventColor, eventColor);

        todayItemTextColor = attrs.getColor(R.styleable.UICalendar_todayItem_textColor, todayItemTextColor);
        Drawable todayItemBackgroundDrawable = attrs.getDrawable(R.styleable.UICalendar_todayItem_background);
        this.todayItemBackgroundDrawable = todayItemBackgroundDrawable != null ? todayItemBackgroundDrawable : getResources().getDrawable(R.drawable.circle_stroke_background);

        selectedItemTextColor = attrs.getColor(
                R.styleable.UICalendar_selectedItem_textColor, selectedItemTextColor);
        Drawable selectedItemBackgroundDrawable = attrs.getDrawable(R.styleable.UICalendar_selectedItem_background);
        this.selectedItemBackgroundDrawable = selectedItemBackgroundDrawable != null ? selectedItemBackgroundDrawable : getResources().getDrawable(R.drawable.circle_solid_background);

        Drawable buttonLeftDrawable = attrs.getDrawable(R.styleable.UICalendar_buttonLeft_drawable);
        this.buttonLeftDrawable = buttonLeftDrawable != null ? buttonLeftDrawable : getResources().getDrawable(R.drawable.left_arrow_icon);

        Drawable buttonRightDrawable = attrs.getDrawable(R.styleable.UICalendar_buttonRight_drawable);
        this.buttonRightDrawable = buttonRightDrawable != null ? buttonRightDrawable : getResources().getDrawable(R.drawable.right_arrow_icon);

        setButtonLeftDrawableTintColor(attrs.getColor(R.styleable.UICalendar_buttonLeft_drawableTintColor, mButtonLeftDrawableTintColor));
        setButtonRightDrawableTintColor(attrs.getColor(R.styleable.UICalendar_buttonRight_drawableTintColor, mButtonRightDrawableTintColor));
        setExpandIconColor(attrs.getColor(R.styleable.UICalendar_expandIconColor, mExpandIconColor));
    }

    public void setButtonLeftDrawableTintColor(int color) {
        this.mButtonLeftDrawableTintColor = color;
        mBtnPrevMonth.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        mBtnPrevWeek.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        redraw();
    }

    public void setButtonRightDrawableTintColor(int color) {

        this.mButtonRightDrawableTintColor = color;
        mBtnNextMonth.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        mBtnNextWeek.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        redraw();
    }

    public void setExpandIconColor(int color) {
        this.mExpandIconColor = color;
        expandIconView.setColor(color);
    }

    public abstract void changeToToday();

    public abstract void redraw();

    public abstract void reload();

}
