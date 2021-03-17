package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import org.jetbrains.annotations.Nullable;

/* compiled from: ToggleRangeBehavior.kt */
public final class ToggleRangeBehavior$updateRange$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ ToggleRangeBehavior this$0;

    ToggleRangeBehavior$updateRange$$inlined$apply$lambda$2(ToggleRangeBehavior toggleRangeBehavior) {
        this.this$0 = toggleRangeBehavior;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        this.this$0.rangeAnimator = null;
    }
}
