package com.android.systemui.statusbar.phone;

import android.graphics.drawable.Drawable;

public class SignalDrawable extends Drawable {
    public static int getAirplaneModeState(int i) {
        return (i << 8) | 262144;
    }

    public static int getCarrierChangeState(int i) {
        return (i << 8) | 196608;
    }

    public static int getEmptyState(int i) {
        return (i << 8) | 65536;
    }

    public static int getState(int i, int i2, boolean z) {
        return i | (i2 << 8) | ((z ? 2 : 0) << 16);
    }

    static {
        Math.tan(0.39269908169872414d);
    }
}
