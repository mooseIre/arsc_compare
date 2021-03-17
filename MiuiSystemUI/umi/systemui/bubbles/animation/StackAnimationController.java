package com.android.systemui.bubbles.animation;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import com.google.android.collect.Sets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;
import java.util.function.IntSupplier;

public class StackAnimationController extends PhysicsAnimationLayout.PhysicsAnimationController {
    private final PhysicsAnimator.SpringConfig mAnimateOutSpringConfig = new PhysicsAnimator.SpringConfig(1000.0f, 1.0f);
    private Rect mAnimatingToBounds = new Rect();
    private int mBubbleBitmapSize;
    private IntSupplier mBubbleCountSupplier;
    private int mBubbleOffscreen;
    private int mBubblePaddingTop;
    private int mBubbleSize;
    private boolean mFirstBubbleSpringingToTouch = false;
    private FloatingContentCoordinator mFloatingContentCoordinator;
    private float mImeHeight = 0.0f;
    private boolean mIsMovingFromFlinging = false;
    private MagnetizedObject<StackAnimationController> mMagnetizedStack;
    private Runnable mOnBubbleAnimatedOutAction;
    private float mPreImeY = -1.4E-45f;
    private PointF mRestingStackPosition;
    private boolean mSpringToTouchOnNextMotionEvent = false;
    private final FloatingContentCoordinator.FloatingContent mStackFloatingContent = new FloatingContentCoordinator.FloatingContent() {
        /* class com.android.systemui.bubbles.animation.StackAnimationController.AnonymousClass1 */
        private final Rect mFloatingBoundsOnScreen = new Rect();

        @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
        public void moveToBounds(Rect rect) {
            StackAnimationController.this.springStack((float) rect.left, (float) rect.top, 200.0f);
        }

        @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
        public Rect getAllowedFloatingBoundsRegion() {
            Rect floatingBoundsOnScreen = getFloatingBoundsOnScreen();
            Rect rect = new Rect();
            StackAnimationController.this.getAllowableStackPositionRegion().roundOut(rect);
            rect.right += floatingBoundsOnScreen.width();
            rect.bottom += floatingBoundsOnScreen.height();
            return rect;
        }

        @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
        public Rect getFloatingBoundsOnScreen() {
            if (!StackAnimationController.this.mAnimatingToBounds.isEmpty()) {
                return StackAnimationController.this.mAnimatingToBounds;
            }
            if (StackAnimationController.this.mLayout.getChildCount() > 0) {
                this.mFloatingBoundsOnScreen.set((int) StackAnimationController.this.mStackPosition.x, (int) StackAnimationController.this.mStackPosition.y, ((int) StackAnimationController.this.mStackPosition.x) + StackAnimationController.this.mBubbleSize, ((int) StackAnimationController.this.mStackPosition.y) + StackAnimationController.this.mBubbleSize + StackAnimationController.this.mBubblePaddingTop);
            } else {
                this.mFloatingBoundsOnScreen.setEmpty();
            }
            return this.mFloatingBoundsOnScreen;
        }
    };
    private boolean mStackMovedToStartPosition = false;
    private float mStackOffset;
    private PointF mStackPosition = new PointF(-1.0f, -1.0f);
    private HashMap<DynamicAnimation.ViewProperty, DynamicAnimation> mStackPositionAnimations = new HashMap<>();
    private int mStackStartingVerticalOffset;
    private float mStatusBarHeight;

    public StackAnimationController(FloatingContentCoordinator floatingContentCoordinator, IntSupplier intSupplier, Runnable runnable) {
        this.mFloatingContentCoordinator = floatingContentCoordinator;
        this.mBubbleCountSupplier = intSupplier;
        this.mOnBubbleAnimatedOutAction = runnable;
    }

    public void moveFirstBubbleWithStackFollowing(float f, float f2) {
        this.mAnimatingToBounds.setEmpty();
        this.mPreImeY = -1.4E-45f;
        moveFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_X, f);
        moveFirstBubbleWithStackFollowing(DynamicAnimation.TRANSLATION_Y, f2);
        this.mIsMovingFromFlinging = false;
    }

    public PointF getStackPosition() {
        return this.mStackPosition;
    }

    public boolean isStackOnLeftSide() {
        if (this.mLayout == null || !isStackPositionSet() || this.mStackPosition.x + ((float) (this.mBubbleBitmapSize / 2)) < ((float) (this.mLayout.getWidth() / 2))) {
            return true;
        }
        return false;
    }

    public void springStack(float f, float f2, float f3) {
        notifyFloatingCoordinatorStackAnimatingTo(f, f2);
        DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
        SpringForce springForce = new SpringForce();
        springForce.setStiffness(f3);
        springForce.setDampingRatio(0.85f);
        springFirstBubbleWithStackFollowing(viewProperty, springForce, 0.0f, f, new Runnable[0]);
        DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
        SpringForce springForce2 = new SpringForce();
        springForce2.setStiffness(f3);
        springForce2.setDampingRatio(0.85f);
        springFirstBubbleWithStackFollowing(viewProperty2, springForce2, 0.0f, f2, new Runnable[0]);
    }

    public void springStackAfterFling(float f, float f2) {
        springStack(f, f2, 750.0f);
    }

    public float flingStackThenSpringToEdge(float f, float f2, float f3) {
        float f4;
        boolean z = !(((f - ((float) (this.mBubbleBitmapSize / 2))) > ((float) (this.mLayout.getWidth() / 2)) ? 1 : ((f - ((float) (this.mBubbleBitmapSize / 2))) == ((float) (this.mLayout.getWidth() / 2)) ? 0 : -1)) < 0) ? f2 < -750.0f : f2 < 750.0f;
        RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
        float f5 = z ? allowableStackPositionRegion.left : allowableStackPositionRegion.right;
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        if (!(physicsAnimationLayout == null || physicsAnimationLayout.getChildCount() == 0)) {
            ContentResolver contentResolver = this.mLayout.getContext().getContentResolver();
            float f6 = Settings.Secure.getFloat(contentResolver, "bubble_stiffness", 750.0f);
            float f7 = Settings.Secure.getFloat(contentResolver, "bubble_damping", 0.85f);
            float f8 = Settings.Secure.getFloat(contentResolver, "bubble_friction", 2.2f);
            float f9 = (f5 - f) * 4.2f * f8;
            notifyFloatingCoordinatorStackAnimatingTo(f5, PhysicsAnimator.estimateFlingEndValue(this.mStackPosition.y, f3, new PhysicsAnimator.FlingConfig(f8, allowableStackPositionRegion.top, allowableStackPositionRegion.bottom)));
            if (z) {
                f4 = Math.min(f9, f2);
            } else {
                f4 = Math.max(f9, f2);
            }
            DynamicAnimation.ViewProperty viewProperty = DynamicAnimation.TRANSLATION_X;
            SpringForce springForce = new SpringForce();
            springForce.setStiffness(f6);
            springForce.setDampingRatio(f7);
            flingThenSpringFirstBubbleWithStackFollowing(viewProperty, f4, f8, springForce, Float.valueOf(f5));
            DynamicAnimation.ViewProperty viewProperty2 = DynamicAnimation.TRANSLATION_Y;
            SpringForce springForce2 = new SpringForce();
            springForce2.setStiffness(f6);
            springForce2.setDampingRatio(f7);
            flingThenSpringFirstBubbleWithStackFollowing(viewProperty2, f3, f8, springForce2, null);
            this.mFirstBubbleSpringingToTouch = false;
            this.mIsMovingFromFlinging = true;
        }
        return f5;
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

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("StackAnimationController state:");
        printWriter.print("  isActive:             ");
        printWriter.println(isActiveController());
        printWriter.print("  restingStackPos:      ");
        PointF pointF = this.mRestingStackPosition;
        printWriter.println(pointF != null ? pointF.toString() : "null");
        printWriter.print("  currentStackPos:      ");
        printWriter.println(this.mStackPosition.toString());
        printWriter.print("  isMovingFromFlinging: ");
        printWriter.println(this.mIsMovingFromFlinging);
        printWriter.print("  withinDismiss:        ");
        printWriter.println(isStackStuckToTarget());
        printWriter.print("  firstBubbleSpringing: ");
        printWriter.println(this.mFirstBubbleSpringingToTouch);
    }

    /* access modifiers changed from: protected */
    public void flingThenSpringFirstBubbleWithStackFollowing(DynamicAnimation.ViewProperty viewProperty, float f, float f2, SpringForce springForce, Float f3) {
        float f4;
        float f5;
        if (isActiveController()) {
            Log.d("Bubbs.StackCtrl", String.format("Flinging %s.", PhysicsAnimationLayout.getReadablePropertyName(viewProperty)));
            StackPositionProperty stackPositionProperty = new StackPositionProperty(viewProperty);
            float value = stackPositionProperty.getValue(this);
            RectF allowableStackPositionRegion = getAllowableStackPositionRegion();
            if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
                f4 = allowableStackPositionRegion.left;
            } else {
                f4 = allowableStackPositionRegion.top;
            }
            if (viewProperty.equals(DynamicAnimation.TRANSLATION_X)) {
                f5 = allowableStackPositionRegion.right;
            } else {
                f5 = allowableStackPositionRegion.bottom;
            }
            FlingAnimation flingAnimation = new FlingAnimation(this, stackPositionProperty);
            flingAnimation.setFriction(f2);
            flingAnimation.setStartVelocity(f);
            flingAnimation.setMinValue(Math.min(value, f4));
            flingAnimation.setMaxValue(Math.max(value, f5));
            flingAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener(viewProperty, springForce, f3, f4, f5) {
                /* class com.android.systemui.bubbles.animation.$$Lambda$StackAnimationController$bZgezj9fblRl_isenTD4ApewvoU */
                public final /* synthetic */ DynamicAnimation.ViewProperty f$1;
                public final /* synthetic */ SpringForce f$2;
                public final /* synthetic */ Float f$3;
                public final /* synthetic */ float f$4;
                public final /* synthetic */ float f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
                public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                    StackAnimationController.this.lambda$flingThenSpringFirstBubbleWithStackFollowing$0$StackAnimationController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, dynamicAnimation, z, f, f2);
                }
            });
            cancelStackPositionAnimation(viewProperty);
            this.mStackPositionAnimations.put(viewProperty, flingAnimation);
            flingAnimation.start();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$flingThenSpringFirstBubbleWithStackFollowing$0 */
    public /* synthetic */ void lambda$flingThenSpringFirstBubbleWithStackFollowing$0$StackAnimationController(DynamicAnimation.ViewProperty viewProperty, SpringForce springForce, Float f, float f2, float f3, DynamicAnimation dynamicAnimation, boolean z, float f4, float f5) {
        float f6;
        if (!z) {
            this.mRestingStackPosition.set(this.mStackPosition);
            if (f != null) {
                f6 = f.floatValue();
            } else {
                f6 = Math.max(f2, Math.min(f3, f4));
            }
            springFirstBubbleWithStackFollowing(viewProperty, springForce, f5, f6, new Runnable[0]);
        }
    }

    public void cancelStackPositionAnimations() {
        cancelStackPositionAnimation(DynamicAnimation.TRANSLATION_X);
        cancelStackPositionAnimation(DynamicAnimation.TRANSLATION_Y);
        removeEndActionForProperty(DynamicAnimation.TRANSLATION_X);
        removeEndActionForProperty(DynamicAnimation.TRANSLATION_Y);
    }

    public void setImeHeight(int i) {
        this.mImeHeight = (float) i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x002a  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0049  */
    /* JADX WARNING: Removed duplicated region for block: B:17:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public float animateForImeVisibility(boolean r9) {
        /*
            r8 = this;
            android.graphics.RectF r0 = r8.getAllowableStackPositionRegion()
            float r0 = r0.bottom
            r1 = -2147483647(0xffffffff80000001, float:-1.4E-45)
            if (r9 == 0) goto L_0x001c
            android.graphics.PointF r9 = r8.mStackPosition
            float r9 = r9.y
            int r2 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r2 <= 0) goto L_0x0025
            float r2 = r8.mPreImeY
            int r2 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1))
            if (r2 != 0) goto L_0x0025
            r8.mPreImeY = r9
            goto L_0x0026
        L_0x001c:
            float r0 = r8.mPreImeY
            int r9 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r9 == 0) goto L_0x0025
            r8.mPreImeY = r1
            goto L_0x0026
        L_0x0025:
            r0 = r1
        L_0x0026:
            int r9 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r9 == 0) goto L_0x0046
            androidx.dynamicanimation.animation.DynamicAnimation$ViewProperty r3 = androidx.dynamicanimation.animation.DynamicAnimation.TRANSLATION_Y
            r1 = 0
            androidx.dynamicanimation.animation.SpringForce r4 = r8.getSpringForce(r3, r1)
            r1 = 1128792064(0x43480000, float:200.0)
            r4.setStiffness(r1)
            r5 = 0
            r1 = 0
            java.lang.Runnable[] r7 = new java.lang.Runnable[r1]
            r2 = r8
            r6 = r0
            r2.springFirstBubbleWithStackFollowing(r3, r4, r5, r6, r7)
            android.graphics.PointF r1 = r8.mStackPosition
            float r1 = r1.x
            r8.notifyFloatingCoordinatorStackAnimatingTo(r1, r0)
        L_0x0046:
            if (r9 == 0) goto L_0x0049
            goto L_0x004d
        L_0x0049:
            android.graphics.PointF r8 = r8.mStackPosition
            float r0 = r8.y
        L_0x004d:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.bubbles.animation.StackAnimationController.animateForImeVisibility(boolean):float");
    }

    private void notifyFloatingCoordinatorStackAnimatingTo(float f, float f2) {
        Rect floatingBoundsOnScreen = this.mStackFloatingContent.getFloatingBoundsOnScreen();
        floatingBoundsOnScreen.offsetTo((int) f, (int) f2);
        this.mAnimatingToBounds = floatingBoundsOnScreen;
        this.mFloatingContentCoordinator.onContentMoved(this.mStackFloatingContent);
    }

    public RectF getAllowableStackPositionRegion() {
        WindowInsets rootWindowInsets = this.mLayout.getRootWindowInsets();
        RectF rectF = new RectF();
        if (rootWindowInsets != null) {
            int i = 0;
            rectF.left = (float) ((-this.mBubbleOffscreen) + Math.max(rootWindowInsets.getSystemWindowInsetLeft(), rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetLeft() : 0));
            rectF.right = (float) (((this.mLayout.getWidth() - this.mBubbleSize) + this.mBubbleOffscreen) - Math.max(rootWindowInsets.getSystemWindowInsetRight(), rootWindowInsets.getDisplayCutout() != null ? rootWindowInsets.getDisplayCutout().getSafeInsetRight() : 0));
            float f = 0.0f;
            rectF.top = ((float) this.mBubblePaddingTop) + Math.max(this.mStatusBarHeight, rootWindowInsets.getDisplayCutout() != null ? (float) rootWindowInsets.getDisplayCutout().getSafeInsetTop() : 0.0f);
            int height = this.mLayout.getHeight() - this.mBubbleSize;
            int i2 = this.mBubblePaddingTop;
            float f2 = (float) (height - i2);
            float f3 = this.mImeHeight;
            if (f3 != -1.4E-45f) {
                f = f3 + ((float) i2);
            }
            float f4 = f2 - f;
            int stableInsetBottom = rootWindowInsets.getStableInsetBottom();
            if (rootWindowInsets.getDisplayCutout() != null) {
                i = rootWindowInsets.getDisplayCutout().getSafeInsetBottom();
            }
            rectF.bottom = f4 - ((float) Math.max(stableInsetBottom, i));
        }
        return rectF;
    }

    public void moveStackFromTouch(float f, float f2) {
        if (this.mSpringToTouchOnNextMotionEvent) {
            springStack(f, f2, 12000.0f);
            this.mSpringToTouchOnNextMotionEvent = false;
            this.mFirstBubbleSpringingToTouch = true;
        } else if (this.mFirstBubbleSpringingToTouch) {
            SpringAnimation springAnimation = (SpringAnimation) this.mStackPositionAnimations.get(DynamicAnimation.TRANSLATION_X);
            SpringAnimation springAnimation2 = (SpringAnimation) this.mStackPositionAnimations.get(DynamicAnimation.TRANSLATION_Y);
            if (springAnimation.isRunning() || springAnimation2.isRunning()) {
                springAnimation.animateToFinalPosition(f);
                springAnimation2.animateToFinalPosition(f2);
            } else {
                this.mFirstBubbleSpringingToTouch = false;
            }
        }
        if (!this.mFirstBubbleSpringingToTouch && !isStackStuckToTarget()) {
            moveFirstBubbleWithStackFollowing(f, f2);
        }
    }

    public void onUnstuckFromTarget() {
        this.mSpringToTouchOnNextMotionEvent = true;
    }

    public void animateStackDismissal(float f, Runnable runnable) {
        animationsForChildrenFromIndex(0, new PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator(f) {
            /* class com.android.systemui.bubbles.animation.$$Lambda$StackAnimationController$D5Qpma319hSMbP8sqDKnJq90JU */
            public final /* synthetic */ float f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController.ChildAnimationConfigurator
            public final void configureAnimationForChildAtIndex(int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
                StackAnimationController.this.lambda$animateStackDismissal$1$StackAnimationController(this.f$1, i, physicsPropertyAnimator);
            }
        }).startAll(runnable);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateStackDismissal$1 */
    public /* synthetic */ void lambda$animateStackDismissal$1$StackAnimationController(float f, int i, PhysicsAnimationLayout.PhysicsPropertyAnimator physicsPropertyAnimator) {
        physicsPropertyAnimator.scaleX(0.0f, new Runnable[0]);
        physicsPropertyAnimator.scaleY(0.0f, new Runnable[0]);
        physicsPropertyAnimator.alpha(0.0f, new Runnable[0]);
        physicsPropertyAnimator.translationY(this.mLayout.getChildAt(i).getTranslationY() + f, new Runnable[0]);
        physicsPropertyAnimator.withStiffness(10000.0f);
    }

    /* access modifiers changed from: protected */
    public void springFirstBubbleWithStackFollowing(DynamicAnimation.ViewProperty viewProperty, SpringForce springForce, float f, float f2, Runnable... runnableArr) {
        if (this.mLayout.getChildCount() != 0 && isActiveController()) {
            Log.d("Bubbs.StackCtrl", String.format("Springing %s to final position %f.", PhysicsAnimationLayout.getReadablePropertyName(viewProperty), Float.valueOf(f2)));
            boolean z = this.mSpringToTouchOnNextMotionEvent;
            SpringAnimation springAnimation = new SpringAnimation(this, new StackPositionProperty(viewProperty));
            springAnimation.setSpring(springForce);
            springAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener(z, runnableArr) {
                /* class com.android.systemui.bubbles.animation.$$Lambda$StackAnimationController$TVDnndU2JkVcHNzVQaINweVCLk */
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ Runnable[] f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
                public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                    StackAnimationController.this.lambda$springFirstBubbleWithStackFollowing$2$StackAnimationController(this.f$1, this.f$2, dynamicAnimation, z, f, f2);
                }
            });
            SpringAnimation springAnimation2 = springAnimation;
            springAnimation2.setStartVelocity(f);
            SpringAnimation springAnimation3 = springAnimation2;
            cancelStackPositionAnimation(viewProperty);
            this.mStackPositionAnimations.put(viewProperty, springAnimation3);
            springAnimation3.animateToFinalPosition(f2);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$springFirstBubbleWithStackFollowing$2 */
    public /* synthetic */ void lambda$springFirstBubbleWithStackFollowing$2$StackAnimationController(boolean z, Runnable[] runnableArr, DynamicAnimation dynamicAnimation, boolean z2, float f, float f2) {
        if (!z) {
            this.mRestingStackPosition.set(this.mStackPosition);
        }
        if (runnableArr != null) {
            for (Runnable runnable : runnableArr) {
                runnable.run();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    public Set<DynamicAnimation.ViewProperty> getAnimatedProperties() {
        return Sets.newHashSet(new DynamicAnimation.ViewProperty[]{DynamicAnimation.TRANSLATION_X, DynamicAnimation.TRANSLATION_Y, DynamicAnimation.ALPHA, DynamicAnimation.SCALE_X, DynamicAnimation.SCALE_Y});
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    public int getNextAnimationInChain(DynamicAnimation.ViewProperty viewProperty, int i) {
        if (viewProperty.equals(DynamicAnimation.TRANSLATION_X) || viewProperty.equals(DynamicAnimation.TRANSLATION_Y)) {
            return i + 1;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    public float getOffsetForChainedPropertyAnimation(DynamicAnimation.ViewProperty viewProperty) {
        if (!viewProperty.equals(DynamicAnimation.TRANSLATION_X) || isStackStuckToTarget()) {
            return 0.0f;
        }
        return this.mLayout.isFirstChildXLeftOfCenter(this.mStackPosition.x) ? -this.mStackOffset : this.mStackOffset;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    public SpringForce getSpringForce(DynamicAnimation.ViewProperty viewProperty, View view) {
        ContentResolver contentResolver = this.mLayout.getContext().getContentResolver();
        float f = Settings.Secure.getFloat(contentResolver, "bubble_stiffness", this.mIsMovingFromFlinging ? 20000.0f : 12000.0f);
        float f2 = Settings.Secure.getFloat(contentResolver, "bubble_damping", 0.9f);
        SpringForce springForce = new SpringForce();
        springForce.setDampingRatio(f2);
        springForce.setStiffness(f);
        return springForce;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    public void onChildAdded(View view, int i) {
        if (!isStackStuckToTarget()) {
            if (getBubbleCount() == 1) {
                moveStackToStartPosition();
            } else if (isStackPositionSet() && this.mLayout.indexOfChild(view) == 0) {
                animateInBubble(view, i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    public void onChildRemoved(View view, int i, Runnable runnable) {
        PhysicsAnimator instance = PhysicsAnimator.getInstance(view);
        instance.spring(DynamicAnimation.ALPHA, 0.0f);
        instance.spring(DynamicAnimation.SCALE_X, 0.0f, this.mAnimateOutSpringConfig);
        instance.spring(DynamicAnimation.SCALE_Y, 0.0f, this.mAnimateOutSpringConfig);
        instance.withEndActions(runnable, this.mOnBubbleAnimatedOutAction);
        instance.start();
        if (getBubbleCount() > 0) {
            PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChildAtIndex = animationForChildAtIndex(0);
            animationForChildAtIndex.translationX(this.mStackPosition.x, new Runnable[0]);
            animationForChildAtIndex.start(new Runnable[0]);
            return;
        }
        PointF pointF = this.mRestingStackPosition;
        if (pointF == null) {
            pointF = getDefaultStartPosition();
        }
        setStackPosition(pointF);
        this.mFloatingContentCoordinator.onContentRemoved(this.mStackFloatingContent);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    public void onChildReordered(View view, int i, int i2) {
        if (isStackPositionSet()) {
            setStackPosition(this.mStackPosition);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.bubbles.animation.PhysicsAnimationLayout.PhysicsAnimationController
    public void onActiveControllerForLayout(PhysicsAnimationLayout physicsAnimationLayout) {
        Resources resources = physicsAnimationLayout.getResources();
        this.mStackOffset = (float) resources.getDimensionPixelSize(C0012R$dimen.bubble_stack_offset);
        this.mBubbleSize = resources.getDimensionPixelSize(C0012R$dimen.individual_bubble_size);
        this.mBubbleBitmapSize = resources.getDimensionPixelSize(C0012R$dimen.bubble_bitmap_size);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(C0012R$dimen.bubble_padding_top);
        this.mBubbleOffscreen = resources.getDimensionPixelSize(C0012R$dimen.bubble_stack_offscreen);
        this.mStackStartingVerticalOffset = resources.getDimensionPixelSize(C0012R$dimen.bubble_stack_starting_offset_y);
        this.mStatusBarHeight = (float) resources.getDimensionPixelSize(17105489);
    }

    public void updateResources(int i) {
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        if (physicsAnimationLayout != null) {
            Resources resources = physicsAnimationLayout.getContext().getResources();
            this.mBubblePaddingTop = resources.getDimensionPixelSize(C0012R$dimen.bubble_padding_top);
            this.mStatusBarHeight = (float) resources.getDimensionPixelSize(17105489);
        }
    }

    private boolean isStackStuckToTarget() {
        MagnetizedObject<StackAnimationController> magnetizedObject = this.mMagnetizedStack;
        return magnetizedObject != null && magnetizedObject.getObjectStuckToTarget();
    }

    private void moveStackToStartPosition() {
        this.mLayout.setVisibility(4);
        this.mLayout.post(new Runnable() {
            /* class com.android.systemui.bubbles.animation.$$Lambda$StackAnimationController$XG5dbVvx6CTopXCQV48uovjmoQo */

            public final void run() {
                StackAnimationController.this.lambda$moveStackToStartPosition$3$StackAnimationController();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$moveStackToStartPosition$3 */
    public /* synthetic */ void lambda$moveStackToStartPosition$3$StackAnimationController() {
        PointF pointF = this.mRestingStackPosition;
        if (pointF == null) {
            pointF = getDefaultStartPosition();
        }
        setStackPosition(pointF);
        this.mStackMovedToStartPosition = true;
        this.mLayout.setVisibility(0);
        if (this.mLayout.getChildCount() > 0) {
            this.mFloatingContentCoordinator.onContentAdded(this.mStackFloatingContent);
            animateInBubble(this.mLayout.getChildAt(0), 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void moveFirstBubbleWithStackFollowing(DynamicAnimation.ViewProperty viewProperty, float f) {
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

    public void setStackPosition(PointF pointF) {
        Log.d("Bubbs.StackCtrl", String.format("Setting position to (%f, %f).", Float.valueOf(pointF.x), Float.valueOf(pointF.y)));
        this.mStackPosition.set(pointF.x, pointF.y);
        if (this.mRestingStackPosition == null) {
            this.mRestingStackPosition = new PointF();
        }
        this.mRestingStackPosition.set(this.mStackPosition);
        if (isActiveController()) {
            this.mLayout.cancelAllAnimationsOfProperties(DynamicAnimation.TRANSLATION_X, DynamicAnimation.TRANSLATION_Y);
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

    public PointF getDefaultStartPosition() {
        float f;
        PhysicsAnimationLayout physicsAnimationLayout = this.mLayout;
        boolean z = true;
        if (physicsAnimationLayout == null || physicsAnimationLayout.getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        if (z) {
            f = getAllowableStackPositionRegion().right;
        } else {
            f = getAllowableStackPositionRegion().left;
        }
        return new PointF(f, getAllowableStackPositionRegion().top + ((float) this.mStackStartingVerticalOffset));
    }

    private boolean isStackPositionSet() {
        return this.mStackMovedToStartPosition;
    }

    private void animateInBubble(View view, int i) {
        if (isActiveController()) {
            float offsetForChainedPropertyAnimation = getOffsetForChainedPropertyAnimation(DynamicAnimation.TRANSLATION_X);
            view.setTranslationX(this.mStackPosition.x + (((float) i) * offsetForChainedPropertyAnimation));
            view.setTranslationY(this.mStackPosition.y);
            view.setScaleX(0.0f);
            view.setScaleY(0.0f);
            int i2 = i + 1;
            if (i2 < this.mLayout.getChildCount()) {
                PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChildAtIndex = animationForChildAtIndex(i2);
                animationForChildAtIndex.translationX(this.mStackPosition.x + (offsetForChainedPropertyAnimation * ((float) i2)), new Runnable[0]);
                animationForChildAtIndex.withStiffness(200.0f);
                animationForChildAtIndex.start(new Runnable[0]);
            }
            PhysicsAnimationLayout.PhysicsPropertyAnimator animationForChild = animationForChild(view);
            animationForChild.scaleX(1.0f, new Runnable[0]);
            animationForChild.scaleY(1.0f, new Runnable[0]);
            animationForChild.withStiffness(1000.0f);
            animationForChild.withStartDelay(this.mLayout.getChildCount() > 1 ? 25 : 0);
            animationForChild.start(new Runnable[0]);
        }
    }

    private void cancelStackPositionAnimation(DynamicAnimation.ViewProperty viewProperty) {
        if (this.mStackPositionAnimations.containsKey(viewProperty)) {
            this.mStackPositionAnimations.get(viewProperty).cancel();
        }
    }

    public MagnetizedObject<StackAnimationController> getMagnetizedStack(MagnetizedObject.MagneticTarget magneticTarget) {
        if (this.mMagnetizedStack == null) {
            AnonymousClass2 r0 = new MagnetizedObject<StackAnimationController>(this.mLayout.getContext(), this, new StackPositionProperty(DynamicAnimation.TRANSLATION_X), new StackPositionProperty(DynamicAnimation.TRANSLATION_Y)) {
                /* class com.android.systemui.bubbles.animation.StackAnimationController.AnonymousClass2 */

                public float getWidth(StackAnimationController stackAnimationController) {
                    return (float) StackAnimationController.this.mBubbleSize;
                }

                public float getHeight(StackAnimationController stackAnimationController) {
                    return (float) StackAnimationController.this.mBubbleSize;
                }

                public void getLocationOnScreen(StackAnimationController stackAnimationController, int[] iArr) {
                    iArr[0] = (int) StackAnimationController.this.mStackPosition.x;
                    iArr[1] = (int) StackAnimationController.this.mStackPosition.y;
                }
            };
            this.mMagnetizedStack = r0;
            r0.addTarget(magneticTarget);
            this.mMagnetizedStack.setHapticsEnabled(true);
            this.mMagnetizedStack.setFlingToTargetMinVelocity(4000.0f);
        }
        ContentResolver contentResolver = this.mLayout.getContext().getContentResolver();
        float f = Settings.Secure.getFloat(contentResolver, "bubble_dismiss_fling_min_velocity", this.mMagnetizedStack.getFlingToTargetMinVelocity());
        float f2 = Settings.Secure.getFloat(contentResolver, "bubble_dismiss_stick_max_velocity", this.mMagnetizedStack.getStickToTargetMaxXVelocity());
        float f3 = Settings.Secure.getFloat(contentResolver, "bubble_dismiss_target_width_percent", this.mMagnetizedStack.getFlingToTargetWidthPercent());
        this.mMagnetizedStack.setFlingToTargetMinVelocity(f);
        this.mMagnetizedStack.setStickToTargetMaxXVelocity(f2);
        this.mMagnetizedStack.setFlingToTargetWidthPercent(f3);
        return this.mMagnetizedStack;
    }

    private int getBubbleCount() {
        return this.mBubbleCountSupplier.getAsInt();
    }

    /* access modifiers changed from: private */
    public class StackPositionProperty extends FloatPropertyCompat<StackAnimationController> {
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
            StackAnimationController.this.moveFirstBubbleWithStackFollowing((StackAnimationController) this.mProperty, (DynamicAnimation.ViewProperty) f);
        }
    }
}
