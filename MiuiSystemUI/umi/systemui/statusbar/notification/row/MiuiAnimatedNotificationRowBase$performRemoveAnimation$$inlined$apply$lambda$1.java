package com.android.systemui.statusbar.notification.row;

import android.animation.AnimatorListenerAdapter;
import miuix.animation.listener.TransitionListener;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiAnimatedNotificationRowBase.kt */
public final class MiuiAnimatedNotificationRowBase$performRemoveAnimation$$inlined$apply$lambda$1 extends TransitionListener {
    final /* synthetic */ Runnable $onFinishedRunnable$inlined;

    MiuiAnimatedNotificationRowBase$performRemoveAnimation$$inlined$apply$lambda$1(AnimatorListenerAdapter animatorListenerAdapter, Runnable runnable) {
        this.$onFinishedRunnable$inlined = runnable;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onComplete(@Nullable Object obj) {
        Runnable runnable = this.$onFinishedRunnable$inlined;
        if (runnable != null) {
            runnable.run();
        }
    }
}
