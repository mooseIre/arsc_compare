package com.android.systemui.util.animation;

import android.os.Looper;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhysicsAnimator {
    public static WeakHashMap animators = new WeakHashMap();
    /* access modifiers changed from: private */
    public float UNSET = -3.4028235E38f;
    private ArrayList endActions = new ArrayList();
    private ArrayList endListeners = new ArrayList();
    public ArrayMap<FloatPropertyCompat, FlingAnimation> flingAnimations = new ArrayMap<>();
    private ArrayMap<FloatPropertyCompat, FlingConfig> flingConfigs = new ArrayMap<>();
    private FlingConfig globalDefaultFling = new FlingConfig(1.0f, -3.4028235E38f, Float.MAX_VALUE, 0.0f);
    private SpringConfig globalDefaultSpring = new SpringConfig(1500.0f, 0.5f);
    public CopyOnWriteArrayList<InternalListener> internalListeners = new CopyOnWriteArrayList<>();
    public ArrayMap<FloatPropertyCompat, SpringAnimation> springAnimations = new ArrayMap<>();
    private ArrayMap<FloatPropertyCompat, SpringConfig> springConfigs = new ArrayMap<>();
    public Object target;
    private ArrayList updateListeners = new ArrayList();
    private boolean verboseLogging = false;

    public interface EndListener {
        void onAnimationEnd(Object obj, FloatPropertyCompat floatPropertyCompat, boolean z, boolean z2, float f, float f2, boolean z3);
    }

    public interface UpdateListener {
        void onAnimationUpdateForProperty(Object obj, ArrayMap<FloatPropertyCompat, AnimationUpdate> arrayMap);
    }

    public PhysicsAnimator(Object obj) {
        this.target = obj;
    }

    public static PhysicsAnimator getInstance(Object obj) {
        if (!animators.containsKey(obj)) {
            animators.put(obj, new PhysicsAnimator(obj));
        }
        return (PhysicsAnimator) animators.get(obj);
    }

    public static float estimateFlingEndValue(float f, float f2, FlingConfig flingConfig) {
        return Math.min(flingConfig.max, Math.max(flingConfig.min, f + (f2 / (flingConfig.friction * 4.2f))));
    }

    public static class AnimationUpdate {
        public AnimationUpdate(float f, float f2) {
        }
    }

    public static class SpringConfig {
        float dampingRatio;
        float finalPosition;
        float startVelocity;
        float stiffness;

        public SpringConfig() {
            this(1500.0f, 0.5f);
        }

        public SpringConfig(float f, float f2) {
            this.startVelocity = 0.0f;
            this.finalPosition = -3.4028235E38f;
            this.stiffness = f;
            this.dampingRatio = f2;
        }

        public SpringConfig(float f, float f2, float f3, float f4) {
            this.startVelocity = 0.0f;
            this.finalPosition = -3.4028235E38f;
            this.stiffness = f;
            this.dampingRatio = f2;
            this.startVelocity = f3;
            this.finalPosition = f4;
        }

        /* access modifiers changed from: package-private */
        public void applyToAnimation(SpringAnimation springAnimation) {
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

    public static class FlingConfig {
        float friction;
        float max;
        float min;
        float startVelocity;

        public FlingConfig() {
            this(1.0f);
        }

        public FlingConfig(float f) {
            this(f, -3.4028235E38f, Float.MAX_VALUE);
        }

        public FlingConfig(float f, float f2, float f3) {
            this.startVelocity = 0.0f;
            this.friction = f;
            this.min = f2;
            this.max = f3;
        }

        public FlingConfig(float f, float f2, float f3, float f4) {
            this.startVelocity = 0.0f;
            this.friction = f;
            this.min = f2;
            this.max = f3;
            this.startVelocity = f4;
        }

        /* access modifiers changed from: package-private */
        public void applyToAnimation(FlingAnimation flingAnimation) {
            flingAnimation.setFriction(this.friction);
            flingAnimation.setMinValue(this.min);
            flingAnimation.setMaxValue(this.max);
            flingAnimation.setStartVelocity(this.startVelocity);
        }
    }

    public class InternalListener {
        private List<Runnable> endActions;
        private List<EndListener> endListeners;
        private int numPropertiesAnimating;
        private Set<FloatPropertyCompat> properties;
        private ArrayMap undispatchedUpdates = new ArrayMap();
        private List<UpdateListener> updateListeners;

        public InternalListener(Set<FloatPropertyCompat> set, List<UpdateListener> list, List<EndListener> list2, List<Runnable> list3) {
            this.properties = set;
            this.updateListeners = list;
            this.endListeners = list2;
            this.endActions = list3;
            this.numPropertiesAnimating = set.size();
        }

        /* access modifiers changed from: package-private */
        public void onInternalAnimationUpdate(FloatPropertyCompat floatPropertyCompat, float f, float f2) {
            if (this.properties.contains(floatPropertyCompat)) {
                this.undispatchedUpdates.put(floatPropertyCompat, new AnimationUpdate(f, f2));
                maybeDispatchUpdates();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean onInternalAnimationEnd(FloatPropertyCompat floatPropertyCompat, boolean z, float f, float f2, boolean z2) {
            FloatPropertyCompat floatPropertyCompat2 = floatPropertyCompat;
            if (!this.properties.contains(floatPropertyCompat)) {
                return false;
            }
            this.numPropertiesAnimating--;
            maybeDispatchUpdates();
            if (this.undispatchedUpdates.containsKey(floatPropertyCompat)) {
                for (UpdateListener onAnimationUpdateForProperty : this.updateListeners) {
                    ArrayMap arrayMap = new ArrayMap();
                    arrayMap.put(floatPropertyCompat, this.undispatchedUpdates.get(floatPropertyCompat));
                    onAnimationUpdateForProperty.onAnimationUpdateForProperty(PhysicsAnimator.this.target, arrayMap);
                }
                this.undispatchedUpdates.remove(floatPropertyCompat);
            }
            boolean z3 = !PhysicsAnimator.this.arePropertiesAnimating(this.properties);
            for (EndListener onAnimationEnd : this.endListeners) {
                onAnimationEnd.onAnimationEnd(PhysicsAnimator.this.target, floatPropertyCompat, z2, z, f, f2, z3);
                if (PhysicsAnimator.this.isPropertyAnimating(floatPropertyCompat)) {
                    return false;
                }
            }
            if (z3 && !z) {
                for (Runnable next : this.endActions) {
                    if (next != null) {
                        next.run();
                    }
                }
            }
            return z3;
        }

        private void maybeDispatchUpdates() {
            if (this.undispatchedUpdates.size() >= this.numPropertiesAnimating && this.undispatchedUpdates.size() > 0) {
                for (UpdateListener onAnimationUpdateForProperty : this.updateListeners) {
                    onAnimationUpdateForProperty.onAnimationUpdateForProperty(PhysicsAnimator.this.target, new ArrayMap(this.undispatchedUpdates));
                }
                this.undispatchedUpdates.clear();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public PhysicsAnimator spring(FloatPropertyCompat floatPropertyCompat, float f, float f2, float f3, float f4) {
        if (this.verboseLogging) {
            Log.d("PhysicsAnimator", "Springing ${getReadablePropertyName(property)} to $toPosition.");
        }
        this.springConfigs.put(floatPropertyCompat, new SpringConfig(f3, f4, f2, f));
        return this;
    }

    public PhysicsAnimator spring(FloatPropertyCompat floatPropertyCompat, float f, float f2, SpringConfig springConfig) {
        spring(floatPropertyCompat, f, f2, springConfig.stiffness, springConfig.dampingRatio);
        return this;
    }

    public PhysicsAnimator spring(FloatPropertyCompat floatPropertyCompat, float f, SpringConfig springConfig) {
        spring(floatPropertyCompat, f, 0.0f, springConfig);
        return this;
    }

    public PhysicsAnimator flingThenSpring(FloatPropertyCompat floatPropertyCompat, float f, FlingConfig flingConfig, SpringConfig springConfig) {
        flingThenSpring(floatPropertyCompat, f, flingConfig, springConfig, false);
        return this;
    }

    public PhysicsAnimator flingThenSpring(FloatPropertyCompat floatPropertyCompat, float f, FlingConfig flingConfig, SpringConfig springConfig, boolean z) {
        FlingConfig flingConfig2 = new FlingConfig(flingConfig.friction, flingConfig.min, flingConfig.max, flingConfig.startVelocity);
        SpringConfig springConfig2 = new SpringConfig(springConfig.stiffness, springConfig.dampingRatio, springConfig.startVelocity, springConfig.finalPosition);
        int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        float f2 = i < 0 ? flingConfig.min : flingConfig.max;
        if (!z || f2 == -3.4028235E38f || f2 == Float.MAX_VALUE) {
            flingConfig2.startVelocity = f;
        } else {
            float value = f2 - floatPropertyCompat.getValue(this.target);
            float f3 = flingConfig.friction * 4.2f * value;
            if (value > 0.0f && f >= 0.0f) {
                flingConfig2.startVelocity = Math.max(f3, f);
            } else if (value >= 0.0f || i > 0) {
                flingConfig2.startVelocity = f;
            } else {
                flingConfig2.startVelocity = Math.min(f3, f);
            }
            springConfig2.finalPosition = f2;
        }
        this.flingConfigs.put(floatPropertyCompat, flingConfig2);
        this.springConfigs.put(floatPropertyCompat, springConfig2);
        return this;
    }

    public PhysicsAnimator addUpdateListener(UpdateListener updateListener) {
        this.updateListeners.add(updateListener);
        return this;
    }

    public PhysicsAnimator addEndListener(EndListener endListener) {
        this.endListeners.add(endListener);
        return this;
    }

    public PhysicsAnimator withEndActions(Runnable runnable) {
        this.endActions.add(runnable);
        return this;
    }

    public void start() {
        startInternal();
    }

    /* access modifiers changed from: package-private */
    public void startInternal() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            Log.e("PhysicsAnimator", "Animations can only be started on the main thread. If you are seeing this message in a test, call PhysicsAnimatorTestUtils#prepareForTest in your test setup.");
        }
        ArrayList arrayList = new ArrayList();
        for (final FloatPropertyCompat next : getAnimatedProperties()) {
            final FlingConfig flingConfig = this.flingConfigs.get(next);
            final SpringConfig springConfig = this.springConfigs.get(next);
            final float value = next.getValue(this.target);
            if (flingConfig != null) {
                arrayList.add(new Runnable() {
                    public void run() {
                        FlingConfig flingConfig = flingConfig;
                        flingConfig.min = Math.min(value, flingConfig.min);
                        FlingConfig flingConfig2 = flingConfig;
                        flingConfig2.max = Math.max(value, flingConfig2.max);
                        PhysicsAnimator.this.cancel(next);
                        FlingAnimation access$000 = PhysicsAnimator.this.getFlingAnimation(next);
                        flingConfig.applyToAnimation(access$000);
                        access$000.start();
                    }
                });
            }
            if (springConfig != null) {
                if (flingConfig == null) {
                    final SpringAnimation springAnimation = getSpringAnimation(next);
                    springConfig.applyToAnimation(springAnimation);
                    arrayList.add(new Runnable(this) {
                        public void run() {
                            springAnimation.start();
                        }
                    });
                } else {
                    final float f = flingConfig.min;
                    final float f2 = flingConfig.max;
                    this.endListeners.add(0, new EndListener() {
                        public void onAnimationEnd(Object obj, FloatPropertyCompat floatPropertyCompat, boolean z, boolean z2, float f, float f2, boolean z3) {
                            if (floatPropertyCompat == next && z && !z2) {
                                boolean z4 = true;
                                boolean z5 = Math.abs(f2) > 0.0f;
                                if (f >= f && f <= f2) {
                                    z4 = false;
                                }
                                if (z5 || z4) {
                                    SpringConfig springConfig = springConfig;
                                    springConfig.startVelocity = f2;
                                    if (springConfig.finalPosition == PhysicsAnimator.this.UNSET) {
                                        if (z5) {
                                            springConfig.finalPosition = f2 < 0.0f ? f : f2;
                                        } else if (z4) {
                                            SpringConfig springConfig2 = springConfig;
                                            float f3 = f;
                                            if (f >= f3) {
                                                f3 = f2;
                                            }
                                            springConfig2.finalPosition = f3;
                                        }
                                    }
                                    SpringAnimation access$200 = PhysicsAnimator.this.getSpringAnimation(next);
                                    springConfig.applyToAnimation(access$200);
                                    access$200.start();
                                }
                            }
                        }
                    });
                }
            }
        }
        this.internalListeners.add(new InternalListener(getAnimatedProperties(), new ArrayList(this.updateListeners), new ArrayList(this.endListeners), new ArrayList(this.endActions)));
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ((Runnable) it.next()).run();
        }
        clearAnimator();
    }

    private void clearAnimator() {
        this.springConfigs.clear();
        this.flingConfigs.clear();
        this.updateListeners.clear();
        this.endListeners.clear();
        this.endActions.clear();
    }

    /* access modifiers changed from: private */
    public SpringAnimation getSpringAnimation(FloatPropertyCompat floatPropertyCompat) {
        if (this.springAnimations.containsKey(floatPropertyCompat)) {
            return this.springAnimations.get(floatPropertyCompat);
        }
        SpringAnimation springAnimation = new SpringAnimation(this.target, floatPropertyCompat);
        configureDynamicAnimation(springAnimation, floatPropertyCompat);
        SpringAnimation springAnimation2 = springAnimation;
        this.springAnimations.put(floatPropertyCompat, springAnimation2);
        return springAnimation2;
    }

    /* access modifiers changed from: private */
    public FlingAnimation getFlingAnimation(FloatPropertyCompat floatPropertyCompat) {
        if (this.flingAnimations.containsKey(floatPropertyCompat)) {
            return this.flingAnimations.get(floatPropertyCompat);
        }
        FlingAnimation flingAnimation = new FlingAnimation(this.target, floatPropertyCompat);
        configureDynamicAnimation(flingAnimation, floatPropertyCompat);
        return flingAnimation;
    }

    private DynamicAnimation configureDynamicAnimation(final DynamicAnimation dynamicAnimation, final FloatPropertyCompat floatPropertyCompat) {
        dynamicAnimation.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            public void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2) {
                Iterator<InternalListener> it = PhysicsAnimator.this.internalListeners.iterator();
                while (it.hasNext()) {
                    it.next().onInternalAnimationUpdate(floatPropertyCompat, f, f2);
                }
            }
        });
        dynamicAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                Iterator<InternalListener> it = PhysicsAnimator.this.internalListeners.iterator();
                while (it.hasNext()) {
                    InternalListener next = it.next();
                    if (next.onInternalAnimationEnd(floatPropertyCompat, z, f, f2, dynamicAnimation instanceof FlingAnimation)) {
                        PhysicsAnimator.this.internalListeners.remove(next);
                    }
                }
            }
        });
        return dynamicAnimation;
    }

    public boolean isPropertyAnimating(FloatPropertyCompat floatPropertyCompat) {
        SpringAnimation springAnimation = this.springAnimations.get(floatPropertyCompat);
        FlingAnimation flingAnimation = this.flingAnimations.get(floatPropertyCompat);
        if ((springAnimation == null || !springAnimation.isRunning()) && flingAnimation != null) {
            boolean isRunning = flingAnimation.isRunning();
        }
        return (springAnimation != null && springAnimation.isRunning()) || (flingAnimation != null && flingAnimation.isRunning());
    }

    public boolean arePropertiesAnimating(Set<FloatPropertyCompat> set) {
        for (FloatPropertyCompat isPropertyAnimating : set) {
            if (isPropertyAnimating(isPropertyAnimating)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public Set<FloatPropertyCompat> getAnimatedProperties() {
        ArraySet arraySet = new ArraySet();
        arraySet.addAll(this.springConfigs.keySet());
        arraySet.addAll(this.flingConfigs.keySet());
        return arraySet;
    }

    /* access modifiers changed from: package-private */
    public void cancelInternal(Set<FloatPropertyCompat> set) {
        for (FloatPropertyCompat cancel : set) {
            cancel(cancel);
        }
    }

    public void cancel() {
        cancelInternal(this.flingAnimations.keySet());
        cancelInternal(this.springAnimations.keySet());
    }

    public void cancel(FloatPropertyCompat floatPropertyCompat) {
        if (this.flingAnimations.get(floatPropertyCompat) != null) {
            this.flingAnimations.get(floatPropertyCompat).cancel();
        }
        if (this.springAnimations.get(floatPropertyCompat) != null) {
            this.springAnimations.get(floatPropertyCompat).cancel();
        }
    }
}
