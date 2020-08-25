package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.android.keyguard.LatencyTracker;
import com.android.systemui.Constants;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.Util;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.miui.statusbar.analytics.NotificationStat;
import com.android.systemui.miui.statusbar.policy.AppMiniWindowManager;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.util.QcomBoostFramework;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public abstract class PanelView extends FrameLayout {
    public static final boolean DEBUG = Constants.DEBUG;
    public static final String TAG = PanelView.class.getSimpleName();
    /* access modifiers changed from: private */
    public boolean mAnimateAfterExpanding;
    private boolean mAnimatingOnDown;
    protected AppMiniWindowManager mAppMiniWindowManager;
    PanelBar mBar;
    private Interpolator mBounceInterpolator;
    protected boolean mClosing;
    private boolean mCollapsedAndHeadsUpOnDown;
    private boolean mExpandLatencyTracking;
    private float mExpandedFraction = 0.0f;
    protected float mExpandedHeight = 0.0f;
    protected boolean mExpanding;
    private FalsingManager mFalsingManager;
    private FlingAnimationUtils mFlingAnimationUtils;
    private FlingAnimationUtils mFlingAnimationUtilsClosing;
    private FlingAnimationUtils mFlingAnimationUtilsDismissing;
    private final Runnable mFlingCollapseRunnable = new Runnable() {
        public void run() {
            PanelView panelView = PanelView.this;
            panelView.fling(0.0f, false, panelView.mNextCollapseSpeedUpFactor, false);
        }
    };
    private boolean mGestureWaitForTouchSlop;
    private boolean mHasLayoutedSinceDown;
    protected HeadsUpManager mHeadsUpManager;
    private ValueAnimator mHeightAnimator;
    protected boolean mHintAnimationRunning;
    private float mHintDistance;
    private boolean mIgnoreXTouchSlop;
    private float mInitialOffsetOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    /* access modifiers changed from: private */
    public boolean mInstantExpanding;
    protected boolean mIsDefaultTheme = true;
    private boolean mIsKeyguardShowingOnDown;
    private boolean mJustPeeked;
    protected KeyguardBottomAreaView mKeyguardBottomArea;
    protected KeyguardVerticalMoveHelper mKeyguardVerticalMoveHelper;
    protected boolean mLaunchingNotification;
    private LockscreenGestureLogger mLockscreenGestureLogger = new LockscreenGestureLogger();
    private float mMinExpandHeight;
    private boolean mMotionAborted;
    /* access modifiers changed from: private */
    public float mNextCollapseSpeedUpFactor = 1.0f;
    private boolean mNotificationsDragEnabled;
    protected boolean mOpening;
    private boolean mOverExpandedBeforeFling;
    protected boolean mPanelAppeared;
    private boolean mPanelClosedOnDown;
    private boolean mPanelUpdateWhenAnimatorEnds;
    private ObjectAnimator mPeekAnimator;
    private float mPeekHeight;
    private boolean mPeekTouching;
    /* access modifiers changed from: private */
    public QcomBoostFramework mPerf = null;
    protected final Runnable mPostCollapseRunnable = new Runnable() {
        public void run() {
            PanelView.this.collapse(false, 1.0f);
        }
    };
    protected float mSpringLength = 0.0f;
    protected StatusBar mStatusBar;
    private boolean mStopTrackingAndCollapsed;
    protected float mStretchLength = 0.0f;
    protected boolean mStretching;
    private boolean mTouchAboveFalsingThreshold;
    private boolean mTouchDisabled;
    protected int mTouchSlop;
    private boolean mTouchSlopExceeded;
    private boolean mTouchStartedInBottomButton;
    private boolean mTouchStartedInEmptyArea;
    protected boolean mTracking;
    private int mTrackingPointer;
    private int mUnlockFalsingThreshold;
    private boolean mUpdateFlingOnLayout;
    private float mUpdateFlingVelocity;
    private boolean mUpwardsWhenTresholdReached;
    private VelocityTrackerInterface mVelocityTracker;
    private String mViewName;

    /* access modifiers changed from: protected */
    /* renamed from: flingSpring */
    public abstract void lambda$endMotionEvent$0$PanelView(float f, boolean z);

    /* access modifiers changed from: protected */
    public abstract int getMaxPanelHeight();

    /* access modifiers changed from: protected */
    public abstract float getOverExpansionAmount();

    /* access modifiers changed from: protected */
    public abstract float getOverExpansionPixels();

    /* access modifiers changed from: protected */
    public abstract float getQsExpansionFraction();

    /* access modifiers changed from: protected */
    public abstract boolean hasConflictingGestures();

    /* access modifiers changed from: protected */
    public boolean isExpandForbiddenInKeyguard() {
        return false;
    }

    /* access modifiers changed from: protected */
    public abstract boolean isInCenterScreen();

    /* access modifiers changed from: protected */
    public abstract boolean isInContentBounds(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract boolean isInUnderlapBounds(float f, float f2);

    public boolean isKeyguardShowing() {
        return false;
    }

    /* access modifiers changed from: protected */
    public abstract boolean isOnBottomIcon(float f, float f2);

    public boolean isOnKeyguard() {
        return false;
    }

    /* access modifiers changed from: protected */
    public abstract boolean isPanelVisibleBecauseOfHeadsUp();

    /* access modifiers changed from: protected */
    public boolean isScrolledToBottom() {
        return true;
    }

    /* access modifiers changed from: protected */
    public abstract boolean isTrackingBlocked();

    /* access modifiers changed from: protected */
    public void onExpandingStarted() {
    }

    /* access modifiers changed from: protected */
    public abstract void onHeightUpdated(float f);

    /* access modifiers changed from: protected */
    public abstract boolean onMiddleClicked();

    /* access modifiers changed from: protected */
    public abstract void onPanelDisplayChanged(boolean z, boolean z2);

    /* access modifiers changed from: protected */
    public abstract void onSpringLengthUpdated(float f);

    public abstract void resetViews();

    /* access modifiers changed from: protected */
    public abstract void setOverExpansion(float f, boolean z);

    /* access modifiers changed from: protected */
    public abstract boolean shouldGestureIgnoreXTouchSlop(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract boolean shouldUseDismissingAnimation();

    /* access modifiers changed from: protected */
    public final void logf(String str, Object... objArr) {
        Log.v(TAG, String.format(str, objArr));
    }

    /* access modifiers changed from: protected */
    public void onExpandingFinished() {
        this.mBar.onExpandingFinished();
    }

    /* access modifiers changed from: private */
    public void notifyExpandingStarted() {
        if (!this.mExpanding) {
            this.mExpanding = true;
            onExpandingStarted();
        }
    }

    /* access modifiers changed from: protected */
    public final void notifyExpandingFinished() {
        endClosing();
        if (this.mExpanding) {
            this.mExpanding = false;
            onExpandingFinished();
        }
    }

    public PanelView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 0.6f, 0.6f);
        this.mFlingAnimationUtilsClosing = new FlingAnimationUtils(context, 0.39f, 0.6f);
        this.mFlingAnimationUtilsDismissing = new FlingAnimationUtils(context, 0.5f, 0.2f, 0.6f, 0.84f);
        this.mBounceInterpolator = new BounceInterpolator();
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mNotificationsDragEnabled = getResources().getBoolean(R.bool.config_enableNotificationShadeDrag);
        this.mPerf = new QcomBoostFramework();
    }

    /* access modifiers changed from: protected */
    public void loadDimens(Resources resources) {
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mHintDistance = resources.getDimension(R.dimen.hint_move_distance);
        this.mUnlockFalsingThreshold = resources.getDimensionPixelSize(R.dimen.unlock_falsing_threshold);
    }

    private void trackMovement(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        VelocityTrackerInterface velocityTrackerInterface = this.mVelocityTracker;
        if (velocityTrackerInterface != null) {
            velocityTrackerInterface.addMovement(motionEvent);
        }
        motionEvent.offsetLocation(-rawX, -rawY);
    }

    public void setTouchDisabled(boolean z) {
        this.mTouchDisabled = z;
        if (this.mTouchDisabled) {
            cancelHeightAnimator();
            if (this.mTracking) {
                onTrackingStopped(true);
            }
            notifyExpandingFinished();
        }
    }

    public void startExpandLatencyTracking() {
        if (LatencyTracker.isEnabled(this.mContext)) {
            LatencyTracker.getInstance(this.mContext).onActionStart(0);
            this.mExpandLatencyTracking = true;
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int pointerId;
        if (this.mStatusBar.getBarState() == 0 && motionEvent.getActionMasked() != 2) {
            Log.d(TAG, String.format("onTouchEvent action=%d x=%.1f y=%.1f", new Object[]{Integer.valueOf(motionEvent.getActionMasked()), Float.valueOf(motionEvent.getX()), Float.valueOf(motionEvent.getY())}));
        }
        if (this.mInstantExpanding || this.mTouchDisabled || (this.mMotionAborted && motionEvent.getActionMasked() != 0)) {
            return false;
        }
        if (!this.mNotificationsDragEnabled) {
            if (this.mTracking) {
                onTrackingStopped(true);
            }
            return false;
        } else if (!isFullyCollapsed() || !motionEvent.isFromSource(8194)) {
            int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
            if (findPointerIndex < 0) {
                this.mTrackingPointer = motionEvent.getPointerId(0);
                findPointerIndex = 0;
            }
            float x = motionEvent.getX(findPointerIndex);
            float y = motionEvent.getY(findPointerIndex);
            if (motionEvent.getActionMasked() == 0) {
                this.mGestureWaitForTouchSlop = isFullyCollapsed() || hasConflictingGestures();
                this.mIgnoreXTouchSlop = isFullyCollapsed() || shouldGestureIgnoreXTouchSlop(x, y);
            }
            if (isKeyguardShowing() && ((motionEvent.getActionMasked() == 0 && isFullyExpanded() && !this.mTracking && !this.mClosing) || (motionEvent.getActionMasked() != 0 && !isFullyCollapsed()))) {
                this.mKeyguardVerticalMoveHelper.onTouchEvent(motionEvent);
            }
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        trackMovement(motionEvent);
                        float f = y - this.mInitialTouchY;
                        if (Math.abs(f) > ((float) this.mTouchSlop) && (Math.abs(f) > Math.abs(x - this.mInitialTouchX) || this.mIgnoreXTouchSlop)) {
                            this.mTouchSlopExceeded = true;
                            if (this.mGestureWaitForTouchSlop && !this.mTracking && !this.mCollapsedAndHeadsUpOnDown && !this.mTouchStartedInBottomButton) {
                                if (!this.mJustPeeked && this.mInitialOffsetOnTouch != 0.0f) {
                                    startExpandMotion(x, y, false, this.mExpandedHeight);
                                    f = 0.0f;
                                }
                                cancelHeightAnimator();
                                onTrackingStarted();
                            }
                        }
                        float max = Math.max(0.0f, this.mInitialOffsetOnTouch + f);
                        if (max > this.mPeekHeight) {
                            ObjectAnimator objectAnimator = this.mPeekAnimator;
                            if (objectAnimator != null) {
                                objectAnimator.cancel();
                            }
                            this.mJustPeeked = false;
                        } else if (this.mPeekAnimator == null && this.mJustPeeked) {
                            float f2 = this.mExpandedHeight;
                            this.mInitialOffsetOnTouch = f2;
                            this.mInitialTouchY = y;
                            this.mMinExpandHeight = f2;
                            this.mJustPeeked = false;
                        }
                        Math.max(max, this.mMinExpandHeight);
                        if ((-f) >= ((float) getFalsingThreshold())) {
                            this.mTouchAboveFalsingThreshold = true;
                            this.mUpwardsWhenTresholdReached = isDirectionUpwards(x, y);
                        }
                        if (!this.mJustPeeked && ((!this.mGestureWaitForTouchSlop || this.mTracking) && !isTrackingBlocked() && ((!this.mIsKeyguardShowingOnDown || isOnShadeLocked()) && !isExpandForbiddenInKeyguard()))) {
                            setStretchLength(f - ((float) this.mTouchSlop));
                        }
                    } else if (actionMasked != 3) {
                        if (actionMasked != 5) {
                            if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                                int i = motionEvent.getPointerId(0) != pointerId ? 0 : 1;
                                float y2 = motionEvent.getY(i);
                                float x2 = motionEvent.getX(i);
                                this.mTrackingPointer = motionEvent.getPointerId(i);
                                startExpandMotion(x2, y2, true, this.mExpandedHeight);
                            }
                        } else if (this.mStatusBar.getBarState() == 1) {
                            this.mMotionAborted = true;
                            endMotionEvent(motionEvent, x, y, true);
                            return false;
                        }
                    }
                }
                trackMovement(motionEvent);
                endMotionEvent(motionEvent, x, y, false);
            } else {
                startExpandMotion(x, y, false, this.mExpandedHeight);
                this.mJustPeeked = false;
                this.mMinExpandHeight = 0.0f;
                this.mPanelClosedOnDown = isFullyCollapsed();
                this.mHasLayoutedSinceDown = false;
                this.mUpdateFlingOnLayout = false;
                this.mMotionAborted = false;
                this.mPeekTouching = this.mPanelClosedOnDown;
                this.mTouchAboveFalsingThreshold = false;
                this.mIsKeyguardShowingOnDown = isKeyguardShowing();
                this.mStopTrackingAndCollapsed = false;
                this.mCollapsedAndHeadsUpOnDown = isFullyCollapsed() && this.mHeadsUpManager.hasPinnedHeadsUp();
                this.mTouchStartedInBottomButton = isOnBottomIcon(x, y);
                if (this.mVelocityTracker == null) {
                    initVelocityTracker();
                }
                trackMovement(motionEvent);
                if (!this.mGestureWaitForTouchSlop || ((this.mHeightAnimator != null && !this.mHintAnimationRunning) || this.mPeekAnimator != null)) {
                    this.mTouchSlopExceeded = (this.mHeightAnimator != null && !this.mHintAnimationRunning) || this.mPeekAnimator != null;
                    cancelHeightAnimator();
                    cancelPeek();
                    onTrackingStarted();
                }
                if (isFullyCollapsed() && !this.mHeadsUpManager.hasPinnedHeadsUp() && !isExpandForbiddenInKeyguard()) {
                    startOpening();
                }
            }
            if (!this.mGestureWaitForTouchSlop || this.mTracking) {
                return true;
            }
            return false;
        } else {
            if (motionEvent.getAction() == 1) {
                expand(true);
            }
            return true;
        }
    }

    private void startOpening() {
        Log.d(TAG, "pv startOpening");
        this.mOpening = true;
        onPanelDisplayChanged(false, true);
        notifyBarPanelExpansionChanged();
    }

    private boolean isDirectionUpwards(float f, float f2) {
        float f3 = f - this.mInitialTouchX;
        float f4 = f2 - this.mInitialTouchY;
        if (f4 < 0.0f && Math.abs(f4) >= Math.abs(f3)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void startExpandMotion(float f, float f2, boolean z, float f3) {
        this.mInitialOffsetOnTouch = f3;
        this.mInitialTouchY = f2;
        this.mInitialTouchX = f;
        if (z) {
            this.mTouchSlopExceeded = true;
            setExpandedHeight(this.mInitialOffsetOnTouch);
            onTrackingStarted();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x0120  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void endMotionEvent(android.view.MotionEvent r9, float r10, float r11, boolean r12) {
        /*
            r8 = this;
            r0 = -1
            r8.mTrackingPointer = r0
            boolean r0 = r8.mTracking
            r1 = 0
            if (r0 == 0) goto L_0x000c
            boolean r0 = r8.mTouchSlopExceeded
            if (r0 != 0) goto L_0x0059
        L_0x000c:
            float r0 = r8.mInitialTouchX
            float r0 = r10 - r0
            float r0 = java.lang.Math.abs(r0)
            int r2 = r8.mTouchSlop
            float r2 = (float) r2
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 > 0) goto L_0x0059
            float r0 = r8.mInitialTouchY
            float r0 = r11 - r0
            float r0 = java.lang.Math.abs(r0)
            int r2 = r8.mTouchSlop
            float r2 = (float) r2
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 > 0) goto L_0x0059
            int r9 = r9.getActionMasked()
            r0 = 3
            if (r9 == r0) goto L_0x0059
            if (r12 == 0) goto L_0x0034
            goto L_0x0059
        L_0x0034:
            boolean r9 = r8.mPanelClosedOnDown
            if (r9 == 0) goto L_0x004e
            com.android.systemui.statusbar.policy.HeadsUpManager r9 = r8.mHeadsUpManager
            boolean r9 = r9.hasPinnedHeadsUp()
            if (r9 != 0) goto L_0x004e
            boolean r9 = r8.mTracking
            if (r9 != 0) goto L_0x004e
            com.android.systemui.statusbar.phone.PanelView$2 r9 = new com.android.systemui.statusbar.phone.PanelView$2
            r9.<init>()
            r8.post(r9)
            goto L_0x0122
        L_0x004e:
            float r9 = r8.mInitialTouchX
            boolean r9 = r8.onEmptySpaceClick(r9)
            r8.onTrackingStopped(r9)
            goto L_0x0122
        L_0x0059:
            com.android.systemui.statusbar.phone.VelocityTrackerInterface r9 = r8.mVelocityTracker
            r0 = 0
            if (r9 == 0) goto L_0x007d
            r0 = 1000(0x3e8, float:1.401E-42)
            r9.computeCurrentVelocity(r0)
            com.android.systemui.statusbar.phone.VelocityTrackerInterface r9 = r8.mVelocityTracker
            float r0 = r9.getYVelocity()
            com.android.systemui.statusbar.phone.VelocityTrackerInterface r9 = r8.mVelocityTracker
            float r9 = r9.getXVelocity()
            double r2 = (double) r9
            com.android.systemui.statusbar.phone.VelocityTrackerInterface r9 = r8.mVelocityTracker
            float r9 = r9.getYVelocity()
            double r4 = (double) r9
            double r2 = java.lang.Math.hypot(r2, r4)
            float r9 = (float) r2
            goto L_0x007e
        L_0x007d:
            r9 = r0
        L_0x007e:
            boolean r2 = r8.mStopTrackingAndCollapsed
            r3 = 1
            if (r2 != 0) goto L_0x008d
            boolean r9 = r8.flingExpands(r0, r9, r10, r11)
            if (r9 != 0) goto L_0x008b
            if (r12 == 0) goto L_0x008d
        L_0x008b:
            r9 = r3
            goto L_0x008e
        L_0x008d:
            r9 = r1
        L_0x008e:
            com.android.systemui.classifier.FalsingManager r12 = r8.mFalsingManager
            r12.onPanelEvent(r9)
            if (r9 != 0) goto L_0x009c
            boolean r12 = r8.mStopTrackingAndCollapsed
            if (r12 == 0) goto L_0x009a
            goto L_0x009c
        L_0x009a:
            r12 = r1
            goto L_0x009d
        L_0x009c:
            r12 = r3
        L_0x009d:
            boolean r2 = r8.mTouchAboveFalsingThreshold
            com.android.systemui.statusbar.phone.StatusBar r4 = r8.mStatusBar
            boolean r4 = r4.isFalsingThresholdNeeded()
            com.android.systemui.statusbar.phone.StatusBar r5 = r8.mStatusBar
            boolean r5 = r5.isWakeUpComingFromTouch()
            com.android.systemui.doze.DozeLog.traceFling(r9, r2, r4, r5)
            if (r12 != 0) goto L_0x00da
            com.android.systemui.statusbar.phone.StatusBar r2 = r8.mStatusBar
            int r2 = r2.getBarState()
            if (r2 != r3) goto L_0x00da
            com.android.systemui.statusbar.phone.StatusBar r2 = r8.mStatusBar
            float r2 = r2.getDisplayDensity()
            float r4 = r8.mInitialTouchY
            float r4 = r11 - r4
            float r4 = r4 / r2
            float r4 = java.lang.Math.abs(r4)
            int r4 = (int) r4
            float r2 = r0 / r2
            float r2 = java.lang.Math.abs(r2)
            int r2 = (int) r2
            com.android.systemui.statusbar.phone.LockscreenGestureLogger r5 = r8.mLockscreenGestureLogger
            android.content.Context r6 = r8.getContext()
            r7 = 186(0xba, float:2.6E-43)
            r5.write(r6, r7, r4, r2)
        L_0x00da:
            if (r9 != 0) goto L_0x00f1
            boolean r2 = r8.isOnKeyguard()
            if (r2 == 0) goto L_0x00f1
            boolean r10 = r8.mExpanding
            if (r10 == 0) goto L_0x00e9
            r8.notifyExpandingFinished()
        L_0x00e9:
            if (r12 != 0) goto L_0x0108
            com.android.systemui.statusbar.phone.StatusBar r10 = r8.mStatusBar
            r10.showBouncer()
            goto L_0x0108
        L_0x00f1:
            boolean r12 = r8.isOnKeyguard()
            if (r12 != 0) goto L_0x0101
            com.android.systemui.statusbar.phone.-$$Lambda$PanelView$J2YZxITIq5LzGfRLI28gYyozSww r10 = new com.android.systemui.statusbar.phone.-$$Lambda$PanelView$J2YZxITIq5LzGfRLI28gYyozSww
            r10.<init>(r0, r9)
            r8.post(r10)
            r10 = r3
            goto L_0x0109
        L_0x0101:
            boolean r10 = r8.isFalseTouch(r10, r11)
            r8.fling(r0, r9, r10)
        L_0x0108:
            r10 = r1
        L_0x0109:
            r8.onTrackingStopped(r9)
            if (r10 != 0) goto L_0x0119
            if (r9 == 0) goto L_0x0119
            boolean r9 = r8.mPanelClosedOnDown
            if (r9 == 0) goto L_0x0119
            boolean r9 = r8.mHasLayoutedSinceDown
            if (r9 != 0) goto L_0x0119
            goto L_0x011a
        L_0x0119:
            r3 = r1
        L_0x011a:
            r8.mUpdateFlingOnLayout = r3
            boolean r9 = r8.mUpdateFlingOnLayout
            if (r9 == 0) goto L_0x0122
            r8.mUpdateFlingVelocity = r0
        L_0x0122:
            com.android.systemui.statusbar.phone.VelocityTrackerInterface r9 = r8.mVelocityTracker
            if (r9 == 0) goto L_0x012c
            r9.recycle()
            r9 = 0
            r8.mVelocityTracker = r9
        L_0x012c:
            r8.mPeekTouching = r1
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.PanelView.endMotionEvent(android.view.MotionEvent, float, float, boolean):void");
    }

    /* access modifiers changed from: protected */
    public float getCurrentExpandVelocity() {
        VelocityTrackerInterface velocityTrackerInterface = this.mVelocityTracker;
        if (velocityTrackerInterface == null) {
            return 0.0f;
        }
        velocityTrackerInterface.computeCurrentVelocity(1000);
        return this.mVelocityTracker.getYVelocity();
    }

    private int getFalsingThreshold() {
        return (int) (((float) this.mUnlockFalsingThreshold) * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    /* access modifiers changed from: protected */
    public void onTrackingStopped(boolean z) {
        this.mOpening = false;
        this.mTracking = false;
        this.mBar.onTrackingStopped(z);
        notifyBarPanelExpansionChanged();
    }

    /* access modifiers changed from: protected */
    public void onTrackingStarted() {
        endClosing();
        this.mTracking = true;
        this.mBar.onTrackingStarted();
        notifyExpandingStarted();
        notifyBarPanelExpansionChanged();
    }

    public void stopTrackingAndCollapsed() {
        this.mStopTrackingAndCollapsed = true;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int pointerId;
        if (this.mInstantExpanding || !this.mNotificationsDragEnabled || this.mTouchDisabled || !isInCenterScreen() || ((!this.mIsDefaultTheme && isKeyguardShowing() && getQsExpansionFraction() == 0.0f) || (this.mMotionAborted && motionEvent.getActionMasked() != 0))) {
            Log.d(TAG, "PanelView not intercept");
            return false;
        }
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        if (findPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            findPointerIndex = 0;
        }
        float x = motionEvent.getX(findPointerIndex);
        float y = motionEvent.getY(findPointerIndex);
        boolean isScrolledToBottom = isScrolledToBottom();
        int actionMasked = motionEvent.getActionMasked();
        boolean z = true;
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = y - this.mInitialTouchY;
                    trackMovement(motionEvent);
                    if (!this.mTouchStartedInBottomButton && (isScrolledToBottom || this.mTouchStartedInEmptyArea || this.mAnimatingOnDown)) {
                        float abs = Math.abs(f);
                        int i = this.mTouchSlop;
                        if ((f < ((float) (-i)) || (this.mAnimatingOnDown && abs > ((float) i))) && abs > Math.abs(x - this.mInitialTouchX)) {
                            cancelHeightAnimator();
                            startExpandMotion(x, y, true, this.mExpandedHeight);
                            return true;
                        }
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked != 5) {
                        if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                            if (motionEvent.getPointerId(0) != pointerId) {
                                z = false;
                            }
                            this.mTrackingPointer = motionEvent.getPointerId(z ? 1 : 0);
                            this.mInitialTouchX = motionEvent.getX(z);
                            this.mInitialTouchY = motionEvent.getY(z);
                        }
                    } else if (this.mStatusBar.getBarState() == 1) {
                        this.mMotionAborted = true;
                        VelocityTrackerInterface velocityTrackerInterface = this.mVelocityTracker;
                        if (velocityTrackerInterface != null) {
                            velocityTrackerInterface.recycle();
                            this.mVelocityTracker = null;
                        }
                    }
                }
            }
            VelocityTrackerInterface velocityTrackerInterface2 = this.mVelocityTracker;
            if (velocityTrackerInterface2 != null) {
                velocityTrackerInterface2.recycle();
                this.mVelocityTracker = null;
            }
        } else {
            this.mStatusBar.userActivity();
            this.mAnimatingOnDown = this.mHeightAnimator != null;
            this.mMinExpandHeight = 0.0f;
            if ((!this.mAnimatingOnDown || !this.mClosing || this.mHintAnimationRunning) && this.mPeekAnimator == null) {
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                if (isInContentBounds(x, y) && !isInUnderlapBounds(x, y)) {
                    z = false;
                }
                this.mTouchStartedInEmptyArea = z;
                this.mTouchStartedInBottomButton = isOnBottomIcon(x, y);
                this.mTouchSlopExceeded = false;
                this.mJustPeeked = false;
                this.mMotionAborted = false;
                this.mPanelClosedOnDown = isFullyCollapsed();
                this.mCollapsedAndHeadsUpOnDown = false;
                this.mHasLayoutedSinceDown = false;
                this.mUpdateFlingOnLayout = false;
                this.mTouchAboveFalsingThreshold = false;
                initVelocityTracker();
                trackMovement(motionEvent);
            } else {
                cancelHeightAnimator();
                cancelPeek();
                this.mTouchSlopExceeded = true;
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void cancelHeightAnimator() {
        ValueAnimator valueAnimator = this.mHeightAnimator;
        if (valueAnimator != null) {
            if (valueAnimator.isRunning()) {
                this.mPanelUpdateWhenAnimatorEnds = false;
            }
            this.mHeightAnimator.cancel();
        }
        endClosing();
    }

    private void endClosing() {
        if (this.mClosing) {
            this.mClosing = false;
            onClosingFinished();
        }
    }

    private void initVelocityTracker() {
        VelocityTrackerInterface velocityTrackerInterface = this.mVelocityTracker;
        if (velocityTrackerInterface != null) {
            velocityTrackerInterface.recycle();
        }
        this.mVelocityTracker = VelocityTrackerFactory.obtain(getContext());
    }

    /* access modifiers changed from: protected */
    public boolean flingExpands(float f, float f2, float f3, float f4) {
        if (isFalseTouch(f3, f4)) {
            return true;
        }
        if (isKeyguardShowing() || (!this.mOpening && !isFullyExpanded())) {
            if (Math.abs(f2) < this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
                if (getExpandedFraction() > 0.5f) {
                    return true;
                }
                return false;
            } else if (f > 0.0f) {
                return true;
            } else {
                return false;
            }
        } else if (!this.mPanelAppeared || (f <= 0.0f && this.mStretchLength <= 0.0f)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isFalseTouch(float f, float f2) {
        if (!this.mStatusBar.isFalsingThresholdNeeded()) {
            return false;
        }
        if (this.mFalsingManager.isClassiferEnabled()) {
            return this.mFalsingManager.isFalseTouch();
        }
        if (!this.mTouchAboveFalsingThreshold) {
            return true;
        }
        if (this.mUpwardsWhenTresholdReached) {
            return false;
        }
        return !isDirectionUpwards(f, f2);
    }

    /* access modifiers changed from: protected */
    public void fling(float f, boolean z) {
        fling(f, z, 1.0f, false);
    }

    /* access modifiers changed from: protected */
    public void fling(float f, boolean z, boolean z2) {
        fling(f, z, 1.0f, z2);
    }

    /* access modifiers changed from: protected */
    public void fling(float f, boolean z, float f2, boolean z2) {
        cancelPeek();
        float f3 = 0.0f;
        if (f < 1000.0f && z) {
            float f4 = this.mExpandedHeight;
            if (0.0f < f4 && f4 < this.mPeekHeight) {
                z = false;
                Log.d(TAG, "warning false touch.");
            }
        }
        boolean z3 = z;
        if (z3) {
            f3 = (float) getMaxPanelHeight();
        }
        float f5 = f3;
        if (!z3) {
            this.mClosing = true;
        }
        flingToHeight(f, z3, f5, f2, z2);
    }

    /* access modifiers changed from: protected */
    public void flingToHeight(float f, boolean z, float f2, float f3, boolean z2) {
        boolean z3 = z;
        float f4 = f2;
        boolean z4 = false;
        Log.d(TAG, String.format("flingToHeight vel=%.1f expand=%b target=%.1f mExpandedHeight=%.1f", new Object[]{Float.valueOf(f), Boolean.valueOf(z), Float.valueOf(f2), Float.valueOf(this.mExpandedHeight)}));
        if (!isOnKeyguard()) {
            resetStretchLength(z);
        }
        if (f4 == this.mExpandedHeight || (getOverExpansionAmount() > 0.0f && z3)) {
            notifyExpandingFinished();
            return;
        }
        if (getOverExpansionAmount() > 0.0f) {
            z4 = true;
        }
        this.mOverExpandedBeforeFling = z4;
        ValueAnimator createHeightAnimator = createHeightAnimator(f2);
        long j = 0;
        if (!z3) {
            if (!shouldUseDismissingAnimation()) {
                this.mFlingAnimationUtilsClosing.apply((Animator) createHeightAnimator, this.mExpandedHeight, f2, f, (float) getHeight());
            } else if (f == 0.0f) {
                createHeightAnimator.setInterpolator(Interpolators.PANEL_CLOSE_ACCELERATED);
                createHeightAnimator.setDuration(Util.isMiuiOptimizationDisabled() ? 0 : (long) (((this.mExpandedHeight / ((float) getHeight())) * 100.0f) + 200.0f));
            } else {
                this.mFlingAnimationUtilsDismissing.apply((Animator) createHeightAnimator, this.mExpandedHeight, f2, f, (float) getHeight());
            }
            if (this.mExpandedHeight < this.mPeekHeight) {
                createHeightAnimator.setDuration(0);
            } else if (f == 0.0f) {
                createHeightAnimator.setDuration((long) (((float) createHeightAnimator.getDuration()) / f3));
            }
        } else if (!isExpandForbiddenInKeyguard()) {
            float f5 = (!z2 || f >= 0.0f) ? f : 0.0f;
            createHeightAnimator.setInterpolator(Interpolators.QUINTIC_EASE_OUT);
            if (f5 != 0.0f || !Util.isMiuiOptimizationDisabled()) {
                j = 400;
            }
            createHeightAnimator.setDuration(j);
        } else {
            return;
        }
        if (this.mPerf != null) {
            this.mPerf.perfHint(4224, this.mContext.getPackageName(), -1, 1);
        }
        createHeightAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            public void onAnimationCancel(Animator animator) {
                if (PanelView.this.mPerf != null) {
                    PanelView.this.mPerf.perfLockRelease();
                }
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                if (PanelView.this.mPerf != null) {
                    PanelView.this.mPerf.perfLockRelease();
                }
                PanelView.this.setAnimator((ValueAnimator) null);
                if (!this.mCancelled) {
                    PanelView.this.notifyExpandingFinished();
                }
                PanelView.this.mKeyguardVerticalMoveHelper.reset();
                PanelView.this.notifyBarPanelExpansionChanged();
                ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelAnimationEnd();
            }
        });
        setAnimator(createHeightAnimator);
        createHeightAnimator.start();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mViewName = getResources().getResourceName(getId());
    }

    public void setExpandedHeight(float f) {
        if (DEBUG) {
            logf("setExpandedHeight(%.1f)", Float.valueOf(f));
        }
        setExpandedHeightInternal(f + getOverExpansionPixels());
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mStatusBar.onPanelLaidOut();
        requestPanelHeightUpdate();
        this.mHasLayoutedSinceDown = true;
        if (this.mUpdateFlingOnLayout) {
            abortAnimations();
            fling(this.mUpdateFlingVelocity, true);
            this.mUpdateFlingOnLayout = false;
        }
    }

    /* access modifiers changed from: protected */
    public void requestPanelHeightUpdate() {
        float maxPanelHeight = (float) getMaxPanelHeight();
        if (isFullyCollapsed() || maxPanelHeight == this.mExpandedHeight || this.mPeekAnimator != null || this.mPeekTouching) {
            return;
        }
        if (this.mTracking && !isTrackingBlocked()) {
            return;
        }
        if (this.mHeightAnimator != null) {
            this.mPanelUpdateWhenAnimatorEnds = true;
        } else {
            setExpandedHeight(maxPanelHeight);
        }
    }

    public void setExpandedHeightInternal(float f) {
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "setExpandedHeightInternal h=" + f);
        }
        float f2 = 0.0f;
        if (this.mExpandLatencyTracking && f != 0.0f) {
            DejankUtils.postAfterTraversal(new Runnable() {
                public void run() {
                    LatencyTracker.getInstance(PanelView.this.mContext).onActionEnd(0);
                }
            });
            this.mExpandLatencyTracking = false;
        }
        float maxPanelHeight = ((float) getMaxPanelHeight()) - getOverExpansionAmount();
        if (this.mHeightAnimator == null) {
            float max = Math.max(0.0f, f - maxPanelHeight);
            if (getOverExpansionPixels() != max && this.mTracking) {
                setOverExpansion(max, true);
            }
            this.mExpandedHeight = Math.min(f, maxPanelHeight) + getOverExpansionAmount();
        } else {
            this.mExpandedHeight = f;
            if (this.mOverExpandedBeforeFling) {
                setOverExpansion(Math.max(0.0f, f - maxPanelHeight), false);
            }
        }
        float f3 = this.mExpandedHeight;
        if (f3 < 1.0f && f3 != 0.0f && this.mClosing) {
            this.mExpandedHeight = 0.0f;
            ValueAnimator valueAnimator = this.mHeightAnimator;
            if (valueAnimator != null) {
                valueAnimator.end();
            }
        }
        if (maxPanelHeight != 0.0f) {
            f2 = this.mExpandedHeight / maxPanelHeight;
        }
        this.mExpandedFraction = Math.min(1.0f, f2);
        onHeightUpdated(this.mExpandedHeight);
        notifyBarPanelExpansionChanged();
    }

    /* access modifiers changed from: protected */
    public void setStretchLength(float f) {
        if (DEBUG) {
            Log.d(TAG, "pv setStretchLength " + f);
        }
        boolean z = true;
        if (this.mOpening && f > ((float) this.mTouchSlop)) {
            flingToPanelHeight(true);
        }
        onPanelDisplayChanged(!this.mOpening ? f > -80.0f : f > 80.0f, false);
        this.mStretchLength = this.mOpening ? Math.max(0.0f, f) : f;
        if ((!this.mOpening || f <= 80.0f) && (this.mOpening || f <= 0.0f)) {
            z = false;
        }
        if (z) {
            if (this.mOpening) {
                f -= 80.0f;
            }
            onSpringLengthUpdated(afterFriction(f, getHeight()) * 0.5f);
        }
    }

    /* access modifiers changed from: protected */
    public void resetStretchLength(boolean z) {
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "resetStretchLength " + z);
        }
        this.mStretchLength = 0.0f;
        onPanelDisplayChanged(z, false);
    }

    /* access modifiers changed from: protected */
    public void flingToPanelHeight(boolean z) {
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "flingToPanelHeight open=" + z);
        }
        setExpandedHeightInternal(z ? (float) getMaxPanelHeight() : 0.0f);
        notifyExpandingFinished();
        notifyBarPanelExpansionChanged();
    }

    private float afterFriction(float f, int i) {
        float f2 = (float) i;
        float min = Math.min(f / f2, 1.0f);
        float f3 = min * min;
        return ((((f3 * min) / 3.0f) - f3) + min) * f2;
    }

    public void setExpandedFraction(float f) {
        setExpandedHeight(((float) getMaxPanelHeight()) * f);
    }

    public float getExpandedHeight() {
        return this.mExpandedHeight;
    }

    public float getExpandedFraction() {
        return this.mExpandedFraction;
    }

    public boolean isFullyExpanded() {
        return this.mExpandedHeight >= ((float) getMaxPanelHeight());
    }

    public boolean isFullyCollapsed() {
        return this.mExpandedFraction <= 0.0f;
    }

    public boolean isCollapsing() {
        return this.mClosing || this.mLaunchingNotification;
    }

    public boolean isTracking() {
        return this.mTracking;
    }

    public boolean isOnShadeLocked() {
        return this.mStatusBar.getBarState() == 2;
    }

    public void setBar(PanelBar panelBar) {
        this.mBar = panelBar;
    }

    public void collapse(boolean z, float f) {
        if (DEBUG) {
            logf("collapse: " + this, new Object[0]);
        }
        if (canPanelBeCollapsed()) {
            cancelHeightAnimator();
            notifyExpandingStarted();
            this.mClosing = true;
            if (z) {
                this.mNextCollapseSpeedUpFactor = f;
                postDelayed(this.mFlingCollapseRunnable, 120);
                return;
            }
            fling(0.0f, false, f, false);
        }
    }

    public boolean canPanelBeCollapsed() {
        return !isFullyCollapsed() && !this.mTracking && !this.mClosing;
    }

    public void cancelPeek() {
        boolean z;
        ObjectAnimator objectAnimator = this.mPeekAnimator;
        if (objectAnimator != null) {
            z = true;
            objectAnimator.cancel();
        } else {
            z = false;
        }
        if (z) {
            notifyBarPanelExpansionChanged();
        }
    }

    public void expand(boolean z) {
        if (isFullyCollapsed() || isCollapsing()) {
            this.mInstantExpanding = true;
            this.mAnimateAfterExpanding = z;
            this.mUpdateFlingOnLayout = false;
            abortAnimations();
            cancelPeek();
            if (this.mTracking) {
                onTrackingStopped(true);
            }
            if (this.mExpanding) {
                notifyExpandingFinished();
            }
            notifyBarPanelExpansionChanged();
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (!PanelView.this.mInstantExpanding) {
                        PanelView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else if (PanelView.this.mStatusBar.getStatusBarWindow().getHeight() != PanelView.this.mStatusBar.getStatusBarHeight()) {
                        PanelView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (PanelView.this.mAnimateAfterExpanding) {
                            PanelView.this.notifyExpandingStarted();
                            PanelView.this.fling(0.0f, true);
                        } else {
                            PanelView.this.setExpandedFraction(1.0f);
                        }
                        boolean unused = PanelView.this.mInstantExpanding = false;
                    }
                }
            });
            requestLayout();
        }
    }

    public void instantCollapse() {
        abortAnimations();
        setExpandedFraction(0.0f);
        if (this.mExpanding) {
            notifyExpandingFinished();
        }
        if (this.mInstantExpanding) {
            this.mInstantExpanding = false;
            notifyBarPanelExpansionChanged();
        }
    }

    private void abortAnimations() {
        cancelPeek();
        cancelHeightAnimator();
        removeCallbacks(this.mPostCollapseRunnable);
        removeCallbacks(this.mFlingCollapseRunnable);
    }

    /* access modifiers changed from: protected */
    public void onClosingFinished() {
        this.mBar.onClosingFinished();
    }

    /* access modifiers changed from: private */
    public void setAnimator(ValueAnimator valueAnimator) {
        this.mHeightAnimator = valueAnimator;
        if (valueAnimator == null && this.mPanelUpdateWhenAnimatorEnds) {
            this.mPanelUpdateWhenAnimatorEnds = false;
            requestPanelHeightUpdate();
        }
    }

    private ValueAnimator createHeightAnimator(float f) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mExpandedHeight, f});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                PanelView.this.setExpandedHeightInternal(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        return ofFloat;
    }

    /* access modifiers changed from: protected */
    public void notifyBarPanelExpansionChanged() {
        PanelBar panelBar = this.mBar;
        if (panelBar != null) {
            float f = this.mExpandedFraction;
            panelBar.panelExpansionChanged(f, f > 0.0f || this.mPeekAnimator != null || this.mInstantExpanding || isPanelVisibleBecauseOfHeadsUp() || this.mTracking || this.mHeightAnimator != null || this.mLaunchingNotification);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onEmptySpaceClick(float f) {
        if (this.mHintAnimationRunning) {
            return true;
        }
        return onMiddleClicked();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        Object[] objArr = new Object[14];
        objArr[0] = Float.valueOf(getExpandedHeight());
        objArr[1] = Integer.valueOf(getMaxPanelHeight());
        String str6 = "T";
        objArr[2] = this.mOpening ? str6 : "f";
        if (this.mClosing) {
            str = str6;
        } else {
            str = "f";
        }
        objArr[3] = str;
        if (this.mTracking) {
            str2 = str6;
        } else {
            str2 = "f";
        }
        objArr[4] = str2;
        if (this.mJustPeeked) {
            str3 = str6;
        } else {
            str3 = "f";
        }
        objArr[5] = str3;
        ObjectAnimator objectAnimator = this.mPeekAnimator;
        objArr[6] = objectAnimator;
        String str7 = " (started)";
        if (objectAnimator == null || !objectAnimator.isStarted()) {
            str4 = "";
        } else {
            str4 = str7;
        }
        objArr[7] = str4;
        ValueAnimator valueAnimator = this.mHeightAnimator;
        objArr[8] = valueAnimator;
        if (valueAnimator == null || !valueAnimator.isStarted()) {
            str7 = "";
        }
        objArr[9] = str7;
        if (this.mTouchDisabled) {
            str5 = str6;
        } else {
            str5 = "f";
        }
        objArr[10] = str5;
        if (!this.mIsDefaultTheme) {
            str6 = "f";
        }
        objArr[11] = str6;
        objArr[12] = Float.valueOf(this.mStretchLength);
        objArr[13] = Float.valueOf(this.mSpringLength);
        printWriter.println(String.format("[PanelView: expandedHeight=%f maxPanelHeight=%d mOpening=%s closing=%s tracking=%s justPeeked=%s peekAnim=%s%s timeAnim=%s%s touchDisabled=%s mIsDefaultTheme=%s mStretchLength=%.1f mSpringLength=%.1f]", objArr));
    }

    public void setLaunchingNotification(boolean z) {
        this.mLaunchingNotification = z;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void setAppMiniWindowManager(AppMiniWindowManager appMiniWindowManager) {
        this.mAppMiniWindowManager = appMiniWindowManager;
    }
}
