package com.android.systemui.media;

import android.animation.ValueAnimator;
import com.android.internal.graphics.ColorUtils;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: IlluminationDrawable.kt */
final class IlluminationDrawable$animateBackground$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ int $finalHighlight$inlined;
    final /* synthetic */ int $initialBackground$inlined;
    final /* synthetic */ int $initialHighlight$inlined;
    final /* synthetic */ IlluminationDrawable this$0;

    IlluminationDrawable$animateBackground$$inlined$apply$lambda$1(IlluminationDrawable illuminationDrawable, int i, int i2, int i3) {
        this.this$0 = illuminationDrawable;
        this.$initialBackground$inlined = i;
        this.$initialHighlight$inlined = i2;
        this.$finalHighlight$inlined = i3;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            float floatValue = ((Float) animatedValue).floatValue();
            this.this$0.paint.setColor(ColorUtils.blendARGB(this.$initialBackground$inlined, this.this$0.backgroundColor, floatValue));
            this.this$0.highlightColor = ColorUtils.blendARGB(this.$initialHighlight$inlined, this.$finalHighlight$inlined, floatValue);
            for (LightSourceDrawable highlightColor : this.this$0.lightSources) {
                highlightColor.setHighlightColor(this.this$0.highlightColor);
            }
            this.this$0.invalidateSelf();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
