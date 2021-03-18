package com.android.systemui.statusbar.notification;

import android.graphics.Matrix;
import miui.util.Pools;
import miuix.animation.base.AnimConfig;

public final class MiuiActivityLaunchAnimatorKt {
    private static final Pools.SimplePool<Matrix> MATRIX_POOL = Pools.createSimplePool(new MiuiActivityLaunchAnimatorKt$MATRIX_POOL$1(), 5);

    public static final AnimConfig springEase(float f, float f2) {
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, f, f2);
        return animConfig;
    }
}
