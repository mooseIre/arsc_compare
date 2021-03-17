package com.android.systemui.statusbar.notification.modal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ModalController.kt */
public final class ModalController$animExitModal$animatorListener$1 extends AnimatorListenerAdapter {
    final /* synthetic */ String $exitMode;
    final /* synthetic */ ModalController this$0;

    ModalController$animExitModal$animatorListener$1(ModalController modalController, String str) {
        this.this$0 = modalController;
        this.$exitMode = str;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        this.this$0.exitModal(this.$exitMode);
        this.this$0.isAnimating = false;
    }
}
