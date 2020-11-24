package com.android.systemui.statusbar.phone;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiHeadsUpTouchHelper.kt */
final class MiuiHeadsUpTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ MiuiHeadsUpTouchHelper this$0;

    MiuiHeadsUpTouchHelper$startResetToNotificationAnimation$$inlined$apply$lambda$1(MiuiHeadsUpTouchHelper miuiHeadsUpTouchHelper) {
        this.this$0 = miuiHeadsUpTouchHelper;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            int intValue = ((Integer) animatedValue).intValue();
            this.this$0.mExpandedParams.setAlpha(((float) 1) - RangesKt___RangesKt.coerceIn(((float) ((intValue - this.this$0.mExpandedParams.getStartHeight()) - this.this$0.mExpandedParams.getTop())) / this.this$0.mMaxTriggerThreshold, 0.0f, 1.0f));
            this.this$0.mExpandedParams.setBottom(intValue);
            this.this$0.onExpandedParamsUpdated();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
    }
}
