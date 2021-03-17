package com.android.keyguard;

import android.animation.TimeInterpolator;

public class Ease$Sine {
    public static final TimeInterpolator easeInOut = new TimeInterpolator() {
        /* class com.android.keyguard.Ease$Sine.AnonymousClass3 */

        public float getInterpolation(float f) {
            return ((((float) Math.cos((((double) f) * 3.141592653589793d) / 1.0d)) - 1.0f) * -0.5f) + 0.0f;
        }
    };
}
