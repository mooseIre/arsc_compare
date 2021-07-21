package com.android.systemui.bubbles.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.FloatProperty;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.C0015R$id;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
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

    public static abstract class PhysicsAnimationController {
        protected PhysicsAnimationLayout mLayout;

        public interface ChildAnimationConfigurator {
            void configureAnimationForChildAtIndex(int i, PhysicsPropertyAnimator physicsPropertyAnimator);
        }

        public interface MultiAnimationStarter {
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

        public boolean isActiveController() {
            PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
            return physicsAnimationLayout != null && this == physicsAnimationLayout.mController;
        }

        public void setLayout(PhysicsAnimationLayout physicsAnimationLayout) {
            this.mLayout = physicsAnimationLayout;
            onActiveControllerForLayout(physicsAnimationLayout);
        }

        public PhysicsPropertyAnimator animationForChild(View view) {
            PhysicsPropertyAnimator physicsPropertyAnimator = (PhysicsPropertyAnimator) view.getTag(C0015R$id.physics_animator_tag);
            if (physicsPropertyAnimator == null) {
                PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
                Objects.requireNonNull(physicsAnimationLayout);
                physicsPropertyAnimator = new PhysicsPropertyAnimator(view);
                view.setTag(C0015R$id.physics_animator_tag, physicsPropertyAnimator);
            }
            physicsPropertyAnimator.clearAnimator();
            physicsPropertyAnimator.setAssociatedController(this);
            return physicsPropertyAnimator;
        }

        public PhysicsPropertyAnimator animationForChildAtIndex(int i) {
            return animationForChild(this.mLayout.getChildAt(i));
        }

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
                /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$PhysicsAnimationController$QukG2X_vIQ5QkpRissMu_oS31l0 */
                public final /* synthetic */ Set f$1;
                public final /* synthetic */ List f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController.MultiAnimationStarter
                public final void startAll(Runnable[] runnableArr) {
                    PhysicsAnimationLayout.PhysicsAnimationController.this.lambda$animationsForChildrenFromIndex$1$PhysicsAnimationLayout$PhysicsAnimationController(this.f$1, this.f$2, runnableArr);
                }
            };
        }

        /* access modifiers changed from: public */
        /* access modifiers changed from: private */
        /* renamed from: lambda$animationsForChildrenFromIndex$1 */
        public /* synthetic */ void lambda$animationsForChildrenFromIndex$1$PhysicsAnimationLayout$PhysicsAnimationController(Set set, List list, Runnable[] runnableArr) {
            $$Lambda$PhysicsAnimationLayout$PhysicsAnimationController$Q2IEgFtVQbcjE9VQhU6hzQCTEA r0 = new Runnable(runnableArr) {
                /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$PhysicsAnimationController$Q2IEgFtVQbcjE9VQhU6hzQCTEA */
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
                setEndActionForMultipleProperties(r0, (DynamicAnimation.ViewProperty[]) set.toArray(new DynamicAnimation.ViewProperty[0]));
            }
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ((PhysicsPropertyAnimator) it.next()).start(new Runnable[0]);
            }
        }

        static /* synthetic */ void lambda$animationsForChildrenFromIndex$0(Runnable[] runnableArr) {
            for (Runnable runnable : runnableArr) {
                runnable.run();
            }
        }

        public void setEndActionForProperty(Runnable runnable, DynamicAnimation.ViewProperty viewProperty) {
            this.mLayout.mEndActionForProperty.put(viewProperty, runnable);
        }

        public void setEndActionForMultipleProperties(Runnable runnable, DynamicAnimation.ViewProperty... viewPropertyArr) {
            $$Lambda$PhysicsAnimationLayout$PhysicsAnimationController$k470cCDrnNZB7vKHsf7OzOwkMRY r0 = new Runnable(viewPropertyArr, runnable) {
                /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$PhysicsAnimationController$k470cCDrnNZB7vKHsf7OzOwkMRY */
                public final /* synthetic */ DynamicAnimation.ViewProperty[] f$1;
                public final /* synthetic */ Runnable f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    PhysicsAnimationLayout.PhysicsAnimationController.this.lambda$setEndActionForMultipleProperties$2$PhysicsAnimationLayout$PhysicsAnimationController(this.f$1, this.f$2);
                }
            };
            for (DynamicAnimation.ViewProperty viewProperty : viewPropertyArr) {
                setEndActionForProperty(r0, viewProperty);
            }
        }

        /* access modifiers changed from: public */
        /* access modifiers changed from: private */
        /* renamed from: lambda$setEndActionForMultipleProperties$2 */
        public /* synthetic */ void lambda$setEndActionForMultipleProperties$2$PhysicsAnimationLayout$PhysicsAnimationController(DynamicAnimation.ViewProperty[] viewPropertyArr, Runnable runnable) {
            if (!this.mLayout.arePropertiesAnimating(viewPropertyArr)) {
                runnable.run();
                for (DynamicAnimation.ViewProperty viewProperty : viewPropertyArr) {
                    removeEndActionForProperty(viewProperty);
                }
            }
        }

        public void removeEndActionForProperty(DynamicAnimation.ViewProperty viewProperty) {
            this.mLayout.mEndActionForProperty.remove(viewProperty);
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
        for (DynamicAnimation.ViewProperty viewProperty : this.mController.getAnimatedProperties()) {
            setUpAnimationsForProperty(viewProperty);
        }
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view, i, layoutParams, false);
    }

    public void removeView(View view) {
        if (this.mController != null) {
            int indexOfChild = indexOfChild(view);
            super.removeView(view);
            addTransientView(view, indexOfChild);
            this.mController.onChildRemoved(view, indexOfChild, new Runnable(view) {
                /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$VGQ81KsCYiJC0albwfA2McXCU */
                public final /* synthetic */ View f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PhysicsAnimationLayout.this.lambda$removeView$0$PhysicsAnimationLayout(this.f$1);
                }
            });
            return;
        }
        super.removeView(view);
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$removeView$0 */
    public /* synthetic */ void lambda$removeView$0$PhysicsAnimationLayout(View view) {
        cancelAnimationsOnView(view);
        removeTransientView(view);
    }

    public void removeViewAt(int i) {
        removeView(getChildAt(i));
    }

    public void reorderView(View view, int i) {
        if (view != null) {
            int indexOfChild = indexOfChild(view);
            super.removeView(view);
            addViewInternal(view, i, view.getLayoutParams(), true);
            PhysicsAnimationController physicsAnimationController = this.mController;
            if (physicsAnimationController != null) {
                physicsAnimationController.onChildReordered(view, indexOfChild, i);
            }
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
        ObjectAnimator targetAnimatorFromView = getTargetAnimatorFromView(view);
        for (DynamicAnimation.ViewProperty viewProperty : viewPropertyArr) {
            SpringAnimation animationFromView = getAnimationFromView(viewProperty, view);
            if (animationFromView != null && animationFromView.isRunning()) {
                return true;
            }
            if ((viewProperty.equals(DynamicAnimation.TRANSLATION_X) || viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) && targetAnimatorFromView != null && targetAnimatorFromView.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public void cancelAllAnimations() {
        PhysicsAnimationController physicsAnimationController = this.mController;
        if (physicsAnimationController != null) {
            cancelAllAnimationsOfProperties((DynamicAnimation.ViewProperty[]) physicsAnimationController.getAnimatedProperties().toArray(new DynamicAnimation.ViewProperty[0]));
        }
    }

    public void cancelAllAnimationsOfProperties(DynamicAnimation.ViewProperty... viewPropertyArr) {
        if (this.mController != null) {
            for (int i = 0; i < getChildCount(); i++) {
                for (DynamicAnimation.ViewProperty viewProperty : viewPropertyArr) {
                    SpringAnimation animationAtIndex = getAnimationAtIndex(viewProperty, i);
                    if (animationAtIndex != null) {
                        animationAtIndex.cancel();
                    }
                }
            }
        }
    }

    public void cancelAnimationsOnView(View view) {
        ObjectAnimator targetAnimatorFromView = getTargetAnimatorFromView(view);
        if (targetAnimatorFromView != null) {
            targetAnimatorFromView.cancel();
        }
        for (DynamicAnimation.ViewProperty viewProperty : this.mController.getAnimatedProperties()) {
            SpringAnimation animationFromView = getAnimationFromView(viewProperty, view);
            if (animationFromView != null) {
                animationFromView.cancel();
            }
        }
    }

    public boolean isActiveController(PhysicsAnimationController physicsAnimationController) {
        return this.mController == physicsAnimationController;
    }

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
        if (!(physicsAnimationController == null || z)) {
            for (DynamicAnimation.ViewProperty viewProperty : physicsAnimationController.getAnimatedProperties()) {
                setUpAnimationForChild(viewProperty, view, i);
            }
            this.mController.onChildAdded(view, i);
        }
    }

    private SpringAnimation getAnimationAtIndex(DynamicAnimation.ViewProperty viewProperty, int i) {
        return getAnimationFromView(viewProperty, getChildAt(i));
    }

    private SpringAnimation getAnimationFromView(DynamicAnimation.ViewProperty viewProperty, View view) {
        return (SpringAnimation) view.getTag(getTagIdForProperty(viewProperty));
    }

    private ObjectAnimator getTargetAnimatorFromView(View view) {
        return (ObjectAnimator) view.getTag(C0015R$id.target_animator_tag);
    }

    private void setUpAnimationsForProperty(DynamicAnimation.ViewProperty viewProperty) {
        for (int i = 0; i < getChildCount(); i++) {
            setUpAnimationForChild(viewProperty, getChildAt(i), i);
        }
    }

    private void setUpAnimationForChild(DynamicAnimation.ViewProperty viewProperty, View view, int i) {
        SpringAnimation springAnimation = new SpringAnimation(view, viewProperty);
        springAnimation.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener(view, viewProperty) {
            /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$zwbjiGEsnfRdNGFmqcdzTxp4TUg */
            public final /* synthetic */ View f$1;
            public final /* synthetic */ DynamicAnimation.ViewProperty f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener
            public final void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2) {
                PhysicsAnimationLayout.this.lambda$setUpAnimationForChild$1$PhysicsAnimationLayout(this.f$1, this.f$2, dynamicAnimation, f, f2);
            }
        });
        springAnimation.setSpring(this.mController.getSpringForce(viewProperty, view));
        springAnimation.addEndListener(new AllAnimationsForPropertyFinishedEndListener(viewProperty));
        view.setTag(getTagIdForProperty(viewProperty), springAnimation);
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$setUpAnimationForChild$1 */
    public /* synthetic */ void lambda$setUpAnimationForChild$1$PhysicsAnimationLayout(View view, DynamicAnimation.ViewProperty viewProperty, DynamicAnimation dynamicAnimation, float f, float f2) {
        SpringAnimation animationAtIndex;
        int indexOfChild = indexOfChild(view);
        int nextAnimationInChain = this.mController.getNextAnimationInChain(viewProperty, indexOfChild);
        if (nextAnimationInChain != -1 && indexOfChild >= 0) {
            float offsetForChainedPropertyAnimation = this.mController.getOffsetForChainedPropertyAnimation(viewProperty);
            if (nextAnimationInChain < getChildCount() && (animationAtIndex = getAnimationAtIndex(viewProperty, nextAnimationInChain)) != null) {
                animationAtIndex.animateToFinalPosition(f + offsetForChainedPropertyAnimation);
            }
        }
    }

    private int getTagIdForProperty(DynamicAnimation.ViewProperty viewProperty) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
            return C0015R$id.translation_x_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            return C0015R$id.translation_y_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.SCALE_X)) {
            return C0015R$id.scale_x_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.SCALE_Y)) {
            return C0015R$id.scale_y_dynamicanimation_tag;
        }
        if (viewProperty.equals(DynamicAnimation.ALPHA)) {
            return C0015R$id.alpha_dynamicanimation_tag;
        }
        return -1;
    }

    public class AllAnimationsForPropertyFinishedEndListener implements DynamicAnimation.OnAnimationEndListener {
        private DynamicAnimation.ViewProperty mProperty;

        AllAnimationsForPropertyFinishedEndListener(DynamicAnimation.ViewProperty viewProperty) {
            PhysicsAnimationLayout.this = r1;
            this.mProperty = viewProperty;
        }

        @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
        public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
            Runnable runnable;
            if (!PhysicsAnimationLayout.this.arePropertiesAnimating(this.mProperty) && PhysicsAnimationLayout.this.mEndActionForProperty.containsKey(this.mProperty) && (runnable = PhysicsAnimationLayout.this.mEndActionForProperty.get(this.mProperty)) != null) {
                runnable.run();
            }
        }
    }

    public class PhysicsPropertyAnimator {
        private Map<DynamicAnimation.ViewProperty, Float> mAnimatedProperties = new HashMap();
        private PhysicsAnimationController mAssociatedController;
        private PointF mCurrentPointOnPath = new PointF();
        private final FloatProperty<PhysicsPropertyAnimator> mCurrentPointOnPathXProperty = new FloatProperty<PhysicsPropertyAnimator>("PathX") {
            /* class com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.AnonymousClass1 */

            public void setValue(PhysicsPropertyAnimator physicsPropertyAnimator, float f) {
                PhysicsPropertyAnimator.this.mCurrentPointOnPath.x = f;
            }

            public Float get(PhysicsPropertyAnimator physicsPropertyAnimator) {
                return Float.valueOf(PhysicsPropertyAnimator.this.mCurrentPointOnPath.x);
            }
        };
        private final FloatProperty<PhysicsPropertyAnimator> mCurrentPointOnPathYProperty = new FloatProperty<PhysicsPropertyAnimator>("PathY") {
            /* class com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.AnonymousClass2 */

            public void setValue(PhysicsPropertyAnimator physicsPropertyAnimator, float f) {
                PhysicsPropertyAnimator.this.mCurrentPointOnPath.y = f;
            }

            public Float get(PhysicsPropertyAnimator physicsPropertyAnimator) {
                return Float.valueOf(PhysicsPropertyAnimator.this.mCurrentPointOnPath.y);
            }
        };
        private float mDampingRatio = -1.0f;
        private float mDefaultStartVelocity = -3.4028235E38f;
        private Map<DynamicAnimation.ViewProperty, Runnable[]> mEndActionsForProperty = new HashMap();
        private Map<DynamicAnimation.ViewProperty, Float> mInitialPropertyValues = new HashMap();
        private ObjectAnimator mPathAnimator;
        private Runnable[] mPositionEndActions;
        private Map<DynamicAnimation.ViewProperty, Float> mPositionStartVelocities = new HashMap();
        private long mStartDelay = 0;
        private float mStiffness = -1.0f;
        private View mView;

        protected PhysicsPropertyAnimator(View view) {
            PhysicsAnimationLayout.this = r3;
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

        public PhysicsPropertyAnimator translationX(float f, Runnable... runnableArr) {
            this.mPathAnimator = null;
            property(DynamicAnimation.TRANSLATION_X, f, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator translationY(float f, Runnable... runnableArr) {
            this.mPathAnimator = null;
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

        public PhysicsPropertyAnimator followAnimatedTargetAlongPath(Path path, int i, TimeInterpolator timeInterpolator, final Runnable... runnableArr) {
            ObjectAnimator objectAnimator = this.mPathAnimator;
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, this.mCurrentPointOnPathXProperty, this.mCurrentPointOnPathYProperty, path);
            this.mPathAnimator = ofFloat;
            if (runnableArr != null) {
                ofFloat.addListener(new AnimatorListenerAdapter(this) {
                    /* class com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.AnonymousClass3 */

                    public void onAnimationEnd(Animator animator) {
                        Runnable[] runnableArr = runnableArr;
                        for (Runnable runnable : runnableArr) {
                            if (runnable != null) {
                                runnable.run();
                            }
                        }
                    }
                });
            }
            this.mPathAnimator.setDuration((long) i);
            this.mPathAnimator.setInterpolator(timeInterpolator);
            clearTranslationValues();
            return this;
        }

        private void clearTranslationValues() {
            this.mAnimatedProperties.remove(DynamicAnimation.TRANSLATION_X);
            this.mAnimatedProperties.remove(DynamicAnimation.TRANSLATION_Y);
            this.mInitialPropertyValues.remove(DynamicAnimation.TRANSLATION_X);
            this.mInitialPropertyValues.remove(DynamicAnimation.TRANSLATION_Y);
            PhysicsAnimationLayout.this.mEndActionForProperty.remove(DynamicAnimation.TRANSLATION_X);
            PhysicsAnimationLayout.this.mEndActionForProperty.remove(DynamicAnimation.TRANSLATION_Y);
        }

        public PhysicsPropertyAnimator scaleX(float f, Runnable... runnableArr) {
            property(DynamicAnimation.SCALE_X, f, runnableArr);
            return this;
        }

        public PhysicsPropertyAnimator scaleY(float f, Runnable... runnableArr) {
            property(DynamicAnimation.SCALE_Y, f, runnableArr);
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

        public PhysicsPropertyAnimator withStartDelay(long j) {
            this.mStartDelay = j;
            return this;
        }

        public void start(Runnable... runnableArr) {
            if (!PhysicsAnimationLayout.this.isActiveController(this.mAssociatedController)) {
                Log.w("Bubbs.PAL", "Only the active animation controller is allowed to start animations. Use PhysicsAnimationLayout#setActiveController to set the active animation controller.");
                return;
            }
            Set<DynamicAnimation.ViewProperty> animatedProperties = getAnimatedProperties();
            if (runnableArr != null && runnableArr.length > 0) {
                this.mAssociatedController.setEndActionForMultipleProperties(new Runnable(runnableArr) {
                    /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$iuqdgR2C6CC4Qpac87e6S6WedyM */
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
                    /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$3DhSPSmkLIWL6PRkLpBmJ3MVps */
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
            if (this.mPathAnimator != null) {
                startPathAnimation();
            }
            for (DynamicAnimation.ViewProperty viewProperty : animatedProperties) {
                if (this.mPathAnimator == null || (!viewProperty.equals(DynamicAnimation.TRANSLATION_X) && !viewProperty.equals(DynamicAnimation.TRANSLATION_Y))) {
                    if (this.mInitialPropertyValues.containsKey(viewProperty)) {
                        viewProperty.setValue(this.mView, this.mInitialPropertyValues.get(viewProperty).floatValue());
                    }
                    SpringForce springForce = PhysicsAnimationLayout.this.mController.getSpringForce(viewProperty, this.mView);
                    View view = this.mView;
                    float floatValue = this.mAnimatedProperties.get(viewProperty).floatValue();
                    float floatValue2 = this.mPositionStartVelocities.getOrDefault(viewProperty, Float.valueOf(this.mDefaultStartVelocity)).floatValue();
                    long j = this.mStartDelay;
                    float f = this.mStiffness;
                    if (f < 0.0f) {
                        f = springForce.getStiffness();
                    }
                    float f2 = this.mDampingRatio;
                    animateValueForChild(viewProperty, view, floatValue, floatValue2, j, f, f2 >= 0.0f ? f2 : springForce.getDampingRatio(), this.mEndActionsForProperty.get(viewProperty));
                } else {
                    return;
                }
            }
            clearAnimator();
        }

        static /* synthetic */ void lambda$start$0(Runnable[] runnableArr) {
            for (Runnable runnable : runnableArr) {
                runnable.run();
            }
        }

        /* access modifiers changed from: public */
        /* access modifiers changed from: private */
        /* renamed from: lambda$start$1 */
        public /* synthetic */ void lambda$start$1$PhysicsAnimationLayout$PhysicsPropertyAnimator(SpringAnimation springAnimation, SpringAnimation springAnimation2) {
            if (!(springAnimation.isRunning() || springAnimation2.isRunning())) {
                Runnable[] runnableArr = this.mPositionEndActions;
                if (runnableArr != null) {
                    for (Runnable runnable : runnableArr) {
                        runnable.run();
                    }
                }
                this.mPositionEndActions = null;
            }
        }

        public Set<DynamicAnimation.ViewProperty> getAnimatedProperties() {
            HashSet hashSet = new HashSet(this.mAnimatedProperties.keySet());
            if (this.mPathAnimator != null) {
                hashSet.add(DynamicAnimation.TRANSLATION_X);
                hashSet.add(DynamicAnimation.TRANSLATION_Y);
            }
            return hashSet;
        }

        public void animateValueForChild(DynamicAnimation.ViewProperty viewProperty, View view, float f, float f2, long j, float f3, float f4, final Runnable... runnableArr) {
            SpringAnimation springAnimation;
            if (view != null && (springAnimation = (SpringAnimation) view.getTag(PhysicsAnimationLayout.this.getTagIdForProperty(viewProperty))) != null) {
                if (runnableArr != null) {
                    springAnimation.addEndListener(new OneTimeEndListener(this) {
                        /* class com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.AnonymousClass4 */

                        @Override // com.android.systemui.bubbles.animation.OneTimeEndListener, androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
                        public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                            super.onAnimationEnd(dynamicAnimation, z, f, f2);
                            for (Runnable runnable : runnableArr) {
                                runnable.run();
                            }
                        }
                    });
                }
                SpringForce spring = springAnimation.getSpring();
                if (spring != null) {
                    $$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$YrUNYDpshnd98P1tIxCkdc37pTc r1 = new Runnable(f3, f4, f2, springAnimation, f) {
                        /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$YrUNYDpshnd98P1tIxCkdc37pTc */
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
                    if (j > 0) {
                        PhysicsAnimationLayout.this.postDelayed(r1, j);
                    } else {
                        r1.run();
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

        private void updateValueForChild(DynamicAnimation.ViewProperty viewProperty, View view, float f) {
            SpringAnimation springAnimation;
            SpringForce spring;
            if (view != null && (springAnimation = (SpringAnimation) view.getTag(PhysicsAnimationLayout.this.getTagIdForProperty(viewProperty))) != null && (spring = springAnimation.getSpring()) != null) {
                spring.setFinalPosition(f);
                springAnimation.start();
            }
        }

        public void startPathAnimation() {
            final SpringForce springForce = PhysicsAnimationLayout.this.mController.getSpringForce(DynamicAnimation.TRANSLATION_X, this.mView);
            final SpringForce springForce2 = PhysicsAnimationLayout.this.mController.getSpringForce(DynamicAnimation.TRANSLATION_Y, this.mView);
            long j = this.mStartDelay;
            if (j > 0) {
                this.mPathAnimator.setStartDelay(j);
            }
            final $$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$VvmQYTYF92KoaeTMVxzFjdA4FFA r2 = new Runnable() {
                /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$VvmQYTYF92KoaeTMVxzFjdA4FFA */

                public final void run() {
                    PhysicsAnimationLayout.PhysicsPropertyAnimator.this.lambda$startPathAnimation$3$PhysicsAnimationLayout$PhysicsPropertyAnimator();
                }
            };
            this.mPathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(r2) {
                /* class com.android.systemui.bubbles.animation.$$Lambda$PhysicsAnimationLayout$PhysicsPropertyAnimator$1Xv4slF4ncwrmkshsfcHipCSgjk */
                public final /* synthetic */ Runnable f$0;

                {
                    this.f$0 = r1;
                }

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    PhysicsAnimationLayout.PhysicsPropertyAnimator.lambda$startPathAnimation$4(this.f$0, valueAnimator);
                }
            });
            this.mPathAnimator.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsPropertyAnimator.AnonymousClass5 */

                public void onAnimationStart(Animator animator) {
                    float f;
                    float f2;
                    PhysicsPropertyAnimator physicsPropertyAnimator = PhysicsPropertyAnimator.this;
                    DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
                    View view = physicsPropertyAnimator.mView;
                    float f3 = PhysicsPropertyAnimator.this.mCurrentPointOnPath.x;
                    float f4 = PhysicsPropertyAnimator.this.mDefaultStartVelocity;
                    float stiffness = PhysicsPropertyAnimator.this.mStiffness >= 0.0f ? PhysicsPropertyAnimator.this.mStiffness : springForce.getStiffness();
                    if (PhysicsPropertyAnimator.this.mDampingRatio >= 0.0f) {
                        f = PhysicsPropertyAnimator.this.mDampingRatio;
                    } else {
                        f = springForce.getDampingRatio();
                    }
                    physicsPropertyAnimator.animateValueForChild(viewProperty, view, f3, f4, 0, stiffness, f, new Runnable[0]);
                    PhysicsPropertyAnimator physicsPropertyAnimator2 = PhysicsPropertyAnimator.this;
                    DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
                    View view2 = physicsPropertyAnimator2.mView;
                    float f5 = PhysicsPropertyAnimator.this.mCurrentPointOnPath.y;
                    float f6 = PhysicsPropertyAnimator.this.mDefaultStartVelocity;
                    float stiffness2 = PhysicsPropertyAnimator.this.mStiffness >= 0.0f ? PhysicsPropertyAnimator.this.mStiffness : springForce2.getStiffness();
                    if (PhysicsPropertyAnimator.this.mDampingRatio >= 0.0f) {
                        f2 = PhysicsPropertyAnimator.this.mDampingRatio;
                    } else {
                        f2 = springForce2.getDampingRatio();
                    }
                    physicsPropertyAnimator2.animateValueForChild(viewProperty2, view2, f5, f6, 0, stiffness2, f2, new Runnable[0]);
                }

                public void onAnimationEnd(Animator animator) {
                    r2.run();
                }
            });
            ObjectAnimator targetAnimatorFromView = PhysicsAnimationLayout.this.getTargetAnimatorFromView(this.mView);
            if (targetAnimatorFromView != null) {
                targetAnimatorFromView.cancel();
            }
            this.mView.setTag(C0015R$id.target_animator_tag, this.mPathAnimator);
            this.mPathAnimator.start();
        }

        /* access modifiers changed from: public */
        /* access modifiers changed from: private */
        /* renamed from: lambda$startPathAnimation$3 */
        public /* synthetic */ void lambda$startPathAnimation$3$PhysicsAnimationLayout$PhysicsPropertyAnimator() {
            updateValueForChild(DynamicAnimation.TRANSLATION_X, this.mView, this.mCurrentPointOnPath.x);
            updateValueForChild(DynamicAnimation.TRANSLATION_Y, this.mView, this.mCurrentPointOnPath.y);
        }

        private void clearAnimator() {
            this.mInitialPropertyValues.clear();
            this.mAnimatedProperties.clear();
            this.mPositionStartVelocities.clear();
            this.mDefaultStartVelocity = -3.4028235E38f;
            this.mStartDelay = 0;
            this.mStiffness = -1.0f;
            this.mDampingRatio = -1.0f;
            this.mEndActionsForProperty.clear();
            this.mPathAnimator = null;
            this.mPositionEndActions = null;
        }

        private void setAssociatedController(PhysicsAnimationController physicsAnimationController) {
            this.mAssociatedController = physicsAnimationController;
        }
    }
}
