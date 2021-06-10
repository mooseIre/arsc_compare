package com.android.systemui.statusbar.notification;

import android.graphics.Matrix;
import miui.util.Pools;
import miuix.animation.base.AnimConfig;
import miuix.animation.utils.EaseManager;

public final class MiuiActivityLaunchAnimatorKt {
    private static final Pools.SimplePool<Matrix> MATRIX_POOL = Pools.createSimplePool(new MiuiActivityLaunchAnimatorKt$MATRIX_POOL$1(), 5);

    public static final AnimConfig springEase(String str, float f, float f2) {
        return new AnimConfig().setSpecial(str, EaseManager.getStyle(-2, f, f2), new float[0]);
    }
}
