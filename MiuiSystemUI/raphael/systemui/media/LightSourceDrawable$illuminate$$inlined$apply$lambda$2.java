package com.android.systemui.media;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: LightSourceDrawable.kt */
final class LightSourceDrawable$illuminate$$inlined$apply$lambda$2 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ LightSourceDrawable this$0;

    LightSourceDrawable$illuminate$$inlined$apply$lambda$2(LightSourceDrawable lightSourceDrawable) {
        this.this$0 = lightSourceDrawable;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        RippleData access$getRippleData$p = this.this$0.rippleData;
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            access$getRippleData$p.setProgress(((Float) animatedValue).floatValue());
            this.this$0.invalidateSelf();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
