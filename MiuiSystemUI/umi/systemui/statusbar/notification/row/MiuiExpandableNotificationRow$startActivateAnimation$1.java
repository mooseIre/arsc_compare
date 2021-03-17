package com.android.systemui.statusbar.notification.row;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiExpandableNotificationRow.kt */
public final class MiuiExpandableNotificationRow$startActivateAnimation$1 extends AnimatorListenerAdapter {
    final /* synthetic */ MiuiExpandableNotificationRow this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiExpandableNotificationRow$startActivateAnimation$1(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        this.this$0 = miuiExpandableNotificationRow;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        this.this$0.updateBackground();
        this.this$0.setTouchAnimatingState(false);
    }
}
