package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import com.android.systemui.plugins.qs.QS;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$endNssCoveringQsMotion$2 implements Animator.AnimatorListener {
    final /* synthetic */ boolean $toCover;
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    public void onAnimationRepeat(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
    }

    public void onAnimationStart(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
    }

    MiuiNotificationPanelViewController$endNssCoveringQsMotion$2(MiuiNotificationPanelViewController miuiNotificationPanelViewController, boolean z) {
        this.this$0 = miuiNotificationPanelViewController;
        this.$toCover = z;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        float f;
        Intrinsics.checkParameterIsNotNull(animator, "animator");
        ValueAnimator valueAnimator = this.this$0.mQsTopPaddingAnimator;
        if (valueAnimator != null) {
            valueAnimator.removeListener(this);
        }
        this.this$0.mQsTopPaddingAnimator = null;
        this.this$0.mNssCoveringQs = false;
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
        if (this.$toCover) {
            QS qs = miuiNotificationPanelViewController.mQs;
            Intrinsics.checkExpressionValueIsNotNull(qs, "mQs");
            View header = qs.getHeader();
            Intrinsics.checkExpressionValueIsNotNull(header, "mQs.header");
            f = (float) header.getHeight();
        } else {
            f = MiuiNotificationPanelViewController$endNssCoveringQsMotion$2.super.calculateQsTopPadding();
        }
        miuiNotificationPanelViewController.updateScrollerTopPadding(f);
    }

    public void onAnimationCancel(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
        ValueAnimator valueAnimator = this.this$0.mQsTopPaddingAnimator;
        if (valueAnimator != null) {
            valueAnimator.removeListener(this);
        }
        this.this$0.mQsTopPaddingAnimator = null;
    }
}
