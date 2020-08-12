package com.android.systemui.bubbles.animation;

import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import com.android.systemui.plugins.R;
import com.google.android.collect.Sets;
import java.util.HashMap;
import java.util.Set;

public class StackAnimationController extends PhysicsAnimationLayout.PhysicsAnimationController {
    private int mBubbleOffscreen;
    private int mBubblePadding;
    private boolean mFirstBubbleSpringingToTouch = false;
    private float mImeHeight = 0.0f;
    private int mIndividualBubbleSize;
    private boolean mIsMovingFromFlinging = false;
    private float mPreImeY = Float.MIN_VALUE;
    private PointF mRestingStackPosition;
    private boolean mStackMovedToStartPosition = false;
    private float mStackOffset;
    private PointF mStackPosition = new PointF(-1.0f, -1.0f);
    private HashMap<DynamicAnimation.ViewProperty, DynamicAnimation> mStackPositionAnimations = new HashMap<>();
    private int mStackStartingVerticalOffset;
    private float mStatusBarHeight;
    private boolean mWithinDismissTarget = false;

    /* access modifiers changed from: package-private */
    public void onChildReordered(View view, int i, int i2) {
    }

    public void moveFirstBubbleWithStackFollowing(float f, float f2) {
        this.mPreImeY = Float.MIN_VALUE;
        moveFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_X, f);
        moveFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_Y, f2);
        this.mIsMovingFromFlinging = false;
    }

    public PointF getStackPosition() {
        return this.mStackPosition;
    }

    public boolean isStackOnLeftSide() {
        if (this.mLayout == null || !isStackPositionSet() || this.mStackPosition.x + ((float) (this.mIndividualBubbleSize / 2)) >= ((float) (this.mLayout.getWidth() / 2))) {
            return false;
        }
        return true;
    }

    public void springStack(float f, float f2) {
        DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
        SpringForce springForce = new SpringForce();
        springForce.setStiffness(750.0f);
        springForce.setDampingRatio(0.85f);
        springFirstBubbleWithStackFollowing(viewProperty, springForce, 0.0f, f);
        DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
        SpringForce springForce2 = new SpringForce();
        springForce2.setStiffness(750.0f);
        springForce2.setDampingRatio(0.85f);
        springFirstBubbleWithStackFollowing(viewProperty2, springForce2, 0.0f, f2);
    }

    public float flingStackThenSpringToEdge(float f, float f2, float f3) {
        float f4;
        float f5;
        float f6 = f2;
        boolean z = !(((f - ((float) (this.mIndividualBubbleSize / 2))) > ((float) (this.mLayout.getWidth() / 2)) ? 1 : ((f - ((float) (this.mIndividualBubbleSize / 2))) == ((float) (this.mLayout.getWidth() / 2)) ? 0 : -1)) < 0) ? f6 < -750.0f : f6 < 750.0f;
        RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
        if (z) {
            f4 = allowableStackPositionRegion.left;
        } else {
            f4 = allowableStackPositionRegion.right;
        }
        float f7 = f4;
        float f8 = (f7 - f) * 9.24f;
        if (z) {
            f5 = Math.min(f8, f2);
        } else {
            f5 = Math.max(f8, f2);
        }
        DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
        SpringForce springForce = new SpringForce();
        springForce.setStiffness(750.0f);
        springForce.setDampingRatio(0.85f);
        flingThenSpringFirstBubbleWithStackFollowing(viewProperty, f5, 2.2f, springForce, Float.valueOf(f7));
        DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
        SpringForce springForce2 = new SpringForce();
        springForce2.setStiffness(750.0f);
        springForce2.setDampingRatio(0.85f);
        flingThenSpringFirstBubbleWithStackFollowing(viewProperty2, f3, 2.2f, springForce2, (Float) null);
        this.mLayout.setEndActionForMultipleProperties(new Runnable() {
            public final void run() {
                StackAnimationController.this.lambda$flingStackThenSpringToEdge$0$StackAnimationController();
            }
        }, DynamicAnimation.TRANSLATION_X, DynamicAnimation.TRANSLATION_Y);
        this.mFirstBubbleSpringingToTouch = false;
        this.mIsMovingFromFlinging = true;
        return f7;
    }

    public /* synthetic */ void lambda$flingStackThenSpringToEdge$0$StackAnimationController() {
        this.mRestingStackPosition = new PointF();
        this.mRestingStackPosition.set(this.mStackPosition);
        this.mLayout.removeEndActionForProperty(DynamicAnimation.TRANSLATION_X);
        this.mLayout.removeEndActionForProperty(DynamicAnimation.TRANSLATION_Y);
    }

    public PointF getStackPositionAlongNearestHorizontalEdge() {
        PointF stackPosition = getStackPosition();
        boolean isFirstChildXLeftOfCenter = this.mLayout.isFirstChildXLeftOfCenter(stackPosition.x);
        RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
        stackPosition.x = isFirstChildXLeftOfCenter ? allowableStackPositionRegion.left : allowableStackPositionRegion.right;
        return stackPosition;
    }

    public void moveStackToSimilarPositionAfterRotation(boolean z, float f) {
        RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
        setStackPosition(new PointF(z ? allowableStackPositionRegion.left : allowableStackPositionRegion.right, ((allowableStackPositionRegion.bottom - allowableStackPositionRegion.top) * f) + allowableStackPositionRegion.top));
    }

    /* access modifiers changed from: protected */
    public void flingThenSpringFirstBubbleWithStackFollowing(DynamicAnimation.ViewProperty viewProperty, float f, float f2, SpringForce springForce, Float f3) {
        float f4;
        float f5;
        DynamicAnimation.ViewProperty viewProperty2 = viewProperty;
        Log.d("Bubbs.StackCtrl", String.format("Flinging %s.", new Object[]{PhysicsAnimationLayout.getReadablePropertyName(viewProperty)}));
        StackPositionProperty stackPositionProperty = new StackPositionProperty(viewProperty);
        float value = stackPositionProperty.getValue(this);
        RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
            f4 = allowableStackPositionRegion.left;
        } else {
            f4 = allowableStackPositionRegion.top;
        }
        float f6 = f4;
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
            f5 = allowableStackPositionRegion.right;
        } else {
            f5 = allowableStackPositionRegion.bottom;
        }
        float f7 = f5;
        FlingAnimation flingAnimation = new FlingAnimation(this, stackPositionProperty);
        float f8 = f2;
        flingAnimation.setFriction(f2);
        float f9 = f;
        flingAnimation.setStartVelocity(f);
        flingAnimation.setMinValue(Math.min(value, f6));
        flingAnimation.setMaxValue(Math.max(value, f7));
        flingAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener(viewProperty, springForce, f3, f6, f7) {
            private final /* synthetic */ DynamicAnimation.ViewProperty f$1;
            private final /* synthetic */ SpringForce f$2;
            private final /* synthetic */ Float f$3;
            private final /* synthetic */ float f$4;
            private final /* synthetic */ float f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                StackAnimationController.this.lambda$flingThenSpringFirstBubbleWithStackFollowing$1$StackAnimationController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, dynamicAnimation, z, f, f2);
            }
        });
        cancelStackPositionAnimation(viewProperty);
        this.mStackPositionAnimations.put(viewProperty, flingAnimation);
        flingAnimation.start();
    }

    public /* synthetic */ void lambda$flingThenSpringFirstBubbleWithStackFollowing$1$StackAnimationController(DynamicAnimation.ViewProperty viewProperty, SpringForce springForce, Float f, float f2, float f3, DynamicAnimation dynamicAnimation, boolean z, float f4, float f5) {
        float f6;
        if (!z) {
            if (f != null) {
                f6 = f.floatValue();
            } else {
                f6 = Math.max(f2, Math.min(f3, f4));
            }
            springFirstBubbleWithStackFollowing(viewProperty, springForce, f5, f6);
        }
    }

    public void cancelStackPositionAnimations() {
        cancelStackPositionAnimation(DynamicAnimation.TRANSLATION_X);
        cancelStackPositionAnimation(DynamicAnimation.TRANSLATION_Y);
        this.mLayout.removeEndActionForProperty(DynamicAnimation.TRANSLATION_X);
        this.mLayout.removeEndActionForProperty(DynamicAnimation.TRANSLATION_Y);
    }

    public void setImeHeight(int i) {
        this.mImeHeight = (float) i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:15:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void animateForImeVisibility(boolean r4) {
        /*
            r3 = this;
            android.graphics.RectF r0 = r3.getAllowableStackPositionRegion()
            float r0 = r0.bottom
            r1 = 1
            if (r4 == 0) goto L_0x001a
            android.graphics.PointF r4 = r3.mStackPosition
            float r4 = r4.y
            int r2 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1))
            if (r2 <= 0) goto L_0x0023
            float r2 = r3.mPreImeY
            int r2 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1))
            if (r2 != 0) goto L_0x0023
            r3.mPreImeY = r4
            goto L_0x0024
        L_0x001a:
            float r0 = r3.mPreImeY
            int r4 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r4 <= 0) goto L_0x0023
            r3.mPreImeY = r1
            goto L_0x0024
        L_0x0023:
            r0 = r1
        L_0x0024:
            int r4 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r4 <= 0) goto L_0x0038
            androidx.dynamicanimation.animation.DynamicAnimation$ViewProperty r4 = androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_Y
            r1 = 0
            androidx.dynamicanimation.animation.SpringForce r1 = r3.getSpringForce(r4, r1)
            r2 = 1128792064(0x43480000, float:200.0)
            r1.setStiffness(r2)
            r2 = 0
            r3.springFirstBubbleWithStackFollowing(r4, r1, r2, r0)
        L_0x0038:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.animation.StackAnimationController.animateForImeVisibility(boolean):void");
    }

    public RectF getAllowableStackPositionRegion() {
        WindowInsets rootWindowInsets = this.mLayout.getRootWindowInsets();
        RectF rectF = new RectF();
        if (rootWindowInsets != null) {
            int i = 0;
            rectF.left = (float) ((-this.mBubbleOffscreen) + Math.max(rootWindowInsets.getSystemWindowInsetLeft(), rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetLeft() : 0));
            rectF.right = (float) (((this.mLayout.getWidth() - this.mIndividualBubbleSize) + this.mBubbleOffscreen) - Math.max(rootWindowInsets.getSystemWindowInsetRight(), rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetRight() : 0));
            float f = 0.0f;
            rectF.top = ((float) this.mBubblePadding) + Math.max(this.mStatusBarHeight, rootWindowInsets.getDisplayCutout() != null ? (float) rootWindowInsets.getDisplayCutout().getSafeInsetTop() : 0.0f);
            int height = this.mLayout.getHeight() - this.mIndividualBubbleSize;
            int i2 = this.mBubblePadding;
            float f2 = (float) (height - i2);
            float f3 = this.mImeHeight;
            if (f3 > Float.MIN_VALUE) {
                f = f3 + ((float) i2);
            }
            float f4 = f2 - f;
            int systemWindowInsetBottom = rootWindowInsets.getSystemWindowInsetBottom();
            if (rootWindowInsets.getDisplayCutout() != null) {
                i = rootWindowInsets.getDisplayCutout().getSafeInsetBottom();
            }
            rectF.bottom = f4 - ((float) Math.max(systemWindowInsetBottom, i));
        }
        return rectF;
    }

    public void moveStackFromTouch(float f, float f2) {
        if (this.mFirstBubbleSpringingToTouch) {
            SpringAnimation springAnimation = (SpringAnimation) this.mStackPositionAnimations.get(DynamicAnimation.TRANSLATION_X);
            SpringAnimation springAnimation2 = (SpringAnimation) this.mStackPositionAnimations.get(DynamicAnimation.TRANSLATION_Y);
            if (springAnimation.isRunning() || springAnimation2.isRunning()) {
                springAnimation.animateToFinalPosition(f);
                springAnimation2.animateToFinalPosition(f2);
            } else {
                this.mFirstBubbleSpringingToTouch = false;
            }
        }
        if (!this.mFirstBubbleSpringingToTouch && !this.mWithinDismissTarget) {
            moveFirstBubbleWithStackFollowing(f, f2);
        }
    }

    public void demagnetizeFromDismissToPoint(float f, float f2, float f3, float f4) {
        this.mWithinDismissTarget = false;
        this.mFirstBubbleSpringingToTouch = true;
        DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
        SpringForce springForce = new SpringForce();
        springForce.setDampingRatio(0.9f);
        springForce.setStiffness(12000.0f);
        springFirstBubbleWithStackFollowing(viewProperty, springForce, f3, f);
        DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
        SpringForce springForce2 = new SpringForce();
        springForce2.setDampingRatio(0.9f);
        springForce2.setStiffness(12000.0f);
        springFirstBubbleWithStackFollowing(viewProperty2, springForce2, f4, f2);
    }

    public void magnetToDismiss(float f, float f2, float f3, Runnable runnable) {
        this.mWithinDismissTarget = true;
        this.mFirstBubbleSpringingToTouch = false;
        PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChildAtIndex = animationForChildAtIndex(0);
        animationForChildAtIndex.translationX((((float) this.mLayout.getWidth()) / 2.0f) - (((float) this.mIndividualBubbleSize) / 2.0f), new Runnable[0]);
        animationForChildAtIndex.translationY(f3, runnable);
        animationForChildAtIndex.withPositionStartVelocities(f, f2);
        animationForChildAtIndex.withStiffness(1500.0f);
        animationForChildAtIndex.withDampingRatio(0.75f);
        animationForChildAtIndex.start(new Runnable[0]);
    }

    public void implodeStack(Runnable runnable) {
        PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChildAtIndex = animationForChildAtIndex(0);
        animationForChildAtIndex.scaleX(0.5f, new Runnable[0]);
        animationForChildAtIndex.scaleY(0.5f, new Runnable[0]);
        animationForChildAtIndex.alpha(0.0f, new Runnable[0]);
        animationForChildAtIndex.withDampingRatio(1.0f);
        animationForChildAtIndex.withStiffness(10000.0f);
        animationForChildAtIndex.start(new Runnable(runnable) {
            private final /* synthetic */ Runnable f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StackAnimationController.this.lambda$implodeStack$2$StackAnimationController(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$implodeStack$2$StackAnimationController(Runnable runnable) {
        runnable.run();
        this.mWithinDismissTarget = false;
    }

    /* access modifiers changed from: protected */
    public void springFirstBubbleWithStackFollowing(DynamicAnimation.ViewProperty viewProperty, SpringForce springForce, float f, float f2) {
        if (this.mLayout.getChildCount() != 0) {
            Log.d("Bubbs.StackCtrl", String.format("Springing %s to final position %f.", new Object[]{PhysicsAnimationLayout.getReadablePropertyName(viewProperty), Float.valueOf(f2)}));
            SpringAnimation springAnimation = new SpringAnimation(this, new StackPositionProperty(viewProperty));
            springAnimation.setSpring(springForce);
            springAnimation.setStartVelocity(f);
            SpringAnimation springAnimation2 = springAnimation;
            cancelStackPositionAnimation(viewProperty);
            this.mStackPositionAnimations.put(viewProperty, springAnimation2);
            springAnimation2.animateToFinalPosition(f2);
        }
    }

    /* access modifiers changed from: package-private */
    public Set<DynamicAnimation.ViewProperty> getAnimatedProperties() {
        return Sets.newHashSet(new DynamicAnimation.ViewProperty[]{DynamicAnimation.TRANSLATION_X, DynamicAnimation.TRANSLATION_Y, DynamicAnimation.ALPHA, DynamicAnimation.SCALE_X, DynamicAnimation.SCALE_Y});
    }

    /* access modifiers changed from: package-private */
    public int getNextAnimationInChain(DynamicAnimation.ViewProperty viewProperty, int i) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X) || viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            return i + 1;
        }
        if (this.mWithinDismissTarget) {
            return i + 1;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public float getOffsetForChainedPropertyAnimation(DynamicAnimation.ViewProperty viewProperty) {
        if (!viewProperty.equals(DynamicAnimation.TRANSLATION_X) || this.mWithinDismissTarget) {
            return 0.0f;
        }
        return this.mLayout.isFirstChildXLeftOfCenter(this.mStackPosition.x) ? -this.mStackOffset : this.mStackOffset;
    }

    /* access modifiers changed from: package-private */
    public SpringForce getSpringForce(DynamicAnimation.ViewProperty viewProperty, View view) {
        SpringForce springForce = new SpringForce();
        springForce.setDampingRatio(0.9f);
        springForce.setStiffness(this.mIsMovingFromFlinging ? 20000.0f : 12000.0f);
        return springForce;
    }

    /* access modifiers changed from: package-private */
    public void onChildAdded(View view, int i) {
        if (this.mLayout.getChildCount() == 1) {
            moveStackToStartPosition();
        } else if (isStackPositionSet() && this.mLayout.indexOfChild(view) == 0) {
            animateInBubble(view);
        }
    }

    /* access modifiers changed from: package-private */
    public void onChildRemoved(View view, int i, Runnable runnable) {
        float offsetForChainedPropertyAnimation = getOffsetForChainedPropertyAnimation(DynamicAnimation.TRANSLATION_X);
        PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChild = animationForChild(view);
        animationForChild.alpha(0.0f, runnable);
        animationForChild.scaleX(1.15f, new Runnable[0]);
        animationForChild.scaleY(1.15f, new Runnable[0]);
        animationForChild.translationX(this.mStackPosition.x - ((-offsetForChainedPropertyAnimation) * 4.0f), new Runnable[0]);
        animationForChild.start(new Runnable[0]);
        if (this.mLayout.getChildCount() > 0) {
            PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChildAtIndex = animationForChildAtIndex(0);
            animationForChildAtIndex.translationX(this.mStackPosition.x, new Runnable[0]);
            animationForChildAtIndex.start(new Runnable[0]);
            return;
        }
        this.mStackPosition = getDefaultStartPosition();
    }

    /* access modifiers changed from: package-private */
    public void onActiveControllerForLayout(PhysicsAnimationLayout physicsAnimationLayout) {
        Resources resources = physicsAnimationLayout.getResources();
        this.mStackOffset = (float) resources.getDimensionPixelSize(R.dimen.bubble_stack_offset);
        this.mIndividualBubbleSize = resources.getDimensionPixelSize(R.dimen.individual_bubble_size);
        this.mBubblePadding = resources.getDimensionPixelSize(R.dimen.bubble_padding);
        this.mBubbleOffscreen = resources.getDimensionPixelSize(R.dimen.bubble_stack_offscreen);
        this.mStackStartingVerticalOffset = resources.getDimensionPixelSize(R.dimen.bubble_stack_starting_offset_y);
        this.mStatusBarHeight = (float) resources.getDimensionPixelSize(17105439);
    }

    private void moveStackToStartPosition() {
        this.mLayout.setVisibility(4);
        this.mLayout.post(new Runnable() {
            public final void run() {
                StackAnimationController.this.lambda$moveStackToStartPosition$3$StackAnimationController();
            }
        });
    }

    public /* synthetic */ void lambda$moveStackToStartPosition$3$StackAnimationController() {
        PointF pointF = this.mRestingStackPosition;
        if (pointF == null) {
            pointF = getDefaultStartPosition();
        }
        setStackPosition(pointF);
        this.mStackMovedToStartPosition = true;
        this.mLayout.setVisibility(0);
        if (this.mLayout.getChildCount() > 0) {
            animateInBubble(this.mLayout.getChildAt(0));
        }
    }

    /* access modifiers changed from: private */
    public void moveFirstBubbleWithStackFollowing(DynamicAnimation.ViewProperty viewProperty, float f) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
            this.mStackPosition.x = f;
        } else if (viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            this.mStackPosition.y = f;
        }
        if (this.mLayout.getChildCount() > 0) {
            viewProperty.setValue(this.mLayout.getChildAt(0), f);
            if (this.mLayout.getChildCount() > 1) {
                PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChildAtIndex = animationForChildAtIndex(1);
                animationForChildAtIndex.property(viewProperty, f + getOffsetForChainedPropertyAnimation(viewProperty), new Runnable[0]);
                animationForChildAtIndex.start(new Runnable[0]);
            }
        }
    }

    private void setStackPosition(PointF pointF) {
        Log.d("Bubbs.StackCtrl", String.format("Setting position to (%f, %f).", new Object[]{Float.valueOf(pointF.x), Float.valueOf(pointF.y)}));
        this.mStackPosition.set(pointF.x, pointF.y);
        if (isActiveController()) {
            this.mLayout.cancelAllAnimations();
            cancelStackPositionAnimations();
            float offsetForChainedPropertyAnimation = getOffsetForChainedPropertyAnimation(DynamicAnimation.TRANSLATION_X);
            float offsetForChainedPropertyAnimation2 = getOffsetForChainedPropertyAnimation(DynamicAnimation.TRANSLATION_Y);
            for (int i = 0; i < this.mLayout.getChildCount(); i++) {
                float f = (float) i;
                this.mLayout.getChildAt(i).setTranslationX(pointF.x + (f * offsetForChainedPropertyAnimation));
                this.mLayout.getChildAt(i).setTranslationY(pointF.y + (f * offsetForChainedPropertyAnimation2));
            }
        }
    }

    private PointF getDefaultStartPosition() {
        return new PointF(getAllowableStackPositionRegion().right, getAllowableStackPositionRegion().top + ((float) this.mStackStartingVerticalOffset));
    }

    private boolean isStackPositionSet() {
        return this.mStackMovedToStartPosition;
    }

    private void animateInBubble(View view) {
        if (isActiveController()) {
            view.setTranslationY(this.mStackPosition.y);
            float offsetForChainedPropertyAnimation = getOffsetForChainedPropertyAnimation(DynamicAnimation.TRANSLATION_X);
            PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChild = animationForChild(view);
            animationForChild.scaleX(1.15f, 1.0f, new Runnable[0]);
            animationForChild.scaleY(1.15f, 1.0f, new Runnable[0]);
            animationForChild.alpha(0.0f, 1.0f, new Runnable[0]);
            float f = this.mStackPosition.x;
            animationForChild.translationX(f - (offsetForChainedPropertyAnimation * 4.0f), f, new Runnable[0]);
            animationForChild.start(new Runnable[0]);
        }
    }

    private void cancelStackPositionAnimation(DynamicAnimation.ViewProperty viewProperty) {
        if (this.mStackPositionAnimations.containsKey(viewProperty)) {
            this.mStackPositionAnimations.get(viewProperty).cancel();
        }
    }

    private class StackPositionProperty extends FloatPropertyCompat<StackAnimationController> {
        private final DynamicAnimation.ViewProperty mProperty;

        private StackPositionProperty(DynamicAnimation.ViewProperty viewProperty) {
            super(viewProperty.toString());
            this.mProperty = viewProperty;
        }

        public float getValue(StackAnimationController stackAnimationController) {
            if (StackAnimationController.this.mLayout.getChildCount() > 0) {
                return this.mProperty.getValue(StackAnimationController.this.mLayout.getChildAt(0));
            }
            return 0.0f;
        }

        public void setValue(StackAnimationController stackAnimationController, float f) {
            StackAnimationController.this.moveFirstBubbleWithStackFollowing(this.mProperty, f);
        }
    }
}
