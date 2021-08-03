package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.ViewPropertyAnimator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment$animateAppearDisappear$1 extends AnimatorListenerAdapter {
    final /* synthetic */ ViewPropertyAnimator $animator;
    final /* synthetic */ MiuiQSFragment this$0;

    MiuiQSFragment$animateAppearDisappear$1(MiuiQSFragment miuiQSFragment, ViewPropertyAnimator viewPropertyAnimator) {
        this.this$0 = miuiQSFragment;
        this.$animator = viewPropertyAnimator;
    }

    public void onAnimationCancel(@Nullable Animator animator) {
        this.$animator.setListener(null);
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        if (!(this.this$0.mAppeared)) {
            this.this$0.setListening(false);
        }
        this.this$0.headerAnimating = false;
        this.$animator.setListener(null);
    }
}
