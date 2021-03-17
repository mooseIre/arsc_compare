package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiKeyButtonRipple.kt */
public final class MiuiKeyButtonRipple$mAnimatorListener$1 extends AnimatorListenerAdapter {
    final /* synthetic */ MiuiKeyButtonRipple this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiKeyButtonRipple$mAnimatorListener$1(MiuiKeyButtonRipple miuiKeyButtonRipple) {
        this.this$0 = miuiKeyButtonRipple;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        this.this$0.getMRunningAnimations().remove(animator);
        if (this.this$0.getMRunningAnimations().isEmpty() && !this.this$0.getMPressed()) {
            MiuiKeyButtonRipple.access$setMVisible$p(this.this$0, false);
            this.this$0.setMDrawingHardwareGlow(false);
            this.this$0.invalidateSelf();
        }
    }
}
