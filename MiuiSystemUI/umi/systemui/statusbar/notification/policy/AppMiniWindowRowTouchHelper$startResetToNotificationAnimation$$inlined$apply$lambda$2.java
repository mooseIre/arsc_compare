package com.android.systemui.statusbar.notification.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import org.jetbrains.annotations.Nullable;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    AppMiniWindowRowTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$2(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper) {
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        this.this$0.onMiniWindowReset();
    }
}
