package com.android.systemui.util.animation;

import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.FloatCompanionObject;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PhysicsAnimator.kt */
public final class PhysicsAnimator<T> {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static Function1<Object, ? extends PhysicsAnimator<?>> instanceConstructor = PhysicsAnimator$Companion$instanceConstructor$1.INSTANCE;
    @NotNull
    private Function1<? super Set<? extends FloatPropertyCompat<? super T>>, Unit> cancelAction;
    private SpringConfig defaultSpring;
    private final ArrayList<Function0<Unit>> endActions;
    private final ArrayList<EndListener<T>> endListeners;
    private final ArrayMap<FloatPropertyCompat<? super T>, FlingAnimation> flingAnimations;
    private final ArrayMap<FloatPropertyCompat<? super T>, FlingConfig> flingConfigs;
    @NotNull
    private ArrayList<PhysicsAnimator<T>.InternalListener> internalListeners;
    private final ArrayMap<FloatPropertyCompat<? super T>, SpringAnimation> springAnimations;
    private final ArrayMap<FloatPropertyCompat<? super T>, SpringConfig> springConfigs;
    @NotNull
    private Function0<Unit> startAction;
    private final ArrayList<UpdateListener<T>> updateListeners;
    @NotNull
    private final WeakReference<T> weakTarget;

    /* compiled from: PhysicsAnimator.kt */
    public interface EndListener<T> {
        void onAnimationEnd(T t, @NotNull FloatPropertyCompat<? super T> floatPropertyCompat, boolean z, boolean z2, float f, float f2, boolean z3);
    }

    /* compiled from: PhysicsAnimator.kt */
    public interface UpdateListener<T> {
        void onAnimationUpdateForProperty(T t, @NotNull ArrayMap<FloatPropertyCompat<? super T>, AnimationUpdate> arrayMap);
    }

    public static final float estimateFlingEndValue(float f, float f2, @NotNull FlingConfig flingConfig) {
        return Companion.estimateFlingEndValue(f, f2, flingConfig);
    }

    @NotNull
    public static final <T> PhysicsAnimator<T> getInstance(@NotNull T t) {
        return Companion.getInstance(t);
    }

    @NotNull
    public final PhysicsAnimator<T> flingThenSpring(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, float f, @NotNull FlingConfig flingConfig, @NotNull SpringConfig springConfig) {
        flingThenSpring$default(this, floatPropertyCompat, f, flingConfig, springConfig, false, 16, null);
        return this;
    }

    private PhysicsAnimator(T t) {
        this.weakTarget = new WeakReference<>(t);
        this.springAnimations = new ArrayMap<>();
        this.flingAnimations = new ArrayMap<>();
        this.springConfigs = new ArrayMap<>();
        this.flingConfigs = new ArrayMap<>();
        this.updateListeners = new ArrayList<>();
        this.endListeners = new ArrayList<>();
        this.endActions = new ArrayList<>();
        this.defaultSpring = PhysicsAnimatorKt.access$getGlobalDefaultSpring$p();
        PhysicsAnimatorKt.access$getGlobalDefaultFling$p();
        this.internalListeners = new ArrayList<>();
        this.startAction = new PhysicsAnimator$startAction$1(this);
        this.cancelAction = new PhysicsAnimator$cancelAction$1(this);
    }

    public /* synthetic */ PhysicsAnimator(Object obj, DefaultConstructorMarker defaultConstructorMarker) {
        this(obj);
    }

    /* compiled from: PhysicsAnimator.kt */
    public static final class AnimationUpdate {
        private final float value;
        private final float velocity;

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AnimationUpdate)) {
                return false;
            }
            AnimationUpdate animationUpdate = (AnimationUpdate) obj;
            return Float.compare(this.value, animationUpdate.value) == 0 && Float.compare(this.velocity, animationUpdate.velocity) == 0;
        }

        public int hashCode() {
            return (Float.hashCode(this.value) * 31) + Float.hashCode(this.velocity);
        }

        @NotNull
        public String toString() {
            return "AnimationUpdate(value=" + this.value + ", velocity=" + this.velocity + ")";
        }

        public AnimationUpdate(float f, float f2) {
            this.value = f;
            this.velocity = f2;
        }
    }

    @NotNull
    public final ArrayList<PhysicsAnimator<T>.InternalListener> getInternalListeners$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
        return this.internalListeners;
    }

    @NotNull
    public final PhysicsAnimator<T> spring(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, float f, float f2, float f3, float f4) {
        Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
        if (PhysicsAnimatorKt.access$getVerboseLogging$p()) {
            Log.d("PhysicsAnimator", "Springing " + Companion.getReadablePropertyName(floatPropertyCompat) + " to " + f + '.');
        }
        this.springConfigs.put(floatPropertyCompat, new SpringConfig(f3, f4, f2, f));
        return this;
    }

    public static /* synthetic */ PhysicsAnimator spring$default(PhysicsAnimator physicsAnimator, FloatPropertyCompat floatPropertyCompat, float f, float f2, SpringConfig springConfig, int i, Object obj) {
        if ((i & 8) != 0) {
            springConfig = physicsAnimator.defaultSpring;
        }
        physicsAnimator.spring(floatPropertyCompat, f, f2, springConfig);
        return physicsAnimator;
    }

    @NotNull
    public final PhysicsAnimator<T> spring(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, float f, float f2, @NotNull SpringConfig springConfig) {
        Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
        Intrinsics.checkParameterIsNotNull(springConfig, "config");
        spring(floatPropertyCompat, f, f2, springConfig.getStiffness$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(), springConfig.getDampingRatio$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core());
        return this;
    }

    @NotNull
    public final PhysicsAnimator<T> spring(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, float f, @NotNull SpringConfig springConfig) {
        Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
        Intrinsics.checkParameterIsNotNull(springConfig, "config");
        spring(floatPropertyCompat, f, 0.0f, springConfig);
        return this;
    }

    @NotNull
    public final PhysicsAnimator<T> spring(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, float f) {
        Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
        spring$default(this, floatPropertyCompat, f, 0.0f, null, 8, null);
        return this;
    }

    public static /* synthetic */ PhysicsAnimator flingThenSpring$default(PhysicsAnimator physicsAnimator, FloatPropertyCompat floatPropertyCompat, float f, FlingConfig flingConfig, SpringConfig springConfig, boolean z, int i, Object obj) {
        if ((i & 16) != 0) {
            z = false;
        }
        physicsAnimator.flingThenSpring(floatPropertyCompat, f, flingConfig, springConfig, z);
        return physicsAnimator;
    }

    @NotNull
    public final PhysicsAnimator<T> flingThenSpring(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, float f, @NotNull FlingConfig flingConfig, @NotNull SpringConfig springConfig, boolean z) {
        Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
        Intrinsics.checkParameterIsNotNull(flingConfig, "flingConfig");
        Intrinsics.checkParameterIsNotNull(springConfig, "springConfig");
        T t = this.weakTarget.get();
        if (t == null) {
            Log.w("PhysicsAnimator", "Trying to animate a GC-ed target.");
            return this;
        }
        FlingConfig copy$default = FlingConfig.copy$default(flingConfig, 0.0f, 0.0f, 0.0f, 0.0f, 15, null);
        SpringConfig copy$default2 = SpringConfig.copy$default(springConfig, 0.0f, 0.0f, 0.0f, 0.0f, 15, null);
        float f2 = (float) 0;
        int i = (f > f2 ? 1 : (f == f2 ? 0 : -1));
        float min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core = i < 0 ? flingConfig.getMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() : flingConfig.getMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core();
        if (!z || !isValidValue(min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core)) {
            copy$default.setStartVelocity$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(f);
        } else {
            float value = floatPropertyCompat.getValue(t) + (f / (flingConfig.getFriction$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() * 4.2f));
            float min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core2 = (flingConfig.getMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() + flingConfig.getMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core()) / ((float) 2);
            if ((i < 0 && value > min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core2) || (f > f2 && value < min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core2)) {
                float min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core3 = value < min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core2 ? flingConfig.getMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() : flingConfig.getMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core();
                if (isValidValue(min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core3)) {
                    spring(floatPropertyCompat, min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core3, f, springConfig);
                    return this;
                }
            }
            float value2 = min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core - floatPropertyCompat.getValue(t);
            float friction$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core = flingConfig.getFriction$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() * 4.2f * value2;
            if (value2 > 0.0f && f >= 0.0f) {
                f = Math.max(friction$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core, f);
            } else if (value2 < 0.0f && f <= 0.0f) {
                f = Math.min(friction$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core, f);
            }
            copy$default.setStartVelocity$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(f);
            copy$default2.setFinalPosition$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(min$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core);
        }
        this.flingConfigs.put(floatPropertyCompat, copy$default);
        this.springConfigs.put(floatPropertyCompat, copy$default2);
        return this;
    }

    private final boolean isValidValue(float f) {
        return f < FloatCompanionObject.INSTANCE.getMAX_VALUE() && f > (-FloatCompanionObject.INSTANCE.getMAX_VALUE());
    }

    @NotNull
    public final PhysicsAnimator<T> addUpdateListener(@NotNull UpdateListener<T> updateListener) {
        Intrinsics.checkParameterIsNotNull(updateListener, "listener");
        this.updateListeners.add(updateListener);
        return this;
    }

    @NotNull
    public final PhysicsAnimator<T> addEndListener(@NotNull EndListener<T> endListener) {
        Intrinsics.checkParameterIsNotNull(endListener, "listener");
        this.endListeners.add(endListener);
        return this;
    }

    @NotNull
    public final PhysicsAnimator<T> withEndActions(@NotNull Function0<Unit>... function0Arr) {
        Intrinsics.checkParameterIsNotNull(function0Arr, "endActions");
        this.endActions.addAll(ArraysKt___ArraysKt.filterNotNull(function0Arr));
        return this;
    }

    @NotNull
    public final PhysicsAnimator<T> withEndActions(@NotNull Runnable... runnableArr) {
        Intrinsics.checkParameterIsNotNull(runnableArr, "endActions");
        ArrayList<Function0<Unit>> arrayList = this.endActions;
        List<T> list = ArraysKt___ArraysKt.filterNotNull(runnableArr);
        ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (T t : list) {
            arrayList2.add(new PhysicsAnimator$withEndActions$1$1(t));
        }
        arrayList.addAll(arrayList2);
        return this;
    }

    public final void setDefaultSpringConfig(@NotNull SpringConfig springConfig) {
        Intrinsics.checkParameterIsNotNull(springConfig, "defaultSpring");
        this.defaultSpring = springConfig;
    }

    public final void start() {
        this.startAction.invoke();
    }

    public final void startInternal$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
        Looper mainLooper = Looper.getMainLooper();
        Intrinsics.checkExpressionValueIsNotNull(mainLooper, "Looper.getMainLooper()");
        if (!mainLooper.isCurrentThread()) {
            Log.e("PhysicsAnimator", "Animations can only be started on the main thread. If you are seeing this message in a test, call PhysicsAnimatorTestUtils#prepareForTest in your test setup.");
        }
        T t = this.weakTarget.get();
        if (t == null) {
            Log.w("PhysicsAnimator", "Trying to animate a GC-ed object.");
            return;
        }
        ArrayList<T> arrayList = new ArrayList();
        for (FloatPropertyCompat<? super T> floatPropertyCompat : getAnimatedProperties$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core()) {
            FlingConfig flingConfig = this.flingConfigs.get(floatPropertyCompat);
            SpringConfig springConfig = this.springConfigs.get(floatPropertyCompat);
            float value = floatPropertyCompat.getValue(t);
            if (flingConfig != null) {
                arrayList.add(new PhysicsAnimator$startInternal$1(this, flingConfig, value, floatPropertyCompat, t));
            }
            if (springConfig != null) {
                if (flingConfig == null) {
                    SpringAnimation springAnimation = getSpringAnimation(floatPropertyCompat, t);
                    springConfig.applyToAnimation$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(springAnimation);
                    arrayList.add(new PhysicsAnimator$startInternal$2(springAnimation));
                } else {
                    this.endListeners.add(0, new PhysicsAnimator$startInternal$3(this, floatPropertyCompat, flingConfig.getMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(), flingConfig.getMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(), springConfig));
                }
            }
        }
        this.internalListeners.add(new InternalListener(this, t, getAnimatedProperties$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(), new ArrayList(this.updateListeners), new ArrayList(this.endListeners), new ArrayList(this.endActions)));
        for (T t2 : arrayList) {
            t2.invoke();
        }
        clearAnimator();
    }

    private final void clearAnimator() {
        this.springConfigs.clear();
        this.flingConfigs.clear();
        this.updateListeners.clear();
        this.endListeners.clear();
        this.endActions.clear();
    }

    /* access modifiers changed from: private */
    public final SpringAnimation getSpringAnimation(FloatPropertyCompat<? super T> floatPropertyCompat, T t) {
        ArrayMap<FloatPropertyCompat<? super T>, SpringAnimation> arrayMap = this.springAnimations;
        SpringAnimation springAnimation = arrayMap.get(floatPropertyCompat);
        if (springAnimation == null) {
            SpringAnimation springAnimation2 = new SpringAnimation(t, floatPropertyCompat);
            configureDynamicAnimation(springAnimation2, floatPropertyCompat);
            springAnimation = springAnimation2;
            arrayMap.put(floatPropertyCompat, springAnimation);
        }
        Intrinsics.checkExpressionValueIsNotNull(springAnimation, "springAnimations.getOrPu…    as SpringAnimation })");
        return springAnimation;
    }

    /* access modifiers changed from: private */
    public final FlingAnimation getFlingAnimation(FloatPropertyCompat<? super T> floatPropertyCompat, T t) {
        ArrayMap<FloatPropertyCompat<? super T>, FlingAnimation> arrayMap = this.flingAnimations;
        FlingAnimation flingAnimation = arrayMap.get(floatPropertyCompat);
        if (flingAnimation == null) {
            FlingAnimation flingAnimation2 = new FlingAnimation(t, floatPropertyCompat);
            configureDynamicAnimation(flingAnimation2, floatPropertyCompat);
            flingAnimation = flingAnimation2;
            arrayMap.put(floatPropertyCompat, flingAnimation);
        }
        Intrinsics.checkExpressionValueIsNotNull(flingAnimation, "flingAnimations.getOrPut…     as FlingAnimation })");
        return flingAnimation;
    }

    private final DynamicAnimation<?> configureDynamicAnimation(DynamicAnimation<?> dynamicAnimation, FloatPropertyCompat<? super T> floatPropertyCompat) {
        dynamicAnimation.addUpdateListener(new PhysicsAnimator$configureDynamicAnimation$1(this, floatPropertyCompat));
        dynamicAnimation.addEndListener(new PhysicsAnimator$configureDynamicAnimation$2(this, floatPropertyCompat, dynamicAnimation));
        return dynamicAnimation;
    }

    /* compiled from: PhysicsAnimator.kt */
    public final class InternalListener {
        private List<? extends Function0<Unit>> endActions;
        private List<? extends EndListener<T>> endListeners;
        private int numPropertiesAnimating;
        private Set<? extends FloatPropertyCompat<? super T>> properties;
        private final T target;
        final /* synthetic */ PhysicsAnimator this$0;
        private final ArrayMap<FloatPropertyCompat<? super T>, AnimationUpdate> undispatchedUpdates = new ArrayMap<>();
        private List<? extends UpdateListener<T>> updateListeners;

        public InternalListener(PhysicsAnimator physicsAnimator, @NotNull T t, @NotNull Set<? extends FloatPropertyCompat<? super T>> set, @NotNull List<? extends UpdateListener<T>> list, @NotNull List<? extends EndListener<T>> list2, List<? extends Function0<Unit>> list3) {
            Intrinsics.checkParameterIsNotNull(set, "properties");
            Intrinsics.checkParameterIsNotNull(list, "updateListeners");
            Intrinsics.checkParameterIsNotNull(list2, "endListeners");
            Intrinsics.checkParameterIsNotNull(list3, "endActions");
            this.this$0 = physicsAnimator;
            this.target = t;
            this.properties = set;
            this.updateListeners = list;
            this.endListeners = list2;
            this.endActions = list3;
            this.numPropertiesAnimating = set.size();
        }

        public final void onInternalAnimationUpdate$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, float f, float f2) {
            Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
            if (this.properties.contains(floatPropertyCompat)) {
                this.undispatchedUpdates.put(floatPropertyCompat, new AnimationUpdate(f, f2));
                maybeDispatchUpdates();
            }
        }

        public final boolean onInternalAnimationEnd$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat, boolean z, float f, float f2, boolean z2) {
            Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
            if (!this.properties.contains(floatPropertyCompat)) {
                return false;
            }
            this.numPropertiesAnimating--;
            maybeDispatchUpdates();
            if (this.undispatchedUpdates.containsKey(floatPropertyCompat)) {
                Iterator<T> it = this.updateListeners.iterator();
                while (it.hasNext()) {
                    T t = this.target;
                    ArrayMap<FloatPropertyCompat<? super T>, AnimationUpdate> arrayMap = new ArrayMap<>();
                    arrayMap.put(floatPropertyCompat, this.undispatchedUpdates.get(floatPropertyCompat));
                    it.next().onAnimationUpdateForProperty(t, arrayMap);
                }
                this.undispatchedUpdates.remove(floatPropertyCompat);
            }
            boolean z3 = !this.this$0.arePropertiesAnimating(this.properties);
            Iterator<T> it2 = this.endListeners.iterator();
            while (it2.hasNext()) {
                it2.next().onAnimationEnd(this.target, floatPropertyCompat, z2, z, f, f2, z3);
                if (this.this$0.isPropertyAnimating(floatPropertyCompat)) {
                    return false;
                }
            }
            if (z3 && !z) {
                Iterator<T> it3 = this.endActions.iterator();
                while (it3.hasNext()) {
                    it3.next().invoke();
                }
            }
            return z3;
        }

        private final void maybeDispatchUpdates() {
            if (this.undispatchedUpdates.size() >= this.numPropertiesAnimating && this.undispatchedUpdates.size() > 0) {
                Iterator<T> it = this.updateListeners.iterator();
                while (it.hasNext()) {
                    it.next().onAnimationUpdateForProperty(this.target, new ArrayMap<>(this.undispatchedUpdates));
                }
                this.undispatchedUpdates.clear();
            }
        }
    }

    public final boolean isRunning() {
        Set<FloatPropertyCompat<? super T>> keySet = this.springAnimations.keySet();
        Intrinsics.checkExpressionValueIsNotNull(keySet, "springAnimations.keys");
        Set<FloatPropertyCompat<? super T>> keySet2 = this.flingAnimations.keySet();
        Intrinsics.checkExpressionValueIsNotNull(keySet2, "flingAnimations.keys");
        return arePropertiesAnimating(CollectionsKt___CollectionsKt.union(keySet, keySet2));
    }

    public final boolean isPropertyAnimating(@NotNull FloatPropertyCompat<? super T> floatPropertyCompat) {
        Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
        SpringAnimation springAnimation = this.springAnimations.get(floatPropertyCompat);
        if (!(springAnimation != null ? springAnimation.isRunning() : false)) {
            FlingAnimation flingAnimation = this.flingAnimations.get(floatPropertyCompat);
            if (flingAnimation != null ? flingAnimation.isRunning() : false) {
                return true;
            }
            return false;
        }
        return true;
    }

    @NotNull
    public final Set<FloatPropertyCompat<? super T>> getAnimatedProperties$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
        Set<FloatPropertyCompat<? super T>> keySet = this.springConfigs.keySet();
        Intrinsics.checkExpressionValueIsNotNull(keySet, "springConfigs.keys");
        Set<FloatPropertyCompat<? super T>> keySet2 = this.flingConfigs.keySet();
        Intrinsics.checkExpressionValueIsNotNull(keySet2, "flingConfigs.keys");
        return CollectionsKt___CollectionsKt.union(keySet, keySet2);
    }

    public final void cancelInternal$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(@NotNull Set<? extends FloatPropertyCompat<? super T>> set) {
        Intrinsics.checkParameterIsNotNull(set, "properties");
        Iterator<? extends FloatPropertyCompat<? super T>> it = set.iterator();
        while (it.hasNext()) {
            FloatPropertyCompat floatPropertyCompat = (FloatPropertyCompat) it.next();
            FlingAnimation flingAnimation = this.flingAnimations.get(floatPropertyCompat);
            if (flingAnimation != null) {
                flingAnimation.cancel();
            }
            SpringAnimation springAnimation = this.springAnimations.get(floatPropertyCompat);
            if (springAnimation != null) {
                springAnimation.cancel();
            }
        }
    }

    public final void cancel() {
        Function1<? super Set<? extends FloatPropertyCompat<? super T>>, Unit> function1 = this.cancelAction;
        Set<FloatPropertyCompat<? super T>> keySet = this.flingAnimations.keySet();
        Intrinsics.checkExpressionValueIsNotNull(keySet, "flingAnimations.keys");
        function1.invoke(keySet);
        Function1<? super Set<? extends FloatPropertyCompat<? super T>>, Unit> function12 = this.cancelAction;
        Set<FloatPropertyCompat<? super T>> keySet2 = this.springAnimations.keySet();
        Intrinsics.checkExpressionValueIsNotNull(keySet2, "springAnimations.keys");
        function12.invoke(keySet2);
    }

    public final void cancel(@NotNull FloatPropertyCompat<? super T>... floatPropertyCompatArr) {
        Intrinsics.checkParameterIsNotNull(floatPropertyCompatArr, "properties");
        this.cancelAction.invoke(ArraysKt___ArraysKt.toSet(floatPropertyCompatArr));
    }

    /* compiled from: PhysicsAnimator.kt */
    public static final class SpringConfig {
        private float dampingRatio;
        private float finalPosition;
        private float startVelocity;
        private float stiffness;

        public static /* synthetic */ SpringConfig copy$default(SpringConfig springConfig, float f, float f2, float f3, float f4, int i, Object obj) {
            if ((i & 1) != 0) {
                f = springConfig.stiffness;
            }
            if ((i & 2) != 0) {
                f2 = springConfig.dampingRatio;
            }
            if ((i & 4) != 0) {
                f3 = springConfig.startVelocity;
            }
            if ((i & 8) != 0) {
                f4 = springConfig.finalPosition;
            }
            return springConfig.copy(f, f2, f3, f4);
        }

        @NotNull
        public final SpringConfig copy(float f, float f2, float f3, float f4) {
            return new SpringConfig(f, f2, f3, f4);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SpringConfig)) {
                return false;
            }
            SpringConfig springConfig = (SpringConfig) obj;
            return Float.compare(this.stiffness, springConfig.stiffness) == 0 && Float.compare(this.dampingRatio, springConfig.dampingRatio) == 0 && Float.compare(this.startVelocity, springConfig.startVelocity) == 0 && Float.compare(this.finalPosition, springConfig.finalPosition) == 0;
        }

        public int hashCode() {
            return (((((Float.hashCode(this.stiffness) * 31) + Float.hashCode(this.dampingRatio)) * 31) + Float.hashCode(this.startVelocity)) * 31) + Float.hashCode(this.finalPosition);
        }

        @NotNull
        public String toString() {
            return "SpringConfig(stiffness=" + this.stiffness + ", dampingRatio=" + this.dampingRatio + ", startVelocity=" + this.startVelocity + ", finalPosition=" + this.finalPosition + ")";
        }

        public SpringConfig(float f, float f2, float f3, float f4) {
            this.stiffness = f;
            this.dampingRatio = f2;
            this.startVelocity = f3;
            this.finalPosition = f4;
        }

        public final float getStiffness$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            return this.stiffness;
        }

        public final float getDampingRatio$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            return this.dampingRatio;
        }

        public final void setStartVelocity$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(float f) {
            this.startVelocity = f;
        }

        /* JADX INFO: this call moved to the top of the method (can break code semantics) */
        public /* synthetic */ SpringConfig(float f, float f2, float f3, float f4, int i, DefaultConstructorMarker defaultConstructorMarker) {
            this(f, f2, (i & 4) != 0 ? 0.0f : f3, (i & 8) != 0 ? PhysicsAnimatorKt.access$getUNSET$p() : f4);
        }

        public final float getFinalPosition$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            return this.finalPosition;
        }

        public final void setFinalPosition$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(float f) {
            this.finalPosition = f;
        }

        public SpringConfig() {
            this(PhysicsAnimatorKt.access$getGlobalDefaultSpring$p().stiffness, PhysicsAnimatorKt.access$getGlobalDefaultSpring$p().dampingRatio);
        }

        public SpringConfig(float f, float f2) {
            this(f, f2, 0.0f, 0.0f, 8, null);
        }

        public final void applyToAnimation$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(@NotNull SpringAnimation springAnimation) {
            Intrinsics.checkParameterIsNotNull(springAnimation, "anim");
            SpringForce spring = springAnimation.getSpring();
            if (spring == null) {
                spring = new SpringForce();
            }
            spring.setStiffness(this.stiffness);
            spring.setDampingRatio(this.dampingRatio);
            spring.setFinalPosition(this.finalPosition);
            springAnimation.setSpring(spring);
            float f = this.startVelocity;
            if (f != 0.0f) {
                springAnimation.setStartVelocity(f);
            }
        }
    }

    /* compiled from: PhysicsAnimator.kt */
    public static final class FlingConfig {
        private float friction;
        private float max;
        private float min;
        private float startVelocity;

        public static /* synthetic */ FlingConfig copy$default(FlingConfig flingConfig, float f, float f2, float f3, float f4, int i, Object obj) {
            if ((i & 1) != 0) {
                f = flingConfig.friction;
            }
            if ((i & 2) != 0) {
                f2 = flingConfig.min;
            }
            if ((i & 4) != 0) {
                f3 = flingConfig.max;
            }
            if ((i & 8) != 0) {
                f4 = flingConfig.startVelocity;
            }
            return flingConfig.copy(f, f2, f3, f4);
        }

        @NotNull
        public final FlingConfig copy(float f, float f2, float f3, float f4) {
            return new FlingConfig(f, f2, f3, f4);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FlingConfig)) {
                return false;
            }
            FlingConfig flingConfig = (FlingConfig) obj;
            return Float.compare(this.friction, flingConfig.friction) == 0 && Float.compare(this.min, flingConfig.min) == 0 && Float.compare(this.max, flingConfig.max) == 0 && Float.compare(this.startVelocity, flingConfig.startVelocity) == 0;
        }

        public int hashCode() {
            return (((((Float.hashCode(this.friction) * 31) + Float.hashCode(this.min)) * 31) + Float.hashCode(this.max)) * 31) + Float.hashCode(this.startVelocity);
        }

        @NotNull
        public String toString() {
            return "FlingConfig(friction=" + this.friction + ", min=" + this.min + ", max=" + this.max + ", startVelocity=" + this.startVelocity + ")";
        }

        public FlingConfig(float f, float f2, float f3, float f4) {
            this.friction = f;
            this.min = f2;
            this.max = f3;
            this.startVelocity = f4;
        }

        public final float getFriction$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            return this.friction;
        }

        public final float getMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            return this.min;
        }

        public final void setMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(float f) {
            this.min = f;
        }

        public final float getMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            return this.max;
        }

        public final void setMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(float f) {
            this.max = f;
        }

        public final void setStartVelocity$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(float f) {
            this.startVelocity = f;
        }

        public FlingConfig() {
            this(PhysicsAnimatorKt.access$getGlobalDefaultFling$p().friction);
        }

        public FlingConfig(float f) {
            this(f, PhysicsAnimatorKt.access$getGlobalDefaultFling$p().min, PhysicsAnimatorKt.access$getGlobalDefaultFling$p().max);
        }

        public FlingConfig(float f, float f2, float f3) {
            this(f, f2, f3, 0.0f);
        }

        public final void applyToAnimation$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(@NotNull FlingAnimation flingAnimation) {
            Intrinsics.checkParameterIsNotNull(flingAnimation, "anim");
            flingAnimation.setFriction(this.friction);
            flingAnimation.setMinValue(this.min);
            flingAnimation.setMaxValue(this.max);
            flingAnimation.setStartVelocity(this.startVelocity);
        }
    }

    /* compiled from: PhysicsAnimator.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final Function1<Object, PhysicsAnimator<?>> getInstanceConstructor$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            return PhysicsAnimator.instanceConstructor;
        }

        @NotNull
        public final <T> PhysicsAnimator<T> getInstance(@NotNull T t) {
            Intrinsics.checkParameterIsNotNull(t, "target");
            if (!PhysicsAnimatorKt.getAnimators().containsKey(t)) {
                PhysicsAnimatorKt.getAnimators().put(t, getInstanceConstructor$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core().invoke(t));
            }
            PhysicsAnimator<?> physicsAnimator = PhysicsAnimatorKt.getAnimators().get(t);
            if (physicsAnimator != null) {
                return (PhysicsAnimator<T>) physicsAnimator;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.util.animation.PhysicsAnimator<T>");
        }

        public final float estimateFlingEndValue(float f, float f2, @NotNull FlingConfig flingConfig) {
            Intrinsics.checkParameterIsNotNull(flingConfig, "flingConfig");
            return Math.min(flingConfig.getMax$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(), Math.max(flingConfig.getMin$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(), f + (f2 / (flingConfig.getFriction$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() * 4.2f))));
        }

        @NotNull
        public final String getReadablePropertyName(@NotNull FloatPropertyCompat<?> floatPropertyCompat) {
            Intrinsics.checkParameterIsNotNull(floatPropertyCompat, "property");
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.TRANSLATION_X)) {
                return "translationX";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.TRANSLATION_Y)) {
                return "translationY";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.TRANSLATION_Z)) {
                return "translationZ";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.SCALE_X)) {
                return "scaleX";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.SCALE_Y)) {
                return "scaleY";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.ROTATION)) {
                return "rotation";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.ROTATION_X)) {
                return "rotationX";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.ROTATION_Y)) {
                return "rotationY";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.SCROLL_X)) {
                return "scrollX";
            }
            if (Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.SCROLL_Y)) {
                return "scrollY";
            }
            return Intrinsics.areEqual(floatPropertyCompat, DynamicAnimation.ALPHA) ? "alpha" : "Custom FloatPropertyCompat instance";
        }
    }

    public final boolean arePropertiesAnimating(@NotNull Set<? extends FloatPropertyCompat<? super T>> set) {
        Intrinsics.checkParameterIsNotNull(set, "properties");
        if ((set instanceof Collection) && set.isEmpty()) {
            return false;
        }
        Iterator<T> it = set.iterator();
        while (it.hasNext()) {
            if (isPropertyAnimating(it.next())) {
                return true;
            }
        }
        return false;
    }
}
