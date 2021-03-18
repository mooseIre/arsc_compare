package com.android.systemui.statusbar.notification.modal;

import android.animation.ValueAnimator;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: ModalController.kt */
public final class ModalController$animExitModal$updateListener$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ ModalController this$0;

    ModalController$animExitModal$updateListener$1(ModalController modalController) {
        this.this$0 = modalController;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        ModalController modalController = this.this$0;
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "it");
        modalController.updateExpandState(((float) 1) - valueAnimator.getAnimatedFraction());
    }
}
