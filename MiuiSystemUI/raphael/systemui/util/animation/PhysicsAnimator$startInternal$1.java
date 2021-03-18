package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.systemui.util.animation.PhysicsAnimator;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: PhysicsAnimator.kt */
public final class PhysicsAnimator$startInternal$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ FloatPropertyCompat $animatedProperty;
    final /* synthetic */ float $currentValue;
    final /* synthetic */ PhysicsAnimator.FlingConfig $flingConfig;
    final /* synthetic */ Object $target;
    final /* synthetic */ PhysicsAnimator this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    PhysicsAnimator$startInternal$1(PhysicsAnimator physicsAnimator, PhysicsAnimator.FlingConfig flingConfig, float f, FloatPropertyCompat floatPropertyCompat, Object obj) {
        super(0);
        this.this$0 = physicsAnimator;
        this.$flingConfig = flingConfig;
        this.$currentValue = f;
        this.$animatedProperty = floatPropertyCompat;
        this.$target = obj;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        PhysicsAnimator.FlingConfig flingConfig = this.$flingConfig;
        flingConfig.setMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(Math.min(this.$currentValue, flingConfig.getMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core()));
        flingConfig.setMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(Math.max(this.$currentValue, flingConfig.getMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core()));
        this.this$0.cancel(this.$animatedProperty);
        FlingAnimation flingAnimation = this.this$0.getFlingAnimation(this.$animatedProperty, this.$target);
        this.$flingConfig.applyToAnimation$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(flingAnimation);
        flingAnimation.start();
    }
}
