package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$reload$1 extends AnimatorListenerAdapter {
    final /* synthetic */ ViewGroup $parent;
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$reload$1(ControlsUiControllerImpl controlsUiControllerImpl, ViewGroup viewGroup) {
        this.this$0 = controlsUiControllerImpl;
        this.$parent = viewGroup;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        ControlsUiControllerImpl.access$getControlViewsById$p(this.this$0).clear();
        ControlsUiControllerImpl.access$getControlsById$p(this.this$0).clear();
        ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
        controlsUiControllerImpl.show(this.$parent, ControlsUiControllerImpl.access$getDismissGlobalActions$p(controlsUiControllerImpl));
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.$parent, "alpha", 0.0f, 1.0f);
        ofFloat.setInterpolator(new DecelerateInterpolator(1.0f));
        ofFloat.setDuration(200L);
        ofFloat.start();
    }
}
