package com.android.systemui.statusbar.notification.stack;

import android.animation.ValueAnimator;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationBackgroundView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;

/* compiled from: MiuiNotificationChildrenContainer.kt */
final class MiuiNotificationChildrenContainer$startBackgroundAnimation$4 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ float $begin;
    final /* synthetic */ float $end;
    final /* synthetic */ NotificationBackgroundView $summaryBackground;
    final /* synthetic */ float $yTranslation;
    final /* synthetic */ MiuiNotificationChildrenContainer this$0;

    MiuiNotificationChildrenContainer$startBackgroundAnimation$4(MiuiNotificationChildrenContainer miuiNotificationChildrenContainer, float f, float f2, NotificationBackgroundView notificationBackgroundView, float f3) {
        this.this$0 = miuiNotificationChildrenContainer;
        this.$begin = f;
        this.$end = f2;
        this.$summaryBackground = notificationBackgroundView;
        this.$yTranslation = f3;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "valueAnimator");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            float floatValue = ((Float) animatedValue).floatValue();
            float f = this.$begin;
            this.$summaryBackground.setActualHeight((int) (f + ((this.$end - f) * floatValue)));
            this.$summaryBackground.setVisibility(0);
            float f2 = ((float) 1) - floatValue;
            this.$summaryBackground.setTranslationY(this.$yTranslation * f2);
            this.$summaryBackground.setAlpha(1.0f);
            for (int i = RangesKt___RangesKt.coerceAtMost((this.this$0.getMaxAllowedVisibleChildren() - 1) + 1, this.this$0.mAttachedChildren.size() - 1); i >= 1; i--) {
                ExpandableNotificationRow expandableNotificationRow = this.this$0.mAttachedChildren.get(i);
                if (expandableNotificationRow != null) {
                    ((MiuiExpandableNotificationRow) expandableNotificationRow).getAnimatedBackground().setAlpha(f2);
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow");
                }
            }
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
