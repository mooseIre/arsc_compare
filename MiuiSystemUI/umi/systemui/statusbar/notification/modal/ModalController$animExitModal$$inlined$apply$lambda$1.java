package com.android.systemui.statusbar.notification.modal;

import android.animation.ValueAnimator;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: ModalController.kt */
public final class ModalController$animExitModal$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ ModalController this$0;

    ModalController$animExitModal$$inlined$apply$lambda$1(ModalController modalController, long j) {
        this.this$0 = modalController;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = (MiuiExpandableNotificationRow) this.this$0.modalRow;
        if (miuiExpandableNotificationRow != null) {
            Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
            Object animatedValue = valueAnimator.getAnimatedValue();
            if (animatedValue != null) {
                miuiExpandableNotificationRow.setAlpha(((Float) animatedValue).floatValue());
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
        }
    }
}
