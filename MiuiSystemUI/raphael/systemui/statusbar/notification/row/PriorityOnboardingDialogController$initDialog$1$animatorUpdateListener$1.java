package com.android.systemui.statusbar.notification.row;

import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: PriorityOnboardingDialogController.kt */
public final class PriorityOnboardingDialogController$initDialog$1$animatorUpdateListener$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ int $baseSize;
    final /* synthetic */ ImageView $mImportanceRingView;
    final /* synthetic */ GradientDrawable $ring;
    final /* synthetic */ int $ringColor;

    PriorityOnboardingDialogController$initDialog$1$animatorUpdateListener$1(GradientDrawable gradientDrawable, int i, int i2, ImageView imageView) {
        this.$ring = gradientDrawable;
        this.$ringColor = i;
        this.$baseSize = i2;
        this.$mImportanceRingView = imageView;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animation");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            int intValue = ((Integer) animatedValue).intValue();
            this.$ring.setStroke(intValue, this.$ringColor);
            int i = this.$baseSize + (intValue * 2);
            this.$ring.setSize(i, i);
            this.$mImportanceRingView.invalidate();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
    }
}
