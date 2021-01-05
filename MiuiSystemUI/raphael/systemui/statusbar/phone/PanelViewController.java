package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.SystemClock;
import android.util.BoostFramework;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.PanelView;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class PanelViewController {
    public static final boolean DEBUG = PanelBar.DEBUG;
    public static final String TAG = PanelView.class.getSimpleName();
    /* access modifiers changed from: private */
    public boolean mAnimateAfterExpanding;
    /* access modifiers changed from: private */
    public boolean mAnimatingOnDown;
    PanelBar mBar;
    protected boolean mClosing;
    /* access modifiers changed from: private */
    public boolean mCollapsedAndHeadsUpOnDown;
    protected long mDownTime;
    private final DozeLog mDozeLog;
    private boolean mExpandLatencyTracking;
    private float mExpandedFraction = 0.0f;
    protected float mExpandedHeight = 0.0f;
    protected boolean mExpanding;
    protected ArrayList<PanelExpansionListener> mExpansionListeners = new ArrayList<>();
    private final FalsingManager mFalsingManager;
    private int mFixedDuration = -1;
    private FlingAnimationUtils mFlingAnimationUtils;
    private FlingAnimationUtils mFlingAnimationUtilsClosing;
    private FlingAnimationUtils mFlingAnimationUtilsDismissing;
    private final Runnable mFlingCollapseRunnable = new Runnable() {
        public void run() {
            PanelViewController panelViewController = PanelViewController.this;
            panelViewController.fling(0.0f, false, panelViewController.mNextCollapseSpeedUpFactor, false);
        }
    };
    /* access modifiers changed from: private */
    public boolean mGestureWaitForTouchSlop;
    /* access modifiers changed from: private */
    public boolean mHasLayoutedSinceDown;
    protected HeadsUpManagerPhone mHeadsUpManager;
    /* access modifiers changed from: private */
    public ValueAnimator mHeightAnimator;
    public boolean mHintAnimationRunning;
    /* access modifiers changed from: private */
    public boolean mIgnoreXTouchSlop;
    /* access modifiers changed from: private */
    public float mInitialOffsetOnTouch;
    /* access modifiers changed from: private */
    public float mInitialTouchX;
    /* access modifiers changed from: private */
    public float mInitialTouchY;
    /* access modifiers changed from: private */
    public boolean mInstantExpanding;
    /* access modifiers changed from: private */
    public boolean mJustPeeked;
    protected KeyguardBottomAreaView mKeyguardBottomArea;
    protected final KeyguardStateController mKeyguardStateController;
    private final LatencyTracker mLatencyTracker;
    protected boolean mLaunchingNotification;
    private LockscreenGestureLogger mLockscreenGestureLogger = new LockscreenGestureLogger();
    /* access modifiers changed from: private */
    public float mMinExpandHeight;
    /* access modifiers changed from: private */
    public boolean mMotionAborted;
    /* access modifiers changed from: private */
    public float mNextCollapseSpeedUpFactor = 1.0f;
    /* access modifiers changed from: private */
    public boolean mNotificationsDragEnabled;
    private boolean mOverExpandedBeforeFling;
    /* access modifiers changed from: private */
    public boolean mPanelClosedOnDown;
    private boolean mPanelUpdateWhenAnimatorEnds;
    /* access modifiers changed from: private */
    public ObjectAnimator mPeekAnimator;
    /* access modifiers changed from: private */
    public float mPeekHeight;
    /* access modifiers changed from: private */
    public boolean mPeekTouching;
    /* access modifiers changed from: private */
    public BoostFramework mPerf = null;
    protected final Runnable mPostCollapseRunnable = new Runnable() {
        public void run() {
            PanelViewController.this.collapse(false, 1.0f);
        }
    };
    protected final Resources mResources;
    private float mSlopMultiplier;
    protected StatusBar mStatusBar;
    protected final SysuiStatusBarStateController mStatusBarStateController;
    protected final StatusBarTouchableRegionManager mStatusBarTouchableRegionManager;
    /* access modifiers changed from: private */
    public boolean mTouchAboveFalsingThreshold;
    /* access modifiers changed from: private */
    public boolean mTouchDisabled;
    private int mTouchSlop;
    /* access modifiers changed from: private */
    public boolean mTouchSlopExceeded;
    protected boolean mTouchSlopExceededBeforeDown;
    /* access modifiers changed from: private */
    public boolean mTouchStartedInEmptyArea;
    protected boolean mTracking;
    /* access modifiers changed from: private */
    public int mTrackingPointer;
    private int mUnlockFalsingThreshold;
    /* access modifiers changed from: private */
    public boolean mUpdateFlingOnLayout;
    /* access modifiers changed from: private */
    public float mUpdateFlingVelocity;
    /* access modifiers changed from: private */
    public boolean mUpwardsWhenThresholdReached;
    /* access modifiers changed from: private */
    public final VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private boolean mVibrateOnOpening;
    private final VibratorHelper mVibratorHelper;
    /* access modifiers changed from: private */
    public final PanelView mView;
    /* access modifiers changed from: private */
    public String mViewName;

    /* access modifiers changed from: protected */
    public abstract boolean canCollapsePanelOnTouch();

    public abstract OnLayoutChangeListener createLayoutChangeListener();

    /* access modifiers changed from: protected */
    public abstract OnConfigurationChangedListener createOnConfigurationChangedListener();

    /* access modifiers changed from: protected */
    public abstract TouchHandler createTouchHandler();

    /* access modifiers changed from: protected */
    public abstract boolean fullyExpandedClearAllVisible();

    /* access modifiers changed from: protected */
    public abstract int getClearAllHeightWithPadding();

    /* access modifiers changed from: protected */
    public abstract int getMaxPanelHeight();

    /* access modifiers changed from: protected */
    public abstract float getOpeningHeight();

    /* access modifiers changed from: protected */
    public abstract float getOverExpansionAmount();

    /* access modifiers changed from: protected */
    public abstract float getOverExpansionPixels();

    /* access modifiers changed from: protected */
    public abstract float getPeekHeight();

    public void goToLockedShade(View view) {
    }

    /* access modifiers changed from: protected */
    public abstract boolean isClearAllVisible();

    /* access modifiers changed from: protected */
    public abstract boolean isInContentBounds(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract boolean isPanelVisibleBecauseOfHeadsUp();

    /* access modifiers changed from: protected */
    public boolean isStatusBarExpandable() {
        return false;
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

    public abstract void resetViews(boolean z);

    /* access modifiers changed from: protected */
    public abstract void setOverExpansion(float f, boolean z);

    /* access modifiers changed from: protected */
    public abstract boolean shouldGestureIgnoreXTouchSlop(float f, float f2);

    /* access modifiers changed from: protected */
    public abstract boolean shouldGestureWaitForTouchSlop();

    /* access modifiers changed from: protected */
    public abstract boolean shouldUseDismissingAnimation();

    /* access modifiers changed from: protected */
    public void startUnlockHintAnimation() {
    }

    private void logf(String str, Object... objArr) {
        String str2;
        String str3 = TAG;
        StringBuilder sb = new StringBuilder();
        if (this.mViewName != null) {
            str2 = this.mViewName + ": ";
        } else {
            str2 = "";
        }
        sb.append(str2);
        sb.append(String.format(str, objArr));
        Log.v(str3, sb.toString());
    }

    /* access modifiers changed from: protected */
    public void onExpandingFinished() {
        this.mBar.onExpandingFinished();
    }

    /* access modifiers changed from: protected */
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

    /* access modifiers changed from: protected */
    public void runPeekAnimation(long j, float f, final boolean z) {
        this.mPeekHeight = f;
        if (DEBUG) {
            logf("peek to height=%.1f", Float.valueOf(f));
        }
        if (this.mHeightAnimator == null) {
            ObjectAnimator objectAnimator = this.mPeekAnimator;
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator duration = ObjectAnimator.ofFloat(this, "expandedHeight", new float[]{this.mPeekHeight}).setDuration(j);
            this.mPeekAnimator = duration;
            duration.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            this.mPeekAnimator.addListener(new AnimatorListenerAdapter() {
                private boolean mCancelled;

                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    ObjectAnimator unused = PanelViewController.this.mPeekAnimator = null;
                    if (!this.mCancelled && z) {
                        PanelViewController.this.mView.postOnAnimation(PanelViewController.this.mPostCollapseRunnable);
                    }
                }
            });
            notifyExpandingStarted();
            this.mPeekAnimator.start();
            this.mJustPeeked = true;
        }
    }

    public PanelViewController(PanelView panelView, FalsingManager falsingManager, DozeLog dozeLog, KeyguardStateController keyguardStateController, SysuiStatusBarStateController sysuiStatusBarStateController, VibratorHelper vibratorHelper, LatencyTracker latencyTracker, FlingAnimationUtils.Builder builder, StatusBarTouchableRegionManager statusBarTouchableRegionManager) {
        this.mView = panelView;
        panelView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            public void onViewDetachedFromWindow(View view) {
            }

            public void onViewAttachedToWindow(View view) {
                PanelViewController panelViewController = PanelViewController.this;
                String unused = panelViewController.mViewName = panelViewController.mResources.getResourceName(panelViewController.mView.getId());
            }
        });
        this.mView.addOnLayoutChangeListener(createLayoutChangeListener());
        this.mView.setOnTouchListener(createTouchHandler());
        this.mView.setOnConfigurationChangedListener(createOnConfigurationChangedListener());
        this.mResources = this.mView.getResources();
        this.mKeyguardStateController = keyguardStateController;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        builder.reset();
        builder.setMaxLengthSeconds(0.6f);
        builder.setSpeedUpFactor(0.6f);
        this.mFlingAnimationUtils = builder.build();
        builder.reset();
        builder.setMaxLengthSeconds(0.5f);
        builder.setSpeedUpFactor(0.6f);
        this.mFlingAnimationUtilsClosing = builder.build();
        builder.reset();
        builder.setMaxLengthSeconds(0.5f);
        builder.setSpeedUpFactor(0.6f);
        builder.setX2(0.6f);
        builder.setY2(0.84f);
        this.mFlingAnimationUtilsDismissing = builder.build();
        this.mLatencyTracker = latencyTracker;
        this.mFalsingManager = falsingManager;
        this.mDozeLog = dozeLog;
        this.mNotificationsDragEnabled = this.mResources.getBoolean(C0010R$bool.config_enableNotificationShadeDrag);
        this.mVibratorHelper = vibratorHelper;
        this.mVibrateOnOpening = this.mResources.getBoolean(C0010R$bool.config_vibrateOnIconAnimation);
        this.mStatusBarTouchableRegionManager = statusBarTouchableRegionManager;
        this.mPerf = new BoostFramework();
    }

    /* access modifiers changed from: protected */
    public void loadDimens() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mView.getContext());
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mSlopMultiplier = viewConfiguration.getScaledAmbiguousGestureMultiplier();
        this.mResources.getDimension(C0012R$dimen.hint_move_distance);
        this.mUnlockFalsingThreshold = this.mResources.getDimensionPixelSize(C0012R$dimen.unlock_falsing_threshold);
    }

    /* access modifiers changed from: protected */
    public float getTouchSlop(MotionEvent motionEvent) {
        if (motionEvent.getClassification() == 1) {
            return ((float) this.mTouchSlop) * this.mSlopMultiplier;
        }
        return (float) this.mTouchSlop;
    }

    /* access modifiers changed from: private */
    public void addMovement(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        this.mVelocityTracker.addMovement(motionEvent);
        motionEvent.offsetLocation(-rawX, -rawY);
    }

    public void setTouchAndAnimationDisabled(boolean z) {
        this.mTouchDisabled = z;
        if (z) {
            cancelHeightAnimator();
            if (this.mTracking) {
                onTrackingStopped(true);
            }
            notifyExpandingFinished();
        }
    }

    public void startExpandLatencyTracking() {
        if (this.mLatencyTracker.isEnabled()) {
            this.mLatencyTracker.onActionStart(0);
            this.mExpandLatencyTracking = true;
        }
    }

    /* access modifiers changed from: protected */
    public void startOpening(MotionEvent motionEvent) {
        runPeekAnimation(200, getOpeningHeight(), false);
        notifyBarPanelExpansionChanged();
        maybeVibrateOnOpening();
        float displayWidth = this.mStatusBar.getDisplayWidth();
        float displayHeight = this.mStatusBar.getDisplayHeight();
        this.mLockscreenGestureLogger.writeAtFractionalPosition(1328, (int) ((motionEvent.getX() / displayWidth) * 100.0f), (int) ((motionEvent.getY() / displayHeight) * 100.0f), this.mStatusBar.getRotation());
        this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_UNLOCKED_NOTIFICATION_PANEL_EXPAND);
    }

    /* access modifiers changed from: protected */
    public void maybeVibrateOnOpening() {
        if (this.mVibrateOnOpening) {
            this.mVibratorHelper.vibrate(2);
        }
    }

    /* access modifiers changed from: private */
    public boolean isDirectionUpwards(float f, float f2) {
        float f3 = f - this.mInitialTouchX;
        float f4 = f2 - this.mInitialTouchY;
        if (f4 < 0.0f && Math.abs(f4) >= Math.abs(f3)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void startExpandingFromPeek() {
        this.mStatusBar.handlePeekToExpandTransistion();
    }

    /* access modifiers changed from: protected */
    public void startExpandMotion(float f, float f2, boolean z, float f3) {
        this.mInitialOffsetOnTouch = f3;
        this.mInitialTouchY = f2;
        this.mInitialTouchX = f;
        if (z) {
            this.mTouchSlopExceeded = true;
            setExpandedHeight(f3);
            onTrackingStarted();
        }
    }

    /* access modifiers changed from: private */
    public void endMotionEvent(MotionEvent motionEvent, float f, float f2, boolean z) {
        this.mTrackingPointer = -1;
        boolean z2 = true;
        if ((this.mTracking && this.mTouchSlopExceeded) || Math.abs(f - this.mInitialTouchX) > ((float) this.mTouchSlop) || Math.abs(f2 - this.mInitialTouchY) > ((float) this.mTouchSlop) || motionEvent.getActionMasked() == 3 || z) {
            this.mVelocityTracker.computeCurrentVelocity(1000);
            float yVelocity = this.mVelocityTracker.getYVelocity();
            float hypot = (float) Math.hypot((double) this.mVelocityTracker.getXVelocity(), (double) this.mVelocityTracker.getYVelocity());
            boolean z3 = this.mStatusBarStateController.getState() == 1;
            if (motionEvent.getActionMasked() != 3 && !z) {
                z2 = flingExpands(yVelocity, hypot, f, f2);
            } else if (!z3) {
                z2 = true ^ this.mPanelClosedOnDown;
            }
            this.mDozeLog.traceFling(z2, this.mTouchAboveFalsingThreshold, this.mStatusBar.isFalsingThresholdNeeded(), this.mStatusBar.isWakeUpComingFromTouch());
            if (!z2 && z3) {
                float displayDensity = this.mStatusBar.getDisplayDensity();
                this.mLockscreenGestureLogger.write(186, (int) Math.abs((f2 - this.mInitialTouchY) / displayDensity), (int) Math.abs(yVelocity / displayDensity));
                this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_UNLOCK);
            }
            fling(yVelocity, z2, isFalseTouch(f, f2));
            onTrackingStopped(z2);
            if (this.mUpdateFlingOnLayout) {
                this.mUpdateFlingVelocity = yVelocity;
            }
        } else if (!this.mPanelClosedOnDown || this.mHeadsUpManager.hasPinnedHeadsUp() || this.mTracking || this.mStatusBar.isBouncerShowing() || this.mKeyguardStateController.isKeyguardFadingAway()) {
            if (!this.mStatusBar.isBouncerShowing()) {
                onTrackingStopped(onEmptySpaceClick(this.mInitialTouchX));
            }
        } else if (SystemClock.uptimeMillis() - this.mDownTime < ((long) ViewConfiguration.getLongPressTimeout())) {
            runPeekAnimation(360, getPeekHeight(), true);
        } else {
            this.mView.postOnAnimation(this.mPostCollapseRunnable);
        }
        this.mVelocityTracker.clear();
        this.mPeekTouching = false;
    }

    /* access modifiers changed from: protected */
    public float getCurrentExpandVelocity() {
        this.mVelocityTracker.computeCurrentVelocity(1000);
        return this.mVelocityTracker.getYVelocity();
    }

    /* access modifiers changed from: private */
    public int getFalsingThreshold() {
        return (int) (((float) this.mUnlockFalsingThreshold) * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    /* access modifiers changed from: protected */
    public void onTrackingStopped(boolean z) {
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

    /* access modifiers changed from: protected */
    public boolean flingExpands(float f, float f2, float f3, float f4) {
        if (this.mFalsingManager.isUnlockingDisabled() || isFalseTouch(f3, f4)) {
            return true;
        }
        if (Math.abs(f2) < this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            return shouldExpandWhenNotFlinging();
        }
        if (f > 0.0f) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldExpandWhenNotFlinging() {
        return getExpandedFraction() > 0.5f;
    }

    private boolean isFalseTouch(float f, float f2) {
        if (!this.mStatusBar.isFalsingThresholdNeeded()) {
            return false;
        }
        if (this.mFalsingManager.isClassifierEnabled()) {
            return this.mFalsingManager.isFalseTouch();
        }
        if (!this.mTouchAboveFalsingThreshold) {
            return true;
        }
        if (this.mUpwardsWhenThresholdReached) {
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
        float maxPanelHeight = z ? (float) getMaxPanelHeight() : 0.0f;
        if (!z) {
            this.mClosing = true;
        }
        flingToHeight(f, z, maxPanelHeight, f2, z2);
    }

    /* access modifiers changed from: protected */
    public void flingToHeight(float f, boolean z, float f2, float f3, boolean z2) {
        boolean z3 = true;
        final boolean z4 = z && shouldExpandToTopOfClearAll((float) (getMaxPanelHeight() - getClearAllHeightWithPadding()));
        if (z4) {
            f2 = (float) (getMaxPanelHeight() - getClearAllHeightWithPadding());
        }
        float f4 = f2;
        if (f4 == this.mExpandedHeight || (getOverExpansionAmount() > 0.0f && z)) {
            notifyExpandingFinished();
            return;
        }
        if (getOverExpansionAmount() <= 0.0f) {
            z3 = false;
        }
        this.mOverExpandedBeforeFling = z3;
        ValueAnimator createHeightAnimator = createHeightAnimator(f4);
        if (z) {
            if (z2 && f < 0.0f) {
                f = 0.0f;
            }
            this.mFlingAnimationUtils.apply(createHeightAnimator, this.mExpandedHeight, f4, f, (float) this.mView.getHeight());
            if (f == 0.0f) {
                createHeightAnimator.setDuration(350);
            }
        } else {
            if (!shouldUseDismissingAnimation()) {
                this.mFlingAnimationUtilsClosing.apply(createHeightAnimator, this.mExpandedHeight, f4, f, (float) this.mView.getHeight());
            } else if (f == 0.0f) {
                createHeightAnimator.setInterpolator(Interpolators.PANEL_CLOSE_ACCELERATED);
                createHeightAnimator.setDuration((long) (((this.mExpandedHeight / ((float) this.mView.getHeight())) * 100.0f) + 200.0f));
            } else {
                this.mFlingAnimationUtilsDismissing.apply(createHeightAnimator, this.mExpandedHeight, f4, f, (float) this.mView.getHeight());
            }
            if (f == 0.0f) {
                createHeightAnimator.setDuration((long) (((float) createHeightAnimator.getDuration()) / f3));
            }
            int i = this.mFixedDuration;
            if (i != -1) {
                createHeightAnimator.setDuration((long) i);
            }
        }
        if (this.mPerf != null) {
            this.mPerf.perfHint(4224, this.mView.getContext().getPackageName(), -1, 3);
        }
        createHeightAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            public void onAnimationCancel(Animator animator) {
                if (PanelViewController.this.mPerf != null) {
                    PanelViewController.this.mPerf.perfLockRelease();
                }
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                if (PanelViewController.this.mPerf != null) {
                    PanelViewController.this.mPerf.perfLockRelease();
                }
                if (z4 && !this.mCancelled) {
                    PanelViewController panelViewController = PanelViewController.this;
                    panelViewController.setExpandedHeightInternal((float) panelViewController.getMaxPanelHeight());
                }
                PanelViewController.this.setAnimator((ValueAnimator) null);
                if (!this.mCancelled) {
                    PanelViewController.this.notifyExpandingFinished();
                }
                ((KeyguardPanelViewInjector) Dependency.get(KeyguardPanelViewInjector.class)).resetVerticalTouchEvent();
                PanelViewController.this.notifyBarPanelExpansionChanged();
            }
        });
        setAnimator(createHeightAnimator);
        createHeightAnimator.start();
    }

    /* access modifiers changed from: protected */
    public boolean shouldExpandToTopOfClearAll(float f) {
        return fullyExpandedClearAllVisible() && this.mExpandedHeight < f && !isClearAllVisible();
    }

    public void setExpandedHeight(float f) {
        if (DEBUG) {
            logf("setExpandedHeight(%.1f)", Float.valueOf(f));
        }
        setExpandedHeightInternal(f + getOverExpansionPixels());
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
        if (Float.isNaN(f)) {
            Log.wtf(TAG, "ExpandedHeight set to NaN");
        }
        float f2 = 0.0f;
        if (this.mExpandLatencyTracking && f != 0.0f) {
            DejankUtils.postAfterTraversal(new Runnable() {
                public final void run() {
                    PanelViewController.this.lambda$setExpandedHeightInternal$0$PanelViewController();
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

    /* access modifiers changed from: private */
    /* renamed from: lambda$setExpandedHeightInternal$0 */
    public /* synthetic */ void lambda$setExpandedHeightInternal$0$PanelViewController() {
        this.mLatencyTracker.onActionEnd(0);
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
                this.mView.postDelayed(this.mFlingCollapseRunnable, 120);
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
            this.mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (!PanelViewController.this.mInstantExpanding) {
                        PanelViewController.this.mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else if (PanelViewController.this.mStatusBar.getNotificationShadeWindowView().isVisibleToUser()) {
                        PanelViewController.this.mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (PanelViewController.this.mAnimateAfterExpanding) {
                            PanelViewController.this.notifyExpandingStarted();
                            PanelViewController.this.fling(0.0f, true);
                        } else {
                            PanelViewController.this.setExpandedFraction(1.0f);
                        }
                        boolean unused = PanelViewController.this.mInstantExpanding = false;
                    }
                }
            });
            this.mView.requestLayout();
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

    /* access modifiers changed from: private */
    public void abortAnimations() {
        cancelPeek();
        cancelHeightAnimator();
        this.mView.removeCallbacks(this.mPostCollapseRunnable);
        this.mView.removeCallbacks(this.mFlingCollapseRunnable);
    }

    /* access modifiers changed from: protected */
    public void onClosingFinished() {
        this.mBar.onClosingFinished();
    }

    /* access modifiers changed from: protected */
    public void onUnlockHintFinished() {
        this.mStatusBar.onHintFinished();
    }

    /* access modifiers changed from: protected */
    public void onUnlockHintStarted() {
        this.mStatusBar.onUnlockHintStarted();
    }

    public boolean isUnlockHintRunning() {
        return this.mHintAnimationRunning;
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
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                PanelViewController.this.lambda$createHeightAnimator$3$PanelViewController(valueAnimator);
            }
        });
        return ofFloat;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createHeightAnimator$3 */
    public /* synthetic */ void lambda$createHeightAnimator$3$PanelViewController(ValueAnimator valueAnimator) {
        setExpandedHeightInternal(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* access modifiers changed from: protected */
    public void notifyBarPanelExpansionChanged() {
        PanelBar panelBar = this.mBar;
        if (panelBar != null) {
            float f = this.mExpandedFraction;
            panelBar.panelExpansionChanged(f, f > 0.0f || this.mPeekAnimator != null || this.mInstantExpanding || isPanelVisibleBecauseOfHeadsUp() || this.mTracking || this.mHeightAnimator != null);
        }
        for (int i = 0; i < this.mExpansionListeners.size(); i++) {
            this.mExpansionListeners.get(i).onPanelExpansionChanged(this.mExpandedFraction, this.mTracking);
        }
    }

    public void addExpansionListener(PanelExpansionListener panelExpansionListener) {
        this.mExpansionListeners.add(panelExpansionListener);
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
        Object[] objArr = new Object[11];
        objArr[0] = getClass().getSimpleName();
        objArr[1] = Float.valueOf(getExpandedHeight());
        objArr[2] = Integer.valueOf(getMaxPanelHeight());
        String str2 = "T";
        objArr[3] = this.mClosing ? str2 : "f";
        objArr[4] = this.mTracking ? str2 : "f";
        objArr[5] = this.mJustPeeked ? str2 : "f";
        ObjectAnimator objectAnimator = this.mPeekAnimator;
        objArr[6] = objectAnimator;
        String str3 = " (started)";
        if (objectAnimator == null || !objectAnimator.isStarted()) {
            str = "";
        } else {
            str = str3;
        }
        objArr[7] = str;
        ValueAnimator valueAnimator = this.mHeightAnimator;
        objArr[8] = valueAnimator;
        if (valueAnimator == null || !valueAnimator.isStarted()) {
            str3 = "";
        }
        objArr[9] = str3;
        if (!this.mTouchDisabled) {
            str2 = "f";
        }
        objArr[10] = str2;
        printWriter.println(String.format("[PanelView(%s): expandedHeight=%f maxPanelHeight=%d closing=%s tracking=%s justPeeked=%s peekAnim=%s%s timeAnim=%s%s touchDisabled=%s]", objArr));
    }

    public void setHeadsUpManager(HeadsUpManagerPhone headsUpManagerPhone) {
        this.mHeadsUpManager = headsUpManagerPhone;
    }

    public void setLaunchingNotification(boolean z) {
        this.mLaunchingNotification = z;
    }

    public ViewGroup getView() {
        return this.mView;
    }

    public boolean isEnabled() {
        return this.mView.isEnabled();
    }

    public class TouchHandler implements View.OnTouchListener {
        public TouchHandler() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:59:0x0147, code lost:
            if (r3.mHintAnimationRunning == false) goto L_0x0151;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onInterceptTouchEvent(android.view.MotionEvent r9) {
            /*
                r8 = this;
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r0 = r0.mInstantExpanding
                r1 = 0
                if (r0 != 0) goto L_0x01a8
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r0 = r0.mNotificationsDragEnabled
                if (r0 == 0) goto L_0x01a8
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r0 = r0.mTouchDisabled
                if (r0 != 0) goto L_0x01a8
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r0 = r0.mMotionAborted
                if (r0 == 0) goto L_0x0029
                int r0 = r9.getActionMasked()
                if (r0 == 0) goto L_0x0029
                goto L_0x01a8
            L_0x0029:
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                int r0 = r0.mTrackingPointer
                int r0 = r9.findPointerIndex(r0)
                if (r0 >= 0) goto L_0x003f
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                int r2 = r9.getPointerId(r1)
                int unused = r0.mTrackingPointer = r2
                r0 = r1
            L_0x003f:
                float r2 = r9.getX(r0)
                float r0 = r9.getY(r0)
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r3 = r3.canCollapsePanelOnTouch()
                int r4 = r9.getActionMasked()
                r5 = 1
                if (r4 == 0) goto L_0x0114
                if (r4 == r5) goto L_0x0109
                r6 = 2
                if (r4 == r6) goto L_0x00b2
                r0 = 3
                if (r4 == r0) goto L_0x0109
                r0 = 5
                if (r4 == r0) goto L_0x0098
                r0 = 6
                if (r4 == r0) goto L_0x0064
                goto L_0x01a8
            L_0x0064:
                int r0 = r9.getActionIndex()
                int r0 = r9.getPointerId(r0)
                com.android.systemui.statusbar.phone.PanelViewController r2 = com.android.systemui.statusbar.phone.PanelViewController.this
                int r2 = r2.mTrackingPointer
                if (r2 != r0) goto L_0x01a8
                int r2 = r9.getPointerId(r1)
                if (r2 == r0) goto L_0x007b
                r5 = r1
            L_0x007b:
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                int r2 = r9.getPointerId(r5)
                int unused = r0.mTrackingPointer = r2
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                float r2 = r9.getX(r5)
                float unused = r0.mInitialTouchX = r2
                com.android.systemui.statusbar.phone.PanelViewController r8 = com.android.systemui.statusbar.phone.PanelViewController.this
                float r9 = r9.getY(r5)
                float unused = r8.mInitialTouchY = r9
                goto L_0x01a8
            L_0x0098:
                com.android.systemui.statusbar.phone.PanelViewController r9 = com.android.systemui.statusbar.phone.PanelViewController.this
                com.android.systemui.statusbar.SysuiStatusBarStateController r9 = r9.mStatusBarStateController
                int r9 = r9.getState()
                if (r9 != r5) goto L_0x01a8
                com.android.systemui.statusbar.phone.PanelViewController r9 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean unused = r9.mMotionAborted = r5
                com.android.systemui.statusbar.phone.PanelViewController r8 = com.android.systemui.statusbar.phone.PanelViewController.this
                android.view.VelocityTracker r8 = r8.mVelocityTracker
                r8.clear()
                goto L_0x01a8
            L_0x00b2:
                com.android.systemui.statusbar.phone.PanelViewController r4 = com.android.systemui.statusbar.phone.PanelViewController.this
                float r4 = r4.mInitialTouchY
                float r4 = r0 - r4
                com.android.systemui.statusbar.phone.PanelViewController r6 = com.android.systemui.statusbar.phone.PanelViewController.this
                r6.addMovement(r9)
                if (r3 != 0) goto L_0x00d1
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r3 = r3.mTouchStartedInEmptyArea
                if (r3 != 0) goto L_0x00d1
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r3 = r3.mAnimatingOnDown
                if (r3 == 0) goto L_0x01a8
            L_0x00d1:
                float r3 = java.lang.Math.abs(r4)
                com.android.systemui.statusbar.phone.PanelViewController r6 = com.android.systemui.statusbar.phone.PanelViewController.this
                float r9 = r6.getTouchSlop(r9)
                float r6 = -r9
                int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r4 < 0) goto L_0x00ec
                com.android.systemui.statusbar.phone.PanelViewController r4 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r4 = r4.mAnimatingOnDown
                if (r4 == 0) goto L_0x01a8
                int r9 = (r3 > r9 ? 1 : (r3 == r9 ? 0 : -1))
                if (r9 <= 0) goto L_0x01a8
            L_0x00ec:
                com.android.systemui.statusbar.phone.PanelViewController r9 = com.android.systemui.statusbar.phone.PanelViewController.this
                float r9 = r9.mInitialTouchX
                float r9 = r2 - r9
                float r9 = java.lang.Math.abs(r9)
                int r9 = (r3 > r9 ? 1 : (r3 == r9 ? 0 : -1))
                if (r9 <= 0) goto L_0x01a8
                com.android.systemui.statusbar.phone.PanelViewController r9 = com.android.systemui.statusbar.phone.PanelViewController.this
                r9.cancelHeightAnimator()
                com.android.systemui.statusbar.phone.PanelViewController r8 = com.android.systemui.statusbar.phone.PanelViewController.this
                float r9 = r8.mExpandedHeight
                r8.startExpandMotion(r2, r0, r5, r9)
                return r5
            L_0x0109:
                com.android.systemui.statusbar.phone.PanelViewController r8 = com.android.systemui.statusbar.phone.PanelViewController.this
                android.view.VelocityTracker r8 = r8.mVelocityTracker
                r8.clear()
                goto L_0x01a8
            L_0x0114:
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                com.android.systemui.statusbar.phone.StatusBar r3 = r3.mStatusBar
                r3.userActivity()
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                android.animation.ValueAnimator r4 = r3.mHeightAnimator
                if (r4 == 0) goto L_0x0125
                r4 = r5
                goto L_0x0126
            L_0x0125:
                r4 = r1
            L_0x0126:
                boolean unused = r3.mAnimatingOnDown = r4
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                r4 = 0
                float unused = r3.mMinExpandHeight = r4
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                long r6 = android.os.SystemClock.uptimeMillis()
                r3.mDownTime = r6
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r3 = r3.mAnimatingOnDown
                if (r3 == 0) goto L_0x0149
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r4 = r3.mClosing
                if (r4 == 0) goto L_0x0149
                boolean r3 = r3.mHintAnimationRunning
                if (r3 == 0) goto L_0x0151
            L_0x0149:
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                android.animation.ObjectAnimator r3 = r3.mPeekAnimator
                if (r3 == 0) goto L_0x0161
            L_0x0151:
                com.android.systemui.statusbar.phone.PanelViewController r9 = com.android.systemui.statusbar.phone.PanelViewController.this
                r9.cancelHeightAnimator()
                com.android.systemui.statusbar.phone.PanelViewController r9 = com.android.systemui.statusbar.phone.PanelViewController.this
                r9.cancelPeek()
                com.android.systemui.statusbar.phone.PanelViewController r8 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean unused = r8.mTouchSlopExceeded = r5
                return r5
            L_0x0161:
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                float unused = r3.mInitialTouchY = r0
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                float unused = r3.mInitialTouchX = r2
                com.android.systemui.statusbar.phone.PanelViewController r3 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r0 = r3.isInContentBounds(r2, r0)
                r0 = r0 ^ r5
                boolean unused = r3.mTouchStartedInEmptyArea = r0
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r2 = r0.mTouchSlopExceededBeforeDown
                boolean unused = r0.mTouchSlopExceeded = r2
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean unused = r0.mJustPeeked = r1
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean unused = r0.mMotionAborted = r1
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean r2 = r0.isFullyCollapsed()
                boolean unused = r0.mPanelClosedOnDown = r2
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean unused = r0.mCollapsedAndHeadsUpOnDown = r1
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean unused = r0.mHasLayoutedSinceDown = r1
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean unused = r0.mUpdateFlingOnLayout = r1
                com.android.systemui.statusbar.phone.PanelViewController r0 = com.android.systemui.statusbar.phone.PanelViewController.this
                boolean unused = r0.mTouchAboveFalsingThreshold = r1
                com.android.systemui.statusbar.phone.PanelViewController r8 = com.android.systemui.statusbar.phone.PanelViewController.this
                r8.addMovement(r9)
            L_0x01a8:
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.PanelViewController.TouchHandler.onInterceptTouchEvent(android.view.MotionEvent):boolean");
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            int pointerId;
            if (PanelViewController.this.mInstantExpanding) {
                return false;
            }
            if (PanelViewController.this.mTouchDisabled && motionEvent.getActionMasked() != 3) {
                return false;
            }
            if (PanelViewController.this.mMotionAborted && motionEvent.getActionMasked() != 0) {
                return false;
            }
            if (!PanelViewController.this.mNotificationsDragEnabled) {
                PanelViewController panelViewController = PanelViewController.this;
                if (panelViewController.mTracking) {
                    panelViewController.onTrackingStopped(true);
                }
                return false;
            } else if (!PanelViewController.this.isFullyCollapsed() || !motionEvent.isFromSource(8194)) {
                int findPointerIndex = motionEvent.findPointerIndex(PanelViewController.this.mTrackingPointer);
                if (findPointerIndex < 0) {
                    int unused = PanelViewController.this.mTrackingPointer = motionEvent.getPointerId(0);
                    findPointerIndex = 0;
                }
                float x = motionEvent.getX(findPointerIndex);
                float y = motionEvent.getY(findPointerIndex);
                if (motionEvent.getActionMasked() == 0) {
                    PanelViewController panelViewController2 = PanelViewController.this;
                    boolean unused2 = panelViewController2.mGestureWaitForTouchSlop = panelViewController2.shouldGestureWaitForTouchSlop();
                    PanelViewController panelViewController3 = PanelViewController.this;
                    boolean unused3 = panelViewController3.mIgnoreXTouchSlop = panelViewController3.isFullyCollapsed() || PanelViewController.this.shouldGestureIgnoreXTouchSlop(x, y);
                }
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked != 0) {
                    if (actionMasked != 1) {
                        if (actionMasked == 2) {
                            PanelViewController.this.addMovement(motionEvent);
                            float access$1700 = y - PanelViewController.this.mInitialTouchY;
                            if (Math.abs(access$1700) > PanelViewController.this.getTouchSlop(motionEvent) && (Math.abs(access$1700) > Math.abs(x - PanelViewController.this.mInitialTouchX) || PanelViewController.this.mIgnoreXTouchSlop)) {
                                boolean unused4 = PanelViewController.this.mTouchSlopExceeded = true;
                                if (PanelViewController.this.mGestureWaitForTouchSlop) {
                                    PanelViewController panelViewController4 = PanelViewController.this;
                                    if (!panelViewController4.mTracking && !panelViewController4.mCollapsedAndHeadsUpOnDown) {
                                        if (!PanelViewController.this.mJustPeeked && PanelViewController.this.mInitialOffsetOnTouch != 0.0f) {
                                            PanelViewController panelViewController5 = PanelViewController.this;
                                            panelViewController5.startExpandMotion(x, y, false, panelViewController5.mExpandedHeight);
                                            access$1700 = 0.0f;
                                        }
                                        PanelViewController.this.cancelHeightAnimator();
                                        PanelViewController.this.onTrackingStarted();
                                    }
                                }
                            }
                            float max = Math.max(0.0f, PanelViewController.this.mInitialOffsetOnTouch + access$1700);
                            if (max > PanelViewController.this.mPeekHeight) {
                                if (PanelViewController.this.mPeekAnimator != null) {
                                    PanelViewController.this.mPeekAnimator.cancel();
                                }
                                boolean unused5 = PanelViewController.this.mJustPeeked = false;
                            } else if (PanelViewController.this.mPeekAnimator == null && PanelViewController.this.mJustPeeked) {
                                PanelViewController panelViewController6 = PanelViewController.this;
                                float unused6 = panelViewController6.mInitialOffsetOnTouch = panelViewController6.mExpandedHeight;
                                float unused7 = PanelViewController.this.mInitialTouchY = y;
                                PanelViewController panelViewController7 = PanelViewController.this;
                                float unused8 = panelViewController7.mMinExpandHeight = panelViewController7.mExpandedHeight;
                                boolean unused9 = PanelViewController.this.mJustPeeked = false;
                            }
                            float max2 = Math.max(max, PanelViewController.this.mMinExpandHeight);
                            if ((-access$1700) >= ((float) PanelViewController.this.getFalsingThreshold())) {
                                boolean unused10 = PanelViewController.this.mTouchAboveFalsingThreshold = true;
                                PanelViewController panelViewController8 = PanelViewController.this;
                                boolean unused11 = panelViewController8.mUpwardsWhenThresholdReached = panelViewController8.isDirectionUpwards(x, y);
                            }
                            if (!PanelViewController.this.mJustPeeked && ((!PanelViewController.this.mGestureWaitForTouchSlop || PanelViewController.this.mTracking) && !PanelViewController.this.isTrackingBlocked() && PanelViewController.this.isStatusBarExpandable())) {
                                PanelViewController.this.setExpandedHeightInternal(max2);
                            }
                        } else if (actionMasked != 3) {
                            if (actionMasked != 5) {
                                if (actionMasked == 6 && PanelViewController.this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                                    int i = motionEvent.getPointerId(0) != pointerId ? 0 : 1;
                                    float y2 = motionEvent.getY(i);
                                    float x2 = motionEvent.getX(i);
                                    int unused12 = PanelViewController.this.mTrackingPointer = motionEvent.getPointerId(i);
                                    PanelViewController panelViewController9 = PanelViewController.this;
                                    panelViewController9.startExpandMotion(x2, y2, true, panelViewController9.mExpandedHeight);
                                }
                            } else if (PanelViewController.this.mStatusBarStateController.getState() == 1) {
                                boolean unused13 = PanelViewController.this.mMotionAborted = true;
                                PanelViewController.this.endMotionEvent(motionEvent, x, y, true);
                                return false;
                            }
                        }
                    }
                    PanelViewController.this.addMovement(motionEvent);
                    PanelViewController.this.endMotionEvent(motionEvent, x, y, false);
                } else {
                    PanelViewController panelViewController10 = PanelViewController.this;
                    panelViewController10.startExpandMotion(x, y, false, panelViewController10.mExpandedHeight);
                    boolean unused14 = PanelViewController.this.mJustPeeked = false;
                    float unused15 = PanelViewController.this.mMinExpandHeight = 0.0f;
                    PanelViewController panelViewController11 = PanelViewController.this;
                    boolean unused16 = panelViewController11.mPanelClosedOnDown = panelViewController11.isFullyCollapsed();
                    boolean unused17 = PanelViewController.this.mHasLayoutedSinceDown = false;
                    boolean unused18 = PanelViewController.this.mUpdateFlingOnLayout = false;
                    boolean unused19 = PanelViewController.this.mMotionAborted = false;
                    PanelViewController panelViewController12 = PanelViewController.this;
                    boolean unused20 = panelViewController12.mPeekTouching = panelViewController12.mPanelClosedOnDown;
                    PanelViewController.this.mDownTime = SystemClock.uptimeMillis();
                    boolean unused21 = PanelViewController.this.mTouchAboveFalsingThreshold = false;
                    PanelViewController panelViewController13 = PanelViewController.this;
                    boolean unused22 = panelViewController13.mCollapsedAndHeadsUpOnDown = panelViewController13.isFullyCollapsed() && PanelViewController.this.mHeadsUpManager.hasPinnedHeadsUp();
                    PanelViewController.this.addMovement(motionEvent);
                    if (!PanelViewController.this.mGestureWaitForTouchSlop || ((PanelViewController.this.mHeightAnimator != null && !PanelViewController.this.mHintAnimationRunning) || PanelViewController.this.mPeekAnimator != null)) {
                        PanelViewController panelViewController14 = PanelViewController.this;
                        boolean unused23 = panelViewController14.mTouchSlopExceeded = (panelViewController14.mHeightAnimator != null && !PanelViewController.this.mHintAnimationRunning) || PanelViewController.this.mPeekAnimator != null || PanelViewController.this.mTouchSlopExceededBeforeDown;
                        PanelViewController.this.cancelHeightAnimator();
                        PanelViewController.this.cancelPeek();
                        PanelViewController.this.onTrackingStarted();
                    }
                    if (PanelViewController.this.isFullyCollapsed() && !PanelViewController.this.mHeadsUpManager.hasPinnedHeadsUp() && !PanelViewController.this.mStatusBar.isBouncerShowing()) {
                        PanelViewController.this.startOpening(motionEvent);
                    }
                }
                if (!PanelViewController.this.mGestureWaitForTouchSlop || PanelViewController.this.mTracking) {
                    return true;
                }
                return false;
            } else {
                if (motionEvent.getAction() == 1) {
                    PanelViewController.this.expand(true);
                }
                return true;
            }
        }
    }

    public class OnLayoutChangeListener implements View.OnLayoutChangeListener {
        public OnLayoutChangeListener() {
        }

        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            PanelViewController.this.mStatusBar.onPanelLaidOut();
            PanelViewController.this.requestPanelHeightUpdate();
            boolean unused = PanelViewController.this.mHasLayoutedSinceDown = true;
            if (PanelViewController.this.mUpdateFlingOnLayout) {
                PanelViewController.this.abortAnimations();
                PanelViewController panelViewController = PanelViewController.this;
                panelViewController.fling(panelViewController.mUpdateFlingVelocity, true);
                boolean unused2 = PanelViewController.this.mUpdateFlingOnLayout = false;
            }
        }
    }

    public class OnConfigurationChangedListener implements PanelView.OnConfigurationChangedListener {
        public OnConfigurationChangedListener() {
        }

        public void onConfigurationChanged(Configuration configuration) {
            PanelViewController.this.loadDimens();
        }
    }
}
