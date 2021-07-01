package com.android.systemui.statusbar.notification.policy;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
final class AppMiniWindowRowTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    AppMiniWindowRowTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$1(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper) {
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            int intValue = ((Integer) animatedValue).intValue();
            AppMiniWindowRowTouchHelper.access$getMExpandedParams$p(this.this$0).setAlpha(((float) 1) - RangesKt___RangesKt.coerceIn(((float) ((intValue - AppMiniWindowRowTouchHelper.access$getMExpandedParams$p(this.this$0).getStartHeight()) - AppMiniWindowRowTouchHelper.access$getMExpandedParams$p(this.this$0).getTop())) / this.this$0.mMaxTriggerThreshold, 0.0f, 1.0f));
            AppMiniWindowRowTouchHelper.access$getMExpandedParams$p(this.this$0).setBottom(intValue);
            AppMiniWindowRowTouchHelper.access$onExpandedParamsUpdated(this.this$0);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
    }
}
