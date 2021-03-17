package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.GradientDrawable;
import kotlin.jvm.internal.Ref$IntRef;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlViewHolder.kt */
public final class ControlViewHolder$animateBackgroundChange$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ ControlViewHolder this$0;

    ControlViewHolder$animateBackgroundChange$$inlined$apply$lambda$2(GradientDrawable gradientDrawable, int i, int i2, int i3, float f, ControlViewHolder controlViewHolder, int i4, Ref$IntRef ref$IntRef, boolean z, Ref$IntRef ref$IntRef2) {
        this.this$0 = controlViewHolder;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        this.this$0.stateAnimator = null;
    }
}
