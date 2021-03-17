package com.android.systemui.statusbar.phone;

import android.view.animation.Interpolator;

/* compiled from: MiuiKeyButtonRipple.kt */
final class LogInterpolator implements Interpolator {
    public float getInterpolation(float f) {
        return ((float) 1) - ((float) Math.pow(400.0d, ((double) (-f)) * 1.4d));
    }
}
