package com.android.systemui.statusbar.notification;

import android.graphics.Matrix;
import kotlin.jvm.internal.Intrinsics;
import miui.util.Pools;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiActivityLaunchAnimator.kt */
public final class MiuiActivityLaunchAnimatorKt$MATRIX_POOL$1 extends Pools.Manager<Matrix> {
    MiuiActivityLaunchAnimatorKt$MATRIX_POOL$1() {
    }

    @NotNull
    public Matrix createInstance() {
        return new Matrix();
    }

    public void onAcquire(@NotNull Matrix matrix) {
        Intrinsics.checkParameterIsNotNull(matrix, "element");
        MiuiActivityLaunchAnimatorKt$MATRIX_POOL$1.super.onAcquire(matrix);
        matrix.reset();
    }
}
