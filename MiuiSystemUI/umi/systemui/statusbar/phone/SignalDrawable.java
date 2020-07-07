package com.android.systemui.statusbar.phone;

import android.graphics.drawable.Drawable;

public class SignalDrawable extends Drawable {
    private static final float[] FIT = {2.26f, -3.02f, 1.76f};
    private static final float INV_TAN = (1.0f / ((float) Math.tan(0.39269908169872414d)));
    private static float[][] X_PATH = {new float[]{0.91249996f, 0.7083333f}, new float[]{-0.045833334f, -0.045833334f}, new float[]{-0.079166666f, 0.079166666f}, new float[]{-0.079166666f, -0.079166666f}, new float[]{-0.045833334f, 0.045833334f}, new float[]{0.079166666f, 0.079166666f}, new float[]{-0.079166666f, 0.079166666f}, new float[]{0.045833334f, 0.045833334f}, new float[]{0.079166666f, -0.079166666f}, new float[]{0.079166666f, 0.079166666f}, new float[]{0.045833334f, -0.045833334f}, new float[]{-0.079166666f, -0.079166666f}};

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
}
