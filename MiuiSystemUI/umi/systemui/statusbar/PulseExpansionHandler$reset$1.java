package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PulseExpansionHandler.kt */
public final class PulseExpansionHandler$reset$1 extends AnimatorListenerAdapter {
    final /* synthetic */ ExpandableView $child;
    final /* synthetic */ PulseExpansionHandler this$0;

    PulseExpansionHandler$reset$1(PulseExpansionHandler pulseExpansionHandler, ExpandableView expandableView) {
        this.this$0 = pulseExpansionHandler;
        this.$child = expandableView;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        this.this$0.setUserLocked(this.$child, false);
    }
}
