package com.collapsiblecalendar.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.collapsiblecalendar.R;
import com.collapsiblecalendar.view.BounceAnimator;
import com.collapsiblecalendar.view.CalendarAdapter;
import com.collapsiblecalendar.view.Day;
import com.collapsiblecalendar.view.Event;
import com.collapsiblecalendar.view.ExpandIconView;
import com.collapsiblecalendar.view.UICalendar;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class CollapsibleCalendarView extends UICalendar implements View.OnClickListener {
    /**
     * The date has been selected and can be used on Calender Listener
     */
    private Day selectedDay = null;
    private int selectedItemPosition = -1;
    private int todayItemPosition;
    private Params params = null;
    private CalendarAdapter mAdapter = null;
    private CalendarListener mListener = null;
    private boolean expanded = false;
    private int mInitHeight = 0;
    private Handler mHandler = new Handler();
    private boolean mIsWaitingForUpdate = false;
    private int mCurrentWeekIndex = 0;

    public CollapsibleCalendarView(Context context) {
        super(context);
        init(context);
    }

    public CollapsibleCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CollapsibleCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void changeToToday() {
        Calendar today = Calendar.getInstance();
        selectedItemPosition = -1;
        onItemClicked(mTodayIcon, new Day(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)));
        mCurrentWeekIndex = getSuitableRowIndex();
        collapseTo(mCurrentWeekIndex);
    }

    public int getYear() {
        return mAdapter.getCalendar().get(Calendar.YEAR);
    }

    public int getMonth() {
        return mAdapter.getCalendar().get(Calendar.MONTH);
    }

    public int getDay() {
        return mAdapter.getCalendar().get(Calendar.DAY_OF_MONTH);
    }

    public Day getSelectedDay() {
        return selectedDay;
    }

    public void setSelectedDay(Day day) {
        selectedDay = day;
        redraw();
        if (mListener != null && selectedDay != null) {
            mListener.onDaySelected();
        }
    }

    public int getSelectedItemPosition() {
        int position = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Day day = mAdapter.getItem(i);

            if (isSelectedDay(day)) {
                position = i;
                break;
            }
        }
        if (position == -1) {
            position = todayItemPosition;
        }
        return position;
    }

    public int getTodayItemPosition() {
        int position = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Day day = mAdapter.getItem(i);

            if (isToday(day)) {
                position = i;
                break;
            }
        }
        return position;
    }

    public boolean isSelectedDay(Day day) {
        return (day != null
                && selectedDay != null
                && day.getYear() == selectedDay.getYear()
                && day.getMonth() == selectedDay.getMonth()
                && day.getDay() == selectedDay.getDay());
    }

    public boolean isToday(Day day) {
        Calendar todayCal = Calendar.getInstance();
        return (day != null
                && day.getYear() == todayCal.get(Calendar.YEAR)
                && day.getMonth() == todayCal.get(Calendar.MONTH)
                && day.getDay() == todayCal.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onClick(View view) {
        if (mListener == null) {
            expandIconView.performClick();
        } else {
            mListener.onClicked();
        }
    }

    private int getSuitableRowIndex() {
        selectedItemPosition = getSelectedItemPosition();
        todayItemPosition = getTodayItemPosition();
        if (selectedItemPosition != -1) {
            View view = mAdapter.getView(selectedItemPosition);
            TableRow row = (TableRow) view.getParent();

            return mTableBody.indexOfChild(row);
        } else if (todayItemPosition != -1) {
            View view = mAdapter.getView(todayItemPosition);
            TableRow row = (TableRow) view.getParent();

            return mTableBody.indexOfChild(row);
        } else {
            return 0;
        }
    }


    private void init(Context context) {
        setAdapter(new CalendarAdapter(context, Calendar.getInstance()));


        // bind events

        mBtnPrevMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                prevMonth();
            }
        });

        mBtnNextMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMonth();
            }
        });

        mBtnPrevWeek.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                prevWeek();
            }
        });

        mBtnNextWeek.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                nextWeek();
            }
        });

        mTodayIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changeToToday();
            }
        });

        expandIconView.setState(ExpandIconView.MORE, true);


        expandIconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expanded) {
                    collapse(400);
                } else {
                    expand(400);
                }
            }
        });

        this.post(new Runnable() {
            @Override
            public void run() {
                collapseTo(mCurrentWeekIndex);
            }
        });


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mInitHeight = mTableBody.getMeasuredHeight();

        if (mIsWaitingForUpdate) {
            redraw();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    collapseTo(mCurrentWeekIndex);
                }
            });
            mIsWaitingForUpdate = false;
        }
    }

    @Override
    public void redraw() {
        // redraw all views of week
        TableRow rowWeek = (TableRow) mTableHead.getChildAt(0);
        if (rowWeek != null) {
            for (int i = 0; i < rowWeek.getChildCount(); i++) {
                ((TextView) rowWeek.getChildAt(i)).setTextColor(textColor);
            }
        }
        // redraw all views of day
        if (mAdapter != null) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                Day day = mAdapter.getItem(i);
                View view = mAdapter.getView(i);
                TextView txtDay = view.findViewById(R.id.txt_day);
                txtDay.setBackgroundColor(Color.TRANSPARENT);
                txtDay.setTextColor(textColor);

                // set today's item
                if (isToday(day)) {
                    txtDay.setBackgroundDrawable(todayItemBackgroundDrawable);
                    txtDay.setTextColor(todayItemTextColor);
                }

                // set the selected item
                if (isSelectedDay(day)) {
                    txtDay.setBackgroundDrawable(selectedItemBackgroundDrawable);
                    txtDay.setTextColor(selectedItemTextColor);
                }
            }
        }
    }

    @Override
    public void reload() {
        mAdapter.refresh();
        String tempDatePattern;
        if (Calendar.getInstance().get(Calendar.YEAR) != mAdapter.getCalendar().get(Calendar.YEAR)) {
            tempDatePattern = "MMMM YYYY";
        } else {
            tempDatePattern = datePattern;
        }
        // reset UI
        SimpleDateFormat dateFormat = new SimpleDateFormat(tempDatePattern, getCurrentLocale(getContext()));
        dateFormat.setTimeZone(mAdapter.getCalendar().getTimeZone());
        mTxtTitle.setText(dateFormat.format(mAdapter.getCalendar().getTime()));
        mTableHead.removeAllViews();
        mTableBody.removeAllViews();


        TableRow rowCurrent = new TableRow(getContext());
        rowCurrent.setLayoutParams(new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < 7; i++) {
            View view = mInflater.inflate(R.layout.layout_day_of_week, null);
            TextView txtDayOfWeek = view.findViewById(R.id.txt_day_of_week);
            txtDayOfWeek.setText(new DateFormatSymbols().getShortWeekdays()[(i + firstDayOfWeek) % 7 + 1]);
            view.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f));
            rowCurrent.addView(view);
        }
        mTableHead.addView(rowCurrent);

        // set day view
        for (int i = 0; i < mAdapter.getCount(); i++) {

            if (i % 7 == 0) {
                rowCurrent = new TableRow(getContext());
                rowCurrent.setLayoutParams(new TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                mTableBody.addView(rowCurrent);
            }
            View view = mAdapter.getView(i);
            view.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f));
            if (params != null && (mAdapter.getItem(i).getDiff() < params.prevDays || mAdapter.getItem(i).getDiff() > params.nextDaysBlocked)) {
                view.setClickable(false);
                view.setAlpha(0.3f);
            } else {
                final int pos = i;
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClicked(view, mAdapter.getItem(pos));
                    }
                });
            }
            rowCurrent.addView(view);
        }

        redraw();
        mIsWaitingForUpdate = true;
    }

    public void onItemClicked(View view, Day day) {
        setSelectedDay(day);
        int newYear = day.getYear();
        int newMonth = day.getMonth();
        Calendar cal = mAdapter.getCalendar();
        int oldYear = cal.get(Calendar.YEAR);
        int oldMonth = cal.get(Calendar.MONTH);
        if (newMonth != oldMonth) {
            cal.set(day.getYear(), day.getMonth(), 1);

            if (newYear > oldYear || newMonth > oldMonth) {
                mCurrentWeekIndex = 0;
            }
            if (newYear < oldYear || newMonth < oldMonth) {
                mCurrentWeekIndex = -1;
            }

            reload();
            if (mListener != null) {
                mListener.onMonthChanged();
            }
        }
    }

    // public methods
    public void setAdapter(CalendarAdapter adapter) {
        Calendar cal = Calendar.getInstance();
        mAdapter = adapter == null ? new CalendarAdapter(getContext(), cal) : adapter;
        mAdapter.setFirstDayOfWeek(firstDayOfWeek);
        //selectedDay = new Day(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        reload();
        // init week
        mCurrentWeekIndex = getSuitableRowIndex();
    }

    public void addEventTag(int numYear, int numMonth, int numDay) {
        mAdapter.addEvent(new Event(numYear, numMonth, numDay, eventColor));
        reload();
    }

    public void addEventTag(int numYear, int numMonth, int numDay, int color) {
        mAdapter.addEvent(new Event(numYear, numMonth, numDay, color));
        reload();
    }

    public void addEventTag(int numYear, int numMonth, int numDay, android.graphics.drawable.Drawable drawable) {
        mAdapter.addEvent(new Event(numYear, numMonth, numDay, drawable));
        reload();
    }

    public void clearEventTag() {
        mAdapter.getEventList().clear();
        reload();
    }

    public void setEventTagList(List<Event> events) {
        mAdapter.setEventList(events == null ? new ArrayList<Event>() : events);
        reload();
    }

    public void prevMonth() {
        Calendar cal = mAdapter.getCalendar();
        if (params != null && (Calendar.getInstance().get(Calendar.YEAR) * 12 + Calendar.getInstance().get(Calendar.MONTH) + params.prevDays / 30) > (cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH))) {
            Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
            myAnim.setInterpolator(new BounceAnimator(0.1, 10.0));
            mTableBody.startAnimation(myAnim);
            mTableHead.startAnimation(myAnim);
            return;
        }
        if (cal.get(Calendar.MONTH) == cal.getActualMinimum(Calendar.MONTH)) {
            cal.set(cal.get(Calendar.YEAR) - 1, cal.getActualMaximum(Calendar.MONTH), 1);
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        }
        reload();
        if (mListener != null) {
            mListener.onMonthChanged();
        }

    }

    public void nextMonth() {
        Calendar cal = mAdapter.getCalendar();
        if (params != null && (Calendar.getInstance().get(Calendar.YEAR) * 12 + Calendar.getInstance().get(Calendar.MONTH) + params.nextDaysBlocked / 30) < (cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH))) {
            Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
            myAnim.setInterpolator(new BounceAnimator(0.1, 10.0));
            mTableBody.startAnimation(myAnim);
            mTableHead.startAnimation(myAnim);
            return;
        }
        if (cal.get(Calendar.MONTH) == cal.getActualMaximum(Calendar.MONTH)) {
            cal.set(cal.get(Calendar.YEAR) + 1, cal.getActualMinimum(Calendar.MONTH), 1);
        } else {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        }
        reload();
        if (mListener != null) {
            mListener.onMonthChanged();
        }
    }

    public void nextDay() {
        if (selectedItemPosition == mAdapter.getCount() - 1) {
            nextMonth();
            mAdapter.getView(0).performClick();
            reload();
            mCurrentWeekIndex = 0;
            collapseTo(mCurrentWeekIndex);
        } else {
            mAdapter.getView(selectedItemPosition + 1).performClick();
            if (((selectedItemPosition + 1 - mAdapter.getCalendar().getFirstDayOfWeek()) / 7) > mCurrentWeekIndex) {
                nextWeek();
            }
        }
        if (mListener != null)
            mListener.onDayChanged();
    }

    public void prevDay() {
        if (selectedItemPosition == 0) {
            prevMonth();
            mAdapter.getView(mAdapter.getCount() - 1).performClick();
            reload();
            return;
        } else {
            mAdapter.getView(selectedItemPosition - 1).performClick();
            if (((selectedItemPosition - 1 + mAdapter.getCalendar().getFirstDayOfWeek()) / 7) < mCurrentWeekIndex) {
                prevWeek();
            }
        }
        if (mListener != null) mListener.onDayChanged();
    }

    public void prevWeek() {
        if (mCurrentWeekIndex - 1 < 0) {
            mCurrentWeekIndex = -1;
            prevMonth();
        } else {
            mCurrentWeekIndex--;
            collapseTo(mCurrentWeekIndex);
        }
    }

    public void nextWeek() {
        if (mCurrentWeekIndex + 1 >= mTableBody.getChildCount()) {
            mCurrentWeekIndex = 0;
            nextMonth();
        } else {
            mCurrentWeekIndex++;
            collapseTo(mCurrentWeekIndex);
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    protected void setState(int state) {
        super.setState(state);
        if (state == STATE_COLLAPSED) {
            expanded = false;
        }
        if (state == STATE_EXPANDED) {
            expanded = true;
        }
        if (state != state) {
            mIsWaitingForUpdate = true;
            requestLayout();
        }
    }

    public void setExpandIconVisible(boolean visible) {
        if (visible) {
            expandIconView.setVisibility(View.VISIBLE);
        } else {
            expandIconView.setVisibility(View.GONE);
        }
    }

    /**
     * collapse in milliseconds
     */
    public void collapse(int duration) {
        if (state == STATE_EXPANDED) {
            setState(STATE_PROCESSING);

            mLayoutBtnGroupMonth.setVisibility(View.GONE);
            mLayoutBtnGroupWeek.setVisibility(View.VISIBLE);
            mBtnPrevWeek.setClickable(false);
            mBtnNextWeek.setClickable(false);

            int index = getSuitableRowIndex();
            mCurrentWeekIndex = index;

            final int currentHeight = mInitHeight;
            final int targetHeight = mTableBody.getChildAt(index).getMeasuredHeight();
            int tempHeight = 0;
            for (int i = 0; i < index; i++) {
                tempHeight += mTableBody.getChildAt(i).getMeasuredHeight();
            }
            final int topHeight = tempHeight;

            Animation anim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    if (interpolatedTime == 1f)
                        mScrollViewBody.getLayoutParams().height = targetHeight;
                    else
                        mScrollViewBody.getLayoutParams().height = currentHeight - ((int) ((currentHeight - targetHeight) * interpolatedTime));
                    mScrollViewBody.requestLayout();

                    if (mScrollViewBody.getMeasuredHeight() < topHeight + targetHeight) {
                        int position = topHeight + targetHeight - mScrollViewBody.getMeasuredHeight();
                        mScrollViewBody.smoothScrollTo(0, position);
                    }

                    if (interpolatedTime == 1f) {
                        setState(UICalendar.STATE_COLLAPSED);
                        expanded = false;
                        mBtnPrevWeek.setClickable(true);
                        mBtnNextWeek.setClickable(true);
                    }
                }
            };
            anim.setDuration(duration);
            startAnimation(anim);
        }

        expandIconView.setState(ExpandIconView.MORE, true);
        reload();
    }

    private void collapseTo(int index) {
        if (state == STATE_COLLAPSED) {
            if (index == -1) {
                index = mTableBody.getChildCount() - 1;
            }
            mCurrentWeekIndex = index;
            int targetHeight = mTableBody.getChildAt(index).getMeasuredHeight();
            int tempHeight = 0;
            for (int i = 0; i < index; i++) {
                tempHeight += mTableBody.getChildAt(i).getMeasuredHeight();
            }
            final int topHeight = tempHeight;

            mScrollViewBody.getLayoutParams().height = targetHeight;
            mScrollViewBody.requestLayout();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mScrollViewBody.smoothScrollTo(0, topHeight);
                }
            });


            if (mListener != null) {
                mListener.onWeekChanged(mCurrentWeekIndex);
            }
        }
    }

    public void expand(int duration) {
        if (state == STATE_COLLAPSED) {
            setState(STATE_PROCESSING);

            mLayoutBtnGroupMonth.setVisibility(View.VISIBLE);
            mLayoutBtnGroupWeek.setVisibility(View.GONE);
            mBtnPrevMonth.setClickable(false);
            mBtnNextMonth.setClickable(false);

            final int currentHeight = mScrollViewBody.getMeasuredHeight();
            final int targetHeight = mInitHeight;

            Animation anim = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    if (interpolatedTime == 1f)
                        mScrollViewBody.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    else
                        mScrollViewBody.getLayoutParams().height = currentHeight - ((int) ((currentHeight - targetHeight) * interpolatedTime));
                    mScrollViewBody.requestLayout();

                    if (interpolatedTime == 1f) {
                        setState(STATE_EXPANDED);
                        expanded = true;
                        mBtnPrevMonth.setClickable(true);
                        mBtnNextMonth.setClickable(true);
                    }
                }
            };
            anim.setDuration(duration);
            startAnimation(anim);
        }

        expandIconView.setState(ExpandIconView.LESS, true);
        reload();
    }

    // callback
    public void setCalendarListener(CalendarListener listener) {
        mListener = listener;
    }

    public interface CalendarListener {
        // triggered when a day is selected programmatically or clicked by user.
        void onDaySelected();

        // triggered when the month are changed.
        void onMonthChanged();

        // triggered when the week position are changed.
        void onWeekChanged(int position);

        void onClicked();

        void onDayChanged();
    }

    public static class Params {
        int prevDays;
        int nextDaysBlocked;

        public Params(int prevDays, int nextDaysBlocked) {
            this.prevDays = prevDays;
            this.nextDaysBlocked = nextDaysBlocked;
        }
    }

}