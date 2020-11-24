package com.android.systemui.statusbar.notification.modal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import com.android.systemui.statusbar.notification.modal.ModalController;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ModalController.kt */
public final class ModalController$animEnterModal$animatorListener$1 extends AnimatorListenerAdapter {
    final /* synthetic */ ModalController this$0;

    ModalController$animEnterModal$animatorListener$1(ModalController modalController) {
        this.this$0 = modalController;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        this.this$0.isAnimating = false;
        for (ModalController.OnModalChangeListener onChange : this.this$0.onModalChangeListeners) {
            onChange.onChange(true);
        }
    }
}
