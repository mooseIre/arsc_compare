package com.android.keyguard;

import android.animation.TimeInterpolator;

public class Ease$Quint {
    public static final TimeInterpolator easeOut = new TimeInterpolator() {
        /* class com.android.keyguard.Ease$Quint.AnonymousClass2 */

        public float getInterpolation(float f) {
            float f2 = (f / 1.0f) - 1.0f;
            return (((f2 * f2 * f2 * f2 * f2) + 1.0f) * 1.0f) + 0.0f;
        }
    };
}
