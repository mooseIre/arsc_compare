package com.android.systemui.statusbar.notification.row;

import android.animation.ValueAnimator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: ChannelEditorListView.kt */
public final class ChannelRow$playHighlight$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ ChannelRow this$0;

    ChannelRow$playHighlight$1(ChannelRow channelRow) {
        this.this$0 = channelRow;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        ChannelRow channelRow = this.this$0;
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animator");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            channelRow.setBackgroundColor(((Integer) animatedValue).intValue());
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
    }
}
