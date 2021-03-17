package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import com.android.systemui.util.animation.PhysicsAnimator;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PhysicsAnimator.kt */
final class PhysicsAnimator$configureDynamicAnimation$2 implements DynamicAnimation.OnAnimationEndListener {
    final /* synthetic */ DynamicAnimation $anim;
    final /* synthetic */ FloatPropertyCompat $property;
    final /* synthetic */ PhysicsAnimator this$0;

    PhysicsAnimator$configureDynamicAnimation$2(PhysicsAnimator physicsAnimator, FloatPropertyCompat floatPropertyCompat, DynamicAnimation dynamicAnimation) {
        this.this$0 = physicsAnimator;
        this.$property = floatPropertyCompat;
        this.$anim = dynamicAnimation;
    }

    public final void onAnimationEnd(DynamicAnimation<DynamicAnimation<?>> dynamicAnimation, final boolean z, final float f, final float f2) {
        boolean unused = CollectionsKt__MutableCollectionsKt.removeAll(this.this$0.getInternalListeners$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(), new Function1<PhysicsAnimator<T>.InternalListener, Boolean>(this) {
            final /* synthetic */ PhysicsAnimator$configureDynamicAnimation$2 this$0;

            {
                this.this$0 = r1;
            }

            public /* bridge */ /* synthetic */ Object invoke(Object obj) {
                return Boolean.valueOf(invoke((PhysicsAnimator<T>.InternalListener) (PhysicsAnimator.InternalListener) obj));
            }

            public final boolean invoke(@NotNull PhysicsAnimator<T>.InternalListener internalListener) {
                Intrinsics.checkParameterIsNotNull(internalListener, "it");
                PhysicsAnimator$configureDynamicAnimation$2 physicsAnimator$configureDynamicAnimation$2 = this.this$0;
                return internalListener.onInternalAnimationEnd$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(physicsAnimator$configureDynamicAnimation$2.$property, z, f, f2, physicsAnimator$configureDynamicAnimation$2.$anim instanceof FlingAnimation);
            }
        });
        if (Intrinsics.areEqual((Object) (SpringAnimation) this.this$0.springAnimations.get(this.$property), (Object) this.$anim)) {
            this.this$0.springAnimations.remove(this.$property);
        }
        if (Intrinsics.areEqual((Object) (FlingAnimation) this.this$0.flingAnimations.get(this.$property), (Object) this.$anim)) {
            this.this$0.flingAnimations.remove(this.$property);
        }
    }
}
