package com.android.systemui.media;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: LightSourceDrawable.kt */
public final class LightSourceDrawable$illuminate$$inlined$apply$lambda$2 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ LightSourceDrawable this$0;

    LightSourceDrawable$illuminate$$inlined$apply$lambda$2(LightSourceDrawable lightSourceDrawable) {
        this.this$0 = lightSourceDrawable;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        RippleData rippleData = this.this$0.rippleData;
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            rippleData.setProgress(((Float) animatedValue).floatValue());
            this.this$0.invalidateSelf();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
