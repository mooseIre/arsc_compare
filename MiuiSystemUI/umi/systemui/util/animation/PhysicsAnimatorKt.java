package com.android.systemui.util.animation;

import android.view.View;
import com.android.systemui.util.animation.PhysicsAnimator;
import java.util.WeakHashMap;
import kotlin.jvm.internal.FloatCompanionObject;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PhysicsAnimator.kt */
public final class PhysicsAnimatorKt {
    /* access modifiers changed from: private */
    public static final float UNSET = (-FloatCompanionObject.INSTANCE.getMAX_VALUE());
    @NotNull
    private static final WeakHashMap<Object, PhysicsAnimator<?>> animators = new WeakHashMap<>();
    /* access modifiers changed from: private */
    public static final PhysicsAnimator.FlingConfig globalDefaultFling = new PhysicsAnimator.FlingConfig(1.0f, -FloatCompanionObject.INSTANCE.getMAX_VALUE(), FloatCompanionObject.INSTANCE.getMAX_VALUE());
    /* access modifiers changed from: private */
    public static final PhysicsAnimator.SpringConfig globalDefaultSpring = new PhysicsAnimator.SpringConfig(1500.0f, 0.5f);
    /* access modifiers changed from: private */
    public static boolean verboseLogging;

    @NotNull
    public static final <T extends View> PhysicsAnimator<T> getPhysicsAnimator(@NotNull T t) {
        Intrinsics.checkParameterIsNotNull(t, "$this$physicsAnimator");
        return PhysicsAnimator.Companion.getInstance(t);
    }

    @NotNull
    public static final WeakHashMap<Object, PhysicsAnimator<?>> getAnimators() {
        return animators;
    }
}
