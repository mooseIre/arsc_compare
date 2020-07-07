package com.android.systemui.bubbles;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.StatsLog;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.ViewClippingUtil;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.animation.ExpandedAnimationController;
import com.android.systemui.bubbles.animation.PhysicsAnimationLayout;
import com.android.systemui.bubbles.animation.StackAnimationController;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NotificationData;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public class BubbleStackView extends FrameLayout {
    private static final SurfaceSynchronizer DEFAULT_SURFACE_SYNCHRONIZER = new SurfaceSynchronizer() {
        public void syncSurfaceAndRun(final Runnable runnable) {
            Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
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
    private Runnable mAfterFlyoutHides;
    private final DynamicAnimation.OnAnimationEndListener mAfterFlyoutTransitionSpring = new DynamicAnimation.OnAnimationEndListener() {
        public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
            BubbleStackView.this.lambda$new$1$BubbleStackView(dynamicAnimation, z, f, f2);
        }
    };
    private Runnable mAfterMagnet;
    private boolean mAnimatingMagnet = false;
    private PhysicsAnimationLayout mBubbleContainer;
    private final BubbleData mBubbleData;
    private int mBubblePadding;
    /* access modifiers changed from: private */
    public int mBubbleSize;
    private ViewClippingUtil.ClippingParameters mClippingParameters = new ViewClippingUtil.ClippingParameters() {
        public boolean shouldFinish(View view) {
            return false;
        }

        public boolean isClippingEnablingAllowed(View view) {
            return !BubbleStackView.this.mIsExpanded;
        }
    };
    private final ValueAnimator mDesaturateAndDarkenAnimator;
    private final Paint mDesaturateAndDarkenPaint = new Paint();
    private View mDesaturateAndDarkenTargetView;
    private BubbleDismissView mDismissContainer;
    private Point mDisplaySize;
    private boolean mDraggingInDismissTarget = false;
    private BubbleController.BubbleExpandListener mExpandListener;
    private int mExpandedAnimateXDistance;
    private int mExpandedAnimateYDistance;
    private ExpandedAnimationController mExpandedAnimationController;
    private Bubble mExpandedBubble;
    private FrameLayout mExpandedViewContainer;
    private int mExpandedViewPadding;
    private final SpringAnimation mExpandedViewXAnim;
    private final SpringAnimation mExpandedViewYAnim;
    private BubbleFlyoutView mFlyout;
    private final FloatPropertyCompat mFlyoutCollapseProperty = new FloatPropertyCompat("FlyoutCollapseSpring") {
        public float getValue(Object obj) {
            return BubbleStackView.this.mFlyoutDragDeltaX;
        }

        public void setValue(Object obj, float f) {
            BubbleStackView.this.onFlyoutDragged(f);
        }
    };
    /* access modifiers changed from: private */
    public float mFlyoutDragDeltaX = 0.0f;
    private final SpringAnimation mFlyoutTransitionSpring = new SpringAnimation(this, this.mFlyoutCollapseProperty);
    private Runnable mHideFlyout = new Runnable() {
        public final void run() {
            BubbleStackView.this.lambda$new$0$BubbleStackView();
        }
    };
    private int mImeOffset;
    private boolean mImeVisible;
    private LayoutInflater mInflater;
    /* access modifiers changed from: private */
    public boolean mIsExpanded;
    private boolean mIsExpansionAnimating = false;
    private boolean mIsGestureInProgress = false;
    private View.OnLayoutChangeListener mMoveStackToValidPositionOnLayoutListener;
    private int mPipDismissHeight;
    private int mPointerHeight;
    private boolean mShowingDismiss = false;
    private StackAnimationController mStackAnimationController;
    private boolean mStackOnLeftOrWillBe = false;
    private int mStatusBarHeight;
    private boolean mSuppressFlyout = false;
    private boolean mSuppressNewDot = false;
    private final SurfaceSynchronizer mSurfaceSynchronizer;
    private ViewTreeObserver.OnDrawListener mSystemGestureExcludeUpdater = new ViewTreeObserver.OnDrawListener() {
        public final void onDraw() {
            BubbleStackView.this.updateSystemGestureExcludeRects();
        }
    };
    private final List<Rect> mSystemGestureExclusionRects = Collections.singletonList(new Rect());
    int[] mTempLoc = new int[2];
    RectF mTempRect = new RectF();
    private BubbleTouchHandler mTouchHandler;
    private float mVerticalPosPercentBeforeRotation = -1.0f;
    private final Vibrator mVibrator;
    /* access modifiers changed from: private */
    public boolean mViewUpdatedRequested = false;
    /* access modifiers changed from: private */
    public ViewTreeObserver.OnPreDrawListener mViewUpdater = new ViewTreeObserver.OnPreDrawListener() {
        public boolean onPreDraw() {
            BubbleStackView.this.getViewTreeObserver().removeOnPreDrawListener(BubbleStackView.this.mViewUpdater);
            BubbleStackView.this.applyCurrentState();
            boolean unused = BubbleStackView.this.mViewUpdatedRequested = false;
            return true;
        }
    };
    private boolean mWasOnLeftBeforeRotation = false;

    interface SurfaceSynchronizer {
        void syncSurfaceAndRun(Runnable runnable);
    }

    public /* synthetic */ void lambda$new$0$BubbleStackView() {
        animateFlyoutCollapsed(true, 0.0f);
    }

    public /* synthetic */ void lambda$new$1$BubbleStackView(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
        if (this.mFlyoutDragDeltaX == 0.0f) {
            this.mFlyout.postDelayed(this.mHideFlyout, 5000);
        } else {
            this.mFlyout.hideFlyout();
        }
    }

    public BubbleStackView(Context context, BubbleData bubbleData, SurfaceSynchronizer surfaceSynchronizer) {
        super(context);
        this.mBubbleData = bubbleData;
        this.mInflater = LayoutInflater.from(context);
        this.mTouchHandler = new BubbleTouchHandler(this, bubbleData, context);
        setOnTouchListener(this.mTouchHandler);
        this.mInflater = LayoutInflater.from(context);
        Resources resources = getResources();
        this.mBubbleSize = resources.getDimensionPixelSize(R.dimen.individual_bubble_size);
        this.mBubblePadding = resources.getDimensionPixelSize(R.dimen.bubble_padding);
        this.mExpandedAnimateXDistance = resources.getDimensionPixelSize(R.dimen.bubble_expanded_animate_x_distance);
        this.mExpandedAnimateYDistance = resources.getDimensionPixelSize(R.dimen.bubble_expanded_animate_y_distance);
        this.mPointerHeight = resources.getDimensionPixelSize(R.dimen.bubble_pointer_height);
        this.mStatusBarHeight = resources.getDimensionPixelSize(17105478);
        this.mPipDismissHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.pip_dismiss_gradient_height);
        this.mImeOffset = resources.getDimensionPixelSize(R.dimen.pip_ime_offset);
        this.mDisplaySize = new Point();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getSize(this.mDisplaySize);
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mExpandedViewPadding = resources.getDimensionPixelSize(R.dimen.bubble_expanded_view_padding);
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.bubble_elevation);
        this.mStackAnimationController = new StackAnimationController();
        this.mExpandedAnimationController = new ExpandedAnimationController(this.mDisplaySize, this.mExpandedViewPadding);
        this.mSurfaceSynchronizer = surfaceSynchronizer == null ? DEFAULT_SURFACE_SYNCHRONIZER : surfaceSynchronizer;
        this.mBubbleContainer = new PhysicsAnimationLayout(context);
        this.mBubbleContainer.setActiveController(this.mStackAnimationController);
        float f = (float) dimensionPixelSize;
        this.mBubbleContainer.setElevation(f);
        this.mBubbleContainer.setClipChildren(false);
        addView(this.mBubbleContainer, new FrameLayout.LayoutParams(-1, -1));
        this.mExpandedViewContainer = new FrameLayout(context);
        this.mExpandedViewContainer.setElevation(f);
        FrameLayout frameLayout = this.mExpandedViewContainer;
        int i = this.mExpandedViewPadding;
        frameLayout.setPadding(i, i, i, i);
        this.mExpandedViewContainer.setClipChildren(false);
        addView(this.mExpandedViewContainer);
        this.mFlyout = new BubbleFlyoutView(context);
        this.mFlyout.setVisibility(8);
        this.mFlyout.animate().setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator());
        addView(this.mFlyout, new FrameLayout.LayoutParams(-2, -2));
        SpringAnimation springAnimation = this.mFlyoutTransitionSpring;
        SpringForce springForce = new SpringForce();
        springForce.setStiffness(1500.0f);
        springForce.setDampingRatio(0.75f);
        springAnimation.setSpring(springForce);
        this.mFlyoutTransitionSpring.addEndListener(this.mAfterFlyoutTransitionSpring);
        this.mDismissContainer = new BubbleDismissView(this.mContext);
        this.mDismissContainer.setLayoutParams(new FrameLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.pip_dismiss_gradient_height), 80));
        addView(this.mDismissContainer);
        this.mDismissContainer = new BubbleDismissView(this.mContext);
        this.mDismissContainer.setLayoutParams(new FrameLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.pip_dismiss_gradient_height), 80));
        addView(this.mDismissContainer);
        this.mExpandedViewXAnim = new SpringAnimation(this.mExpandedViewContainer, DynamicAnimation.TRANSLATION_X);
        SpringAnimation springAnimation2 = this.mExpandedViewXAnim;
        SpringForce springForce2 = new SpringForce();
        springForce2.setStiffness(200.0f);
        springForce2.setDampingRatio(0.75f);
        springAnimation2.setSpring(springForce2);
        this.mExpandedViewYAnim = new SpringAnimation(this.mExpandedViewContainer, DynamicAnimation.TRANSLATION_Y);
        SpringAnimation springAnimation3 = this.mExpandedViewYAnim;
        SpringForce springForce3 = new SpringForce();
        springForce3.setStiffness(200.0f);
        springForce3.setDampingRatio(0.75f);
        springAnimation3.setSpring(springForce3);
        this.mExpandedViewYAnim.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                BubbleStackView.this.lambda$new$2$BubbleStackView(dynamicAnimation, z, f, f2);
            }
        });
        setClipChildren(false);
        setFocusable(true);
        this.mBubbleContainer.bringToFront();
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return BubbleStackView.this.lambda$new$4$BubbleStackView(view, windowInsets);
            }
        });
        this.mMoveStackToValidPositionOnLayoutListener = new View.OnLayoutChangeListener() {
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                BubbleStackView.this.lambda$new$5$BubbleStackView(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        };
        getViewTreeObserver().addOnDrawListener(this.mSystemGestureExcludeUpdater);
        ColorMatrix colorMatrix = new ColorMatrix();
        ColorMatrix colorMatrix2 = new ColorMatrix();
        this.mDesaturateAndDarkenAnimator = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
        this.mDesaturateAndDarkenAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(colorMatrix, colorMatrix2) {
            private final /* synthetic */ ColorMatrix f$1;
            private final /* synthetic */ ColorMatrix f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                BubbleStackView.this.lambda$new$6$BubbleStackView(this.f$1, this.f$2, valueAnimator);
            }
        });
    }

    public /* synthetic */ void lambda$new$2$BubbleStackView(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
        Bubble bubble;
        if (this.mIsExpanded && (bubble = this.mExpandedBubble) != null) {
            bubble.expandedView.updateView();
        }
    }

    public /* synthetic */ WindowInsets lambda$new$4$BubbleStackView(View view, WindowInsets windowInsets) {
        int systemWindowInsetBottom = windowInsets.getSystemWindowInsetBottom() - windowInsets.getStableInsetBottom();
        if (!this.mIsExpanded || this.mIsExpansionAnimating) {
            return view.onApplyWindowInsets(windowInsets);
        }
        this.mImeVisible = systemWindowInsetBottom != 0;
        this.mExpandedViewYAnim.animateToFinalPosition(getYPositionForExpandedView());
        this.mExpandedAnimationController.updateYPosition(new Runnable(windowInsets) {
            private final /* synthetic */ WindowInsets f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                BubbleStackView.this.lambda$new$3$BubbleStackView(this.f$1);
            }
        });
        return view.onApplyWindowInsets(windowInsets);
    }

    public /* synthetic */ void lambda$new$3$BubbleStackView(WindowInsets windowInsets) {
        this.mExpandedBubble.expandedView.updateInsets(windowInsets);
    }

    public /* synthetic */ void lambda$new$5$BubbleStackView(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        float f = this.mVerticalPosPercentBeforeRotation;
        if (f >= 0.0f) {
            this.mStackAnimationController.moveStackToSimilarPositionAfterRotation(this.mWasOnLeftBeforeRotation, f);
        }
        removeOnLayoutChangeListener(this.mMoveStackToValidPositionOnLayoutListener);
    }

    public /* synthetic */ void lambda$new$6$BubbleStackView(ColorMatrix colorMatrix, ColorMatrix colorMatrix2, ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        colorMatrix.setSaturation(floatValue);
        float f = 1.0f - ((1.0f - floatValue) * 0.3f);
        colorMatrix2.setScale(f, f, f, 1.0f);
        colorMatrix.postConcat(colorMatrix2);
        this.mDesaturateAndDarkenPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        this.mDesaturateAndDarkenTargetView.setLayerPaint(this.mDesaturateAndDarkenPaint);
    }

    public void onThemeChanged() {
        for (Bubble next : this.mBubbleData.getBubbles()) {
            next.iconView.updateViews();
            next.expandedView.applyThemeAttrs();
        }
    }

    public void onOrientationChanged() {
        RectF allowableStackPositionRegion = this.mStackAnimationController.getAllowableStackPositionRegion();
        this.mWasOnLeftBeforeRotation = this.mStackAnimationController.isStackOnLeftSide();
        float f = this.mStackAnimationController.getStackPosition().y;
        float f2 = allowableStackPositionRegion.top;
        this.mVerticalPosPercentBeforeRotation = (f - f2) / (allowableStackPositionRegion.bottom - f2);
        addOnLayoutChangeListener(this.mMoveStackToValidPositionOnLayoutListener);
        hideFlyoutImmediate();
    }

    public void getBoundsOnScreen(Rect rect, boolean z) {
        getBoundsOnScreen(rect);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.mViewUpdater);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        if (!this.mIsExpanded || !isIntersecting(this.mExpandedViewContainer, rawX, rawY)) {
            return isIntersecting(this.mBubbleContainer, rawX, rawY);
        }
        return false;
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfoInternal(accessibilityNodeInfo);
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_top_left, getContext().getResources().getString(R.string.bubble_accessibility_action_move_top_left)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_top_right, getContext().getResources().getString(R.string.bubble_accessibility_action_move_top_right)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_bottom_left, getContext().getResources().getString(R.string.bubble_accessibility_action_move_bottom_left)));
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_bottom_right, getContext().getResources().getString(R.string.bubble_accessibility_action_move_bottom_right)));
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
            return true;
        } else if (i == 524288) {
            this.mBubbleData.setExpanded(false);
            return true;
        } else if (i == 262144) {
            this.mBubbleData.setExpanded(true);
            return true;
        } else if (i == R.id.action_move_top_left) {
            this.mStackAnimationController.springStack(allowableStackPositionRegion.left, allowableStackPositionRegion.top);
            return true;
        } else if (i == R.id.action_move_top_right) {
            this.mStackAnimationController.springStack(allowableStackPositionRegion.right, allowableStackPositionRegion.top);
            return true;
        } else if (i == R.id.action_move_bottom_left) {
            this.mStackAnimationController.springStack(allowableStackPositionRegion.left, allowableStackPositionRegion.bottom);
            return true;
        } else if (i != R.id.action_move_bottom_right) {
            return false;
        } else {
            this.mStackAnimationController.springStack(allowableStackPositionRegion.right, allowableStackPositionRegion.bottom);
            return true;
        }
    }

    public void updateContentDescription() {
        if (!this.mBubbleData.getBubbles().isEmpty()) {
            Bubble bubble = this.mBubbleData.getBubbles().get(0);
            String appName = bubble.getAppName();
            CharSequence charSequence = bubble.entry.notification.getNotification().extras.getCharSequence("android.title");
            String string = getResources().getString(R.string.stream_notification);
            if (charSequence != null) {
                string = charSequence.toString();
            }
            int childCount = this.mBubbleContainer.getChildCount() - 1;
            String string2 = getResources().getString(R.string.bubble_content_description_single, new Object[]{string, appName});
            String string3 = getResources().getString(R.string.bubble_content_description_stack, new Object[]{string, appName, Integer.valueOf(childCount)});
            if (!this.mIsExpanded) {
                if (childCount > 0) {
                    this.mBubbleContainer.setContentDescription(string3);
                } else {
                    this.mBubbleContainer.setContentDescription(string2);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateSystemGestureExcludeRects() {
        Rect rect = this.mSystemGestureExclusionRects.get(0);
        if (this.mBubbleContainer.getChildCount() > 0) {
            View childAt = this.mBubbleContainer.getChildAt(0);
            rect.set(childAt.getLeft(), childAt.getTop(), childAt.getRight(), childAt.getBottom());
            rect.offset((int) (childAt.getTranslationX() + 0.5f), (int) (childAt.getTranslationY() + 0.5f));
            this.mBubbleContainer.setSystemGestureExclusionRects(this.mSystemGestureExclusionRects);
            return;
        }
        rect.setEmpty();
        this.mBubbleContainer.setSystemGestureExclusionRects(Collections.emptyList());
    }

    public void updateDotVisibility(String str) {
        Bubble bubbleWithKey = this.mBubbleData.getBubbleWithKey(str);
        if (bubbleWithKey != null) {
            bubbleWithKey.updateDotVisibility();
        }
    }

    public void setExpandListener(BubbleController.BubbleExpandListener bubbleExpandListener) {
        this.mExpandListener = bubbleExpandListener;
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    /* access modifiers changed from: package-private */
    public BubbleView getExpandedBubbleView() {
        Bubble bubble = this.mExpandedBubble;
        if (bubble != null) {
            return bubble.iconView;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public Bubble getExpandedBubble() {
        return this.mExpandedBubble;
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public void setExpandedBubble(String str) {
        Bubble bubbleWithKey = this.mBubbleData.getBubbleWithKey(str);
        if (bubbleWithKey != null) {
            setSelectedBubble(bubbleWithKey);
            bubbleWithKey.entry.setShowInShadeWhenBubble(false);
            setExpanded(true);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setExpandedBubble(NotificationData.Entry entry) {
        for (int i = 0; i < this.mBubbleContainer.getChildCount(); i++) {
            if (entry.equals(((BubbleView) this.mBubbleContainer.getChildAt(i)).getEntry())) {
                setExpandedBubble(entry.key);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addBubble(Bubble bubble) {
        bubble.inflate(this.mInflater, this);
        this.mBubbleContainer.addView(bubble.iconView, 0, new FrameLayout.LayoutParams(-2, -2));
        ViewClippingUtil.setClippingDeactivated(bubble.iconView, true, this.mClippingParameters);
        BubbleView bubbleView = bubble.iconView;
        if (bubbleView != null) {
            bubbleView.setSuppressDot(this.mSuppressNewDot, false);
        }
        animateInFlyoutForBubble(bubble);
        requestUpdate();
        logBubbleEvent(bubble, 1);
        updatePointerPosition();
    }

    /* access modifiers changed from: package-private */
    public void removeBubble(Bubble bubble) {
        int indexOfChild = this.mBubbleContainer.indexOfChild(bubble.iconView);
        if (indexOfChild >= 0) {
            this.mBubbleContainer.removeViewAt(indexOfChild);
            logBubbleEvent(bubble, 5);
        } else {
            Log.d("BubbleStackView", "was asked to remove Bubble, but didn't find the view! " + bubble);
        }
        updatePointerPosition();
    }

    /* access modifiers changed from: package-private */
    public void updateBubble(Bubble bubble) {
        animateInFlyoutForBubble(bubble);
        requestUpdate();
        logBubbleEvent(bubble, 2);
    }

    public void updateBubbleOrder(List<Bubble> list) {
        for (int i = 0; i < list.size(); i++) {
            this.mBubbleContainer.reorderView(list.get(i).iconView, i);
        }
    }

    public void setSelectedBubble(Bubble bubble) {
        Bubble bubble2 = this.mExpandedBubble;
        if (bubble2 == null || !bubble2.equals(bubble)) {
            Bubble bubble3 = this.mExpandedBubble;
            this.mExpandedBubble = bubble;
            if (this.mIsExpanded) {
                this.mExpandedViewContainer.setAlpha(0.0f);
                this.mSurfaceSynchronizer.syncSurfaceAndRun(new Runnable(bubble3, bubble) {
                    private final /* synthetic */ Bubble f$1;
                    private final /* synthetic */ Bubble f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        BubbleStackView.this.lambda$setSelectedBubble$7$BubbleStackView(this.f$1, this.f$2);
                    }
                });
            }
        }
    }

    public /* synthetic */ void lambda$setSelectedBubble$7$BubbleStackView(Bubble bubble, Bubble bubble2) {
        NotificationData.Entry entry;
        updateExpandedBubble();
        updatePointerPosition();
        requestUpdate();
        logBubbleEvent(bubble, 4);
        logBubbleEvent(bubble2, 3);
        notifyExpansionChanged(bubble.entry, false);
        if (bubble2 == null) {
            entry = null;
        } else {
            entry = bubble2.entry;
        }
        notifyExpansionChanged(entry, true);
    }

    public void setExpanded(boolean z) {
        boolean z2 = this.mIsExpanded;
        if (z != z2) {
            if (z2) {
                animateExpansion(false);
                logBubbleEvent(this.mExpandedBubble, 4);
            } else {
                animateExpansion(true);
                logBubbleEvent(this.mExpandedBubble, 3);
                logBubbleEvent(this.mExpandedBubble, 15);
            }
            notifyExpansionChanged(this.mExpandedBubble.entry, this.mIsExpanded);
        }
    }

    public View getTargetView(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        if (!this.mIsExpanded) {
            return (this.mFlyout.getVisibility() != 0 || !isIntersecting(this.mFlyout, rawX, rawY)) ? this : this.mFlyout;
        }
        if (isIntersecting(this.mBubbleContainer, rawX, rawY)) {
            for (int i = 0; i < this.mBubbleContainer.getChildCount(); i++) {
                BubbleView bubbleView = (BubbleView) this.mBubbleContainer.getChildAt(i);
                if (isIntersecting(bubbleView, rawX, rawY)) {
                    return bubbleView;
                }
            }
            return null;
        } else if (isIntersecting(this.mExpandedViewContainer, rawX, rawY)) {
            return this.mExpandedViewContainer;
        } else {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public View getFlyoutView() {
        return this.mFlyout;
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public void collapseStack() {
        this.mBubbleData.setExpanded(false);
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public void collapseStack(Runnable runnable) {
        collapseStack();
        runnable.run();
    }

    private void animateExpansion(boolean z) {
        int i;
        if (this.mIsExpanded != z) {
            hideFlyoutImmediate();
            this.mIsExpanded = z;
            updateExpandedBubble();
            applyCurrentState();
            this.mIsExpansionAnimating = true;
            $$Lambda$BubbleStackView$5c3dXYvEqr4qSSbPrW_SOEdPjE r0 = new Runnable() {
                public final void run() {
                    BubbleStackView.this.lambda$animateExpansion$8$BubbleStackView();
                }
            };
            if (z) {
                this.mBubbleContainer.setActiveController(this.mExpandedAnimationController);
                this.mExpandedAnimationController.expandFromStack(new Runnable(r0) {
                    private final /* synthetic */ Runnable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        BubbleStackView.this.lambda$animateExpansion$9$BubbleStackView(this.f$1);
                    }
                });
            } else {
                this.mBubbleContainer.cancelAllAnimations();
                this.mExpandedAnimationController.collapseBackToStack(this.mStackAnimationController.getStackPositionAlongNearestHorizontalEdge(), new Runnable(r0) {
                    private final /* synthetic */ Runnable f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        BubbleStackView.this.lambda$animateExpansion$10$BubbleStackView(this.f$1);
                    }
                });
            }
            if (this.mStackAnimationController.getStackPosition().x < ((float) (getWidth() / 2))) {
                i = -this.mExpandedAnimateXDistance;
            } else {
                i = this.mExpandedAnimateXDistance;
            }
            float f = (float) i;
            float min = Math.min(this.mStackAnimationController.getStackPosition().y, (float) this.mExpandedAnimateYDistance);
            float yPositionForExpandedView = getYPositionForExpandedView();
            float f2 = 0.0f;
            if (z) {
                this.mExpandedViewContainer.setTranslationX(f);
                this.mExpandedViewContainer.setTranslationY(min);
                this.mExpandedViewContainer.setAlpha(0.0f);
            }
            SpringAnimation springAnimation = this.mExpandedViewXAnim;
            if (z) {
                f = 0.0f;
            }
            springAnimation.animateToFinalPosition(f);
            SpringAnimation springAnimation2 = this.mExpandedViewYAnim;
            if (z) {
                min = yPositionForExpandedView;
            }
            springAnimation2.animateToFinalPosition(min);
            ViewPropertyAnimator duration = this.mExpandedViewContainer.animate().setDuration(100);
            if (z) {
                f2 = 1.0f;
            }
            duration.alpha(f2);
        }
    }

    public /* synthetic */ void lambda$animateExpansion$8$BubbleStackView() {
        applyCurrentState();
        this.mIsExpansionAnimating = false;
        requestUpdate();
    }

    public /* synthetic */ void lambda$animateExpansion$9$BubbleStackView(Runnable runnable) {
        updatePointerPosition();
        runnable.run();
    }

    public /* synthetic */ void lambda$animateExpansion$10$BubbleStackView(Runnable runnable) {
        this.mBubbleContainer.setActiveController(this.mStackAnimationController);
        runnable.run();
    }

    private void notifyExpansionChanged(NotificationData.Entry entry, boolean z) {
        BubbleController.BubbleExpandListener bubbleExpandListener = this.mExpandListener;
        if (bubbleExpandListener != null) {
            bubbleExpandListener.onBubbleExpandChanged(z, entry != null ? entry.key : null);
        }
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mStackAnimationController.setImeHeight(i + this.mImeOffset);
        if (!this.mIsExpanded) {
            this.mStackAnimationController.animateForImeVisibility(z);
        }
    }

    public void onBubbleDragStart(View view) {
        this.mExpandedAnimationController.prepareForBubbleDrag(view);
    }

    public void onBubbleDragged(View view, float f, float f2) {
        if (this.mIsExpanded && !this.mIsExpansionAnimating) {
            this.mExpandedAnimationController.dragBubbleOut(view, f, f2);
            springInDismissTarget();
        }
    }

    public void onBubbleDragFinish(View view, float f, float f2, float f3, float f4) {
        if (this.mIsExpanded && !this.mIsExpansionAnimating) {
            this.mExpandedAnimationController.snapBubbleBack(view, f3, f4);
            springOutDismissTargetAndHideCircle();
        }
    }

    /* access modifiers changed from: package-private */
    public void onDragStart() {
        if (!this.mIsExpanded && !this.mIsExpansionAnimating) {
            this.mStackAnimationController.cancelStackPositionAnimations();
            this.mBubbleContainer.setActiveController(this.mStackAnimationController);
            hideFlyoutImmediate();
            this.mDraggingInDismissTarget = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void onDragged(float f, float f2) {
        if (!this.mIsExpanded && !this.mIsExpansionAnimating) {
            springInDismissTarget();
            this.mStackAnimationController.moveStackFromTouch(f, f2);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDragFinish(float f, float f2, float f3, float f4) {
        if (!this.mIsExpanded && !this.mIsExpansionAnimating) {
            float flingStackThenSpringToEdge = this.mStackAnimationController.flingStackThenSpringToEdge(f, f3, f4);
            logBubbleEvent((Bubble) null, 7);
            this.mStackOnLeftOrWillBe = flingStackThenSpringToEdge <= 0.0f;
            updateBubbleShadowsAndDotPosition(true);
            springOutDismissTargetAndHideCircle();
        }
    }

    /* access modifiers changed from: package-private */
    public void onFlyoutDragStart() {
        this.mFlyout.removeCallbacks(this.mHideFlyout);
    }

    /* access modifiers changed from: package-private */
    public void onFlyoutDragged(float f) {
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

    /* access modifiers changed from: package-private */
    public void onFlyoutDragFinished(float f, float f2) {
        boolean isStackOnLeftSide = this.mStackAnimationController.isStackOnLeftSide();
        boolean z = true;
        boolean z2 = !isStackOnLeftSide ? f2 > 2000.0f : f2 < -2000.0f;
        boolean z3 = !isStackOnLeftSide ? f > ((float) this.mFlyout.getWidth()) * 0.25f : f < ((float) (-this.mFlyout.getWidth())) * 0.25f;
        boolean z4 = !isStackOnLeftSide ? f2 < 0.0f : f2 > 0.0f;
        if (!z2 && (!z3 || z4)) {
            z = false;
        }
        this.mFlyout.removeCallbacks(this.mHideFlyout);
        animateFlyoutCollapsed(z, f2);
    }

    /* access modifiers changed from: package-private */
    public void onGestureStart() {
        this.mIsGestureInProgress = true;
    }

    /* access modifiers changed from: package-private */
    public void onGestureFinished() {
        this.mIsGestureInProgress = false;
        if (this.mIsExpanded) {
            this.mExpandedAnimationController.onGestureFinished();
        }
    }

    private void animateDesaturateAndDarken(View view, boolean z) {
        this.mDesaturateAndDarkenTargetView = view;
        if (z) {
            this.mDesaturateAndDarkenTargetView.setLayerType(2, this.mDesaturateAndDarkenPaint);
            this.mDesaturateAndDarkenAnimator.removeAllListeners();
            this.mDesaturateAndDarkenAnimator.start();
            return;
        }
        this.mDesaturateAndDarkenAnimator.removeAllListeners();
        this.mDesaturateAndDarkenAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                BubbleStackView.this.resetDesaturationAndDarken();
            }
        });
        this.mDesaturateAndDarkenAnimator.reverse();
    }

    /* access modifiers changed from: private */
    public void resetDesaturationAndDarken() {
        this.mDesaturateAndDarkenAnimator.removeAllListeners();
        this.mDesaturateAndDarkenAnimator.cancel();
        this.mDesaturateAndDarkenTargetView.setLayerType(0, (Paint) null);
    }

    /* access modifiers changed from: package-private */
    public void animateMagnetToDismissTarget(View view, boolean z, float f, float f2, float f3, float f4) {
        this.mDraggingInDismissTarget = z;
        int i = 0;
        if (z) {
            float dismissTargetCenterY = this.mDismissContainer.getDismissTargetCenterY() - (((float) this.mBubbleSize) / 2.0f);
            this.mAnimatingMagnet = true;
            $$Lambda$BubbleStackView$oLhNqxGbPa3FqJeraIwHlBcS7tk r6 = new Runnable() {
                public final void run() {
                    BubbleStackView.this.lambda$animateMagnetToDismissTarget$11$BubbleStackView();
                }
            };
            if (view == this) {
                this.mStackAnimationController.magnetToDismiss(f3, f4, dismissTargetCenterY, r6);
                animateDesaturateAndDarken(this.mBubbleContainer, true);
            } else {
                this.mExpandedAnimationController.magnetBubbleToDismiss(view, f3, f4, dismissTargetCenterY, r6);
                animateDesaturateAndDarken(view, true);
            }
            this.mDismissContainer.animateEncircleCenterWithX(true);
        } else {
            this.mAnimatingMagnet = false;
            if (view == this) {
                this.mStackAnimationController.demagnetizeFromDismissToPoint(f, f2, f3, f4);
                animateDesaturateAndDarken(this.mBubbleContainer, false);
            } else {
                this.mExpandedAnimationController.demagnetizeBubbleTo(f, f2, f3, f4);
                animateDesaturateAndDarken(view, false);
            }
            this.mDismissContainer.animateEncircleCenterWithX(false);
        }
        Vibrator vibrator = this.mVibrator;
        if (!z) {
            i = 2;
        }
        vibrator.vibrate(VibrationEffect.get(i));
    }

    public /* synthetic */ void lambda$animateMagnetToDismissTarget$11$BubbleStackView() {
        this.mAnimatingMagnet = false;
        Runnable runnable = this.mAfterMagnet;
        if (runnable != null) {
            runnable.run();
        }
    }

    /* access modifiers changed from: package-private */
    public void magnetToStackIfNeededThenAnimateDismissal(View view, float f, float f2, Runnable runnable) {
        $$Lambda$BubbleStackView$wNBb9TcVorXyGaagZMMDs0nXEJw r1 = new Runnable(view, runnable, this.mExpandedAnimationController.getDraggedOutBubble()) {
            private final /* synthetic */ View f$1;
            private final /* synthetic */ Runnable f$2;
            private final /* synthetic */ View f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                BubbleStackView.this.lambda$magnetToStackIfNeededThenAnimateDismissal$14$BubbleStackView(this.f$1, this.f$2, this.f$3);
            }
        };
        if (this.mAnimatingMagnet) {
            this.mAfterMagnet = r1;
        } else if (this.mDraggingInDismissTarget) {
            r1.run();
        } else {
            animateMagnetToDismissTarget(view, true, -1.0f, -1.0f, f, f2);
            this.mAfterMagnet = r1;
        }
    }

    public /* synthetic */ void lambda$magnetToStackIfNeededThenAnimateDismissal$14$BubbleStackView(View view, Runnable runnable, View view2) {
        this.mAfterMagnet = null;
        this.mVibrator.vibrate(VibrationEffect.get(0));
        this.mDismissContainer.animateEncirclingCircleDisappearance();
        if (view == this) {
            this.mStackAnimationController.implodeStack(new Runnable(runnable) {
                private final /* synthetic */ Runnable f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    BubbleStackView.this.lambda$magnetToStackIfNeededThenAnimateDismissal$12$BubbleStackView(this.f$1);
                }
            });
        } else {
            this.mExpandedAnimationController.dismissDraggedOutBubble(view2, new Runnable(runnable) {
                private final /* synthetic */ Runnable f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    BubbleStackView.this.lambda$magnetToStackIfNeededThenAnimateDismissal$13$BubbleStackView(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$magnetToStackIfNeededThenAnimateDismissal$12$BubbleStackView(Runnable runnable) {
        this.mAnimatingMagnet = false;
        this.mShowingDismiss = false;
        this.mDraggingInDismissTarget = false;
        runnable.run();
        resetDesaturationAndDarken();
    }

    public /* synthetic */ void lambda$magnetToStackIfNeededThenAnimateDismissal$13$BubbleStackView(Runnable runnable) {
        this.mAnimatingMagnet = false;
        this.mShowingDismiss = false;
        this.mDraggingInDismissTarget = false;
        resetDesaturationAndDarken();
        runnable.run();
    }

    private void springInDismissTarget() {
        if (!this.mShowingDismiss) {
            this.mShowingDismiss = true;
            this.mDismissContainer.springIn();
            this.mDismissContainer.bringToFront();
            this.mDismissContainer.setZ(32766.0f);
        }
    }

    private void springOutDismissTargetAndHideCircle() {
        if (this.mShowingDismiss) {
            this.mDismissContainer.springOut();
            this.mShowingDismiss = false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInDismissTarget(MotionEvent motionEvent) {
        return isIntersecting(this.mDismissContainer.getDismissTarget(), motionEvent.getRawX(), motionEvent.getRawY());
    }

    private void animateFlyoutCollapsed(boolean z, float f) {
        float f2;
        boolean isStackOnLeftSide = this.mStackAnimationController.isStackOnLeftSide();
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
    public int getMaxExpandedHeight() {
        return ((this.mDisplaySize.y - ((int) this.mExpandedAnimationController.getExpandedY())) - this.mBubbleSize) - (this.mPipDismissHeight - getBottomInset());
    }

    /* access modifiers changed from: package-private */
    public float getYPositionForExpandedView() {
        return (float) (getStatusBarHeight() + this.mBubbleSize + this.mBubblePadding + this.mPointerHeight);
    }

    /* access modifiers changed from: package-private */
    public void setSuppressNewDot(boolean z) {
        this.mSuppressNewDot = z;
        for (int i = 0; i < this.mBubbleContainer.getChildCount(); i++) {
            ((BubbleView) this.mBubbleContainer.getChildAt(i)).setSuppressDot(z, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSuppressFlyout(boolean z) {
        this.mSuppressFlyout = z;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void animateInFlyoutForBubble(Bubble bubble) {
        CharSequence updateMessage = Utils.getUpdateMessage(getContext(), bubble.entry.notification);
        if (updateMessage != null && !isExpanded() && !this.mIsExpansionAnimating && !this.mIsGestureInProgress && !this.mSuppressFlyout) {
            BubbleView bubbleView = bubble.iconView;
            if (bubbleView != null) {
                bubbleView.setSuppressDot(true, false);
                this.mFlyoutDragDeltaX = 0.0f;
                this.mFlyout.setAlpha(0.0f);
                Runnable runnable = this.mAfterFlyoutHides;
                if (runnable != null) {
                    runnable.run();
                }
                this.mAfterFlyoutHides = new Runnable(bubble) {
                    private final /* synthetic */ Bubble f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        BubbleStackView.this.lambda$animateInFlyoutForBubble$15$BubbleStackView(this.f$1);
                    }
                };
                post(new Runnable(updateMessage, bubble) {
                    private final /* synthetic */ CharSequence f$1;
                    private final /* synthetic */ Bubble f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        BubbleStackView.this.lambda$animateInFlyoutForBubble$16$BubbleStackView(this.f$1, this.f$2);
                    }
                });
            }
            this.mFlyout.removeCallbacks(this.mHideFlyout);
            this.mFlyout.postDelayed(this.mHideFlyout, 5000);
            logBubbleEvent(bubble, 16);
        }
    }

    public /* synthetic */ void lambda$animateInFlyoutForBubble$15$BubbleStackView(Bubble bubble) {
        BubbleView bubbleView = bubble.iconView;
        if (bubbleView != null) {
            if (this.mSuppressNewDot) {
                bubbleView.setSuppressDot(false, false);
            }
            BubbleView bubbleView2 = bubble.iconView;
            boolean z = this.mSuppressNewDot;
            bubbleView2.setSuppressDot(z, z);
        }
    }

    public /* synthetic */ void lambda$animateInFlyoutForBubble$16$BubbleStackView(CharSequence charSequence, Bubble bubble) {
        if (!isExpanded()) {
            this.mFlyout.showFlyout(charSequence, this.mStackAnimationController.getStackPosition(), (float) getWidth(), this.mStackAnimationController.isStackOnLeftSide(), bubble.iconView.getBadgeColor(), this.mAfterFlyoutHides);
        }
    }

    private void hideFlyoutImmediate() {
        Runnable runnable = this.mAfterFlyoutHides;
        if (runnable != null) {
            runnable.run();
        }
        this.mFlyout.removeCallbacks(this.mHideFlyout);
        this.mFlyout.hideFlyout();
    }

    public void getBoundsOnScreen(Rect rect) {
        if (this.mIsExpanded) {
            this.mBubbleContainer.getBoundsOnScreen(rect);
        } else if (this.mBubbleContainer.getChildCount() > 0) {
            this.mBubbleContainer.getChildAt(0).getBoundsOnScreen(rect);
        }
        if (this.mFlyout.getVisibility() == 0) {
            Rect rect2 = new Rect();
            this.mFlyout.getBoundsOnScreen(rect2);
            rect.union(rect2);
        }
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

    private int getBottomInset() {
        if (getRootWindowInsets() != null) {
            return getRootWindowInsets().getSystemWindowInsetBottom();
        }
        return 0;
    }

    private boolean isIntersecting(View view, float f, float f2) {
        this.mTempLoc = view.getLocationOnScreen();
        RectF rectF = this.mTempRect;
        int[] iArr = this.mTempLoc;
        rectF.set((float) iArr[0], (float) iArr[1], (float) (iArr[0] + view.getWidth()), (float) (this.mTempLoc[1] + view.getHeight()));
        return this.mTempRect.contains(f, f2);
    }

    private void requestUpdate() {
        if (!this.mViewUpdatedRequested && !this.mIsExpansionAnimating) {
            this.mViewUpdatedRequested = true;
            getViewTreeObserver().addOnPreDrawListener(this.mViewUpdater);
            invalidate();
        }
    }

    private void updateExpandedBubble() {
        this.mExpandedViewContainer.removeAllViews();
        Bubble bubble = this.mExpandedBubble;
        if (bubble != null && this.mIsExpanded) {
            this.mExpandedViewContainer.addView(bubble.expandedView);
            this.mExpandedBubble.expandedView.populateExpandedView();
            this.mExpandedViewContainer.setVisibility(this.mIsExpanded ? 0 : 8);
            this.mExpandedViewContainer.setAlpha(1.0f);
        }
    }

    /* access modifiers changed from: private */
    public void applyCurrentState() {
        this.mExpandedViewContainer.setVisibility(this.mIsExpanded ? 0 : 8);
        if (this.mIsExpanded) {
            this.mExpandedBubble.expandedView.updateView();
            float yPositionForExpandedView = getYPositionForExpandedView();
            if (!this.mExpandedViewYAnim.isRunning()) {
                this.mExpandedViewContainer.setTranslationY(yPositionForExpandedView);
                this.mExpandedBubble.expandedView.updateView();
            } else {
                this.mExpandedViewYAnim.animateToFinalPosition(yPositionForExpandedView);
            }
        }
        this.mStackOnLeftOrWillBe = this.mStackAnimationController.isStackOnLeftSide();
        updateBubbleShadowsAndDotPosition(false);
    }

    private void updateBubbleShadowsAndDotPosition(boolean z) {
        int childCount = this.mBubbleContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            BubbleView bubbleView = (BubbleView) this.mBubbleContainer.getChildAt(i);
            bubbleView.updateDotVisibility(true);
            bubbleView.setZ((float) ((getResources().getDimensionPixelSize(R.dimen.bubble_elevation) * 5) - i));
            bubbleView.setOutlineProvider(new ViewOutlineProvider() {
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, BubbleStackView.this.mBubbleSize, BubbleStackView.this.mBubbleSize);
                }
            });
            bubbleView.setClipToOutline(false);
            boolean dotPositionOnLeft = bubbleView.getDotPositionOnLeft();
            boolean z2 = this.mStackOnLeftOrWillBe;
            if (dotPositionOnLeft == z2) {
                bubbleView.setDotPosition(!z2, z);
            }
        }
    }

    private void updatePointerPosition() {
        Bubble expandedBubble = getExpandedBubble();
        if (expandedBubble != null) {
            expandedBubble.expandedView.setPointerPosition((this.mExpandedAnimationController.getBubbleLeft(getBubbleIndex(expandedBubble)) + (((float) this.mBubbleSize) / 2.0f)) - ((float) this.mExpandedViewPadding));
        }
    }

    public int getBubbleCount() {
        return this.mBubbleContainer.getChildCount();
    }

    /* access modifiers changed from: package-private */
    public int getBubbleIndex(Bubble bubble) {
        if (bubble == null) {
            return 0;
        }
        return this.mBubbleContainer.indexOfChild(bubble.iconView);
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

    private void logBubbleEvent(Bubble bubble, int i) {
        NotificationData.Entry entry;
        ExpandedNotification expandedNotification;
        Bubble bubble2 = bubble;
        if (bubble2 == null || (entry = bubble2.entry) == null || (expandedNotification = entry.notification) == null) {
            StatsLog.write(149, (String) null, (String) null, 0, 0, getBubbleCount(), i, getNormalizedXPosition(), getNormalizedYPosition(), false, false, false);
            return;
        }
        StatsLog.write(149, expandedNotification.getPackageName(), expandedNotification.getNotification().getChannelId(), expandedNotification.getId(), getBubbleIndex(bubble), getBubbleCount(), i, getNormalizedXPosition(), getNormalizedYPosition(), bubble2.entry.showInShadeWhenBubble(), bubble2.entry.isForegroundService(), BubbleController.isForegroundApp(this.mContext, expandedNotification.getPackageName()));
    }

    /* access modifiers changed from: package-private */
    public boolean performBackPressIfNeeded() {
        if (!isExpanded()) {
            return false;
        }
        return this.mExpandedBubble.expandedView.performBackPressIfNeeded();
    }
}
