package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import com.android.systemui.util.animation.PhysicsAnimator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PhysicsAnimator.kt */
public final class PhysicsAnimator$startInternal$3 implements PhysicsAnimator.EndListener<T> {
    final /* synthetic */ FloatPropertyCompat $animatedProperty;
    final /* synthetic */ float $flingMax;
    final /* synthetic */ float $flingMin;
    final /* synthetic */ PhysicsAnimator.SpringConfig $springConfig;
    final /* synthetic */ PhysicsAnimator this$0;

    PhysicsAnimator$startInternal$3(PhysicsAnimator physicsAnimator, FloatPropertyCompat floatPropertyCompat, float f, float f2, PhysicsAnimator.SpringConfig springConfig) {
        this.this$0 = physicsAnimator;
        this.$animatedProperty = floatPropertyCompat;
        this.$flingMin = f;
        this.$flingMax = f2;
        this.$springConfig = springConfig;
    }

    @Override // com.android.systemui.util.animation.PhysicsAnimator.EndListener
    public void onAnimationEnd(T t, @NotNull FloatPropertyCompat<? super T> floatPropertyCompat, boolean z, boolean z2, float f, float f2, boolean z3) {
        Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
        boolean z4 = true;
        if (!(!Intrinsics.areEqual(floatPropertyCompat, this.$animatedProperty)) && z && !z2) {
            float f3 = (float) 0;
            boolean z5 = Math.abs(f2) > f3;
            if (f >= this.$flingMin && f <= this.$flingMax) {
                z4 = false;
            }
            if (z5 || z4) {
                this.$springConfig.setStartVelocity$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(f2);
                if (this.$springConfig.getFinalPosition$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() == PhysicsAnimatorKt.UNSET) {
                    if (z5) {
                        this.$springConfig.setFinalPosition$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(f2 < f3 ? this.$flingMin : this.$flingMax);
                    } else if (z4) {
                        PhysicsAnimator.SpringConfig springConfig = this.$springConfig;
                        float f4 = this.$flingMin;
                        if (f >= f4) {
                            f4 = this.$flingMax;
                        }
                        springConfig.setFinalPosition$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(f4);
                    }
                }
                SpringAnimation springAnimation = this.this$0.getSpringAnimation(this.$animatedProperty, t);
                this.$springConfig.applyToAnimation$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(springAnimation);
                springAnimation.start();
            }
        }
    }
}
