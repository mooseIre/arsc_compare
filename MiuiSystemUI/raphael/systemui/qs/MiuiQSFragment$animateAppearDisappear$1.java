package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment$animateAppearDisappear$1 extends AnimatorListenerAdapter {
    final /* synthetic */ MiuiQSFragment this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiQSFragment$animateAppearDisappear$1(MiuiQSFragment miuiQSFragment) {
        this.this$0 = miuiQSFragment;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        if (!(this.this$0.mAppeared)) {
            this.this$0.setListening(false);
        }
        this.this$0.headerAnimating = false;
    }
}
