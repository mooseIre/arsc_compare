package com.android.systemui.statusbar.phone;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$changeExpansion$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$changeExpansion$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animation");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            this.this$0.setAnimatingHeight(((Float) animatedValue).floatValue());
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
