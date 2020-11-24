package com.android.systemui.statusbar.phone;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiHeadsUpTouchHelper.kt */
final class MiuiHeadsUpTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ MiuiHeadsUpTouchHelper this$0;

    MiuiHeadsUpTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$1(MiuiHeadsUpTouchHelper miuiHeadsUpTouchHelper) {
        this.this$0 = miuiHeadsUpTouchHelper;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            float floatValue = ((Float) animatedValue).floatValue();
            this.this$0.mExpandedParams.setIconAlpha(floatValue);
            this.this$0.mExpandedParams.setBackgroundAlpha(floatValue);
            this.this$0.onExpandedParamsUpdated();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
