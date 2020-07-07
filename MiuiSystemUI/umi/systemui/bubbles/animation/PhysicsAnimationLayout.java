package com.android.systemui.bubbles.animation;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PhysicsAnimationLayout extends FrameLayout {
    protected PhysicsAnimationController mController;
    protected final HashMap<DynamicAnimation.ViewProperty, Runnable> mEndActionForProperty = new HashMap<>();

    /* access modifiers changed from: protected */
    public boolean canReceivePointerEvents() {
        return false;
    }

    static abstract class PhysicsAnimationController {
        protected PhysicsAnimationLayout mLayout;

        interface ChildAnimationConfigurator {
            void configureAnimationForChildAtIndex(int i, PhysicsPropertyAnimator physicsPropertyAnimator);
        }

        interface MultiAnimationStarter {
            void startAll(Runnable... runnableArr);
        }

        /* access modifiers changed from: package-private */
        public abstract Set<DynamicAnimation.ViewProperty> getAnimatedProperties();

        /* access modifiers changed from: package-private */
        public abstract int getNextAnimationInChain(DynamicAnimation.ViewProperty viewProperty, int i);

        /* access modifiers changed from: package-private */
        public abstract float getOffsetForChainedPropertyAnimation(DynamicAnimation.ViewProperty viewProperty);

        /* access modifiers changed from: package-private */
        public abstract SpringForce getSpringForce(DynamicAnimation.ViewProperty viewProperty, View view);

        /* access modifiers changed from: package-private */
        public abstract void onActiveControllerForLayout(PhysicsAnimationLayout physicsAnimationLayout);

        /* access modifiers changed from: package-private */
        public abstract void onChildAdded(View view, int i);

        /* access modifiers changed from: package-private */
        public abstract void onChildRemoved(View view, int i, Runnable runnable);

        /* access modifiers changed from: package-private */
        public abstract void onChildReordered(View view, int i, int i2);

        PhysicsAnimationController() {
        }

        /* access modifiers changed from: protected */
        public boolean isActiveController() {
            return this == this.mLayout.mController;
        }

        /* access modifiers changed from: protected */
        public void setLayout(PhysicsAnimationLayout physicsAnimationLayout) {
            this.mLayout = physicsAnimationLayout;
            onActiveControllerForLayout(physicsAnimationLayout);
        }

        /* access modifiers changed from: protected */
        public PhysicsPropertyAnimator animationForChild(View view) {
            PhysicsPropertyAnimator physicsPropertyAnimator = (PhysicsPropertyAnimator) view.getTag(R.id.physics_animator_tag);
            if (physicsPropertyAnimator == null) {
                PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
                Objects.requireNonNull(physicsAnimationLayout);
                physicsPropertyAnimator = new PhysicsPropertyAnimator(view);
                view.setTag(R.id.physics_animator_tag, physicsPropertyAnimator);
            }
            physicsPropertyAnimator.clearAnimator();
            physicsPropertyAnimator.setAssociatedController(this);
            return physicsPropertyAnimator;
        }

        /* access modifiers changed from: protected */
        public PhysicsPropertyAnimator animationForChildAtIndex(int i) {
            return animationForChild(this.mLayout.getChildAt(i));
        }

        /* access modifiers changed from: protected */
        public MultiAnimationStarter animationsForChildrenFromIndex(int i, ChildAnimationConfigurator childAnimationConfigurator) {
            HashSet hashSet = new HashSet();
            ArrayList arrayList = new ArrayList();
            while (i < this.mLayout.getChildCount()) {
                PhysicsPropertyAnimator animationForChildAtIndex = animationForChildAtIndex(i);
                childAnimationConfigurator.configureAnimationForChildAtIndex(i, animationForChildAtIndex);
                hashSet.addAll(animationForChildAtIndex.getAnimatedProperties());
                arrayList.add(animationForChildAtIndex);
                i++;
            }
            return new MultiAnimationStarter(hashSet, arrayList) {
                public final /* synthetic */ Set f$1;
                public final /* synthetic */ List f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void startAll(Runnable[] runnableArr) {
                    PhysicsAnimationLayout.PhysicsAnimationController.this.lambda$animationsForChildrenFromIndex$1$PhysicsAnimationLayout$PhysicsAnimationController(this.f$1, this.f$2, runnableArr);
                }
            };
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$animationsForChildrenFromIndex$1 */
        public /* synthetic */ void lambda$animationsForChildrenFromIndex$1$PhysicsAnimationLayout$PhysicsAnimationController(Set set, List list, Runnable[] runnableArr) {
            $$Lambda$PhysicsAnimationLayout$PhysicsAnimationController$Q2IEgFtVQbcjE9VQhU6hzQCTEA r0 = new Runnable(runnableArr) {
                public final /* synthetic */ Runnable[] f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    PhysicsAnimationLayout.PhysicsAnimationController.lambda$animationsForChildrenFromIndex$0(this.f$0);
                }
            };
            if (this.mLayout.getChildCount() == 0) {
                r0.run();
                return;
            }
            if (runnableArr != null) {
                this.mLayout.setEndActionForMultipleProperties(r0, (DynamicAnimation.ViewProperty[]) set.toArray(new DynamicAnimation.ViewProperty[0]));
            }
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ((PhysicsPropertyAnimator) it.next()).start(new Runnable[0]);
            }
        }

        static /* synthetic */ void lambda$animationsForChildrenFromIndex$0(Runnable[] runnableArr) {
            for (Runnable run : runnableArr) {
                run.run();
            }
        }
    }

    public PhysicsAnimationLayout(Context context) {
        super(context);
    }

    public void setActiveController(PhysicsAnimationController physicsAnimationController) {
        cancelAllAnimations();
        this.mEndActionForProperty.clear();
        this.mController = physicsAnimationController;
        physicsAnimationController.setLayout(this);
        for (DynamicAnimation.ViewProperty upAnimationsForProperty : this.mController.getAnimatedProperties()) {
            setUpAnimationsForProperty(upAnimationsForProperty);
        }
    }

    public void setEndActionForProperty(Runnable runnable, DynamicAnimation.ViewProperty viewProperty) {
        this.mEndActionForProperty.put(viewProperty, runnable);
    }

    public void setEndActionForMultipleProperties(Runnable runnable, DynamicAnimation.ViewProperty... viewPropertyArr) {
        $$Lambda$PhysicsAnimationLayout$6ge2pmTTnwvHqQK7y5u9mvtjqgk r0 = new Runnable(viewPropertyArr, runnable) {
            public final /* synthetic */ DynamicAnimation.ViewProperty[] f$1;
            public final /* synthetic */ Runnable f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                PhysicsAnimationLayout.this.lambda$setEndActionForMultipleProperties$0$PhysicsAnimationLayout(this.f$1, this.f$2);
            }
        };
        for (DynamicAnimation.ViewProperty endActionForProperty : viewPropertyArr) {
            setEndActionForProperty(r0, endActionForProperty);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setEndActionForMultipleProperties$0 */
    public /* synthetic */ void lambda$setEndActionForMultipleProperties$0$PhysicsAnimationLayout(DynamicAnimation.ViewProperty[] viewPropertyArr, Runnable runnable) {
        if (!arePropertiesAnimating(viewPropertyArr)) {
            runnable.run();
            for (DynamicAnimation.ViewProperty removeEndActionForProperty : viewPropertyArr) {
                removeEndActionForProperty(removeEndActionForProperty);
            }
        }
    }

    public void removeEndActionForProperty(DynamicAnimation.ViewProperty viewProperty) {
        this.mEndActionForProperty.remove(viewProperty);
    }

    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view, i, layoutParams, false);
    }

    public void removeView(View view) {
        if (this.mController != null) {
            int indexOfChild = indexOfChild(view);
            super.removeView(view);
            addTransientView(view, indexOfChild);
            this.mController.onChildRemoved(view, indexOfChild, new Runnable(view) {
                public final /* synthetic */ View f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhysicsAnimationLayout.this.lambda$removeView$1$PhysicsAnimationLayout(this.f$1);
                }
            });
            return;
        }
        super.removeView(view);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$removeView$1 */
    public /* synthetic */ void lambda$removeView$1$PhysicsAnimationLayout(View view) {
        cancelAnimationsOnView(view);
        removeTransientView(view);
    }

    public void removeViewAt(int i) {
        removeView(getChildAt(i));
    }

    public void reorderView(View view, int i) {
        int indexOfChild = indexOfChild(view);
        super.removeView(view);
        addViewInternal(view, i, view.getLayoutParams(), true);
        PhysicsAnimationController physicsAnimationController = this.mController;
        if (physicsAnimationController != null) {
            physicsAnimationController.onChildReordered(view, indexOfChild, i);
        }
    }

    public boolean arePropertiesAnimating(DynamicAnimation.ViewProperty... viewPropertyArr) {
        for (int i = 0; i < getChildCount(); i++) {
            if (arePropertiesAnimatingOnView(getChildAt(i), viewPropertyArr)) {
                return true;
            }
        }
        return false;
    }

    public boolean arePropertiesAnimatingOnView(View view, DynamicAnimation.ViewProperty... viewPropertyArr) {
        for (DynamicAnimation.ViewProperty animationFromView : viewPropertyArr) {
            SpringAnimation animationFromView2 = getAnimationFromView(animationFromView, view);
            if (animationFromView2 != null && animationFromView2.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public void cancelAllAnimations() {
        if (this.mController != null) {
            for (int i = 0; i < getChildCount(); i++) {
                for (DynamicAnimation.ViewProperty animationAtIndex : this.mController.getAnimatedProperties()) {
                    SpringAnimation animationAtIndex2 = getAnimationAtIndex(animationAtIndex, i);
                    if (animationAtIndex2 != null) {
                        animationAtIndex2.cancel();
                    }
                }
            }
        }
    }

    public void cancelAnimationsOnView(View view) {
        for (DynamicAnimation.ViewProperty animationFromView : this.mController.getAnimatedProperties()) {
            getAnimationFromView(animationFromView, view).cancel();
        }
    }

    /* access modifiers changed from: protected */
    public boolean isActiveController(PhysicsAnimationController physicsAnimationController) {
        return this.mController == physicsAnimationController;
    }

    /* access modifiers changed from: protected */
    public boolean isFirstChildXLeftOfCenter(float f) {
        if (getChildCount() <= 0 || f + ((float) (getChildAt(0).getWidth() / 2)) >= ((float) (getWidth() / 2))) {
            return false;
        }
        return true;
    }

    protected static String getReadablePropertyName(DynamicAnimation.ViewProperty viewProperty) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
            return "TRANSLATION_X";
        }
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            return "TRANSLATION_Y";
        }
        if (viewProperty.equals(DynamicAnimation.SCALE_X)) {
            return "SCALE_X";
        }
        if (viewProperty.equals(DynamicAnimation.SCALE_Y)) {
            return "SCALE_Y";
        }
        return viewProperty.equals(DynamicAnimation.ALPHA) ? "ALPHA" : "Unknown animation property.";
    }

    private void addViewInternal(View view, int i, ViewGroup.LayoutParams layoutParams, boolean z) {
        super.addView(view, i, layoutParams);
        PhysicsAnimationController physicsAnimationController = this.mController;
        if (physicsAnimationController != null && !z) {
            for (DynamicAnimation.ViewProperty upAnimationForChild : physicsAnimationController.getAnimatedProperties()) {
                setUpAnimationForChild(upAnimationForChild, view, i);
            }
            this.mController.onChildAdded(view, i);
        }
    }

    private SpringAnimation getAnimationAtIndex(DynamicAnimation.ViewProperty viewProperty, int i) {
        return getAnimationFromView(viewProperty, getChildAt(i));
    }

    /* access modifiers changed from: private */
    public SpringAnimation getAnimationFromView(DynamicAnimation.ViewProperty viewProperty, View view) {
        return (SpringAnimation) view.getTag(getTagIdForProperty(viewProperty));
    }

    private void setUpAnimationsForProperty(DynamicAnimation.ViewProperty viewProperty) {
        for (int i = 0; i < getChildCount(); i++) {
            setUpAnimationForChild(viewProperty, getChildAt(i), i);
        }
    }

    private void setUpAnimationForChild(DynamicAnimation.ViewProperty viewProperty, View view, int i) {
        SpringAnimation springAnimation = new SpringAnimation(view, viewProperty);
        springAnimation.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener(view, viewProperty) {
            public final /* synthetic */ View f$1;
            public final /* synthetic */ DynamicAnimation.ViewProperty f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2) {
                PhysicsAnimationLayout.this.lambda$setUpAnimationForChild$2$PhysicsAnimationLayout(this.f$1, this.f$2, dynamicAnimation, f, f2);
            }
        });
        springAnimation.setSpring(this.mController.getSpringForce(viewProperty, view));
        springAnimation.addEndListener(new AllAnimationsForPropertyFinishedEndListener(viewProperty));
        view.setTag(getTagIdForProperty(viewProperty), springAnimation);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setUpAnimationForChild$2 */
    public /* synthetic */ void lambda$setUpAnimationForChild$2$PhysicsAnimationLayout(View view, DynamicAnimation.ViewProperty viewProperty, DynamicAnimation dynamicAnimation, float f, float f2) {
        int indexOfChild = indexOfChild(view);
        int nextAnimationInChain = this.mController.getNextAnimationInChain(viewProperty, indexOfChild);
        if (nextAnimationInChain != -1 && indexOfChild >= 0) {
            float offsetForChainedPropertyAnimation = this.mController.getOffsetForChainedPropertyAnimation(viewProperty);
            if (nextAnimationInChain < getChildCount()) {
                getAnimationAtIndex(viewProperty, nextAnimationInChain).animateToFinalPosition(f + offsetForChainedPropertyAnimation);
            }
        }
    }

    /* access modifiers changed from: private */
    public int getTagIdForProperty(DynamicAnimation.ViewProperty viewProperty) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
            return R.id.translation_x_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            return R.id.translation_y_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.SCALE_X)) {
            return R.id.scale_x_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.SCALE_Y)) {
            return R.id.scale_y_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.ALPHA)) {
            return R.id.alpha_dynamicanimation_tag;
        }
        return -1;
    }

    protected class AllAnimationsForPropertyFinishedEndListener implements DynamicAnimation.OnAnimationEndListener {
        private DynamicAnimation.ViewProperty mProperty;

        AllAnimationsForPropertyFinishedEndListener(DynamicAnimation.ViewProperty viewProperty) {
            this.mProperty = viewProperty;
        }

        public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
            Runnable runnable;
            if (!PhysicsAnimationLayout.this.arePropertiesAnimating(this.mProperty) && PhysicsAnimationLayout.this.mEndActionForProperty.containsKey(this.mProperty) && (runnable = PhysicsAnimationLayout.this.mEndActionForProperty.get(this.mProperty)) != null) {
                runnable.run();
            }
        }
    }

    protected class PhysicsPropertyAnimator {
        private Map<DynamicAnimation.ViewProperty, Float> mAnimatedProperties = new HashMap();
        private PhysicsAnimationController mAssociatedController;
        private float mDampingRatio = -1.0f;
        private float mDefaultStartVelocity = -3.4028235E38f;
        private Map<DynamicAnimation.ViewProperty, Runnable[]> mEndActionsForProperty = new HashMap();
        private Map<DynamicAnimation.ViewProperty, Float> mInitialPropertyValues = new HashMap();
        private Runnable[] mPositionEndActions;
        private Map<DynamicAnimation.ViewProperty, Float> mPositionStartVelocities = new HashMap();
        private long mStartDelay = 0;
        private float mStiffness = -1.0f;
        private View mView;

        protected PhysicsPropertyAnimator(View view) {
            this.mView = view;
        }

        public PhysicsPropertyAnimator property(DynamicAnimation.ViewProperty viewProperty, float f, Runnable... runnableArr) {
            this.mAnimatedProperties.put(viewProperty, Float.valueOf(f));
            this.mEndActionsForProperty.put(viewProperty, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator alpha(float f, Runnable... runnableArr) {
            property(DynamicAnimation.ALPHA, f, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator alpha(float f, float f2, Runnable... runnableArr) {
            this.mInitialPropertyValues.put(DynamicAnimation.ALPHA, Float.valueOf(f));
            alpha(f2, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator translationX(float f, Runnable... runnableArr) {
            property(DynamicAnimation.TRANSLATION_X, f, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator translationX(float f, float f2, Runnable... runnableArr) {
            this.mInitialPropertyValues.put(DynamicAnimation.TRANSLATION_X, Float.valueOf(f));
            translationX(f2, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator translationY(float f, Runnable... runnableArr) {
            property(DynamicAnimation.TRANSLATION_Y, f, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator translationY(float f, float f2, Runnable... runnableArr) {
            this.mInitialPropertyValues.put(DynamicAnimation.TRANSLATION_Y, Float.valueOf(f));
            translationY(f2, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator position(float f, float f2, Runnable... runnableArr) {
            this.mPositionEndActions = runnableArr;
            translationX(f, new Runnable[0]);
            translationY(f2, new Runnable[0]);
            return this;
        }

        public PhysicsPropertyAnimator scaleX(float f, Runnable... runnableArr) {
            property(DynamicAnimation.SCALE_X, f, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator scaleX(float f, float f2, Runnable... runnableArr) {
            this.mInitialPropertyValues.put(DynamicAnimation.SCALE_X, Float.valueOf(f));
            scaleX(f2, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator scaleY(float f, Runnable... runnableArr) {
            property(DynamicAnimation.SCALE_Y, f, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator scaleY(float f, float f2, Runnable... runnableArr) {
            this.mInitialPropertyValues.put(DynamicAnimation.SCALE_Y, Float.valueOf(f));
            scaleY(f2, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator withDampingRatio(float f) {
            this.mDampingRatio = f;
            return this;
        }

        public PhysicsPropertyAnimator withStiffness(float f) {
            this.mStiffness = f;
            return this;
        }

        public PhysicsPropertyAnimator withPositionStartVelocities(float f, float f2) {
            this.mPositionStartVelocities.put(DynamicAnimation.TRANSLATION_X, Float.valueOf(f));
            this.mPositionStartVelocities.put(DynamicAnimation.TRANSLATION_Y, Float.valueOf(f2));
            return this;
        }

        public void start(Runnable... runnableArr) {
            if (!PhysicsAnimationLayout.this.isActiveController(this.mAssociatedController)) {
                Log.w("Bubbs.PAL", "Only the active animation controller is allowed to start animations. Use PhysicsAnimationLayout#setActiveController to set the active animation controller.");
                return;
            }
            Set<DynamicAnimation.ViewProperty> animatedProperties = getAnimatedProperties();
            if (runnableArr != null && runnableArr.length > 0) {
                PhysicsAnimationLayout.this.setEndActionForMultipleProperties(new Runnable(runnableArr) {
                    public final /* synthetic */ Runnable[] f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run() {
                        PhysicsAnimationLayout.PhysicsPropertyAnimator.lambda$start$0(this.f$0);
                    }
                }, (DynamicAnimation.ViewProperty[]) animatedProperties.toArray(new DynamicAnimation.ViewProperty[0]));
            }
            if (this.mPositionEndActions != null) {
                $$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$3DhSPSmkLIWL6PRkLpBmJ3MVps r3 = new Runnable(PhysicsAnimationLayout.this.getAnimationFromView(DynamicAnimation.TRANSLATION_X, this.mView), PhysicsAnimationLayout.this.getAnimationFromView(DynamicAnimation.TRANSLATION_Y, this.mView)) {
                    public final /* synthetic */ SpringAnimation f$1;
                    public final /* synthetic */ SpringAnimation f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        PhysicsAnimationLayout.PhysicsPropertyAnimator.this.lambda$start$1$PhysicsAnimationLayout$PhysicsPropertyAnimator(this.f$1, this.f$2);
                    }
                };
                this.mEndActionsForProperty.put(DynamicAnimation.TRANSLATION_X, new Runnable[]{r3});
                this.mEndActionsForProperty.put(DynamicAnimation.TRANSLATION_Y, new Runnable[]{r3});
            }
            for (DynamicAnimation.ViewProperty next : animatedProperties) {
                if (this.mInitialPropertyValues.containsKey(next)) {
                    next.setValue(this.mView, this.mInitialPropertyValues.get(next).floatValue());
                }
                SpringForce springForce = PhysicsAnimationLayout.this.mController.getSpringForce(next, this.mView);
                View view = this.mView;
                float floatValue = this.mAnimatedProperties.get(next).floatValue();
                float floatValue2 = this.mPositionStartVelocities.getOrDefault(next, Float.valueOf(this.mDefaultStartVelocity)).floatValue();
                long j = this.mStartDelay;
                float f = this.mStiffness;
                if (f < 0.0f) {
                    f = springForce.getStiffness();
                }
                float f2 = f;
                float f3 = this.mDampingRatio;
                animateValueForChild(next, view, floatValue, floatValue2, j, f2, f3 >= 0.0f ? f3 : springForce.getDampingRatio(), this.mEndActionsForProperty.get(next));
            }
            clearAnimator();
        }

        static /* synthetic */ void lambda$start$0(Runnable[] runnableArr) {
            for (Runnable run : runnableArr) {
                run.run();
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$start$1 */
        public /* synthetic */ void lambda$start$1$PhysicsAnimationLayout$PhysicsPropertyAnimator(SpringAnimation springAnimation, SpringAnimation springAnimation2) {
            if (!springAnimation.isRunning() && !springAnimation2.isRunning()) {
                Runnable[] runnableArr = this.mPositionEndActions;
                if (runnableArr != null) {
                    for (Runnable run : runnableArr) {
                        run.run();
                    }
                }
                this.mPositionEndActions = null;
            }
        }

        /* access modifiers changed from: protected */
        public Set<DynamicAnimation.ViewProperty> getAnimatedProperties() {
            return this.mAnimatedProperties.keySet();
        }

        /* access modifiers changed from: protected */
        public void animateValueForChild(DynamicAnimation.ViewProperty viewProperty, View view, float f, float f2, long j, float f3, float f4, Runnable[] runnableArr) {
            long j2 = j;
            final Runnable[] runnableArr2 = runnableArr;
            if (view != null) {
                DynamicAnimation.ViewProperty viewProperty2 = viewProperty;
                SpringAnimation springAnimation = (SpringAnimation) view.getTag(PhysicsAnimationLayout.this.getTagIdForProperty(viewProperty));
                if (runnableArr2 != null) {
                    springAnimation.addEndListener(new OneTimeEndListener(this) {
                        public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                            super.onAnimationEnd(dynamicAnimation, z, f, f2);
                            for (Runnable run : runnableArr2) {
                                run.run();
                            }
                        }
                    });
                }
                SpringForce spring = springAnimation.getSpring();
                if (spring != null) {
                    $$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$YrUNYDpshnd98P1tIxCkdc37pTc r4 = new Runnable(f3, f4, f2, springAnimation, f) {
                        public final /* synthetic */ float f$1;
                        public final /* synthetic */ float f$2;
                        public final /* synthetic */ float f$3;
                        public final /* synthetic */ SpringAnimation f$4;
                        public final /* synthetic */ float f$5;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                            this.f$5 = r6;
                        }

                        public final void run() {
                            PhysicsAnimationLayout.PhysicsPropertyAnimator.lambda$animateValueForChild$2(SpringForce.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                        }
                    };
                    if (j2 > 0) {
                        PhysicsAnimationLayout.this.postDelayed(r4, j2);
                    } else {
                        r4.run();
                    }
                }
            }
        }

        static /* synthetic */ void lambda$animateValueForChild$2(SpringForce springForce, float f, float f2, float f3, SpringAnimation springAnimation, float f4) {
            springForce.setStiffness(f);
            springForce.setDampingRatio(f2);
            if (f3 > -3.4028235E38f) {
                springAnimation.setStartVelocity(f3);
            }
            springForce.setFinalPosition(f4);
            springAnimation.start();
        }

        /* access modifiers changed from: private */
        public void clearAnimator() {
            this.mInitialPropertyValues.clear();
            this.mAnimatedProperties.clear();
            this.mPositionStartVelocities.clear();
            this.mDefaultStartVelocity = -3.4028235E38f;
            this.mStartDelay = 0;
            this.mStiffness = -1.0f;
            this.mDampingRatio = -1.0f;
            this.mEndActionsForProperty.clear();
        }

        /* access modifiers changed from: private */
        public void setAssociatedController(PhysicsAnimationController physicsAnimationController) {
            this.mAssociatedController = physicsAnimationController;
        }
    }
}
