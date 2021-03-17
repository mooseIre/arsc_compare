package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator;

/* compiled from: MiuiActivityLaunchAnimator.kt */
final class MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationCancelled$1 implements Runnable {
    final /* synthetic */ MiuiActivityLaunchAnimator.HeadsUpNotificationAnimationRunner this$0;

    MiuiActivityLaunchAnimator$HeadsUpNotificationAnimationRunner$onAnimationCancelled$1(MiuiActivityLaunchAnimator.HeadsUpNotificationAnimationRunner headsUpNotificationAnimationRunner) {
        this.this$0 = headsUpNotificationAnimationRunner;
    }

    public final void run() {
        this.this$0.this$0.setAnimationPending(false);
        MiuiActivityLaunchAnimator.access$getMCallback$p(this.this$0.this$0).onLaunchAnimationCancelled();
    }
}
