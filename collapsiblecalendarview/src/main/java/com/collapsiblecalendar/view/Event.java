package com.collapsiblecalendar.view;

import android.graphics.drawable.Drawable;

public class Event {
    private int mYear;
    private int mMonth;
    private int mDay;
    private int mColor;
    private Drawable drawable;

    public Event(int year, int month, int day) {
        this.mYear = year;
        this.mMonth = month;
        this.mDay = day;
    }

    public Event(int year, int month, int day, int color) {
        this.mYear = year;
        this.mMonth = month;
        this.mDay = day;
        this.mColor = color;
    }

    public Event(int year, int month, int day, Drawable drawable) {
        this.mYear = year;
        this.mMonth = month;
        this.mDay = day;
        this.drawable = drawable;
    }

    public int getMonth() {
        return mMonth;
    }

    public int getYear() {
        return mYear;
    }

    public int getDay() {
        return mDay;
    }

    public int getColor() {
        return mColor;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
