package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiHeadsUpTouchHelper.kt */
public final class MiuiHeadsUpTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ MiuiHeadsUpTouchHelper this$0;

    MiuiHeadsUpTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$2(MiuiHeadsUpTouchHelper miuiHeadsUpTouchHelper) {
        this.this$0 = miuiHeadsUpTouchHelper;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        this.this$0.onMiniWindowReset();
    }
}
