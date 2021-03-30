package com.android.systemui.statusbar.phone;

import android.view.animation.Interpolator;

/* access modifiers changed from: package-private */
/* compiled from: MiuiKeyButtonRipple.kt */
public final class SineInterpolator implements Interpolator {
    public float getInterpolation(float f) {
        return ((float) (((double) 1) - Math.cos(((double) f) * 3.141592653589793d))) / ((float) 2);
    }
}
