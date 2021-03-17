package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Trace;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiKeyButtonRipple.kt */
final class TraceAnimatorListener extends AnimatorListenerAdapter {
    private final String mName;

    public TraceAnimatorListener(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "mName");
        this.mName = str;
    }

    public void onAnimationStart(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        Trace.beginSection("KeyButtonRipple.start." + this.mName);
        Trace.endSection();
    }

    public void onAnimationCancel(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        Trace.beginSection("KeyButtonRipple.cancel." + this.mName);
        Trace.endSection();
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        Trace.beginSection("KeyButtonRipple.end." + this.mName);
        Trace.endSection();
    }
}
