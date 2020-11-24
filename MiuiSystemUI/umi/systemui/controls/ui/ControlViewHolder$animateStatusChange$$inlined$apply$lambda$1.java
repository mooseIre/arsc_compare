package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlViewHolder.kt */
public final class ControlViewHolder$animateStatusChange$$inlined$apply$lambda$1 extends AnimatorListenerAdapter {
    final /* synthetic */ Function0 $statusRowUpdater$inlined;

    ControlViewHolder$animateStatusChange$$inlined$apply$lambda$1(Function0 function0) {
        this.$statusRowUpdater$inlined = function0;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        this.$statusRowUpdater$inlined.invoke();
    }
}
