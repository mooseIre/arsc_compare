package com.android.systemui.controls.ui;

import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.util.MathUtils;
import com.android.internal.graphics.ColorUtils;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$IntRef;

/* access modifiers changed from: package-private */
/* compiled from: ControlViewHolder.kt */
public final class ControlViewHolder$animateBackgroundChange$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ int $newBaseColor$inlined;
    final /* synthetic */ Ref$IntRef $newClipColor$inlined;
    final /* synthetic */ float $oldAlpha$inlined;
    final /* synthetic */ int $oldBaseColor$inlined;
    final /* synthetic */ int $oldColor$inlined;
    final /* synthetic */ GradientDrawable $this_apply$inlined;
    final /* synthetic */ ControlViewHolder this$0;

    ControlViewHolder$animateBackgroundChange$$inlined$apply$lambda$1(GradientDrawable gradientDrawable, int i, int i2, int i3, float f, ControlViewHolder controlViewHolder, int i4, Ref$IntRef ref$IntRef, boolean z, Ref$IntRef ref$IntRef2) {
        this.$this_apply$inlined = gradientDrawable;
        this.$oldColor$inlined = i;
        this.$oldBaseColor$inlined = i2;
        this.$newBaseColor$inlined = i3;
        this.$oldAlpha$inlined = f;
        this.this$0 = controlViewHolder;
        this.$newClipColor$inlined = ref$IntRef;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        GradientDrawable gradientDrawable = this.$this_apply$inlined;
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            gradientDrawable.setAlpha(((Integer) animatedValue).intValue());
            this.$this_apply$inlined.setColor(ColorUtils.blendARGB(this.$oldColor$inlined, this.$newClipColor$inlined.element, valueAnimator.getAnimatedFraction()));
            this.this$0.baseLayer.setColor(ColorUtils.blendARGB(this.$oldBaseColor$inlined, this.$newBaseColor$inlined, valueAnimator.getAnimatedFraction()));
            this.this$0.getLayout().setAlpha(MathUtils.lerp(this.$oldAlpha$inlined, 1.0f, valueAnimator.getAnimatedFraction()));
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
    }
}
