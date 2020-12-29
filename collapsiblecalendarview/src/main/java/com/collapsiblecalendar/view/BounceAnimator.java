package com.collapsiblecalendar.view;

public class BounceAnimator implements android.view.animation.Interpolator {
    private double mAmplitude = 1.0;
    private double mFrequency = 10.0;

    public BounceAnimator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    @Override
    public float getInterpolation(float time) {
        return (float) (-1.0 * Math.pow(Math.E, -time / mAmplitude) * Math.cos(mFrequency * time) + 1);
    }
}