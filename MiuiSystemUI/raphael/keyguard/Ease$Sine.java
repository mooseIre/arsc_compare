package com.android.keyguard;

import android.animation.TimeInterpolator;

public class Ease$Sine {
    public static final TimeInterpolator easeIn = new TimeInterpolator() {
        public float getInterpolation(float f) {
            return (((float) Math.cos(((double) (f / 1.0f)) * 1.5707963267948966d)) * -1.0f) + 1.0f + 0.0f;
        }
    };
    public static final TimeInterpolator easeInOut = new TimeInterpolator() {
        public float getInterpolation(float f) {
            return ((((float) Math.cos((((double) f) * 3.141592653589793d) / 1.0d)) - 1.0f) * -0.5f) + 0.0f;
        }
    };
    public static final TimeInterpolator easeOut = new TimeInterpolator() {
        public float getInterpolation(float f) {
            return (((float) Math.sin(((double) (f / 1.0f)) * 1.5707963267948966d)) * 1.0f) + 0.0f;
        }
    };
}
