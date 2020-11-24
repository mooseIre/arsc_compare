package com.android.systemui.statusbar.phone;

import android.view.animation.Interpolator;

/* compiled from: MiuiKeyButtonRipple.kt */
final class SineInterpolator implements Interpolator {
    public float getInterpolation(float f) {
        return ((float) (((double) 1) - Math.cos(((double) f) * 3.141592653589793d))) / ((float) 2);
    }
}
