package com.android.systemui.bubbles;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityView;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Choreographer;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.bubbles.BadgedImageView;
import com.android.systemui.bubbles.Bubble;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleStackView;
import com.android.systemui.bubbles.animation.AnimatableScaleMatrix;
import com.android.systemui.bubbles.animation.ExpandedAnimationController;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import com.android.systemui.bubbles.animation.StackAnimationController;
import com.android.systemui.model.SysUiState;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.util.DismissCircleView;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.RelativeTouchListener;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class BubbleStackView extends FrameLayout implements ViewTreeObserver.OnComputeInternalInsetsListener {
    private static final SurfaceSynchronizer DEFAULT_SURFACE_SYNCHRONIZER = new SurfaceSynchronizer() {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass1 */

        @Override // com.android.systemui.bubbles.BubbleStackView.SurfaceSynchronizer
        public void syncSurfaceAndRun(final Runnable runnable) {
            Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback(this) {
                /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass1.AnonymousClass1 */
                private int mFrameWait = 2;

                public void doFrame(long j) {
                    int i = this.mFrameWait - 1;
                    this.mFrameWait = i;
                    if (i > 0) {
                        Choreographer.getInstance().postFrameCallback(this);
                    } else {
                        runnable.run();
                    }
                }
            });
        }
    };
    @VisibleForTesting
    static final int FLYOUT_HIDE_AFTER = 5000;
    private static final PhysicsAnimator.SpringConfig FLYOUT_IME_ANIMATION_SPRING_CONFIG = new PhysicsAnimator.SpringConfig(200.0f, 0.9f);
    private Runnable mAfterFlyoutHidden;
    private final DynamicAnimation.OnAnimationEndListener mAfterFlyoutTransitionSpring = new DynamicAnimation.OnAnimationEndListener() {
        /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$qNTN7f0ovKQkRVyENDOFd8Z5ydA */

        @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
        public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
            BubbleStackView.this.lambda$new$1$BubbleStackView(dynamicAnimation, z, f, f2);
        }
    };
    private Runnable mAnimateInFlyout;
    private final Runnable mAnimateTemporarilyInvisibleImmediate = new Runnable() {
        /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$m1Oanm_NNH1J1i2WLfzcc3QC7Uw */

        public final void run() {
            BubbleStackView.this.lambda$new$9$BubbleStackView();
        }
    };
    private boolean mAnimatingEducationAway;
    private boolean mAnimatingManageEducationAway;
    private SurfaceControl.ScreenshotGraphicBuffer mAnimatingOutBubbleBuffer;
    private FrameLayout mAnimatingOutSurfaceContainer;
    private SurfaceView mAnimatingOutSurfaceView;
    private View.OnClickListener mBubbleClickListener = new View.OnClickListener() {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass6 */

        public void onClick(View view) {
            Bubble bubbleWithView;
            BubbleStackView.this.mIsDraggingStack = false;
            if (!BubbleStackView.this.mIsExpansionAnimating && !BubbleStackView.this.mIsBubbleSwitchAnimating && (bubbleWithView = BubbleStackView.this.mBubbleData.getBubbleWithView(view)) != null) {
                boolean equals = bubbleWithView.getKey().equals(BubbleStackView.this.mExpandedBubble.getKey());
                if (BubbleStackView.this.isExpanded()) {
                    BubbleStackView.this.mExpandedAnimationController.onGestureFinished();
                }
                if (!BubbleStackView.this.isExpanded() || equals) {
                    if (!BubbleStackView.this.maybeShowStackUserEducation()) {
                        BubbleStackView.this.mBubbleData.setExpanded(!BubbleStackView.this.mBubbleData.isExpanded());
                    }
                } else if (bubbleWithView != BubbleStackView.this.mBubbleData.getSelectedBubble()) {
                    BubbleStackView.this.mBubbleData.setSelectedBubble(bubbleWithView);
                } else {
                    BubbleStackView.this.setSelectedBubble(bubbleWithView);
                }
            }
        }
    };
    private PhysicsAnimationLayout mBubbleContainer;
    private final BubbleData mBubbleData;
    private int mBubbleElevation;
    private BubbleOverflow mBubbleOverflow;
    private int mBubblePaddingTop;
    private int mBubbleSize;
    private Bubble mBubbleToExpandAfterFlyoutCollapse = null;
    private RelativeTouchListener mBubbleTouchListener = new RelativeTouchListener() {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass7 */

        @Override // com.android.systemui.util.RelativeTouchListener
        public boolean onDown(View view, MotionEvent motionEvent) {
            if (BubbleStackView.this.mIsExpansionAnimating) {
                return true;
            }
            if (BubbleStackView.this.mShowingManage) {
                BubbleStackView.this.showManageMenu(false);
            }
            if (BubbleStackView.this.mBubbleData.isExpanded()) {
                BubbleStackView.this.maybeShowManageEducation(false);
                BubbleStackView.this.mExpandedAnimationController.prepareForBubbleDrag(view, BubbleStackView.this.mMagneticTarget, BubbleStackView.this.mIndividualBubbleMagnetListener);
                BubbleStackView.this.hideCurrentInputMethod();
                BubbleStackView bubbleStackView = BubbleStackView.this;
                bubbleStackView.mMagnetizedObject = bubbleStackView.mExpandedAnimationController.getMagnetizedBubbleDraggingOut();
            } else {
                BubbleStackView.this.mStackAnimationController.cancelStackPositionAnimations();
                BubbleStackView.this.mBubbleContainer.setActiveController(BubbleStackView.this.mStackAnimationController);
                BubbleStackView.this.hideFlyoutImmediate();
                BubbleStackView bubbleStackView2 = BubbleStackView.this;
                bubbleStackView2.mMagnetizedObject = bubbleStackView2.mStackAnimationController.getMagnetizedStack(BubbleStackView.this.mMagneticTarget);
                BubbleStackView.this.mMagnetizedObject.setMagnetListener(BubbleStackView.this.mStackMagnetListener);
                BubbleStackView.this.mIsDraggingStack = true;
                BubbleStackView.this.updateTemporarilyInvisibleAnimation(false);
            }
            BubbleStackView.this.passEventToMagnetizedObject(motionEvent);
            return true;
        }

        @Override // com.android.systemui.util.RelativeTouchListener
        public void onMove(View view, MotionEvent motionEvent, float f, float f2, float f3, float f4) {
            if (!BubbleStackView.this.mIsExpansionAnimating) {
                BubbleStackView.this.springInDismissTargetMaybe();
                if (BubbleStackView.this.passEventToMagnetizedObject(motionEvent)) {
                    return;
                }
                if (BubbleStackView.this.mBubbleData.isExpanded()) {
                    BubbleStackView.this.mExpandedAnimationController.dragBubbleOut(view, f + f3, f2 + f4);
                    return;
                }
                BubbleStackView.this.hideStackUserEducation(false);
                BubbleStackView.this.mStackAnimationController.moveStackFromTouch(f + f3, f2 + f4);
            }
        }

        @Override // com.android.systemui.util.RelativeTouchListener
        public void onUp(View view, MotionEvent motionEvent, float f, float f2, float f3, float f4, float f5, float f6) {
            if (!BubbleStackView.this.mIsExpansionAnimating) {
                if (!BubbleStackView.this.passEventToMagnetizedObject(motionEvent)) {
                    if (BubbleStackView.this.mBubbleData.isExpanded()) {
                        BubbleStackView.this.mExpandedAnimationController.snapBubbleBack(view, f5, f6);
                    } else {
                        BubbleStackView bubbleStackView = BubbleStackView.this;
                        bubbleStackView.mStackOnLeftOrWillBe = bubbleStackView.mStackAnimationController.flingStackThenSpringToEdge(f + f3, f5, f6) <= 0.0f;
                        BubbleStackView.this.updateBubbleZOrdersAndDotPosition(true);
                        BubbleStackView.this.logBubbleEvent(null, 7);
                    }
                    BubbleStackView.this.hideDismissTarget();
                }
                BubbleStackView.this.mIsDraggingStack = false;
                BubbleStackView.this.updateTemporarilyInvisibleAnimation(false);
            }
        }
    };
    private int mBubbleTouchPadding;
    private int mCornerRadius;
    private final Handler mDelayedAnimationHandler = new Handler();
    private final ValueAnimator mDesaturateAndDarkenAnimator;
    private final Paint mDesaturateAndDarkenPaint = new Paint();
    private View mDesaturateAndDarkenTargetView;
    private PhysicsAnimator<View> mDismissTargetAnimator;
    private View mDismissTargetCircle;
    private ViewGroup mDismissTargetContainer;
    private PhysicsAnimator.SpringConfig mDismissTargetSpring = new PhysicsAnimator.SpringConfig(200.0f, 0.75f);
    private Point mDisplaySize;
    private BubbleController.BubbleExpandListener mExpandListener;
    private ExpandedAnimationController mExpandedAnimationController;
    private BubbleViewProvider mExpandedBubble;
    private FrameLayout mExpandedViewContainer;
    private final AnimatableScaleMatrix mExpandedViewContainerMatrix = new AnimatableScaleMatrix();
    private int mExpandedViewPadding;
    private BubbleFlyoutView mFlyout;
    private View.OnClickListener mFlyoutClickListener = new View.OnClickListener() {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass8 */

        public void onClick(View view) {
            if (BubbleStackView.this.maybeShowStackUserEducation()) {
                BubbleStackView.this.mBubbleToExpandAfterFlyoutCollapse = null;
            } else {
                BubbleStackView bubbleStackView = BubbleStackView.this;
                bubbleStackView.mBubbleToExpandAfterFlyoutCollapse = bubbleStackView.mBubbleData.getSelectedBubble();
            }
            BubbleStackView.this.mFlyout.removeCallbacks(BubbleStackView.this.mHideFlyout);
            BubbleStackView.this.mHideFlyout.run();
        }
    };
    private final FloatPropertyCompat mFlyoutCollapseProperty = new FloatPropertyCompat("FlyoutCollapseSpring") {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass3 */

        @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
        public float getValue(Object obj) {
            return BubbleStackView.this.mFlyoutDragDeltaX;
        }

        @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
        public void setValue(Object obj, float f) {
            BubbleStackView.this.setFlyoutStateForDragLength(f);
        }
    };
    private float mFlyoutDragDeltaX = 0.0f;
    private RelativeTouchListener mFlyoutTouchListener = new RelativeTouchListener() {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass9 */

        @Override // com.android.systemui.util.RelativeTouchListener
        public boolean onDown(View view, MotionEvent motionEvent) {
            BubbleStackView.this.mFlyout.removeCallbacks(BubbleStackView.this.mHideFlyout);
            return true;
        }

        @Override // com.android.systemui.util.RelativeTouchListener
        public void onMove(View view, MotionEvent motionEvent, float f, float f2, float f3, float f4) {
            BubbleStackView.this.setFlyoutStateForDragLength(f3);
        }

        @Override // com.android.systemui.util.RelativeTouchListener
        public void onUp(View view, MotionEvent motionEvent, float f, float f2, float f3, float f4, float f5, float f6) {
            boolean isStackOnLeftSide = BubbleStackView.this.mStackAnimationController.isStackOnLeftSide();
            boolean z = true;
            boolean z2 = !isStackOnLeftSide ? f5 > 2000.0f : f5 < -2000.0f;
            boolean z3 = !isStackOnLeftSide ? f3 > ((float) BubbleStackView.this.mFlyout.getWidth()) * 0.25f : f3 < ((float) (-BubbleStackView.this.mFlyout.getWidth())) * 0.25f;
            boolean z4 = !isStackOnLeftSide ? f5 < 0.0f : f5 > 0.0f;
            if (!z2 && (!z3 || z4)) {
                z = false;
            }
            BubbleStackView.this.mFlyout.removeCallbacks(BubbleStackView.this.mHideFlyout);
            BubbleStackView.this.animateFlyoutCollapsed(z, f5);
            BubbleStackView.this.maybeShowStackUserEducation();
        }
    };
    private final SpringAnimation mFlyoutTransitionSpring = new SpringAnimation(this, this.mFlyoutCollapseProperty);
    private final Runnable mHideCurrentInputMethodCallback;
    private Runnable mHideFlyout = new Runnable() {
        /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$jXS10HgKCVgyvjX1UcSgdO2D_ug */

        public final void run() {
            BubbleStackView.this.lambda$new$0$BubbleStackView();
        }
    };
    private int mImeOffset;
    private final MagnetizedObject.MagnetListener mIndividualBubbleMagnetListener = new MagnetizedObject.MagnetListener() {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass4 */

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onStuckToTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            if (BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble() != null) {
                BubbleStackView bubbleStackView = BubbleStackView.this;
                bubbleStackView.animateDesaturateAndDarken(bubbleStackView.mExpandedAnimationController.getDraggedOutBubble(), true);
            }
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onUnstuckFromTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z) {
            if (BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble() != null) {
                BubbleStackView bubbleStackView = BubbleStackView.this;
                bubbleStackView.animateDesaturateAndDarken(bubbleStackView.mExpandedAnimationController.getDraggedOutBubble(), false);
                if (z) {
                    BubbleStackView.this.mExpandedAnimationController.snapBubbleBack(BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble(), f, f2);
                    BubbleStackView.this.hideDismissTarget();
                    return;
                }
                BubbleStackView.this.mExpandedAnimationController.onUnstuckFromTarget();
            }
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onReleasedInTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            if (BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble() != null) {
                BubbleStackView.this.mExpandedAnimationController.dismissDraggedOutBubble(BubbleStackView.this.mExpandedAnimationController.getDraggedOutBubble(), (float) BubbleStackView.this.mDismissTargetContainer.getHeight(), new Runnable() {
                    /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$4$aSvR96CLWJ5wWFJfNBEMIcwA5g */

                    public final void run() {
                        BubbleStackView.this.dismissMagnetizedObject();
                    }
                });
                BubbleStackView.this.hideDismissTarget();
            }
        }
    };
    private LayoutInflater mInflater;
    private boolean mIsBubbleSwitchAnimating = false;
    private boolean mIsDraggingStack = false;
    private boolean mIsExpanded;
    private boolean mIsExpansionAnimating = false;
    private boolean mIsGestureInProgress = false;
    private MagnetizedObject.MagneticTarget mMagneticTarget;
    private MagnetizedObject<?> mMagnetizedObject;
    private BubbleManageEducationView mManageEducationView;
    private ViewGroup mManageMenu;
    private ImageView mManageSettingsIcon;
    private TextView mManageSettingsText;
    private PhysicsAnimator.SpringConfig mManageSpringConfig = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
    private int mMaxBubbles;
    private int mOrientation = 0;
    private View.OnLayoutChangeListener mOrientationChangedListener;
    private int mPointerIndexDown = -1;
    private final PhysicsAnimator.SpringConfig mScaleInSpringConfig = new PhysicsAnimator.SpringConfig(300.0f, 0.9f);
    private final PhysicsAnimator.SpringConfig mScaleOutSpringConfig = new PhysicsAnimator.SpringConfig(900.0f, 1.0f);
    private boolean mShouldShowManageEducation;
    private boolean mShouldShowUserEducation;
    private boolean mShowingDismiss = false;
    private boolean mShowingManage = false;
    private StackAnimationController mStackAnimationController;
    private final MagnetizedObject.MagnetListener mStackMagnetListener = new MagnetizedObject.MagnetListener() {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass5 */

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onStuckToTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            BubbleStackView bubbleStackView = BubbleStackView.this;
            bubbleStackView.animateDesaturateAndDarken(bubbleStackView.mBubbleContainer, true);
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onUnstuckFromTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z) {
            BubbleStackView bubbleStackView = BubbleStackView.this;
            bubbleStackView.animateDesaturateAndDarken(bubbleStackView.mBubbleContainer, false);
            if (z) {
                BubbleStackView.this.mStackAnimationController.flingStackThenSpringToEdge(BubbleStackView.this.mStackAnimationController.getStackPosition().x, f, f2);
                BubbleStackView.this.hideDismissTarget();
                return;
            }
            BubbleStackView.this.mStackAnimationController.onUnstuckFromTarget();
        }

        @Override // com.android.systemui.util.magnetictarget.MagnetizedObject.MagnetListener
        public void onReleasedInTarget(MagnetizedObject.MagneticTarget magneticTarget) {
            BubbleStackView.this.mStackAnimationController.animateStackDismissal((float) BubbleStackView.this.mDismissTargetContainer.getHeight(), new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$5$LrrLsLBxyFUc80K5g0SFXrW0Yg */

                public final void run() {
                    BubbleStackView.AnonymousClass5.this.lambda$onReleasedInTarget$0$BubbleStackView$5();
                }
            });
            BubbleStackView.this.hideDismissTarget();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onReleasedInTarget$0 */
        public /* synthetic */ void lambda$onReleasedInTarget$0$BubbleStackView$5() {
            BubbleStackView.this.resetDesaturationAndDarken();
            BubbleStackView.this.dismissMagnetizedObject();
        }
    };
    private boolean mStackOnLeftOrWillBe = true;
    private int mStatusBarHeight;
    private final SurfaceSynchronizer mSurfaceSynchronizer;
    private SysUiState mSysUiState;
    private ViewTreeObserver.OnDrawListener mSystemGestureExcludeUpdater = new ViewTreeObserver.OnDrawListener() {
        /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$cOiserdP7VIvU56hCAARnBncEE */

        public final void onDraw() {
            BubbleStackView.this.updateSystemGestureExcludeRects();
        }
    };
    private final List<Rect> mSystemGestureExclusionRects = Collections.singletonList(new Rect());
    private Rect mTempRect = new Rect();
    private boolean mTemporarilyInvisible = false;
    private final PhysicsAnimator.SpringConfig mTranslateSpringConfig = new PhysicsAnimator.SpringConfig(200.0f, 1.0f);
    private Consumer<String> mUnbubbleConversationCallback;
    private View mUserEducationView;
    private float mVerticalPosPercentBeforeRotation = -1.0f;
    private boolean mViewUpdatedRequested = false;
    private ViewTreeObserver.OnPreDrawListener mViewUpdater = new ViewTreeObserver.OnPreDrawListener() {
        /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass2 */

        public boolean onPreDraw() {
            BubbleStackView.this.getViewTreeObserver().removeOnPreDrawListener(BubbleStackView.this.mViewUpdater);
            BubbleStackView.this.updateExpandedView();
            BubbleStackView.this.mViewUpdatedRequested = false;
            return true;
        }
    };
    private boolean mWasOnLeftBeforeRotation = false;

    /* access modifiers changed from: package-private */
    public interface SurfaceSynchronizer {
        void syncSurfaceAndRun(Runnable runnable);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$BubbleStackView() {
        animateFlyoutCollapsed(true, 0.0f);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Stack view state:");
        printWriter.print("  gestureInProgress:       ");
        printWriter.println(this.mIsGestureInProgress);
        printWriter.print("  showingDismiss:          ");
        printWriter.println(this.mShowingDismiss);
        printWriter.print("  isExpansionAnimating:    ");
        printWriter.println(this.mIsExpansionAnimating);
        printWriter.print("  expandedContainerVis:    ");
        printWriter.println(this.mExpandedViewContainer.getVisibility());
        printWriter.print("  expandedContainerAlpha:  ");
        printWriter.println(this.mExpandedViewContainer.getAlpha());
        printWriter.print("  expandedContainerMatrix: ");
        printWriter.println(this.mExpandedViewContainer.getAnimationMatrix());
        this.mStackAnimationController.dump(fileDescriptor, printWriter, strArr);
        this.mExpandedAnimationController.dump(fileDescriptor, printWriter, strArr);
        if (this.mExpandedBubble != null) {
            printWriter.println("Expanded bubble state:");
            printWriter.println("  expandedBubbleKey: " + this.mExpandedBubble.getKey());
            BubbleExpandedView expandedView = this.mExpandedBubble.getExpandedView();
            if (expandedView != null) {
                printWriter.println("  expandedViewVis:    " + expandedView.getVisibility());
                printWriter.println("  expandedViewAlpha:  " + expandedView.getAlpha());
                printWriter.println("  expandedViewTaskId: " + expandedView.getTaskId());
                ActivityView activityView = expandedView.getActivityView();
                if (activityView != null) {
                    printWriter.println("  activityViewVis:    " + activityView.getVisibility());
                    printWriter.println("  activityViewAlpha:  " + activityView.getAlpha());
                    return;
                }
                printWriter.println("  activityView is null");
                return;
            }
            printWriter.println("Expanded bubble view state: expanded bubble view is null");
            return;
        }
        printWriter.println("Expanded bubble state: expanded bubble is null");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$BubbleStackView(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
        if (this.mFlyoutDragDeltaX == 0.0f) {
            this.mFlyout.postDelayed(this.mHideFlyout, 5000);
        } else {
            this.mFlyout.hideFlyout();
        }
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public BubbleStackView(Context context, BubbleData bubbleData, SurfaceSynchronizer surfaceSynchronizer, FloatingContentCoordinator floatingContentCoordinator, SysUiState sysUiState, Runnable runnable, Consumer<Boolean> consumer, Runnable runnable2) {
        super(context);
        SurfaceSynchronizer surfaceSynchronizer2;
        this.mBubbleData = bubbleData;
        this.mInflater = LayoutInflater.from(context);
        this.mSysUiState = sysUiState;
        Resources resources = getResources();
        this.mMaxBubbles = resources.getInteger(C0016R$integer.bubbles_max_rendered);
        this.mBubbleSize = resources.getDimensionPixelSize(C0012R$dimen.individual_bubble_size);
        this.mBubbleElevation = resources.getDimensionPixelSize(C0012R$dimen.bubble_elevation);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(C0012R$dimen.bubble_padding_top);
        this.mBubbleTouchPadding = resources.getDimensionPixelSize(C0012R$dimen.bubble_touch_padding);
        this.mStatusBarHeight = resources.getDimensionPixelSize(17105489);
        this.mImeOffset = resources.getDimensionPixelSize(C0012R$dimen.pip_ime_offset);
        this.mDisplaySize = new Point();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealSize(this.mDisplaySize);
        this.mExpandedViewPadding = resources.getDimensionPixelSize(C0012R$dimen.bubble_expanded_view_padding);
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.bubble_elevation);
        TypedArray obtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(new int[]{16844145});
        this.mCornerRadius = obtainStyledAttributes.getDimensionPixelSize(0, 0);
        obtainStyledAttributes.recycle();
        $$Lambda$BubbleStackView$Hjz7hXc94PYdpndVbPsPbyIpyWU r9 = new Runnable(runnable) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$Hjz7hXc94PYdpndVbPsPbyIpyWU */
            public final /* synthetic */ Runnable f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                BubbleStackView.this.lambda$new$2$BubbleStackView(this.f$1);
            }
        };
        this.mStackAnimationController = new StackAnimationController(floatingContentCoordinator, new IntSupplier() {
            /* class com.android.systemui.bubbles.$$Lambda$3l4urKvsZCQadEpiHWxUBGPGyvY */

            public final int getAsInt() {
                return BubbleStackView.this.getBubbleCount();
            }
        }, r9);
        this.mExpandedAnimationController = new ExpandedAnimationController(this.mDisplaySize, this.mExpandedViewPadding, resources.getConfiguration().orientation, r9);
        if (surfaceSynchronizer != null) {
            surfaceSynchronizer2 = surfaceSynchronizer;
        } else {
            surfaceSynchronizer2 = DEFAULT_SURFACE_SYNCHRONIZER;
        }
        this.mSurfaceSynchronizer = surfaceSynchronizer2;
        setUpUserEducation();
        setLayoutDirection(0);
        PhysicsAnimationLayout physicsAnimationLayout = new PhysicsAnimationLayout(context);
        this.mBubbleContainer = physicsAnimationLayout;
        physicsAnimationLayout.setActiveController(this.mStackAnimationController);
        float f = (float) dimensionPixelSize;
        this.mBubbleContainer.setElevation(f);
        this.mBubbleContainer.setClipChildren(false);
        addView(this.mBubbleContainer, new FrameLayout.LayoutParams(-1, -1));
        FrameLayout frameLayout = new FrameLayout(context);
        this.mExpandedViewContainer = frameLayout;
        frameLayout.setElevation(f);
        this.mExpandedViewContainer.setClipChildren(false);
        addView(this.mExpandedViewContainer);
        FrameLayout frameLayout2 = new FrameLayout(getContext());
        this.mAnimatingOutSurfaceContainer = frameLayout2;
        frameLayout2.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        addView(this.mAnimatingOutSurfaceContainer);
        SurfaceView surfaceView = new SurfaceView(getContext());
        this.mAnimatingOutSurfaceView = surfaceView;
        surfaceView.setUseAlpha();
        this.mAnimatingOutSurfaceView.setZOrderOnTop(true);
        this.mAnimatingOutSurfaceView.setCornerRadius((float) this.mCornerRadius);
        this.mAnimatingOutSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        this.mAnimatingOutSurfaceContainer.addView(this.mAnimatingOutSurfaceView);
        FrameLayout frameLayout3 = this.mAnimatingOutSurfaceContainer;
        int i = this.mExpandedViewPadding;
        frameLayout3.setPadding(i, i, i, i);
        setUpManageMenu();
        setUpFlyout();
        SpringAnimation springAnimation = this.mFlyoutTransitionSpring;
        SpringForce springForce = new SpringForce();
        springForce.setStiffness(200.0f);
        springForce.setDampingRatio(0.75f);
        springAnimation.setSpring(springForce);
        this.mFlyoutTransitionSpring.addEndListener(this.mAfterFlyoutTransitionSpring);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(C0012R$dimen.dismiss_circle_size);
        this.mDismissTargetCircle = new DismissCircleView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize2, dimensionPixelSize2);
        layoutParams.gravity = 81;
        this.mDismissTargetCircle.setLayoutParams(layoutParams);
        this.mDismissTargetAnimator = PhysicsAnimator.getInstance(this.mDismissTargetCircle);
        FrameLayout frameLayout4 = new FrameLayout(context);
        this.mDismissTargetContainer = frameLayout4;
        frameLayout4.setLayoutParams(new FrameLayout.LayoutParams(-1, getResources().getDimensionPixelSize(C0012R$dimen.floating_dismiss_gradient_height), 80));
        this.mDismissTargetContainer.setPadding(0, 0, 0, getResources().getDimensionPixelSize(C0012R$dimen.floating_dismiss_bottom_margin));
        this.mDismissTargetContainer.setClipToPadding(false);
        this.mDismissTargetContainer.setClipChildren(false);
        this.mDismissTargetContainer.addView(this.mDismissTargetCircle);
        this.mDismissTargetContainer.setVisibility(4);
        this.mDismissTargetContainer.setBackgroundResource(C0013R$drawable.floating_dismiss_gradient_transition);
        addView(this.mDismissTargetContainer);
        this.mDismissTargetCircle.setTranslationY((float) getResources().getDimensionPixelSize(C0012R$dimen.floating_dismiss_gradient_height));
        this.mMagneticTarget = new MagnetizedObject.MagneticTarget(this.mDismissTargetCircle, Settings.Secure.getInt(getContext().getContentResolver(), "bubble_dismiss_radius", this.mBubbleSize * 2));
        setClipChildren(false);
        setFocusable(true);
        this.mBubbleContainer.bringToFront();
        setUpOverflow();
        this.mHideCurrentInputMethodCallback = runnable2;
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener(consumer) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$WdFVrYRSAwHYzZq062ZrQQcAUk8 */
            public final /* synthetic */ Consumer f$1;

            {
                this.f$1 = r2;
            }

            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return BubbleStackView.this.lambda$new$4$BubbleStackView(this.f$1, view, windowInsets);
            }
        });
        this.mOrientationChangedListener = new View.OnLayoutChangeListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$zB8p0_cjtonbCXvIH4kDoBtabk */

            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                BubbleStackView.this.lambda$new$6$BubbleStackView(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        };
        getViewTreeObserver().addOnDrawListener(this.mSystemGestureExcludeUpdater);
        ColorMatrix colorMatrix = new ColorMatrix();
        ColorMatrix colorMatrix2 = new ColorMatrix();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        this.mDesaturateAndDarkenAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(colorMatrix, colorMatrix2) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$nTtH9EoKZ3I47RpPl0BGULUUeI */
            public final /* synthetic */ ColorMatrix f$1;
            public final /* synthetic */ ColorMatrix f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                BubbleStackView.this.lambda$new$7$BubbleStackView(this.f$1, this.f$2, valueAnimator);
            }
        });
        setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$DgIHzfVnE2ZObZ8qcZwxCeDQAK0 */

            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return BubbleStackView.this.lambda$new$8$BubbleStackView(view, motionEvent);
            }
        });
        animate().setInterpolator(Interpolators.PANEL_CLOSE_ACCELERATED).setDuration(320);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$BubbleStackView(Runnable runnable) {
        if (getBubbleCount() == 0) {
            runnable.run();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$4 */
    public /* synthetic */ WindowInsets lambda$new$4$BubbleStackView(Consumer consumer, View view, WindowInsets windowInsets) {
        consumer.accept(Boolean.valueOf(windowInsets.getInsets(WindowInsets.Type.ime()).bottom > 0));
        if (!this.mIsExpanded || this.mIsExpansionAnimating) {
            return view.onApplyWindowInsets(windowInsets);
        }
        this.mExpandedAnimationController.updateYPosition(new Runnable(windowInsets) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$SGgilPVMr7ds9JBrPWP0ZRoSeUQ */
            public final /* synthetic */ WindowInsets f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                BubbleStackView.this.lambda$new$3$BubbleStackView(this.f$1);
            }
        });
        return view.onApplyWindowInsets(windowInsets);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$3 */
    public /* synthetic */ void lambda$new$3$BubbleStackView(WindowInsets windowInsets) {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider != null && bubbleViewProvider.getExpandedView() != null) {
            this.mExpandedBubble.getExpandedView().updateInsets(windowInsets);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$6 */
    public /* synthetic */ void lambda$new$6$BubbleStackView(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int i9;
        int i10;
        this.mExpandedAnimationController.updateResources(this.mOrientation, this.mDisplaySize);
        this.mStackAnimationController.updateResources(this.mOrientation);
        this.mBubbleOverflow.updateDimensions();
        WindowInsets rootWindowInsets = getRootWindowInsets();
        int i11 = this.mExpandedViewPadding;
        if (rootWindowInsets != null) {
            DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
            int i12 = 0;
            if (displayCutout != null) {
                i12 = displayCutout.getSafeInsetLeft();
                i10 = displayCutout.getSafeInsetRight();
            } else {
                i10 = 0;
            }
            int max = Math.max(i12, rootWindowInsets.getStableInsetLeft()) + i11;
            i9 = i11 + Math.max(i10, rootWindowInsets.getStableInsetRight());
            i11 = max;
        } else {
            i9 = i11;
        }
        FrameLayout frameLayout = this.mExpandedViewContainer;
        int i13 = this.mExpandedViewPadding;
        frameLayout.setPadding(i11, i13, i9, i13);
        if (this.mIsExpanded) {
            beforeExpandedViewAnimation();
            updateOverflowVisibility();
            updatePointerPosition();
            this.mExpandedAnimationController.expandFromStack(new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$BhIZ4rN3xHvYX6KiS0mXLMuJug */

                public final void run() {
                    BubbleStackView.this.lambda$new$5$BubbleStackView();
                }
            });
            this.mExpandedViewContainer.setTranslationX(0.0f);
            this.mExpandedViewContainer.setTranslationY(getExpandedViewY());
            this.mExpandedViewContainer.setAlpha(1.0f);
        }
        float f = this.mVerticalPosPercentBeforeRotation;
        if (f >= 0.0f) {
            this.mStackAnimationController.moveStackToSimilarPositionAfterRotation(this.mWasOnLeftBeforeRotation, f);
        }
        removeOnLayoutChangeListener(this.mOrientationChangedListener);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$7 */
    public /* synthetic */ void lambda$new$7$BubbleStackView(ColorMatrix colorMatrix, ColorMatrix colorMatrix2, ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        colorMatrix.setSaturation(floatValue);
        float f = 1.0f - ((1.0f - floatValue) * 0.3f);
        colorMatrix2.setScale(f, f, f, 1.0f);
        colorMatrix.postConcat(colorMatrix2);
        this.mDesaturateAndDarkenPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        View view = this.mDesaturateAndDarkenTargetView;
        if (view != null) {
            view.setLayerPaint(this.mDesaturateAndDarkenPaint);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$8 */
    public /* synthetic */ boolean lambda$new$8$BubbleStackView(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() != 0) {
            return true;
        }
        if (this.mShowingManage) {
            showManageMenu(false);
            return true;
        } else if (!this.mBubbleData.isExpanded()) {
            return true;
        } else {
            this.mBubbleData.setExpanded(false);
            return true;
        }
    }

    public void setTemporarilyInvisible(boolean z) {
        this.mTemporarilyInvisible = z;
        updateTemporarilyInvisibleAnimation(z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTemporarilyInvisibleAnimation(boolean z) {
        removeCallbacks(this.mAnimateTemporarilyInvisibleImmediate);
        if (!this.mIsDraggingStack) {
            postDelayed(this.mAnimateTemporarilyInvisibleImmediate, (!(this.mTemporarilyInvisible && this.mFlyout.getVisibility() != 0) || z) ? 0 : 1000);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$9 */
    public /* synthetic */ void lambda$new$9$BubbleStackView() {
        if (!this.mTemporarilyInvisible || this.mFlyout.getVisibility() == 0) {
            animate().translationX(0.0f).start();
        } else if (this.mStackAnimationController.isStackOnLeftSide()) {
            animate().translationX((float) (-this.mBubbleSize)).start();
        } else {
            animate().translationX((float) this.mBubbleSize).start();
        }
    }

    private void setUpManageMenu() {
        ViewGroup viewGroup = this.mManageMenu;
        if (viewGroup != null) {
            removeView(viewGroup);
        }
        ViewGroup viewGroup2 = (ViewGroup) LayoutInflater.from(getContext()).inflate(C0017R$layout.bubble_manage_menu, (ViewGroup) this, false);
        this.mManageMenu = viewGroup2;
        viewGroup2.setVisibility(4);
        PhysicsAnimator.getInstance(this.mManageMenu).setDefaultSpringConfig(this.mManageSpringConfig);
        this.mManageMenu.setOutlineProvider(new ViewOutlineProvider() {
            /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass10 */

            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) BubbleStackView.this.mCornerRadius);
            }
        });
        this.mManageMenu.setClipToOutline(true);
        this.mManageMenu.findViewById(C0015R$id.bubble_manage_menu_dismiss_container).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$r9lwHRxNgYPwFIdyspAWzwNWX0c */

            public final void onClick(View view) {
                BubbleStackView.this.lambda$setUpManageMenu$10$BubbleStackView(view);
            }
        });
        this.mManageMenu.findViewById(C0015R$id.bubble_manage_menu_dont_bubble_container).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$GRQaewUOo0NVAl_1r4p82XhLmc4 */

            public final void onClick(View view) {
                BubbleStackView.this.lambda$setUpManageMenu$11$BubbleStackView(view);
            }
        });
        this.mManageMenu.findViewById(C0015R$id.bubble_manage_menu_settings_container).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$OgUqEHRcTH_CSY2Hykwvk0S0vx4 */

            public final void onClick(View view) {
                BubbleStackView.this.lambda$setUpManageMenu$13$BubbleStackView(view);
            }
        });
        this.mManageSettingsIcon = (ImageView) this.mManageMenu.findViewById(C0015R$id.bubble_manage_menu_settings_icon);
        this.mManageSettingsText = (TextView) this.mManageMenu.findViewById(C0015R$id.bubble_manage_menu_settings_name);
        this.mManageMenu.setLayoutDirection(3);
        addView(this.mManageMenu);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setUpManageMenu$10 */
    public /* synthetic */ void lambda$setUpManageMenu$10$BubbleStackView(View view) {
        showManageMenu(false);
        dismissBubbleIfExists(this.mBubbleData.getSelectedBubble());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setUpManageMenu$11 */
    public /* synthetic */ void lambda$setUpManageMenu$11$BubbleStackView(View view) {
        showManageMenu(false);
        this.mUnbubbleConversationCallback.accept(this.mBubbleData.getSelectedBubble().getKey());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setUpManageMenu$13 */
    public /* synthetic */ void lambda$setUpManageMenu$13$BubbleStackView(View view) {
        showManageMenu(false);
        Bubble selectedBubble = this.mBubbleData.getSelectedBubble();
        if (selectedBubble != null && this.mBubbleData.hasBubbleInStackWithKey(selectedBubble.getKey())) {
            collapseStack(new Runnable(selectedBubble.getSettingsIntent(((FrameLayout) this).mContext), selectedBubble) {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$2Rn_iGaMRg7HpIeuK7fa52Zy4nY */
                public final /* synthetic */ Intent f$1;
                public final /* synthetic */ Bubble f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    BubbleStackView.this.lambda$setUpManageMenu$12$BubbleStackView(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setUpManageMenu$12 */
    public /* synthetic */ void lambda$setUpManageMenu$12$BubbleStackView(Intent intent, Bubble bubble) {
        ((FrameLayout) this).mContext.startActivityAsUser(intent, bubble.getUser());
        logBubbleEvent(bubble, 9);
    }

    private void setUpUserEducation() {
        View view = this.mUserEducationView;
        if (view != null) {
            removeView(view);
        }
        boolean shouldShowBubblesEducation = shouldShowBubblesEducation();
        this.mShouldShowUserEducation = shouldShowBubblesEducation;
        if (shouldShowBubblesEducation) {
            View inflate = this.mInflater.inflate(C0017R$layout.bubble_stack_user_education, (ViewGroup) this, false);
            this.mUserEducationView = inflate;
            inflate.setVisibility(8);
            TypedArray obtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(new int[]{16843829, 16842809});
            int color = obtainStyledAttributes.getColor(0, -16777216);
            int color2 = obtainStyledAttributes.getColor(1, -1);
            obtainStyledAttributes.recycle();
            int ensureTextContrast = ContrastColorUtil.ensureTextContrast(color2, color, true);
            ((TextView) this.mUserEducationView.findViewById(C0015R$id.user_education_title)).setTextColor(ensureTextContrast);
            ((TextView) this.mUserEducationView.findViewById(C0015R$id.user_education_description)).setTextColor(ensureTextContrast);
            updateUserEducationForLayoutDirection();
            addView(this.mUserEducationView);
        }
        BubbleManageEducationView bubbleManageEducationView = this.mManageEducationView;
        if (bubbleManageEducationView != null) {
            removeView(bubbleManageEducationView);
        }
        boolean shouldShowManageEducation = shouldShowManageEducation();
        this.mShouldShowManageEducation = shouldShowManageEducation;
        if (shouldShowManageEducation) {
            BubbleManageEducationView bubbleManageEducationView2 = (BubbleManageEducationView) this.mInflater.inflate(C0017R$layout.bubbles_manage_button_education, (ViewGroup) this, false);
            this.mManageEducationView = bubbleManageEducationView2;
            bubbleManageEducationView2.setVisibility(8);
            this.mManageEducationView.setElevation((float) this.mBubbleElevation);
            this.mManageEducationView.setLayoutDirection(3);
            addView(this.mManageEducationView);
        }
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void setUpFlyout() {
        BubbleFlyoutView bubbleFlyoutView = this.mFlyout;
        if (bubbleFlyoutView != null) {
            removeView(bubbleFlyoutView);
        }
        BubbleFlyoutView bubbleFlyoutView2 = new BubbleFlyoutView(getContext());
        this.mFlyout = bubbleFlyoutView2;
        bubbleFlyoutView2.setVisibility(8);
        this.mFlyout.animate().setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
        this.mFlyout.setOnClickListener(this.mFlyoutClickListener);
        this.mFlyout.setOnTouchListener(this.mFlyoutTouchListener);
        addView(this.mFlyout, new FrameLayout.LayoutParams(-2, -2));
    }

    private void setUpOverflow() {
        int i;
        BubbleOverflow bubbleOverflow = this.mBubbleOverflow;
        if (bubbleOverflow == null) {
            BubbleOverflow bubbleOverflow2 = new BubbleOverflow(getContext());
            this.mBubbleOverflow = bubbleOverflow2;
            bubbleOverflow2.setUpOverflow(this.mBubbleContainer, this);
            i = 0;
        } else {
            this.mBubbleContainer.removeView(bubbleOverflow.getIconView());
            this.mBubbleOverflow.setUpOverflow(this.mBubbleContainer, this);
            i = this.mBubbleContainer.getChildCount();
        }
        this.mBubbleContainer.addView(this.mBubbleOverflow.getIconView(), i, new FrameLayout.LayoutParams(-2, -2));
        this.mBubbleOverflow.getIconView().setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$gUr7zrwt06iDPovK5kFZqUDsxYs */

            public final void onClick(View view) {
                BubbleStackView.this.lambda$setUpOverflow$14$BubbleStackView(view);
            }
        });
        updateOverflowVisibility();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setUpOverflow$14 */
    public /* synthetic */ void lambda$setUpOverflow$14$BubbleStackView(View view) {
        setSelectedBubble(this.mBubbleOverflow);
        showManageMenu(false);
    }

    public void onThemeChanged() {
        setUpFlyout();
        setUpOverflow();
        setUpUserEducation();
        setUpManageMenu();
        updateExpandedViewTheme();
    }

    public void onOrientationChanged(int i) {
        this.mOrientation = i;
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealSize(this.mDisplaySize);
        Resources resources = getContext().getResources();
        this.mStatusBarHeight = resources.getDimensionPixelSize(17105489);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(C0012R$dimen.bubble_padding_top);
        RectF allowableStackPositionRegion = this.mStackAnimationController.getAllowableStackPositionRegion();
        this.mWasOnLeftBeforeRotation = this.mStackAnimationController.isStackOnLeftSide();
        float f = this.mStackAnimationController.getStackPosition().y;
        float f2 = allowableStackPositionRegion.top;
        float f3 = (f - f2) / (allowableStackPositionRegion.bottom - f2);
        this.mVerticalPosPercentBeforeRotation = f3;
        this.mVerticalPosPercentBeforeRotation = Math.max(0.0f, Math.min(1.0f, f3));
        addOnLayoutChangeListener(this.mOrientationChangedListener);
        hideFlyoutImmediate();
        this.mManageMenu.setVisibility(4);
        this.mShowingManage = false;
    }

    public void onLayoutDirectionChanged(int i) {
        this.mManageMenu.setLayoutDirection(i);
        this.mFlyout.setLayoutDirection(i);
        View view = this.mUserEducationView;
        if (view != null) {
            view.setLayoutDirection(i);
            updateUserEducationForLayoutDirection();
        }
        BubbleManageEducationView bubbleManageEducationView = this.mManageEducationView;
        if (bubbleManageEducationView != null) {
            bubbleManageEducationView.setLayoutDirection(i);
        }
        updateExpandedViewDirection(i);
    }

    public void onDisplaySizeChanged() {
        setUpOverflow();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealSize(this.mDisplaySize);
        Resources resources = getContext().getResources();
        this.mStatusBarHeight = resources.getDimensionPixelSize(17105489);
        this.mBubblePaddingTop = resources.getDimensionPixelSize(C0012R$dimen.bubble_padding_top);
        this.mBubbleSize = getResources().getDimensionPixelSize(C0012R$dimen.individual_bubble_size);
        for (Bubble bubble : this.mBubbleData.getBubbles()) {
            if (bubble.getIconView() == null) {
                Log.d("Bubbles", "Display size changed. Icon null: " + bubble);
            } else {
                BadgedImageView iconView = bubble.getIconView();
                int i = this.mBubbleSize;
                iconView.setLayoutParams(new FrameLayout.LayoutParams(i, i));
            }
        }
        this.mExpandedAnimationController.updateResources(this.mOrientation, this.mDisplaySize);
        this.mStackAnimationController.updateResources(this.mOrientation);
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.dismiss_circle_size);
        this.mDismissTargetCircle.getLayoutParams().width = dimensionPixelSize;
        this.mDismissTargetCircle.getLayoutParams().height = dimensionPixelSize;
        this.mDismissTargetCircle.requestLayout();
        this.mMagneticTarget.setMagneticFieldRadiusPx(this.mBubbleSize * 2);
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        this.mTempRect.setEmpty();
        getTouchableRegion(this.mTempRect);
        internalInsetsInfo.touchableRegion.set(this.mTempRect);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnComputeInternalInsetsListener(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.mViewUpdater);
        getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
        BubbleOverflow bubbleOverflow = this.mBubbleOverflow;
        if (bubbleOverflow != null && bubbleOverflow.getExpandedView() != null) {
            this.mBubbleOverflow.getExpandedView().cleanUpExpandedState();
        }
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfoInternal(accessibilityNodeInfo);
        setupLocalMenu(accessibilityNodeInfo);
    }

    /* access modifiers changed from: package-private */
    public void updateExpandedViewTheme() {
        List<Bubble> bubbles = this.mBubbleData.getBubbles();
        if (!bubbles.isEmpty()) {
            bubbles.forEach($$Lambda$BubbleStackView$gtti_QWIhKA2hCHaS7klo4hfz0Y.INSTANCE);
        }
    }

    static /* synthetic */ void lambda$updateExpandedViewTheme$15(Bubble bubble) {
        if (bubble.getExpandedView() != null) {
            bubble.getExpandedView().applyThemeAttrs();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateExpandedViewDirection(int i) {
        List<Bubble> bubbles = this.mBubbleData.getBubbles();
        if (!bubbles.isEmpty()) {
            bubbles.forEach(new Consumer(i) {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$CKKUSLKaEFdqjL8UshFp6r2378 */
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    BubbleStackView.lambda$updateExpandedViewDirection$16(this.f$0, (Bubble) obj);
                }
            });
        }
    }

    static /* synthetic */ void lambda$updateExpandedViewDirection$16(int i, Bubble bubble) {
        if (bubble.getExpandedView() != null) {
            bubble.getExpandedView().setLayoutDirection(i);
        }
    }

    /* access modifiers changed from: package-private */
    public void setupLocalMenu(AccessibilityNodeInfo accessibilityNodeInfo) {
        Resources resources = ((FrameLayout) this).mContext.getResources();
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(C0015R$id.action_move_top_left, resources.getString(C0021R$string.bubble_accessibility_action_move_top_left)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(C0015R$id.action_move_top_right, resources.getString(C0021R$string.bubble_accessibility_action_move_top_right)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(C0015R$id.action_move_bottom_left, resources.getString(C0021R$string.bubble_accessibility_action_move_bottom_left)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(C0015R$id.action_move_bottom_right, resources.getString(C0021R$string.bubble_accessibility_action_move_bottom_right)));
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_DISMISS);
        if (this.mIsExpanded) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE);
        } else {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
        }
    }

    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (super.performAccessibilityActionInternal(i, bundle)) {
            return true;
        }
        RectF allowableStackPositionRegion = this.mStackAnimationController.getAllowableStackPositionRegion();
        if (i == 1048576) {
            this.mBubbleData.dismissAll(6);
            announceForAccessibility(getResources().getString(C0021R$string.accessibility_bubble_dismissed));
            return true;
        } else if (i == 524288) {
            this.mBubbleData.setExpanded(false);
            return true;
        } else if (i == 262144) {
            this.mBubbleData.setExpanded(true);
            return true;
        } else if (i == C0015R$id.action_move_top_left) {
            this.mStackAnimationController.springStackAfterFling(allowableStackPositionRegion.left, allowableStackPositionRegion.top);
            return true;
        } else if (i == C0015R$id.action_move_top_right) {
            this.mStackAnimationController.springStackAfterFling(allowableStackPositionRegion.right, allowableStackPositionRegion.top);
            return true;
        } else if (i == C0015R$id.action_move_bottom_left) {
            this.mStackAnimationController.springStackAfterFling(allowableStackPositionRegion.left, allowableStackPositionRegion.bottom);
            return true;
        } else if (i != C0015R$id.action_move_bottom_right) {
            return false;
        } else {
            this.mStackAnimationController.springStackAfterFling(allowableStackPositionRegion.right, allowableStackPositionRegion.bottom);
            return true;
        }
    }

    public void updateContentDescription() {
        if (!this.mBubbleData.getBubbles().isEmpty()) {
            for (int i = 0; i < this.mBubbleData.getBubbles().size(); i++) {
                Bubble bubble = this.mBubbleData.getBubbles().get(i);
                String appName = bubble.getAppName();
                String title = bubble.getTitle();
                if (title == null) {
                    title = getResources().getString(C0021R$string.notification_bubble_title);
                }
                if (bubble.getIconView() != null) {
                    if (this.mIsExpanded || i > 0) {
                        bubble.getIconView().setContentDescription(getResources().getString(C0021R$string.bubble_content_description_single, title, appName));
                    } else {
                        bubble.getIconView().setContentDescription(getResources().getString(C0021R$string.bubble_content_description_stack, title, appName, Integer.valueOf(this.mBubbleContainer.getChildCount() - 1)));
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateSystemGestureExcludeRects() {
        Rect rect = this.mSystemGestureExclusionRects.get(0);
        if (getBubbleCount() > 0) {
            View childAt = this.mBubbleContainer.getChildAt(0);
            rect.set(childAt.getLeft(), childAt.getTop(), childAt.getRight(), childAt.getBottom());
            rect.offset((int) (childAt.getTranslationX() + 0.5f), (int) (childAt.getTranslationY() + 0.5f));
            this.mBubbleContainer.setSystemGestureExclusionRects(this.mSystemGestureExclusionRects);
            return;
        }
        rect.setEmpty();
        this.mBubbleContainer.setSystemGestureExclusionRects(Collections.emptyList());
    }

    public void setExpandListener(BubbleController.BubbleExpandListener bubbleExpandListener) {
        this.mExpandListener = bubbleExpandListener;
    }

    public void setUnbubbleConversationCallback(Consumer<String> consumer) {
        this.mUnbubbleConversationCallback = consumer;
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public boolean isExpansionAnimating() {
        return this.mIsExpansionAnimating;
    }

    /* access modifiers changed from: package-private */
    public BubbleViewProvider getExpandedBubble() {
        return this.mExpandedBubble;
    }

    /* access modifiers changed from: package-private */
    @SuppressLint({"ClickableViewAccessibility"})
    public void addBubble(Bubble bubble) {
        if (getBubbleCount() == 0 && this.mShouldShowUserEducation) {
            StackAnimationController stackAnimationController = this.mStackAnimationController;
            stackAnimationController.setStackPosition(stackAnimationController.getDefaultStartPosition());
        }
        if (getBubbleCount() == 0) {
            this.mStackOnLeftOrWillBe = this.mStackAnimationController.isStackOnLeftSide();
        }
        if (bubble.getIconView() != null) {
            bubble.getIconView().setDotPositionOnLeft(!this.mStackOnLeftOrWillBe, false);
            bubble.getIconView().setOnClickListener(this.mBubbleClickListener);
            bubble.getIconView().setOnTouchListener(this.mBubbleTouchListener);
            this.mBubbleContainer.addView(bubble.getIconView(), 0, new FrameLayout.LayoutParams(-2, -2));
            animateInFlyoutForBubble(bubble);
            requestUpdate();
            logBubbleEvent(bubble, 1);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeBubble(Bubble bubble) {
        for (int i = 0; i < getBubbleCount(); i++) {
            View childAt = this.mBubbleContainer.getChildAt(i);
            if ((childAt instanceof BadgedImageView) && ((BadgedImageView) childAt).getKey().equals(bubble.getKey())) {
                this.mBubbleContainer.removeViewAt(i);
                bubble.cleanupViews();
                updatePointerPosition();
                logBubbleEvent(bubble, 5);
                return;
            }
        }
        Log.d("Bubbles", "was asked to remove Bubble, but didn't find the view! " + bubble);
    }

    private void updateOverflowVisibility() {
        BubbleOverflow bubbleOverflow = this.mBubbleOverflow;
        if (bubbleOverflow != null) {
            bubbleOverflow.setVisible(this.mIsExpanded ? 0 : 8);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateBubble(Bubble bubble) {
        animateInFlyoutForBubble(bubble);
        requestUpdate();
        logBubbleEvent(bubble, 2);
    }

    public void updateBubbleOrder(List<Bubble> list) {
        for (int i = 0; i < list.size(); i++) {
            this.mBubbleContainer.reorderView(list.get(i).getIconView(), i);
        }
        updateBubbleZOrdersAndDotPosition(false);
        updatePointerPosition();
    }

    public void setSelectedBubble(BubbleViewProvider bubbleViewProvider) {
        BubbleViewProvider bubbleViewProvider2;
        if (this.mExpandedBubble != bubbleViewProvider) {
            if (bubbleViewProvider == null || bubbleViewProvider.getKey() != "Overflow") {
                this.mBubbleData.setShowingOverflow(false);
            } else {
                this.mBubbleData.setShowingOverflow(true);
            }
            if (this.mIsExpanded && this.mIsExpansionAnimating) {
                cancelAllExpandCollapseSwitchAnimations();
            }
            if (!this.mIsExpanded || (bubbleViewProvider2 = this.mExpandedBubble) == null || bubbleViewProvider2.getExpandedView() == null) {
                showNewlySelectedBubble(bubbleViewProvider);
                return;
            }
            BubbleViewProvider bubbleViewProvider3 = this.mExpandedBubble;
            if (!(bubbleViewProvider3 == null || bubbleViewProvider3.getExpandedView() == null)) {
                this.mExpandedBubble.getExpandedView().setSurfaceZOrderedOnTop(true);
            }
            try {
                screenshotAnimatingOutBubbleIntoSurface(new Consumer(bubbleViewProvider) {
                    /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$v9Fpisep_4rDb1vjFiHap285MHs */
                    public final /* synthetic */ BubbleViewProvider f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        BubbleStackView.this.lambda$setSelectedBubble$17$BubbleStackView(this.f$1, (Boolean) obj);
                    }
                });
            } catch (Exception e) {
                showNewlySelectedBubble(bubbleViewProvider);
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setSelectedBubble$17 */
    public /* synthetic */ void lambda$setSelectedBubble$17$BubbleStackView(BubbleViewProvider bubbleViewProvider, Boolean bool) {
        this.mAnimatingOutSurfaceContainer.setVisibility(bool.booleanValue() ? 0 : 4);
        showNewlySelectedBubble(bubbleViewProvider);
    }

    private void showNewlySelectedBubble(BubbleViewProvider bubbleViewProvider) {
        BubbleViewProvider bubbleViewProvider2 = this.mExpandedBubble;
        this.mExpandedBubble = bubbleViewProvider;
        updatePointerPosition();
        if (this.mIsExpanded) {
            hideCurrentInputMethod();
            this.mExpandedViewContainer.setAlpha(0.0f);
            this.mSurfaceSynchronizer.syncSurfaceAndRun(new Runnable(bubbleViewProvider2, bubbleViewProvider) {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$8QlfeYkGdUNh1TqL6uaiSF62PB0 */
                public final /* synthetic */ BubbleViewProvider f$1;
                public final /* synthetic */ BubbleViewProvider f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    BubbleStackView.this.lambda$showNewlySelectedBubble$18$BubbleStackView(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showNewlySelectedBubble$18 */
    public /* synthetic */ void lambda$showNewlySelectedBubble$18$BubbleStackView(BubbleViewProvider bubbleViewProvider, BubbleViewProvider bubbleViewProvider2) {
        if (bubbleViewProvider != null) {
            bubbleViewProvider.setContentVisibility(false);
        }
        updateExpandedBubble();
        requestUpdate();
        logBubbleEvent(bubbleViewProvider, 4);
        logBubbleEvent(bubbleViewProvider2, 3);
        notifyExpansionChanged(bubbleViewProvider, false);
        notifyExpansionChanged(bubbleViewProvider2, true);
    }

    public void setExpanded(boolean z) {
        if (!z) {
            releaseAnimatingOutBubbleBuffer();
        }
        if (z != this.mIsExpanded) {
            hideCurrentInputMethod();
            SysUiState sysUiState = this.mSysUiState;
            sysUiState.setFlag(16384, z);
            sysUiState.commitUpdate(((FrameLayout) this).mContext.getDisplayId());
            if (this.mIsExpanded) {
                animateCollapse();
                logBubbleEvent(this.mExpandedBubble, 4);
            } else {
                animateExpansion();
                logBubbleEvent(this.mExpandedBubble, 3);
                logBubbleEvent(this.mExpandedBubble, 15);
            }
            notifyExpansionChanged(this.mExpandedBubble, this.mIsExpanded);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean maybeShowStackUserEducation() {
        if (!this.mShouldShowUserEducation || this.mUserEducationView.getVisibility() == 0) {
            return false;
        }
        this.mUserEducationView.setAlpha(0.0f);
        this.mUserEducationView.setVisibility(0);
        updateUserEducationForLayoutDirection();
        this.mUserEducationView.post(new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$qEPeIiYThpJeOpmzYMlHotgNlc */

            public final void run() {
                BubbleStackView.this.lambda$maybeShowStackUserEducation$19$BubbleStackView();
            }
        });
        Prefs.putBoolean(getContext(), "HasSeenBubblesOnboarding", true);
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$maybeShowStackUserEducation$19 */
    public /* synthetic */ void lambda$maybeShowStackUserEducation$19$BubbleStackView() {
        this.mUserEducationView.setTranslationY((this.mStackAnimationController.getDefaultStartPosition().y + ((float) (this.mBubbleSize / 2))) - ((float) (this.mUserEducationView.getHeight() / 2)));
        this.mUserEducationView.animate().setDuration(200).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(1.0f);
    }

    private void updateUserEducationForLayoutDirection() {
        View view = this.mUserEducationView;
        if (view != null) {
            LinearLayout linearLayout = (LinearLayout) view.findViewById(C0015R$id.user_education_view);
            TextView textView = (TextView) this.mUserEducationView.findViewById(C0015R$id.user_education_title);
            TextView textView2 = (TextView) this.mUserEducationView.findViewById(C0015R$id.user_education_description);
            if (getResources().getConfiguration().getLayoutDirection() == 0) {
                this.mUserEducationView.setLayoutDirection(0);
                linearLayout.setBackgroundResource(C0013R$drawable.bubble_stack_user_education_bg);
                textView.setGravity(3);
                textView2.setGravity(3);
                return;
            }
            this.mUserEducationView.setLayoutDirection(1);
            linearLayout.setBackgroundResource(C0013R$drawable.bubble_stack_user_education_bg_rtl);
            textView.setGravity(5);
            textView2.setGravity(5);
        }
    }

    /* access modifiers changed from: package-private */
    public void hideStackUserEducation(boolean z) {
        if (this.mShouldShowUserEducation && this.mUserEducationView.getVisibility() == 0 && !this.mAnimatingEducationAway) {
            this.mAnimatingEducationAway = true;
            this.mUserEducationView.animate().alpha(0.0f).setDuration(z ? 40 : 200).withEndAction(new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$m0XC_pagmXZSQ75lsfSAXgWwAg */

                public final void run() {
                    BubbleStackView.this.lambda$hideStackUserEducation$20$BubbleStackView();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hideStackUserEducation$20 */
    public /* synthetic */ void lambda$hideStackUserEducation$20$BubbleStackView() {
        this.mAnimatingEducationAway = false;
        this.mShouldShowUserEducation = shouldShowBubblesEducation();
        this.mUserEducationView.setVisibility(8);
    }

    /* access modifiers changed from: package-private */
    public void maybeShowManageEducation(boolean z) {
        BubbleManageEducationView bubbleManageEducationView = this.mManageEducationView;
        if (bubbleManageEducationView != null) {
            if (z && this.mShouldShowManageEducation && bubbleManageEducationView.getVisibility() != 0 && this.mIsExpanded && this.mExpandedBubble.getExpandedView() != null) {
                this.mManageEducationView.setAlpha(0.0f);
                this.mManageEducationView.setVisibility(0);
                this.mManageEducationView.post(new Runnable() {
                    /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$HbG4ghYlsoeaS4sVsaHntwSx5g4 */

                    public final void run() {
                        BubbleStackView.this.lambda$maybeShowManageEducation$24$BubbleStackView();
                    }
                });
                Prefs.putBoolean(getContext(), "HasSeenBubblesManageOnboarding", true);
            } else if (!z && this.mManageEducationView.getVisibility() == 0 && !this.mAnimatingManageEducationAway) {
                this.mManageEducationView.animate().alpha(0.0f).setDuration(this.mIsExpansionAnimating ? 40 : 200).withEndAction(new Runnable() {
                    /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$y4EqEexeus3MZ5glmUeVBmnnXPA */

                    public final void run() {
                        BubbleStackView.this.lambda$maybeShowManageEducation$25$BubbleStackView();
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$maybeShowManageEducation$24 */
    public /* synthetic */ void lambda$maybeShowManageEducation$24$BubbleStackView() {
        this.mExpandedBubble.getExpandedView().getManageButtonBoundsOnScreen(this.mTempRect);
        int manageViewHeight = this.mManageEducationView.getManageViewHeight();
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.bubbles_manage_education_top_inset);
        this.mManageEducationView.bringToFront();
        this.mManageEducationView.setManageViewPosition(0, (this.mTempRect.top - manageViewHeight) + dimensionPixelSize);
        this.mManageEducationView.animate().setDuration(200).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).alpha(1.0f);
        this.mManageEducationView.findViewById(C0015R$id.manage).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$9NxqZetHgM6c4X0j2S1B9RZkd0 */

            public final void onClick(View view) {
                BubbleStackView.this.lambda$maybeShowManageEducation$21$BubbleStackView(view);
            }
        });
        this.mManageEducationView.findViewById(C0015R$id.got_it).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$GYZbVitDInH2RKYw7i19AAO6zHM */

            public final void onClick(View view) {
                BubbleStackView.this.lambda$maybeShowManageEducation$22$BubbleStackView(view);
            }
        });
        this.mManageEducationView.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$Nz5_OzRSP1AvTx6QRv1WoAsDRI */

            public final void onClick(View view) {
                BubbleStackView.this.lambda$maybeShowManageEducation$23$BubbleStackView(view);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$maybeShowManageEducation$21 */
    public /* synthetic */ void lambda$maybeShowManageEducation$21$BubbleStackView(View view) {
        this.mExpandedBubble.getExpandedView().findViewById(C0015R$id.settings_button).performClick();
        maybeShowManageEducation(false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$maybeShowManageEducation$22 */
    public /* synthetic */ void lambda$maybeShowManageEducation$22$BubbleStackView(View view) {
        maybeShowManageEducation(false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$maybeShowManageEducation$23 */
    public /* synthetic */ void lambda$maybeShowManageEducation$23$BubbleStackView(View view) {
        maybeShowManageEducation(false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$maybeShowManageEducation$25 */
    public /* synthetic */ void lambda$maybeShowManageEducation$25$BubbleStackView() {
        this.mAnimatingManageEducationAway = false;
        this.mShouldShowManageEducation = shouldShowManageEducation();
        this.mManageEducationView.setVisibility(8);
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public void collapseStack(Runnable runnable) {
        this.mBubbleData.setExpanded(false);
        runnable.run();
    }

    /* access modifiers changed from: package-private */
    public void showExpandedViewContents(int i) {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider != null && bubbleViewProvider.getExpandedView() != null && this.mExpandedBubble.getExpandedView().getVirtualDisplayId() == i) {
            this.mExpandedBubble.setContentVisibility(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void hideCurrentInputMethod() {
        this.mHideCurrentInputMethodCallback.run();
    }

    private void beforeExpandedViewAnimation() {
        this.mIsExpansionAnimating = true;
        hideFlyoutImmediate();
        updateExpandedBubble();
        updateExpandedView();
    }

    /* access modifiers changed from: private */
    /* renamed from: afterExpandedViewAnimation */
    public void lambda$new$5() {
        this.mIsExpansionAnimating = false;
        updateExpandedView();
        requestUpdate();
    }

    private void animateExpansion() {
        cancelDelayedExpandCollapseSwitchAnimations();
        this.mIsExpanded = true;
        hideStackUserEducation(true);
        beforeExpandedViewAnimation();
        this.mBubbleContainer.setActiveController(this.mExpandedAnimationController);
        updateOverflowVisibility();
        updatePointerPosition();
        this.mExpandedAnimationController.expandFromStack(new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$3CiEYd1ciKAdZrEnaQsH0oiFPo */

            public final void run() {
                BubbleStackView.this.lambda$animateExpansion$26$BubbleStackView();
            }
        });
        this.mExpandedViewContainer.setTranslationX(0.0f);
        this.mExpandedViewContainer.setTranslationY(getExpandedViewY());
        this.mExpandedViewContainer.setAlpha(1.0f);
        float bubbleLeft = this.mExpandedAnimationController.getBubbleLeft(this.mBubbleData.getBubbles().indexOf(this.mExpandedBubble));
        long abs = getWidth() > 0 ? (long) (((Math.abs(bubbleLeft - this.mStackAnimationController.getStackPosition().x) / ((float) getWidth())) * 30.0f) + 175.0f) : 0;
        this.mExpandedViewContainerMatrix.setScale(0.0f, 0.0f, (((float) this.mBubbleSize) / 2.0f) + bubbleLeft, getExpandedViewY());
        this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (!(bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null)) {
            this.mExpandedBubble.getExpandedView().setSurfaceZOrderedOnTop(false);
        }
        this.mDelayedAnimationHandler.postDelayed(new Runnable(bubbleLeft) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$UNuCs4sUpvhXoc1ew2TBDXBo2Fg */
            public final /* synthetic */ float f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                BubbleStackView.this.lambda$animateExpansion$29$BubbleStackView(this.f$1);
            }
        }, abs);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateExpansion$26 */
    public /* synthetic */ void lambda$animateExpansion$26$BubbleStackView() {
        lambda$new$5();
        maybeShowManageEducation(true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateExpansion$29 */
    public /* synthetic */ void lambda$animateExpansion$29$BubbleStackView(float f) {
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).cancel();
        PhysicsAnimator instance = PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix);
        instance.spring(AnimatableScaleMatrix.SCALE_X, AnimatableScaleMatrix.getAnimatableValueForScaleFactor(1.0f), this.mScaleInSpringConfig);
        instance.spring(AnimatableScaleMatrix.SCALE_Y, AnimatableScaleMatrix.getAnimatableValueForScaleFactor(1.0f), this.mScaleInSpringConfig);
        instance.addUpdateListener(new PhysicsAnimator.UpdateListener(f) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$b_UaRBEuWlY020n8jg6KaRxhDs */
            public final /* synthetic */ float f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
            public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                BubbleStackView.this.lambda$animateExpansion$27$BubbleStackView(this.f$1, (AnimatableScaleMatrix) obj, arrayMap);
            }
        });
        instance.withEndActions(new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$pisfEoHpfRWWDMgdm3_RYzne28 */

            public final void run() {
                BubbleStackView.this.lambda$animateExpansion$28$BubbleStackView();
            }
        });
        instance.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateExpansion$27 */
    public /* synthetic */ void lambda$animateExpansion$27$BubbleStackView(float f, AnimatableScaleMatrix animatableScaleMatrix, ArrayMap arrayMap) {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider != null && bubbleViewProvider.getIconView() != null) {
            this.mExpandedViewContainerMatrix.postTranslate(this.mExpandedBubble.getIconView().getTranslationX() - f, 0.0f);
            this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateExpansion$28 */
    public /* synthetic */ void lambda$animateExpansion$28$BubbleStackView() {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider != null && bubbleViewProvider.getExpandedView() != null) {
            this.mExpandedBubble.getExpandedView().setContentVisibility(true);
            this.mExpandedBubble.getExpandedView().setSurfaceZOrderedOnTop(false);
        }
    }

    private void animateCollapse() {
        cancelDelayedExpandCollapseSwitchAnimations();
        showManageMenu(false);
        this.mIsExpanded = false;
        this.mIsExpansionAnimating = true;
        this.mBubbleContainer.cancelAllAnimations();
        PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceContainer).cancel();
        this.mAnimatingOutSurfaceContainer.setScaleX(0.0f);
        this.mAnimatingOutSurfaceContainer.setScaleY(0.0f);
        this.mExpandedAnimationController.notifyPreparingToCollapse();
        this.mDelayedAnimationHandler.postDelayed(new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$NMpNWXmTuSktREQh1njzbI8GffU */

            public final void run() {
                BubbleStackView.this.lambda$animateCollapse$31$BubbleStackView();
            }
        }, 105);
        View iconView = this.mExpandedBubble.getIconView();
        float bubbleLeft = this.mExpandedAnimationController.getBubbleLeft(this.mBubbleData.getBubbles().indexOf(this.mExpandedBubble));
        this.mExpandedViewContainerMatrix.setScale(1.0f, 1.0f, (((float) this.mBubbleSize) / 2.0f) + bubbleLeft, getExpandedViewY());
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).cancel();
        PhysicsAnimator instance = PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix);
        instance.spring(AnimatableScaleMatrix.SCALE_X, 0.0f, this.mScaleOutSpringConfig);
        instance.spring(AnimatableScaleMatrix.SCALE_Y, 0.0f, this.mScaleOutSpringConfig);
        instance.addUpdateListener(new PhysicsAnimator.UpdateListener(iconView, bubbleLeft) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$H5vYdJ9CVvPdyUTg_Cbel0Xecw */
            public final /* synthetic */ View f$1;
            public final /* synthetic */ float f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
            public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                BubbleStackView.this.lambda$animateCollapse$32$BubbleStackView(this.f$1, this.f$2, (AnimatableScaleMatrix) obj, arrayMap);
            }
        });
        instance.withEndActions(new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$h3oedcF547c9oDcRUG7HxXzZeI */

            public final void run() {
                BubbleStackView.this.lambda$animateCollapse$33$BubbleStackView();
            }
        });
        instance.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateCollapse$31 */
    public /* synthetic */ void lambda$animateCollapse$31$BubbleStackView() {
        this.mExpandedAnimationController.collapseBackToStack(this.mStackAnimationController.getStackPositionAlongNearestHorizontalEdge(), new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$RLGYh0bXzbo7gLPSUpwtl0rh8zQ */

            public final void run() {
                BubbleStackView.this.lambda$animateCollapse$30$BubbleStackView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateCollapse$30 */
    public /* synthetic */ void lambda$animateCollapse$30$BubbleStackView() {
        this.mBubbleContainer.setActiveController(this.mStackAnimationController);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateCollapse$32 */
    public /* synthetic */ void lambda$animateCollapse$32$BubbleStackView(View view, float f, AnimatableScaleMatrix animatableScaleMatrix, ArrayMap arrayMap) {
        if (view != null) {
            this.mExpandedViewContainerMatrix.postTranslate(view.getTranslationX() - f, 0.0f);
        }
        this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
        if (this.mExpandedViewContainerMatrix.getScaleX() < 0.05f) {
            this.mExpandedViewContainer.setVisibility(4);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateCollapse$33 */
    public /* synthetic */ void lambda$animateCollapse$33$BubbleStackView() {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        beforeExpandedViewAnimation();
        maybeShowManageEducation(false);
        updateOverflowVisibility();
        lambda$new$5();
        if (bubbleViewProvider != null) {
            bubbleViewProvider.setContentVisibility(false);
        }
    }

    /* access modifiers changed from: private */
    public void animateSwitchBubbles() {
        int i;
        if (this.mIsExpanded) {
            boolean z = true;
            this.mIsBubbleSwitchAnimating = true;
            PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceContainer).cancel();
            PhysicsAnimator instance = PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceContainer);
            instance.spring(DynamicAnimation.SCALE_X, 0.0f, this.mScaleOutSpringConfig);
            instance.spring(DynamicAnimation.SCALE_Y, 0.0f, this.mScaleOutSpringConfig);
            instance.spring(DynamicAnimation.TRANSLATION_Y, this.mAnimatingOutSurfaceContainer.getTranslationY() - ((float) (this.mBubbleSize * 2)), this.mTranslateSpringConfig);
            instance.withEndActions(new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$pEtxhN7hJ3YH4z9efDKF3VeMo */

                public final void run() {
                    BubbleStackView.this.releaseAnimatingOutBubbleBuffer();
                }
            });
            instance.start();
            BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
            if (bubbleViewProvider == null || !bubbleViewProvider.getKey().equals("Overflow")) {
                z = false;
            }
            ExpandedAnimationController expandedAnimationController = this.mExpandedAnimationController;
            if (z) {
                i = getBubbleCount();
            } else {
                i = this.mBubbleData.getBubbles().indexOf(this.mExpandedBubble);
            }
            float bubbleLeft = expandedAnimationController.getBubbleLeft(i);
            this.mExpandedViewContainer.setAlpha(1.0f);
            this.mExpandedViewContainer.setVisibility(0);
            this.mExpandedViewContainerMatrix.setScale(0.0f, 0.0f, bubbleLeft + (((float) this.mBubbleSize) / 2.0f), getExpandedViewY());
            this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
            this.mDelayedAnimationHandler.postDelayed(new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$E5IVko96vDa38BXRKaW2m_RUJM */

                public final void run() {
                    BubbleStackView.this.lambda$animateSwitchBubbles$36$BubbleStackView();
                }
            }, 25);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateSwitchBubbles$36 */
    public /* synthetic */ void lambda$animateSwitchBubbles$36$BubbleStackView() {
        if (!this.mIsExpanded) {
            this.mIsBubbleSwitchAnimating = false;
            return;
        }
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).cancel();
        PhysicsAnimator instance = PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix);
        instance.spring(AnimatableScaleMatrix.SCALE_X, AnimatableScaleMatrix.getAnimatableValueForScaleFactor(1.0f), this.mScaleInSpringConfig);
        instance.spring(AnimatableScaleMatrix.SCALE_Y, AnimatableScaleMatrix.getAnimatableValueForScaleFactor(1.0f), this.mScaleInSpringConfig);
        instance.addUpdateListener(new PhysicsAnimator.UpdateListener() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$ddRwJKDnyQ0sMOzts_rJUhoOp9U */

            @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
            public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                BubbleStackView.this.lambda$animateSwitchBubbles$34$BubbleStackView((AnimatableScaleMatrix) obj, arrayMap);
            }
        });
        instance.withEndActions(new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$6gxlC8TSjhqAqfcarGbWjlPEvjk */

            public final void run() {
                BubbleStackView.this.lambda$animateSwitchBubbles$35$BubbleStackView();
            }
        });
        instance.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateSwitchBubbles$34 */
    public /* synthetic */ void lambda$animateSwitchBubbles$34$BubbleStackView(AnimatableScaleMatrix animatableScaleMatrix, ArrayMap arrayMap) {
        this.mExpandedViewContainer.setAnimationMatrix(this.mExpandedViewContainerMatrix);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateSwitchBubbles$35 */
    public /* synthetic */ void lambda$animateSwitchBubbles$35$BubbleStackView() {
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (!(bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null)) {
            this.mExpandedBubble.getExpandedView().setContentVisibility(true);
            this.mExpandedBubble.getExpandedView().setSurfaceZOrderedOnTop(false);
        }
        this.mIsBubbleSwitchAnimating = false;
    }

    private void cancelDelayedExpandCollapseSwitchAnimations() {
        this.mDelayedAnimationHandler.removeCallbacksAndMessages(null);
        this.mIsExpansionAnimating = false;
        this.mIsBubbleSwitchAnimating = false;
    }

    private void cancelAllExpandCollapseSwitchAnimations() {
        cancelDelayedExpandCollapseSwitchAnimations();
        PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceView).cancel();
        PhysicsAnimator.getInstance(this.mExpandedViewContainerMatrix).cancel();
        this.mExpandedViewContainer.setAnimationMatrix(null);
    }

    private void notifyExpansionChanged(BubbleViewProvider bubbleViewProvider, boolean z) {
        BubbleController.BubbleExpandListener bubbleExpandListener = this.mExpandListener;
        if (bubbleExpandListener != null && bubbleViewProvider != null) {
            bubbleExpandListener.onBubbleExpandChanged(z, bubbleViewProvider.getKey());
        }
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mStackAnimationController.setImeHeight(z ? i + this.mImeOffset : 0);
        if (!this.mIsExpanded && getBubbleCount() > 0) {
            float animateForImeVisibility = this.mStackAnimationController.animateForImeVisibility(z) - this.mStackAnimationController.getStackPosition().y;
            if (this.mFlyout.getVisibility() == 0) {
                PhysicsAnimator instance = PhysicsAnimator.getInstance(this.mFlyout);
                instance.spring(DynamicAnimation.TRANSLATION_Y, this.mFlyout.getTranslationY() + animateForImeVisibility, FLYOUT_IME_ANIMATION_SPRING_CONFIG);
                instance.start();
            }
        }
    }

    public void subtractObscuredTouchableRegion(Region region, View view) {
        BubbleManageEducationView bubbleManageEducationView;
        if (!this.mIsExpanded || this.mShowingManage || ((bubbleManageEducationView = this.mManageEducationView) != null && bubbleManageEducationView.getVisibility() == 0)) {
            region.setEmpty();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return super.onInterceptTouchEvent(motionEvent);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (motionEvent.getAction() != 0 && motionEvent.getActionIndex() != this.mPointerIndexDown) {
            return false;
        }
        if (motionEvent.getAction() == 0) {
            this.mPointerIndexDown = motionEvent.getActionIndex();
        } else if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
            this.mPointerIndexDown = -1;
        }
        boolean dispatchTouchEvent = super.dispatchTouchEvent(motionEvent);
        if (!dispatchTouchEvent && !this.mIsExpanded && this.mIsGestureInProgress) {
            dispatchTouchEvent = this.mBubbleTouchListener.onTouch(this, motionEvent);
        }
        if (!(motionEvent.getAction() == 1 || motionEvent.getAction() == 3)) {
            z = true;
        }
        this.mIsGestureInProgress = z;
        return dispatchTouchEvent;
    }

    /* access modifiers changed from: package-private */
    public void setFlyoutStateForDragLength(float f) {
        if (this.mFlyout.getWidth() > 0) {
            boolean isStackOnLeftSide = this.mStackAnimationController.isStackOnLeftSide();
            this.mFlyoutDragDeltaX = f;
            if (isStackOnLeftSide) {
                f = -f;
            }
            float width = f / ((float) this.mFlyout.getWidth());
            float f2 = 0.0f;
            this.mFlyout.setCollapsePercent(Math.min(1.0f, Math.max(0.0f, width)));
            int i = (width > 0.0f ? 1 : (width == 0.0f ? 0 : -1));
            if (i < 0 || width > 1.0f) {
                int i2 = (width > 1.0f ? 1 : (width == 1.0f ? 0 : -1));
                boolean z = false;
                int i3 = 1;
                boolean z2 = i2 > 0;
                if ((isStackOnLeftSide && i2 > 0) || (!isStackOnLeftSide && i < 0)) {
                    z = true;
                }
                float f3 = (z2 ? width - 1.0f : width * -1.0f) * ((float) (z ? -1 : 1));
                float width2 = (float) this.mFlyout.getWidth();
                if (z2) {
                    i3 = 2;
                }
                f2 = f3 * (width2 / (8.0f / ((float) i3)));
            }
            BubbleFlyoutView bubbleFlyoutView = this.mFlyout;
            bubbleFlyoutView.setTranslationX(bubbleFlyoutView.getRestingTranslationX() + f2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean passEventToMagnetizedObject(MotionEvent motionEvent) {
        MagnetizedObject<?> magnetizedObject = this.mMagnetizedObject;
        return magnetizedObject != null && magnetizedObject.maybeConsumeMotionEvent(motionEvent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void dismissMagnetizedObject() {
        if (this.mIsExpanded) {
            dismissBubbleIfExists(this.mBubbleData.getBubbleWithView((View) this.mMagnetizedObject.getUnderlyingObject()));
            return;
        }
        this.mBubbleData.dismissAll(1);
    }

    private void dismissBubbleIfExists(Bubble bubble) {
        if (bubble != null && this.mBubbleData.hasBubbleInStackWithKey(bubble.getKey())) {
            this.mBubbleData.dismissBubbleWithKey(bubble.getKey(), 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animateDesaturateAndDarken(View view, boolean z) {
        this.mDesaturateAndDarkenTargetView = view;
        if (view != null) {
            if (z) {
                view.setLayerType(2, this.mDesaturateAndDarkenPaint);
                this.mDesaturateAndDarkenAnimator.removeAllListeners();
                this.mDesaturateAndDarkenAnimator.start();
                return;
            }
            this.mDesaturateAndDarkenAnimator.removeAllListeners();
            this.mDesaturateAndDarkenAnimator.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.bubbles.BubbleStackView.AnonymousClass11 */

                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    BubbleStackView.this.resetDesaturationAndDarken();
                }
            });
            this.mDesaturateAndDarkenAnimator.reverse();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetDesaturationAndDarken() {
        this.mDesaturateAndDarkenAnimator.removeAllListeners();
        this.mDesaturateAndDarkenAnimator.cancel();
        View view = this.mDesaturateAndDarkenTargetView;
        if (view != null) {
            view.setLayerType(0, null);
            this.mDesaturateAndDarkenTargetView = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void springInDismissTargetMaybe() {
        if (!this.mShowingDismiss) {
            this.mShowingDismiss = true;
            this.mDismissTargetContainer.bringToFront();
            this.mDismissTargetContainer.setZ(32766.0f);
            this.mDismissTargetContainer.setVisibility(0);
            ((TransitionDrawable) this.mDismissTargetContainer.getBackground()).startTransition(200);
            this.mDismissTargetAnimator.cancel();
            PhysicsAnimator<View> physicsAnimator = this.mDismissTargetAnimator;
            physicsAnimator.spring(DynamicAnimation.TRANSLATION_Y, 0.0f, this.mDismissTargetSpring);
            physicsAnimator.start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideDismissTarget() {
        if (this.mShowingDismiss) {
            this.mShowingDismiss = false;
            ((TransitionDrawable) this.mDismissTargetContainer.getBackground()).reverseTransition(200);
            PhysicsAnimator<View> physicsAnimator = this.mDismissTargetAnimator;
            physicsAnimator.spring(DynamicAnimation.TRANSLATION_Y, (float) this.mDismissTargetContainer.getHeight(), this.mDismissTargetSpring);
            physicsAnimator.withEndActions(new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$xFYhUm7TnCCBRG3JRHNKpHUKPlE */

                public final void run() {
                    BubbleStackView.this.lambda$hideDismissTarget$37$BubbleStackView();
                }
            });
            physicsAnimator.start();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hideDismissTarget$37 */
    public /* synthetic */ void lambda$hideDismissTarget$37$BubbleStackView() {
        this.mDismissTargetContainer.setVisibility(4);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animateFlyoutCollapsed(boolean z, float f) {
        float f2;
        boolean isStackOnLeftSide = this.mStackAnimationController.isStackOnLeftSide();
        this.mFlyoutTransitionSpring.getSpring().setStiffness(this.mBubbleToExpandAfterFlyoutCollapse != null ? 1500.0f : 200.0f);
        SpringAnimation springAnimation = this.mFlyoutTransitionSpring;
        springAnimation.setStartValue(this.mFlyoutDragDeltaX);
        SpringAnimation springAnimation2 = springAnimation;
        springAnimation2.setStartVelocity(f);
        SpringAnimation springAnimation3 = springAnimation2;
        if (z) {
            int width = this.mFlyout.getWidth();
            if (isStackOnLeftSide) {
                width = -width;
            }
            f2 = (float) width;
        } else {
            f2 = 0.0f;
        }
        springAnimation3.animateToFinalPosition(f2);
    }

    /* access modifiers changed from: package-private */
    public float getExpandedViewY() {
        return (float) (getStatusBarHeight() + this.mBubbleSize + this.mBubblePaddingTop);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void animateInFlyoutForBubble(Bubble bubble) {
        View view;
        Bubble.FlyoutMessage flyoutMessage = bubble.getFlyoutMessage();
        BadgedImageView iconView = bubble.getIconView();
        if (flyoutMessage != null && flyoutMessage.message != null && bubble.showFlyout() && (((view = this.mUserEducationView) == null || view.getVisibility() != 0) && !isExpanded() && !this.mIsExpansionAnimating && !this.mIsGestureInProgress && this.mBubbleToExpandAfterFlyoutCollapse == null && iconView != null)) {
            this.mFlyoutDragDeltaX = 0.0f;
            clearFlyoutOnHide();
            this.mAfterFlyoutHidden = new Runnable(iconView) {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$CYJd20zKzWrHFqeWE_8Gsgx5kPs */
                public final /* synthetic */ BadgedImageView f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    BubbleStackView.this.lambda$animateInFlyoutForBubble$38$BubbleStackView(this.f$1);
                }
            };
            this.mFlyout.setVisibility(4);
            iconView.addDotSuppressionFlag(BadgedImageView.SuppressionFlag.FLYOUT_VISIBLE);
            post(new Runnable(bubble, flyoutMessage) {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$KX0TpqCGg3cAYGLQEThgtWbFX0 */
                public final /* synthetic */ Bubble f$1;
                public final /* synthetic */ Bubble.FlyoutMessage f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    BubbleStackView.this.lambda$animateInFlyoutForBubble$41$BubbleStackView(this.f$1, this.f$2);
                }
            });
            this.mFlyout.removeCallbacks(this.mHideFlyout);
            this.mFlyout.postDelayed(this.mHideFlyout, 5000);
            logBubbleEvent(bubble, 16);
        } else if (iconView != null) {
            iconView.removeDotSuppressionFlag(BadgedImageView.SuppressionFlag.FLYOUT_VISIBLE);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateInFlyoutForBubble$38 */
    public /* synthetic */ void lambda$animateInFlyoutForBubble$38$BubbleStackView(BadgedImageView badgedImageView) {
        this.mAfterFlyoutHidden = null;
        Bubble bubble = this.mBubbleToExpandAfterFlyoutCollapse;
        if (bubble != null) {
            this.mBubbleData.setSelectedBubble(bubble);
            this.mBubbleData.setExpanded(true);
            this.mBubbleToExpandAfterFlyoutCollapse = null;
        }
        badgedImageView.removeDotSuppressionFlag(BadgedImageView.SuppressionFlag.FLYOUT_VISIBLE);
        this.mFlyout.setVisibility(4);
        updateTemporarilyInvisibleAnimation(false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateInFlyoutForBubble$41 */
    public /* synthetic */ void lambda$animateInFlyoutForBubble$41$BubbleStackView(Bubble bubble, Bubble.FlyoutMessage flyoutMessage) {
        if (!isExpanded()) {
            $$Lambda$BubbleStackView$FdgpI1yIWBqhVrPpLrADoKYyrnw r7 = new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$FdgpI1yIWBqhVrPpLrADoKYyrnw */

                public final void run() {
                    BubbleStackView.this.lambda$animateInFlyoutForBubble$40$BubbleStackView();
                }
            };
            if (bubble.getIconView() != null) {
                this.mFlyout.setupFlyoutStartingAsDot(flyoutMessage, this.mStackAnimationController.getStackPosition(), (float) getWidth(), this.mStackAnimationController.isStackOnLeftSide(), bubble.getIconView().getDotColor(), r7, this.mAfterFlyoutHidden, bubble.getIconView().getDotCenter(), !bubble.showDot());
                this.mFlyout.bringToFront();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateInFlyoutForBubble$40 */
    public /* synthetic */ void lambda$animateInFlyoutForBubble$40$BubbleStackView() {
        $$Lambda$BubbleStackView$1wZEYs1bqQVpEdcpI6IEUDdY0OU r0 = new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$1wZEYs1bqQVpEdcpI6IEUDdY0OU */

            public final void run() {
                BubbleStackView.this.lambda$animateInFlyoutForBubble$39$BubbleStackView();
            }
        };
        this.mAnimateInFlyout = r0;
        this.mFlyout.postDelayed(r0, 200);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateInFlyoutForBubble$39 */
    public /* synthetic */ void lambda$animateInFlyoutForBubble$39$BubbleStackView() {
        int i;
        this.mFlyout.setVisibility(0);
        updateTemporarilyInvisibleAnimation(false);
        if (this.mStackAnimationController.isStackOnLeftSide()) {
            i = -this.mFlyout.getWidth();
        } else {
            i = this.mFlyout.getWidth();
        }
        this.mFlyoutDragDeltaX = (float) i;
        animateFlyoutCollapsed(false, 0.0f);
        this.mFlyout.postDelayed(this.mHideFlyout, 5000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideFlyoutImmediate() {
        clearFlyoutOnHide();
        this.mFlyout.removeCallbacks(this.mAnimateInFlyout);
        this.mFlyout.removeCallbacks(this.mHideFlyout);
        this.mFlyout.hideFlyout();
    }

    private void clearFlyoutOnHide() {
        this.mFlyout.removeCallbacks(this.mAnimateInFlyout);
        Runnable runnable = this.mAfterFlyoutHidden;
        if (runnable != null) {
            runnable.run();
            this.mAfterFlyoutHidden = null;
        }
    }

    public void getTouchableRegion(Rect rect) {
        View view = this.mUserEducationView;
        if (view == null || view.getVisibility() != 0) {
            if (this.mIsExpanded) {
                this.mBubbleContainer.getBoundsOnScreen(rect);
            } else if (getBubbleCount() > 0) {
                this.mBubbleContainer.getChildAt(0).getBoundsOnScreen(rect);
                int i = rect.top;
                int i2 = this.mBubbleTouchPadding;
                rect.top = i - i2;
                rect.left -= i2;
                rect.right += i2;
                rect.bottom += i2;
            }
            if (this.mFlyout.getVisibility() == 0) {
                Rect rect2 = new Rect();
                this.mFlyout.getBoundsOnScreen(rect2);
                rect.union(rect2);
                return;
            }
            return;
        }
        rect.set(0, 0, getWidth(), getHeight());
    }

    private int getStatusBarHeight() {
        int i = 0;
        if (getRootWindowInsets() == null) {
            return 0;
        }
        WindowInsets rootWindowInsets = getRootWindowInsets();
        int i2 = this.mStatusBarHeight;
        if (rootWindowInsets.getDisplayCutout() != null) {
            i = rootWindowInsets.getDisplayCutout().getSafeInsetTop();
        }
        return Math.max(i2, i);
    }

    private void requestUpdate() {
        if (!this.mViewUpdatedRequested && !this.mIsExpansionAnimating) {
            this.mViewUpdatedRequested = true;
            getViewTreeObserver().addOnPreDrawListener(this.mViewUpdater);
            invalidate();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showManageMenu(boolean z) {
        this.mShowingManage = z;
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null) {
            this.mManageMenu.setVisibility(4);
            return;
        }
        if (z && this.mBubbleData.hasBubbleInStackWithKey(this.mExpandedBubble.getKey())) {
            Bubble bubbleInStackWithKey = this.mBubbleData.getBubbleInStackWithKey(this.mExpandedBubble.getKey());
            this.mManageSettingsIcon.setImageDrawable(bubbleInStackWithKey.getBadgedAppIcon());
            this.mManageSettingsText.setText(getResources().getString(C0021R$string.bubbles_app_settings, bubbleInStackWithKey.getAppName()));
        }
        this.mExpandedBubble.getExpandedView().getManageButtonBoundsOnScreen(this.mTempRect);
        boolean z2 = getResources().getConfiguration().getLayoutDirection() == 0;
        Rect rect = this.mTempRect;
        float width = (float) (z2 ? rect.left : rect.right - this.mManageMenu.getWidth());
        float height = (float) (this.mTempRect.bottom - this.mManageMenu.getHeight());
        float width2 = ((float) ((z2 ? 1 : -1) * this.mManageMenu.getWidth())) / 4.0f;
        if (z) {
            this.mManageMenu.setScaleX(0.5f);
            this.mManageMenu.setScaleY(0.5f);
            this.mManageMenu.setTranslationX(width - width2);
            ViewGroup viewGroup = this.mManageMenu;
            viewGroup.setTranslationY((((float) viewGroup.getHeight()) / 4.0f) + height);
            this.mManageMenu.setAlpha(0.0f);
            PhysicsAnimator instance = PhysicsAnimator.getInstance(this.mManageMenu);
            instance.spring(DynamicAnimation.ALPHA, 1.0f);
            instance.spring(DynamicAnimation.SCALE_X, 1.0f);
            instance.spring(DynamicAnimation.SCALE_Y, 1.0f);
            instance.spring(DynamicAnimation.TRANSLATION_X, width);
            instance.spring(DynamicAnimation.TRANSLATION_Y, height);
            instance.withEndActions(new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$66q0xFex2JGOSPpLClbbWQL_UE */

                public final void run() {
                    BubbleStackView.this.lambda$showManageMenu$42$BubbleStackView();
                }
            });
            instance.start();
            this.mManageMenu.setVisibility(0);
        } else {
            PhysicsAnimator instance2 = PhysicsAnimator.getInstance(this.mManageMenu);
            instance2.spring(DynamicAnimation.ALPHA, 0.0f);
            instance2.spring(DynamicAnimation.SCALE_X, 0.5f);
            instance2.spring(DynamicAnimation.SCALE_Y, 0.5f);
            instance2.spring(DynamicAnimation.TRANSLATION_X, width - width2);
            instance2.spring(DynamicAnimation.TRANSLATION_Y, height + (((float) this.mManageMenu.getHeight()) / 4.0f));
            instance2.withEndActions(new Runnable() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$sNHlTJ3HvrRehpCED1n9iqYBH3A */

                public final void run() {
                    BubbleStackView.this.lambda$showManageMenu$43$BubbleStackView();
                }
            });
            instance2.start();
        }
        this.mExpandedBubble.getExpandedView().updateObscuredTouchableRegion();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showManageMenu$42 */
    public /* synthetic */ void lambda$showManageMenu$42$BubbleStackView() {
        this.mManageMenu.getChildAt(0).requestAccessibilityFocus();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showManageMenu$43 */
    public /* synthetic */ void lambda$showManageMenu$43$BubbleStackView() {
        this.mManageMenu.setVisibility(4);
    }

    private void updateExpandedBubble() {
        BubbleViewProvider bubbleViewProvider;
        this.mExpandedViewContainer.removeAllViews();
        if (this.mIsExpanded && (bubbleViewProvider = this.mExpandedBubble) != null && bubbleViewProvider.getExpandedView() != null) {
            BubbleExpandedView expandedView = this.mExpandedBubble.getExpandedView();
            expandedView.setContentVisibility(false);
            this.mExpandedViewContainerMatrix.setScaleX(0.0f);
            this.mExpandedViewContainerMatrix.setScaleY(0.0f);
            this.mExpandedViewContainerMatrix.setTranslate(0.0f, 0.0f);
            this.mExpandedViewContainer.setVisibility(4);
            this.mExpandedViewContainer.setAlpha(0.0f);
            this.mExpandedViewContainer.addView(expandedView);
            expandedView.setManageClickListener(new View.OnClickListener() {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$R5D2WvsrOupUqEboJoXeDAozj24 */

                public final void onClick(View view) {
                    BubbleStackView.this.lambda$updateExpandedBubble$44$BubbleStackView(view);
                }
            });
            expandedView.populateExpandedView();
            if (!this.mIsExpansionAnimating) {
                this.mSurfaceSynchronizer.syncSurfaceAndRun(new Runnable() {
                    /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$ULCkxljaWNb1HjCQS8HNQGEim50 */

                    public final void run() {
                        BubbleStackView.this.lambda$updateExpandedBubble$45$BubbleStackView();
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateExpandedBubble$44 */
    public /* synthetic */ void lambda$updateExpandedBubble$44$BubbleStackView(View view) {
        showManageMenu(!this.mShowingManage);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateExpandedBubble$45 */
    public /* synthetic */ void lambda$updateExpandedBubble$45$BubbleStackView() {
        post(new Runnable() {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$tSRUcpVJ_facfkGXYDqDR9OJwMQ */

            public final void run() {
                BubbleStackView.this.animateSwitchBubbles();
            }
        });
    }

    private void screenshotAnimatingOutBubbleIntoSurface(Consumer<Boolean> consumer) {
        BubbleViewProvider bubbleViewProvider;
        Boolean bool = Boolean.FALSE;
        if (!this.mIsExpanded || (bubbleViewProvider = this.mExpandedBubble) == null || bubbleViewProvider.getExpandedView() == null) {
            consumer.accept(bool);
            return;
        }
        BubbleExpandedView expandedView = this.mExpandedBubble.getExpandedView();
        if (this.mAnimatingOutBubbleBuffer != null) {
            releaseAnimatingOutBubbleBuffer();
        }
        try {
            this.mAnimatingOutBubbleBuffer = expandedView.snapshotActivitySurface();
        } catch (Exception e) {
            Log.wtf("Bubbles", e);
            consumer.accept(bool);
        }
        SurfaceControl.ScreenshotGraphicBuffer screenshotGraphicBuffer = this.mAnimatingOutBubbleBuffer;
        if (screenshotGraphicBuffer == null || screenshotGraphicBuffer.getGraphicBuffer() == null) {
            consumer.accept(bool);
            return;
        }
        PhysicsAnimator.getInstance(this.mAnimatingOutSurfaceContainer).cancel();
        this.mAnimatingOutSurfaceContainer.setScaleX(1.0f);
        this.mAnimatingOutSurfaceContainer.setScaleY(1.0f);
        this.mAnimatingOutSurfaceContainer.setTranslationX(0.0f);
        this.mAnimatingOutSurfaceContainer.setTranslationY(0.0f);
        this.mAnimatingOutSurfaceContainer.setTranslationY((float) (this.mExpandedBubble.getExpandedView().getActivityViewLocationOnScreen()[1] - this.mAnimatingOutSurfaceView.getLocationOnScreen()[1]));
        this.mAnimatingOutSurfaceView.getLayoutParams().width = this.mAnimatingOutBubbleBuffer.getGraphicBuffer().getWidth();
        this.mAnimatingOutSurfaceView.getLayoutParams().height = this.mAnimatingOutBubbleBuffer.getGraphicBuffer().getHeight();
        this.mAnimatingOutSurfaceView.requestLayout();
        post(new Runnable(consumer) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$MCpRhr7435uVYw2HGhQ51yVBRac */
            public final /* synthetic */ Consumer f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                BubbleStackView.this.lambda$screenshotAnimatingOutBubbleIntoSurface$48$BubbleStackView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$screenshotAnimatingOutBubbleIntoSurface$48 */
    public /* synthetic */ void lambda$screenshotAnimatingOutBubbleIntoSurface$48$BubbleStackView(Consumer consumer) {
        Boolean bool = Boolean.FALSE;
        if (this.mAnimatingOutBubbleBuffer.getGraphicBuffer().isDestroyed()) {
            consumer.accept(bool);
        } else if (!this.mIsExpanded) {
            consumer.accept(bool);
        } else {
            this.mAnimatingOutSurfaceView.getHolder().getSurface().attachAndQueueBufferWithColorSpace(this.mAnimatingOutBubbleBuffer.getGraphicBuffer(), this.mAnimatingOutBubbleBuffer.getColorSpace());
            this.mSurfaceSynchronizer.syncSurfaceAndRun(new Runnable(consumer) {
                /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$0p9zU4O79lP7pId00KTtOu3oOm4 */
                public final /* synthetic */ Consumer f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    BubbleStackView.this.lambda$screenshotAnimatingOutBubbleIntoSurface$47$BubbleStackView(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$screenshotAnimatingOutBubbleIntoSurface$47 */
    public /* synthetic */ void lambda$screenshotAnimatingOutBubbleIntoSurface$47$BubbleStackView(Consumer consumer) {
        post(new Runnable(consumer) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleStackView$FdX0ZJ9EYnkK2lpUxRIrtE_sXRw */
            public final /* synthetic */ Consumer f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.accept(Boolean.TRUE);
            }
        });
    }

    /* access modifiers changed from: private */
    public void releaseAnimatingOutBubbleBuffer() {
        SurfaceControl.ScreenshotGraphicBuffer screenshotGraphicBuffer = this.mAnimatingOutBubbleBuffer;
        if (screenshotGraphicBuffer != null && !screenshotGraphicBuffer.getGraphicBuffer().isDestroyed()) {
            this.mAnimatingOutBubbleBuffer.getGraphicBuffer().destroy();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateExpandedView() {
        this.mExpandedViewContainer.setVisibility(this.mIsExpanded ? 0 : 8);
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (!(bubbleViewProvider == null || bubbleViewProvider.getExpandedView() == null)) {
            this.mExpandedViewContainer.setTranslationY(getExpandedViewY());
            this.mExpandedBubble.getExpandedView().updateView(this.mExpandedViewContainer.getLocationOnScreen());
        }
        this.mStackOnLeftOrWillBe = this.mStackAnimationController.isStackOnLeftSide();
        updateBubbleZOrdersAndDotPosition(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBubbleZOrdersAndDotPosition(boolean z) {
        int bubbleCount = getBubbleCount();
        for (int i = 0; i < bubbleCount; i++) {
            BadgedImageView badgedImageView = (BadgedImageView) this.mBubbleContainer.getChildAt(i);
            badgedImageView.setZ((float) ((this.mMaxBubbles * this.mBubbleElevation) - i));
            boolean dotPositionOnLeft = badgedImageView.getDotPositionOnLeft();
            boolean z2 = this.mStackOnLeftOrWillBe;
            if (dotPositionOnLeft == z2) {
                badgedImageView.setDotPositionOnLeft(!z2, z);
            }
            if (this.mIsExpanded || i <= 0) {
                badgedImageView.removeDotSuppressionFlag(BadgedImageView.SuppressionFlag.BEHIND_STACK);
            } else {
                badgedImageView.addDotSuppressionFlag(BadgedImageView.SuppressionFlag.BEHIND_STACK);
            }
        }
    }

    private void updatePointerPosition() {
        int bubbleIndex;
        BubbleViewProvider bubbleViewProvider = this.mExpandedBubble;
        if (bubbleViewProvider != null && bubbleViewProvider.getExpandedView() != null && (bubbleIndex = getBubbleIndex(this.mExpandedBubble)) != -1) {
            this.mExpandedBubble.getExpandedView().setPointerPosition((this.mExpandedAnimationController.getBubbleLeft(bubbleIndex) + (((float) this.mBubbleSize) / 2.0f)) - ((float) this.mExpandedViewContainer.getPaddingLeft()));
        }
    }

    public int getBubbleCount() {
        return this.mBubbleContainer.getChildCount() - 1;
    }

    /* access modifiers changed from: package-private */
    public int getBubbleIndex(BubbleViewProvider bubbleViewProvider) {
        if (bubbleViewProvider == null) {
            return 0;
        }
        return this.mBubbleContainer.indexOfChild(bubbleViewProvider.getIconView());
    }

    public float getNormalizedXPosition() {
        BigDecimal bigDecimal = new BigDecimal((double) (getStackPosition().x / ((float) this.mDisplaySize.x)));
        RoundingMode roundingMode = RoundingMode.CEILING;
        return bigDecimal.setScale(4, RoundingMode.HALF_UP).floatValue();
    }

    public float getNormalizedYPosition() {
        BigDecimal bigDecimal = new BigDecimal((double) (getStackPosition().y / ((float) this.mDisplaySize.y)));
        RoundingMode roundingMode = RoundingMode.CEILING;
        return bigDecimal.setScale(4, RoundingMode.HALF_UP).floatValue();
    }

    public PointF getStackPosition() {
        return this.mStackAnimationController.getStackPosition();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logBubbleEvent(BubbleViewProvider bubbleViewProvider, int i) {
        String str = "Overflow";
        if (bubbleViewProvider == null || bubbleViewProvider.getKey().equals(str)) {
            String str2 = ((FrameLayout) this).mContext.getApplicationInfo().packageName;
            if (bubbleViewProvider == null) {
                str = null;
            }
            SysUiStatsLog.write(149, str2, str, 0, 0, getBubbleCount(), i, getNormalizedXPosition(), getNormalizedYPosition(), false, false, false);
            return;
        }
        bubbleViewProvider.logUIEvent(getBubbleCount(), i, getNormalizedXPosition(), getNormalizedYPosition(), getBubbleIndex(bubbleViewProvider));
    }

    /* access modifiers changed from: package-private */
    public boolean performBackPressIfNeeded() {
        BubbleViewProvider bubbleViewProvider;
        if (!isExpanded() || (bubbleViewProvider = this.mExpandedBubble) == null || bubbleViewProvider.getExpandedView() == null) {
            return false;
        }
        return this.mExpandedBubble.getExpandedView().performBackPressIfNeeded();
    }

    private boolean shouldShowBubblesEducation() {
        if (BubbleDebugConfig.forceShowUserEducation(getContext()) || !Prefs.getBoolean(getContext(), "HasSeenBubblesOnboarding", false)) {
            return true;
        }
        return false;
    }

    private boolean shouldShowManageEducation() {
        if (BubbleDebugConfig.forceShowUserEducation(getContext()) || !Prefs.getBoolean(getContext(), "HasSeenBubblesManageOnboarding", false)) {
            return true;
        }
        return false;
    }
}
