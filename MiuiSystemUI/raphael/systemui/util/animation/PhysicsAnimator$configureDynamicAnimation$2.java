package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import java.util.List;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: PhysicsAnimator.kt */
public final class PhysicsAnimator$configureDynamicAnimation$2 implements DynamicAnimation.OnAnimationEndListener {
    final /* synthetic */ DynamicAnimation $anim;
    final /* synthetic */ FloatPropertyCompat $property;
    final /* synthetic */ PhysicsAnimator this$0;

    PhysicsAnimator$configureDynamicAnimation$2(PhysicsAnimator physicsAnimator, FloatPropertyCompat floatPropertyCompat, DynamicAnimation dynamicAnimation) {
        this.this$0 = physicsAnimator;
        this.$property = floatPropertyCompat;
        this.$anim = dynamicAnimation;
    }

    @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
    public final void onAnimationEnd(DynamicAnimation<DynamicAnimation<?>> dynamicAnimation, final boolean z, final float f, final float f2) {
        boolean unused = CollectionsKt__MutableCollectionsKt.removeAll((List) this.this$0.getInternalListeners$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(), (Function1) new Function1<PhysicsAnimator<T>.InternalListener, Boolean>(this) {
            /* class com.android.systemui.util.animation.PhysicsAnimator$configureDynamicAnimation$2.AnonymousClass1 */
            final /* synthetic */ PhysicsAnimator$configureDynamicAnimation$2 this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Boolean invoke(PhysicsAnimator<T>.InternalListener internalListener) {
                return Boolean.valueOf(invoke(internalListener));
            }

            public final boolean invoke(@NotNull PhysicsAnimator<T>.InternalListener internalListener) {
                Intrinsics.checkParameterIsNotNull(internalListener, "it");
                PhysicsAnimator$configureDynamicAnimation$2 physicsAnimator$configureDynamicAnimation$2 = this.this$0;
                return internalListener.onInternalAnimationEnd$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(physicsAnimator$configureDynamicAnimation$2.$property, z, f, f2, physicsAnimator$configureDynamicAnimation$2.$anim instanceof FlingAnimation);
            }
        });
        if (Intrinsics.areEqual((SpringAnimation) this.this$0.springAnimations.get(this.$property), this.$anim)) {
            this.this$0.springAnimations.remove(this.$property);
        }
        if (Intrinsics.areEqual((FlingAnimation) this.this$0.flingAnimations.get(this.$property), this.$anim)) {
            this.this$0.flingAnimations.remove(this.$property);
        }
    }
}
