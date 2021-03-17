package com.android.systemui.controls.ui;

import android.animation.ValueAnimator;
import android.graphics.drawable.ClipDrawable;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: ToggleRangeBehavior.kt */
public final class ToggleRangeBehavior$updateRange$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ ToggleRangeBehavior this$0;

    ToggleRangeBehavior$updateRange$$inlined$apply$lambda$1(ToggleRangeBehavior toggleRangeBehavior) {
        this.this$0 = toggleRangeBehavior;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        ClipDrawable clipLayer = this.this$0.getCvh().getClipLayer();
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            clipLayer.setLevel(((Integer) animatedValue).intValue());
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
    }
}
