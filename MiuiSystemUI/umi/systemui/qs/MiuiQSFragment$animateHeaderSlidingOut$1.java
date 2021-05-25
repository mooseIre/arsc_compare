package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment$animateHeaderSlidingOut$1 extends AnimatorListenerAdapter {
    final /* synthetic */ MiuiQSFragment this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiQSFragment$animateHeaderSlidingOut$1(MiuiQSFragment miuiQSFragment) {
        this.this$0 = miuiQSFragment;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        if (this.this$0.getView() != null) {
            View view = this.this$0.getView();
            if (view != null) {
                view.animate().setListener(null);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        this.this$0.headerAnimating = false;
        this.this$0.updateQsState();
    }
}
