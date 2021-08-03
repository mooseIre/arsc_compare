package com.android.systemui.statusbar.notification.policy;

import android.animation.ValueAnimator;
import android.graphics.Rect;
import com.android.systemui.statusbar.notification.MiniWindowExpandParameters;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
final class AppMiniWindowRowTouchHelper$startEnterMiniWindowAnimation$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
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
            MiniWindowExpandParameters access$getMExpandedParams$p = AppMiniWindowRowTouchHelper.access$getMExpandedParams$p(this.this$0);
            int i = this.$from$inlined.left;
            access$getMExpandedParams$p.setLeft((int) (((float) i) + (((float) (this.$to$inlined.left - i)) * floatValue)));
            int i2 = this.$from$inlined.top;
            access$getMExpandedParams$p.setTop((int) (((float) i2) + (((float) (this.$to$inlined.top - i2)) * floatValue)));
            int i3 = this.$from$inlined.right;
            access$getMExpandedParams$p.setRight((int) (((float) i3) + (((float) (this.$to$inlined.right - i3)) * floatValue)));
            int i4 = this.$from$inlined.bottom;
            access$getMExpandedParams$p.setBottom((int) (((float) i4) + (((float) (this.$to$inlined.bottom - i4)) * floatValue)));
            AppMiniWindowRowTouchHelper.access$onExpandedParamsUpdated(this.this$0);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
