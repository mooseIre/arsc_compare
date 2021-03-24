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
            IlluminationDrawable.access$getPaint$p(this.this$0).setColor(ColorUtils.blendARGB(this.$initialBackground$inlined, IlluminationDrawable.access$getBackgroundColor$p(this.this$0), floatValue));
            IlluminationDrawable.access$setHighlightColor$p(this.this$0, ColorUtils.blendARGB(this.$initialHighlight$inlined, this.$finalHighlight$inlined, floatValue));
            for (LightSourceDrawable lightSourceDrawable : IlluminationDrawable.access$getLightSources$p(this.this$0)) {
                lightSourceDrawable.setHighlightColor(IlluminationDrawable.access$getHighlightColor$p(this.this$0));
            }
            this.this$0.invalidateSelf();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
