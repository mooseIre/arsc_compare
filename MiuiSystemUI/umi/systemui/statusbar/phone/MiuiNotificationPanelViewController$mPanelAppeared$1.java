package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$mPanelAppeared$1 implements Animator.AnimatorListener {
    final /* synthetic */ boolean $value;
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    public void onAnimationCancel(@Nullable Animator animator) {
    }

    public void onAnimationRepeat(@Nullable Animator animator) {
    }

    public void onAnimationStart(@Nullable Animator animator) {
    }

    MiuiNotificationPanelViewController$mPanelAppeared$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController, boolean z) {
        this.this$0 = miuiNotificationPanelViewController;
        this.$value = z;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        boolean z = this.$value;
        if (z) {
            this.this$0.mQs.setListening(z);
        }
    }
}
