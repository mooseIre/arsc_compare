package com.android.systemui.util.animation;

import android.view.View;
import com.android.systemui.util.animation.PhysicsAnimator;
import java.util.WeakHashMap;
import kotlin.jvm.internal.FloatCompanionObject;
import kotlin.jvm.internal.Intrinsics;

public final class PhysicsAnimatorKt {
    private static final float UNSET = (-FloatCompanionObject.INSTANCE.getMAX_VALUE());
    private static final WeakHashMap<Object, PhysicsAnimator<?>> animators = new WeakHashMap<>();
    private static final PhysicsAnimator.FlingConfig globalDefaultFling = new PhysicsAnimator.FlingConfig(1.0f, -FloatCompanionObject.INSTANCE.getMAX_VALUE(), FloatCompanionObject.INSTANCE.getMAX_VALUE());
    private static final PhysicsAnimator.SpringConfig globalDefaultSpring = new PhysicsAnimator.SpringConfig(1500.0f, 0.5f);
    private static boolean verboseLogging;

    public static final <T extends View> PhysicsAnimator<T> getPhysicsAnimator(T t) {
        Intrinsics.checkParameterIsNotNull(t, "$this$physicsAnimator");
        return PhysicsAnimator.Companion.getInstance(t);
    }

    public static final WeakHashMap<Object, PhysicsAnimator<?>> getAnimators() {
        return animators;
    }
}
