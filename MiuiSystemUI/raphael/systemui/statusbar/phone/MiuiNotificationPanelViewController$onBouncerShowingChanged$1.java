package com.android.systemui.statusbar.phone;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$onBouncerShowingChanged$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$onBouncerShowingChanged$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public final void onAnimationUpdate(@NotNull ValueAnimator valueAnimator) {
        Intrinsics.checkParameterIsNotNull(valueAnimator, "anim");
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            miuiNotificationPanelViewController.setBouncerShowingFraction(((Float) animatedValue).floatValue());
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
