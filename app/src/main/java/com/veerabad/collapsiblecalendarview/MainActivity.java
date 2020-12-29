package com.veerabad.collapsiblecalendarview;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amulyakhare.textdrawable.TextDrawable;
import com.collapsiblecalendar.view.Day;
import com.collapsiblecalendar.widget.CollapsibleCalendarView;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    CollapsibleCalendarView collapsibleCalendar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScrollView relativeLayout = findViewById(R.id.scrollView);
        final TextView textView = findViewById(R.id.tv_date);

        collapsibleCalendar = findViewById(R.id.collapsibleCalendarView);

        //To hide or show expand icon
        collapsibleCalendar.setExpandIconVisible(true);
        Calendar today = Calendar.getInstance();
        TextDrawable.IBuilder mDrawableBuilder = TextDrawable.builder().roundRect(5);

        collapsibleCalendar.addEventTag(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), Color.RED);
        today.add(Calendar.DATE, 1);
        collapsibleCalendar.setSelectedDay(new Day(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)));
        collapsibleCalendar.addEventTag(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), mDrawableBuilder.build("12", Color.RED));
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendarView.CalendarListener() {
            @Override
            public void onDaySelected() {
                textView.setText(collapsibleCalendar.getSelectedDay().toString() + "");
            }

            @Override
            public void onMonthChanged() {

            }

            @Override
            public void onWeekChanged(int position) {

            }

            @Override
            public void onClicked() {
                if (collapsibleCalendar.isExpanded()) {
                    collapsibleCalendar.collapse(400);
                } else {
                    collapsibleCalendar.expand(400);
                }
            }

            @Override
            public void onDayChanged() {

            }
        });
    }
}
