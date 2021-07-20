package com.android.systemui.statusbar.notification.policy;

import android.animation.ValueAnimator;
import android.graphics.Rect;
import com.android.systemui.statusbar.notification.MiniWindowExpandParameters;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper$startEnterMiniWindowAnimation$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ Rect $from$inlined;
    final /* synthetic */ Rect $to$inlined;
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    AppMiniWindowRowTouchHelper$startEnterMiniWindowAnimation$$inlined$apply$lambda$1(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper, Rect rect, Rect rect2) {
        this.this$0 = appMiniWindowRowTouchHelper;
        this.$from$inlined = rect;
        this.$to$inlined = rect2;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            float floatValue = ((Float) animatedValue).floatValue();
            MiniWindowExpandParameters miniWindowExpandParameters = this.this$0.mExpandedParams;
            int i = this.$from$inlined.left;
            miniWindowExpandParameters.setLeft((int) (((float) i) + (((float) (this.$to$inlined.left - i)) * floatValue)));
            int i2 = this.$from$inlined.top;
            miniWindowExpandParameters.setTop((int) (((float) i2) + (((float) (this.$to$inlined.top - i2)) * floatValue)));
            int i3 = this.$from$inlined.right;
            miniWindowExpandParameters.setRight((int) (((float) i3) + (((float) (this.$to$inlined.right - i3)) * floatValue)));
            int i4 = this.$from$inlined.bottom;
            miniWindowExpandParameters.setBottom((int) (((float) i4) + (((float) (this.$to$inlined.bottom - i4)) * floatValue)));
            this.this$0.onExpandedParamsUpdated();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
