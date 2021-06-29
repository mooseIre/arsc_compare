package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$changeExpansion$2 extends AnimatorListenerAdapter {
    final /* synthetic */ float $targetHeight;
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$changeExpansion$2(MiuiNotificationPanelViewController miuiNotificationPanelViewController, float f) {
        this.this$0 = miuiNotificationPanelViewController;
        this.$targetHeight = f;
    }

    public void onAnimationCancel(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        this.this$0.setAnimatingHeight(this.$targetHeight);
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        this.this$0.setAnimatingHeight(this.$targetHeight);
    }
}
