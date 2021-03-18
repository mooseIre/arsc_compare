package com.android.systemui.statusbar;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: PulseExpansionHandler.kt */
public final class PulseExpansionHandler$resetClock$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ PulseExpansionHandler this$0;

    PulseExpansionHandler$resetClock$1(PulseExpansionHandler pulseExpansionHandler) {
        this.this$0 = pulseExpansionHandler;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        PulseExpansionHandler pulseExpansionHandler = this.this$0;
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animation");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            pulseExpansionHandler.setEmptyDragAmount(((Float) animatedValue).floatValue());
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
