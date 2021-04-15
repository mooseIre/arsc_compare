package com.android.systemui.statusbar.notification.stack;

import android.animation.ValueAnimator;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationBackgroundView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;

/* compiled from: MiuiNotificationChildrenContainer.kt */
final class MiuiNotificationChildrenContainer$startBackgroundAnimation$2 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ float $begin;
    final /* synthetic */ float $end;
    final /* synthetic */ ExpandableViewState $firstChildState;
    final /* synthetic */ NotificationBackgroundView $summaryBackground;
    final /* synthetic */ MiuiNotificationChildrenContainer this$0;

    MiuiNotificationChildrenContainer$startBackgroundAnimation$2(MiuiNotificationChildrenContainer miuiNotificationChildrenContainer, float f, float f2, NotificationBackgroundView notificationBackgroundView, ExpandableViewState expandableViewState) {
        this.this$0 = miuiNotificationChildrenContainer;
        this.$begin = f;
        this.$end = f2;
        this.$summaryBackground = notificationBackgroundView;
        this.$firstChildState = expandableViewState;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "valueAnimator");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            float floatValue = ((Float) animatedValue).floatValue();
            float f = this.$begin;
            this.$summaryBackground.setActualHeight((int) (f + ((this.$end - f) * floatValue)));
            this.$summaryBackground.setTranslationY(this.$firstChildState.yTranslation * floatValue);
            this.$summaryBackground.setAlpha(1.0f);
            for (int i = RangesKt___RangesKt.coerceAtMost((this.this$0.getMaxAllowedVisibleChildren() - 1) + 1, this.this$0.mAttachedChildren.size() - 1); i >= 1; i--) {
                ExpandableNotificationRow expandableNotificationRow = this.this$0.mAttachedChildren.get(i);
                if (expandableNotificationRow != null) {
                    MiuiExpandableNotificationRow miuiExpandableNotificationRow = (MiuiExpandableNotificationRow) expandableNotificationRow;
                    NotificationBackgroundView animatedBackground = miuiExpandableNotificationRow.getAnimatedBackground();
                    animatedBackground.setAlpha(floatValue);
                    ExpandableViewState viewState = miuiExpandableNotificationRow.getViewState();
                    if (viewState != null) {
                        animatedBackground.setActualHeight(viewState.height);
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow");
                }
            }
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
