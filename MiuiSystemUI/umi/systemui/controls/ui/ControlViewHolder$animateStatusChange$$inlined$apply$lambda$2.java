package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlViewHolder.kt */
public final class ControlViewHolder$animateStatusChange$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ ControlViewHolder this$0;

    ControlViewHolder$animateStatusChange$$inlined$apply$lambda$2(ControlViewHolder controlViewHolder, ObjectAnimator objectAnimator, ObjectAnimator objectAnimator2) {
        this.this$0 = controlViewHolder;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        this.this$0.status.setAlpha(1.0f);
        this.this$0.statusAnimator = null;
    }
}
