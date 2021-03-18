package com.android.systemui.controls.management;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.Window;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.android.systemui.C0012R$dimen;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsAnimations.kt */
public final class ControlsAnimations$observerForAnimations$1 implements LifecycleObserver {
    final /* synthetic */ ViewGroup $view;
    final /* synthetic */ Window $window;
    private boolean showAnimation;

    ControlsAnimations$observerForAnimations$1(ControlsAnimations controlsAnimations, Window window, ViewGroup viewGroup, Intent intent) {
        this.$window = window;
        this.$view = viewGroup;
        this.showAnimation = intent.getBooleanExtra("extra_animate", false);
        viewGroup.setTransitionGroup(true);
        viewGroup.setTransitionAlpha(0.0f);
        if (ControlsAnimations.access$getTranslationY$p(ControlsAnimations.INSTANCE) == -1.0f) {
            ControlsAnimations controlsAnimations2 = ControlsAnimations.INSTANCE;
            Context context = viewGroup.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "view.context");
            ControlsAnimations.access$setTranslationY$p(controlsAnimations2, (float) context.getResources().getDimensionPixelSize(C0012R$dimen.global_actions_controls_y_translation));
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public final void setup() {
        Window window = this.$window;
        window.setAllowEnterTransitionOverlap(true);
        window.setEnterTransition(ControlsAnimations.INSTANCE.enterWindowTransition(this.$view.getId()));
        window.setExitTransition(ControlsAnimations.INSTANCE.exitWindowTransition(this.$view.getId()));
        window.setReenterTransition(ControlsAnimations.INSTANCE.enterWindowTransition(this.$view.getId()));
        window.setReturnTransition(ControlsAnimations.INSTANCE.exitWindowTransition(this.$view.getId()));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public final void enterAnimation() {
        if (this.showAnimation) {
            ControlsAnimations.INSTANCE.enterAnimation(this.$view).start();
            this.showAnimation = false;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public final void resetAnimation() {
        this.$view.setTranslationY(0.0f);
    }
}
