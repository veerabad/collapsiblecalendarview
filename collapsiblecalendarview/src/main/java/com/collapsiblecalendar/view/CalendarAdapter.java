package com.collapsiblecalendar.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.collapsiblecalendar.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter {
    private int mFirstDayOfWeek = 0;
    private Calendar calendar;
    private Context context;

    private List<Day> mItemList = new ArrayList<>();
    private List<View> mViewList = new ArrayList<>();
    private List<Event> mEventList = new ArrayList<>();

    public CalendarAdapter(Context context, Calendar cal) {
        this.context = context;
        this.calendar = cal == null ? Calendar.getInstance() : (Calendar) cal.clone();
        this.calendar.set(Calendar.DAY_OF_MONTH, 1);
        refresh();
    }

    // public methods
    public Calendar getCalendar() {
        return calendar;
    }

    public int getCount() {
        return mItemList.size();
    }

    public Day getItem(int position) {
        return mItemList.get(position);
    }

    public View getView(int position) {
        return mViewList.get(position);
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        mFirstDayOfWeek = firstDayOfWeek;
    }

    public void addEvent(Event event) {
        mEventList.add(event);
    }

    public List<Event> getEventList() {
        return mEventList;
    }

    public void setEventList(List<Event> mEventList) {
        this.mEventList = mEventList;
    }

    public void refresh() {
        // clear data
        mItemList.clear();
        mViewList.clear();

        // set calendar
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        calendar.set(year, month, 1);

        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // generate day list
        int offset = 0 - (firstDayOfWeek - mFirstDayOfWeek) + 1;
        int length = (int) Math.ceil((double) ((float) (lastDayOfMonth - offset + 1) / 7)) * 7;
        for (int i = offset; i < length + offset; i++) {
            int numYear;
            int numMonth;
            int numDay;

            Calendar tempCal = Calendar.getInstance();
            if (i <= 0) { // prev month
                if (month == 0) {
                    numYear = year - 1;
                    numMonth = 11;
                } else {
                    numYear = year;
                    numMonth = month - 1;
                }
                tempCal.set(numYear, numMonth, 1);
                numDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH) + i;
            } else if (i > lastDayOfMonth) { // next month
                if (month == 11) {
                    numYear = year + 1;
                    numMonth = 0;
                } else {
                    numYear = year;
                    numMonth = month + 1;
                }
                tempCal.set(numYear, numMonth, 1);
                numDay = i - lastDayOfMonth;
            } else {
                numYear = year;
                numMonth = month;
                numDay = i;
            }

            Day day = new Day(numYear, numMonth, numDay);

            View view = LayoutInflater.from(context).inflate(R.layout.day_layout, null);
            TextView txtDay = view.findViewById(R.id.txt_day);
            ImageView imgEventTag = view.findViewById(R.id.img_event_tag);

            txtDay.setText(day.getDay() + "");
            if (day.getMonth() != calendar.get(Calendar.MONTH)) {
                txtDay.setAlpha(0.3f);
            }

            for (int j = 0; j < mEventList.size(); j++) {
                Event event = mEventList.get(j);
                if (day.getYear() == event.getYear()
                        && day.getMonth() == event.getMonth()
                        && day.getDay() == event.getDay()) {
                    imgEventTag.setVisibility(View.VISIBLE);
                    if (event.getDrawable() == null)
                        imgEventTag.setColorFilter(event.getColor(), PorterDuff.Mode.SRC_ATOP);
                    else imgEventTag.setImageDrawable(event.getDrawable());
                }
            }

            mItemList.add(day);
            mViewList.add(view);
        }
    }
}
