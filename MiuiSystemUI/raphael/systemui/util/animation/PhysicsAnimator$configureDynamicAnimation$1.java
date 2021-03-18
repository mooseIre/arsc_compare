package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

/* access modifiers changed from: package-private */
/* compiled from: PhysicsAnimator.kt */
public final class PhysicsAnimator$configureDynamicAnimation$1 implements DynamicAnimation.OnAnimationUpdateListener {
    final /* synthetic */ FloatPropertyCompat $property;
    final /* synthetic */ PhysicsAnimator this$0;

    PhysicsAnimator$configureDynamicAnimation$1(PhysicsAnimator physicsAnimator, FloatPropertyCompat floatPropertyCompat) {
        this.this$0 = physicsAnimator;
        this.$property = floatPropertyCompat;
    }

    @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener
    public final void onAnimationUpdate(DynamicAnimation<DynamicAnimation<?>> dynamicAnimation, float f, float f2) {
        int size = this.this$0.getInternalListeners$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core().size();
        for (int i = 0; i < size; i++) {
            this.this$0.getInternalListeners$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core().get(i).onInternalAnimationUpdate$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(this.$property, f, f2);
        }
    }
}
