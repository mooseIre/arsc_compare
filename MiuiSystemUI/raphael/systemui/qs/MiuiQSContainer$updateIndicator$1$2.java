package com.android.systemui.qs;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: MiuiQSContainer.kt */
public final class MiuiQSContainer$updateIndicator$1$2 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ IndicatorDrawable $drawable;

    MiuiQSContainer$updateIndicator$1$2(IndicatorDrawable indicatorDrawable) {
        this.$drawable = indicatorDrawable;
    }

    public final void onAnimationUpdate(@NotNull ValueAnimator valueAnimator) {
        Intrinsics.checkParameterIsNotNull(valueAnimator, "animator");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            this.$drawable.setCaretProgress(((Float) animatedValue).floatValue());
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
