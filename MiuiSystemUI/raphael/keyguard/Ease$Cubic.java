package com.android.keyguard;

import android.animation.TimeInterpolator;

public class Ease$Cubic {
    public static final TimeInterpolator easeInOut = new TimeInterpolator() {
        /* class com.android.keyguard.Ease$Cubic.AnonymousClass3 */

        public float getInterpolation(float f) {
            float f2 = f / 0.5f;
            if (f2 < 1.0f) {
                return (0.5f * f2 * f2 * f2) + 0.0f;
            }
            float f3 = f2 - 2.0f;
            return (((f3 * f3 * f3) + 2.0f) * 0.5f) + 0.0f;
        }
    };
}
