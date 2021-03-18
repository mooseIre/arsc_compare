package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$onStartedGoingToSleep$1 extends AnimatorListenerAdapter {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiNotificationPanelViewController$onStartedGoingToSleep$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
        this.this$0.mKeyguardPanelViewInjector.setVisibility(4);
    }
}
