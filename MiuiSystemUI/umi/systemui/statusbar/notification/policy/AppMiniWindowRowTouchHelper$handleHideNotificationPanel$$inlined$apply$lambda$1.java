package com.android.systemui.statusbar.notification.policy;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
final class AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$1(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper) {
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            float floatValue = ((Float) animatedValue).floatValue();
            AppMiniWindowRowTouchHelper.access$getMExpandedParams$p(this.this$0).setIconAlpha(floatValue);
            AppMiniWindowRowTouchHelper.access$getMExpandedParams$p(this.this$0).setBackgroundAlpha(AppMiniWindowRowTouchHelper.access$getMExpandedParams$p(this.this$0).getBackgroundAlpha() * floatValue);
            AppMiniWindowRowTouchHelper.access$onExpandedParamsUpdated(this.this$0);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
