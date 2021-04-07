package com.android.systemui.controls.management;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.lifecycle.LifecycleObserver;
import com.android.systemui.Interpolators;
import kotlin.jvm.internal.Intrinsics;

public final class ControlsAnimations {
    public static final ControlsAnimations INSTANCE = new ControlsAnimations();
    private static float translationY;

    private ControlsAnimations() {
    }

    public final LifecycleObserver observerForAnimations(ViewGroup viewGroup, Window window, Intent intent) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "view");
        Intrinsics.checkParameterIsNotNull(window, "window");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        return new ControlsAnimations$observerForAnimations$1(this, window, viewGroup, intent);
    }

    public final Animator enterAnimation(View view) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Log.d("ControlsUiController", "Enter animation for " + view);
        view.setTransitionAlpha(0.0f);
        view.setAlpha(1.0f);
        view.setTranslationY(translationY);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "transitionAlpha", 0.0f, 1.0f);
        ofFloat.setInterpolator(Interpolators.DECELERATE_QUINT);
        ofFloat.setStartDelay(167);
        ofFloat.setDuration(183L);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, "translationY", 0.0f);
        ofFloat2.setInterpolator(Interpolators.DECELERATE_QUINT);
        ofFloat2.setStartDelay(217);
        ofFloat2.setDuration(217L);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ofFloat, ofFloat2);
        return animatorSet;
    }

    public static /* synthetic */ Animator exitAnimation$default(View view, Runnable runnable, int i, Object obj) {
        if ((i & 2) != 0) {
            runnable = null;
        }
        return exitAnimation(view, runnable);
    }

    public static final Animator exitAnimation(View view, Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Log.d("ControlsUiController", "Exit animation for " + view);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "transitionAlpha", 0.0f);
        ofFloat.setInterpolator(Interpolators.ACCELERATE);
        ofFloat.setDuration(167L);
        view.setTranslationY(0.0f);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, "translationY", -translationY);
        ofFloat2.setInterpolator(Interpolators.ACCELERATE);
        ofFloat2.setDuration(183L);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ofFloat, ofFloat2);
        if (runnable != null) {
            animatorSet.addListener(new ControlsAnimations$exitAnimation$1$1$1(runnable));
        }
        return animatorSet;
    }

    public final WindowTransition enterWindowTransition(int i) {
        WindowTransition windowTransition = new WindowTransition(ControlsAnimations$enterWindowTransition$1.INSTANCE);
        windowTransition.addTarget(i);
        return windowTransition;
    }

    public final WindowTransition exitWindowTransition(int i) {
        WindowTransition windowTransition = new WindowTransition(ControlsAnimations$exitWindowTransition$1.INSTANCE);
        windowTransition.addTarget(i);
        return windowTransition;
    }
}
