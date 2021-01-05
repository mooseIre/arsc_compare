package com.android.systemui.doze.util;

import android.util.MathUtils;

/* compiled from: BurnInHelper.kt */
public final class BurnInHelperKt {
    public static final int getBurnInOffset(int i, boolean z) {
        return (int) zigzag(((float) System.currentTimeMillis()) / 60000.0f, (float) i, z ? 83.0f : 521.0f);
    }

    private static final float zigzag(float f, float f2, float f3) {
        float f4 = (float) 2;
        float f5 = (f % f3) / (f3 / f4);
        if (f5 > ((float) 1)) {
            f5 = f4 - f5;
        }
        return MathUtils.lerp(0.0f, f2, f5);
    }
}
