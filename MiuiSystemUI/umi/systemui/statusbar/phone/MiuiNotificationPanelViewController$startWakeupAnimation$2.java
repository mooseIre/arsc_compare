package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$startWakeupAnimation$2 extends AnimatorListenerAdapter {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiNotificationPanelViewController$startWakeupAnimation$2(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public void onAnimationStart(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
        if (this.this$0.wakefulnessLifecycle.getWakefulness() == 1 || this.this$0.wakefulnessLifecycle.getWakefulness() == 2) {
            this.this$0.mKeyguardPanelViewInjector.setVisibility(0);
        }
    }
}
