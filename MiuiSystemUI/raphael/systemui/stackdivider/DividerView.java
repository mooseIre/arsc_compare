package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.SystemUICompat;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.component.ExitMultiModeEvent;
import com.android.systemui.recents.events.ui.RecentsGrowingEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.stackdivider.DividerSnapAlgorithm;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.stackdivider.events.StoppedDragingEvent;
import com.android.systemui.statusbar.FlingAnimationUtils;

public class DividerView extends FrameLayout implements View.OnTouchListener, ViewTreeObserver.OnComputeInternalInsetsListener {
    private static final PathInterpolator DIM_INTERPOLATOR = new PathInterpolator(0.23f, 0.87f, 0.52f, -0.11f);
    private static final Interpolator IME_ADJUST_INTERPOLATOR = new PathInterpolator(0.2f, 0.0f, 0.1f, 1.0f);
    private static final PathInterpolator SLOWDOWN_INTERPOLATOR = new PathInterpolator(0.5f, 1.0f, 0.5f, 1.0f);
    private boolean mAdjustedForIme;
    private View mBackground;
    private boolean mBackgroundLifted;
    /* access modifiers changed from: private */
    public ValueAnimator mCurrentAnimator;
    private int mCurrentTouchAction;
    private final Display mDefaultDisplay;
    private int mDisplayHeight;
    private final Rect mDisplayRect;
    private int mDisplayRotation;
    private int mDisplayWidth;
    private int mDividerInsets;
    private int mDividerSize;
    private int mDividerWindowWidth;
    /* access modifiers changed from: private */
    public int mDockSide;
    private final Rect mDockedInsetRect;
    private final Rect mDockedRect;
    /* access modifiers changed from: private */
    public boolean mDockedStackMinimized;
    private final Rect mDockedTaskRect;
    /* access modifiers changed from: private */
    public boolean mEntranceAnimationRunning;
    /* access modifiers changed from: private */
    public boolean mExitAnimationRunning;
    /* access modifiers changed from: private */
    public int mExitStartPosition;
    private FlingAnimationUtils mFlingAnimationUtils;
    private GestureDetector mGestureDetector;
    private boolean mGrowRecents;
    private DividerHandleView mHandle;
    private final View.AccessibilityDelegate mHandleDelegate;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mHomeStackResizable;
    /* access modifiers changed from: private */
    public boolean mIsInMinimizeInteraction;
    private final Rect mLastResizeRect;
    /* access modifiers changed from: private */
    public int mLongPressEntraceAnimDuration;
    private MinimizedDockShadow mMinimizedShadow;
    private DividerSnapAlgorithm mMinimizedSnapAlgorithm;
    private boolean mMoving;
    private final Rect mOtherInsetRect;
    private final Rect mOtherRect;
    private final Rect mOtherTaskRect;
    private boolean mRemoved;
    private final Runnable mResetBackgroundRunnable;
    /* access modifiers changed from: private */
    public DividerSnapAlgorithm mSnapAlgorithm;
    private DividerSnapAlgorithm.SnapTarget mSnapTargetBeforeMinimized;
    private final Rect mStableInsets;
    private int mStartPosition;
    private int mStartX;
    private int mStartY;
    private DividerState mState;
    private final int[] mTempInt2;
    private int mTouchElevation;
    private int mTouchSlop;
    /* access modifiers changed from: private */
    public boolean mUnDockByUndockingTaskEvent;
    private VelocityTracker mVelocityTracker;
    private DividerWindowManager mWindowManager;
    /* access modifiers changed from: private */
    public final WindowManagerProxy mWindowManagerProxy;

    private static boolean dockSideBottomRight(int i) {
        return i == 4 || i == 3;
    }

    /* access modifiers changed from: private */
    public static boolean dockSideTopLeft(int i) {
        return i == 2 || i == 1;
    }

    public DividerView(Context context) {
        this(context, (AttributeSet) null);
    }

    public DividerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DividerView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public DividerView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTempInt2 = new int[2];
        this.mDockedRect = new Rect();
        this.mDockedTaskRect = new Rect();
        this.mOtherTaskRect = new Rect();
        this.mOtherRect = new Rect();
        this.mDockedInsetRect = new Rect();
        this.mOtherInsetRect = new Rect();
        this.mLastResizeRect = new Rect();
        this.mDisplayRect = new Rect();
        this.mWindowManagerProxy = WindowManagerProxy.getInstance();
        this.mStableInsets = new Rect();
        this.mHandler = new Handler();
        this.mUnDockByUndockingTaskEvent = false;
        this.mHandleDelegate = new View.AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (DividerView.this.isHorizontalDivision()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_full, DividerView.this.mContext.getString(R.string.accessibility_action_divider_top_full)));
                    if (DividerView.this.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_70, DividerView.this.mContext.getString(R.string.accessibility_action_divider_top_70)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_50, DividerView.this.mContext.getString(R.string.accessibility_action_divider_top_50)));
                    if (DividerView.this.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_30, DividerView.this.mContext.getString(R.string.accessibility_action_divider_top_30)));
                    }
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_rb_full, DividerView.this.mContext.getString(R.string.accessibility_action_divider_bottom_full)));
                    return;
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_full, DividerView.this.mContext.getString(R.string.accessibility_action_divider_left_full)));
                if (DividerView.this.mSnapAlgorithm.isFirstSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_70, DividerView.this.mContext.getString(R.string.accessibility_action_divider_left_70)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_50, DividerView.this.mContext.getString(R.string.accessibility_action_divider_left_50)));
                if (DividerView.this.mSnapAlgorithm.isLastSplitTargetAvailable()) {
                    accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_tl_30, DividerView.this.mContext.getString(R.string.accessibility_action_divider_left_30)));
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.action_move_rb_full, DividerView.this.mContext.getString(R.string.accessibility_action_divider_right_full)));
            }

            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                DividerSnapAlgorithm.SnapTarget dismissStartTarget;
                int currentPosition = DividerView.this.getCurrentPosition();
                switch (i) {
                    case R.id.action_move_rb_full:
                        dismissStartTarget = DividerView.this.mSnapAlgorithm.getDismissStartTarget();
                        break;
                    case R.id.action_move_tl_30:
                        dismissStartTarget = DividerView.this.mSnapAlgorithm.getFirstSplitTarget();
                        break;
                    case R.id.action_move_tl_50:
                        dismissStartTarget = DividerView.this.mSnapAlgorithm.getMiddleTarget();
                        break;
                    case R.id.action_move_tl_70:
                        dismissStartTarget = DividerView.this.mSnapAlgorithm.getLastSplitTarget();
                        break;
                    case R.id.action_move_tl_full:
                        dismissStartTarget = DividerView.this.mSnapAlgorithm.getDismissEndTarget();
                        break;
                    default:
                        dismissStartTarget = null;
                        break;
                }
                DividerSnapAlgorithm.SnapTarget snapTarget = dismissStartTarget;
                if (snapTarget == null) {
                    return super.performAccessibilityAction(view, i, bundle);
                }
                DividerView.this.startDragging(true, false);
                DividerView.this.stopDragging(currentPosition, snapTarget, 250, Interpolators.FAST_OUT_SLOW_IN);
                return true;
            }
        };
        this.mResetBackgroundRunnable = new Runnable() {
            public void run() {
                DividerView.this.resetBackground();
            }
        };
        this.mDefaultDisplay = ((DisplayManager) this.mContext.getSystemService("display")).getDisplay(0);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mHandle = (DividerHandleView) findViewById(R.id.docked_divider_handle);
        this.mBackground = findViewById(R.id.docked_divider_background);
        this.mMinimizedShadow = (MinimizedDockShadow) findViewById(R.id.minimized_dock_shadow);
        this.mHandle.setOnTouchListener(this);
        this.mDividerWindowWidth = getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_thickness);
        this.mDividerInsets = getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_insets);
        this.mDividerSize = this.mDividerWindowWidth - (this.mDividerInsets * 2);
        this.mTouchElevation = getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_lift_elevation);
        this.mLongPressEntraceAnimDuration = getResources().getInteger(R.integer.long_press_dock_anim_duration);
        this.mGrowRecents = getResources().getBoolean(R.bool.recents_grow_in_multiwindow);
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        this.mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.3f);
        updateDisplayInfo();
        int i = getResources().getConfiguration().orientation;
        getViewTreeObserver().addOnComputeInternalInsetsListener(this);
        this.mHandle.setAccessibilityDelegate(this.mHandleDelegate);
        this.mGestureDetector = new GestureDetector(this.mContext, new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent motionEvent) {
                DividerView.this.updateDockSide();
                SystemServicesProxy systemServices = Recents.getSystemServices();
                if (DividerView.this.mDockSide == -1 || systemServices.isRecentsActivityVisible()) {
                    return false;
                }
                DividerView.this.mWindowManagerProxy.swapTasks();
                return true;
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mHomeStackResizable && this.mDockSide != -1 && !this.mIsInMinimizeInteraction) {
            saveSnapTargetBeforeMinimized(this.mSnapTargetBeforeMinimized);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDividerRemoved() {
        this.mRemoved = true;
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        if (!(this.mStableInsets.left == windowInsets.getStableInsetLeft() && this.mStableInsets.top == windowInsets.getStableInsetTop() && this.mStableInsets.right == windowInsets.getStableInsetRight() && this.mStableInsets.bottom == windowInsets.getStableInsetBottom())) {
            this.mStableInsets.set(windowInsets.getStableInsetLeft(), windowInsets.getStableInsetTop(), windowInsets.getStableInsetRight(), windowInsets.getStableInsetBottom());
            if (!(this.mSnapAlgorithm == null && this.mMinimizedSnapAlgorithm == null)) {
                this.mSnapAlgorithm = null;
                this.mMinimizedSnapAlgorithm = null;
                initializeSnapAlgorithm();
            }
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        super.onLayout(z, i, i2, i3, i4);
        int i6 = this.mDockSide;
        int i7 = 0;
        if (i6 == 2) {
            i7 = this.mBackground.getTop();
            i5 = 0;
        } else if (i6 == 1) {
            i5 = this.mBackground.getLeft();
        } else {
            i5 = i6 == 3 ? this.mBackground.getRight() - this.mMinimizedShadow.getWidth() : 0;
        }
        MinimizedDockShadow minimizedDockShadow = this.mMinimizedShadow;
        minimizedDockShadow.layout(i5, i7, minimizedDockShadow.getMeasuredWidth() + i5, this.mMinimizedShadow.getMeasuredHeight() + i7);
        if (z) {
            this.mWindowManagerProxy.setTouchRegion(new Rect(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom()));
        }
    }

    public void injectDependencies(DividerWindowManager dividerWindowManager, DividerState dividerState) {
        this.mWindowManager = dividerWindowManager;
        this.mState = dividerState;
        if (this.mStableInsets.isEmpty()) {
            try {
                SystemUICompat.getStableInsets(this.mStableInsets);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (this.mState.mRatioPositionBeforeMinimized == 0.0f) {
            this.mSnapTargetBeforeMinimized = this.mSnapAlgorithm.getMiddleTarget();
        } else {
            repositionSnapTargetBeforeMinimized();
        }
    }

    public WindowManagerProxy getWindowManagerProxy() {
        return this.mWindowManagerProxy;
    }

    public Rect getNonMinimizedSplitScreenSecondaryBounds() {
        calculateBoundsForPosition(this.mSnapTargetBeforeMinimized.position, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
        Rect rect = this.mOtherTaskRect;
        int i = rect.bottom;
        Rect rect2 = this.mStableInsets;
        rect.bottom = i - rect2.bottom;
        int i2 = this.mDockSide;
        if (i2 == 1) {
            rect.top += rect2.top;
            rect.right -= rect2.right;
        } else if (i2 == 3) {
            rect.top += rect2.top;
            rect.left += rect2.left;
        }
        return this.mOtherTaskRect;
    }

    public Rect getMiddleSplitScreenSecondaryBounds() {
        Rect rect = new Rect();
        calculateBoundsForPosition(this.mSnapAlgorithm.getMiddleTarget().position, 4, rect);
        return rect;
    }

    public boolean startDragging(boolean z, boolean z2) {
        cancelFlingAnimation();
        if (z2) {
            this.mHandle.setTouching(true, z);
        }
        this.mDockSide = this.mWindowManagerProxy.getDockSide();
        if (this.mDisplayRotation != this.mDefaultDisplay.getRotation()) {
            updateDisplayInfo();
        }
        initializeSnapAlgorithm();
        this.mWindowManagerProxy.setResizing(true);
        if (z2) {
            this.mWindowManager.setSlippery(false);
            liftBackground();
        }
        RecentsEventBus.getDefault().send(new StartedDragingEvent());
        if (this.mDockSide != -1) {
            return true;
        }
        return false;
    }

    public void stopDragging(int i, float f, boolean z, boolean z2) {
        this.mHandle.setTouching(false, true);
        fling(i, f, z, z2);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, Interpolator interpolator) {
        stopDragging(i, snapTarget, j, 0, 0, interpolator);
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, Interpolator interpolator, long j2) {
        stopDragging(i, snapTarget, j, 0, j2, interpolator);
    }

    public void stopDragging(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, long j2, long j3, Interpolator interpolator) {
        this.mHandle.setTouching(false, true);
        flingTo(i, snapTarget, j, j2, j3, interpolator);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    private void stopDragging() {
        this.mHandle.setTouching(false, true);
        this.mWindowManager.setSlippery(true);
        releaseBackground();
    }

    /* access modifiers changed from: private */
    public void updateDockSide() {
        this.mDockSide = this.mWindowManagerProxy.getDockSide();
        this.mMinimizedShadow.setDockSide(this.mDockSide);
    }

    private void initializeSnapAlgorithm() {
        try {
            SystemUICompat.getStableInsets(this.mStableInsets);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (this.mSnapAlgorithm == null) {
            this.mSnapAlgorithm = new DividerSnapAlgorithm(getContext().getResources(), this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize, isHorizontalDivision(), this.mStableInsets);
            if (this.mSnapTargetBeforeMinimized != null) {
                this.mSnapTargetBeforeMinimized = this.mSnapAlgorithm.getMiddleTarget();
            }
        }
        if (this.mMinimizedSnapAlgorithm == null) {
            this.mMinimizedSnapAlgorithm = new DividerSnapAlgorithm(getContext().getResources(), this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize, isHorizontalDivision(), this.mStableInsets, this.mDockSide, this.mDockedStackMinimized && this.mHomeStackResizable);
        }
    }

    public DividerSnapAlgorithm getSnapAlgorithm() {
        initializeSnapAlgorithm();
        if (!this.mDockedStackMinimized || !this.mHomeStackResizable) {
            return this.mSnapAlgorithm;
        }
        return this.mMinimizedSnapAlgorithm;
    }

    public int getCurrentPosition() {
        int i;
        int i2;
        getLocationOnScreen(this.mTempInt2);
        if (isHorizontalDivision()) {
            i = this.mTempInt2[1];
            i2 = this.mDividerInsets;
        } else {
            i = this.mTempInt2[0];
            i2 = this.mDividerInsets;
        }
        return i + i2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x001c, code lost:
        if (r6 != 3) goto L_0x00b6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouch(android.view.View r6, android.view.MotionEvent r7) {
        /*
            r5 = this;
            r5.convertToScreenCoordinates(r7)
            android.view.GestureDetector r6 = r5.mGestureDetector
            r6.onTouchEvent(r7)
            int r6 = r7.getAction()
            r6 = r6 & 255(0xff, float:3.57E-43)
            r5.mCurrentTouchAction = r6
            int r6 = r5.mCurrentTouchAction
            r0 = 0
            r1 = 1
            if (r6 == 0) goto L_0x00b7
            if (r6 == r1) goto L_0x0084
            r2 = 2
            if (r6 == r2) goto L_0x0020
            r2 = 3
            if (r6 == r2) goto L_0x0084
            goto L_0x00b6
        L_0x0020:
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            r6.addMovement(r7)
            float r6 = r7.getX()
            int r6 = (int) r6
            float r7 = r7.getY()
            int r7 = (int) r7
            boolean r2 = r5.isHorizontalDivision()
            if (r2 == 0) goto L_0x0041
            int r2 = r5.mStartY
            int r2 = r7 - r2
            int r2 = java.lang.Math.abs(r2)
            int r3 = r5.mTouchSlop
            if (r2 > r3) goto L_0x0053
        L_0x0041:
            boolean r2 = r5.isHorizontalDivision()
            if (r2 != 0) goto L_0x0055
            int r2 = r5.mStartX
            int r2 = r6 - r2
            int r2 = java.lang.Math.abs(r2)
            int r3 = r5.mTouchSlop
            if (r2 <= r3) goto L_0x0055
        L_0x0053:
            r2 = r1
            goto L_0x0056
        L_0x0055:
            r2 = r0
        L_0x0056:
            boolean r3 = r5.mMoving
            if (r3 != 0) goto L_0x0062
            if (r2 == 0) goto L_0x0062
            r5.mStartX = r6
            r5.mStartY = r7
            r5.mMoving = r1
        L_0x0062:
            boolean r2 = r5.mMoving
            if (r2 == 0) goto L_0x00b6
            int r2 = r5.mDockSide
            r3 = -1
            if (r2 == r3) goto L_0x00b6
            com.android.systemui.stackdivider.DividerSnapAlgorithm r2 = r5.getSnapAlgorithm()
            int r3 = r5.mStartPosition
            r4 = 0
            com.android.systemui.stackdivider.DividerSnapAlgorithm$SnapTarget r0 = r2.calculateSnapTarget(r3, r4, r0)
            int r6 = r5.calculatePosition(r6, r7)
            int r7 = r5.mStartPosition
            boolean r2 = com.android.systemui.recents.misc.Utilities.isAndroidNorNewer()
            r5.resizeStack(r6, r7, r0, r2)
            goto L_0x00b6
        L_0x0084:
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            r6.addMovement(r7)
            float r6 = r7.getRawX()
            int r6 = (int) r6
            float r7 = r7.getRawY()
            int r7 = (int) r7
            android.view.VelocityTracker r2 = r5.mVelocityTracker
            r3 = 1000(0x3e8, float:1.401E-42)
            r2.computeCurrentVelocity(r3)
            int r6 = r5.calculatePosition(r6, r7)
            boolean r7 = r5.isHorizontalDivision()
            if (r7 == 0) goto L_0x00ab
            android.view.VelocityTracker r7 = r5.mVelocityTracker
            float r7 = r7.getYVelocity()
            goto L_0x00b1
        L_0x00ab:
            android.view.VelocityTracker r7 = r5.mVelocityTracker
            float r7 = r7.getXVelocity()
        L_0x00b1:
            r5.stopDragging((int) r6, (float) r7, (boolean) r0, (boolean) r1)
            r5.mMoving = r0
        L_0x00b6:
            return r1
        L_0x00b7:
            android.view.VelocityTracker r6 = android.view.VelocityTracker.obtain()
            r5.mVelocityTracker = r6
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            r6.addMovement(r7)
            float r6 = r7.getX()
            int r6 = (int) r6
            r5.mStartX = r6
            float r6 = r7.getY()
            int r6 = (int) r6
            r5.mStartY = r6
            boolean r6 = r5.startDragging(r1, r1)
            if (r6 != 0) goto L_0x00d9
            r5.stopDragging()
        L_0x00d9:
            int r7 = r5.getCurrentPosition()
            r5.mStartPosition = r7
            r5.mMoving = r0
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.stackdivider.DividerView.onTouch(android.view.View, android.view.MotionEvent):boolean");
    }

    private void logResizeEvent(DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget == this.mSnapAlgorithm.getDismissStartTarget()) {
            MetricsLogger.action(this.mContext, 390, dockSideTopLeft(this.mDockSide) ? 1 : 0);
        } else if (snapTarget == this.mSnapAlgorithm.getDismissEndTarget()) {
            MetricsLogger.action(this.mContext, 390, dockSideBottomRight(this.mDockSide) ? 1 : 0);
        } else if (snapTarget == this.mSnapAlgorithm.getMiddleTarget()) {
            MetricsLogger.action(this.mContext, 389, 0);
        } else {
            int i = 1;
            if (snapTarget == this.mSnapAlgorithm.getFirstSplitTarget()) {
                Context context = this.mContext;
                if (!dockSideTopLeft(this.mDockSide)) {
                    i = 2;
                }
                MetricsLogger.action(context, 389, i);
            } else if (snapTarget == this.mSnapAlgorithm.getLastSplitTarget()) {
                Context context2 = this.mContext;
                if (dockSideTopLeft(this.mDockSide)) {
                    i = 2;
                }
                MetricsLogger.action(context2, 389, i);
            }
        }
    }

    private void convertToScreenCoordinates(MotionEvent motionEvent) {
        motionEvent.setLocation(motionEvent.getRawX(), motionEvent.getRawY());
    }

    private void fling(int i, float f, boolean z, boolean z2) {
        DividerSnapAlgorithm snapAlgorithm = getSnapAlgorithm();
        DividerSnapAlgorithm.SnapTarget calculateSnapTarget = snapAlgorithm.calculateSnapTarget(i, f);
        if (z && calculateSnapTarget == snapAlgorithm.getDismissStartTarget()) {
            calculateSnapTarget = snapAlgorithm.getFirstSplitTarget();
        }
        if (z2) {
            logResizeEvent(calculateSnapTarget);
        }
        ValueAnimator flingAnimator = getFlingAnimator(i, calculateSnapTarget, 0);
        this.mFlingAnimationUtils.apply((Animator) flingAnimator, (float) i, (float) calculateSnapTarget.position, f);
        flingAnimator.start();
    }

    private void flingTo(int i, DividerSnapAlgorithm.SnapTarget snapTarget, long j, long j2, long j3, Interpolator interpolator) {
        ValueAnimator flingAnimator = getFlingAnimator(i, snapTarget, j3);
        flingAnimator.setDuration(j);
        flingAnimator.setStartDelay(j2);
        flingAnimator.setInterpolator(interpolator);
        flingAnimator.start();
    }

    private ValueAnimator getFlingAnimator(int i, final DividerSnapAlgorithm.SnapTarget snapTarget, long j) {
        if (this.mCurrentAnimator != null) {
            cancelFlingAnimation();
            updateDockSide();
        }
        final boolean z = snapTarget.flag == 0;
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{i, snapTarget.position});
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int i;
                DividerView dividerView = DividerView.this;
                int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                if (!z || valueAnimator.getAnimatedFraction() != 1.0f) {
                    i = snapTarget.taskPosition;
                } else {
                    i = Integer.MAX_VALUE;
                }
                dividerView.resizeStack(intValue, i, snapTarget, Utilities.isAndroidNorNewer());
            }
        });
        final AnonymousClass5 r10 = new Runnable() {
            public void run() {
                DividerView.this.commitSnapFlags(snapTarget);
                DividerView.this.mWindowManagerProxy.setResizing(false);
                int unused = DividerView.this.mDockSide = -1;
                ValueAnimator unused2 = DividerView.this.mCurrentAnimator = null;
                boolean unused3 = DividerView.this.mEntranceAnimationRunning = false;
                boolean unused4 = DividerView.this.mExitAnimationRunning = false;
                RecentsEventBus.getDefault().send(new StoppedDragingEvent());
                if (DividerView.this.mHomeStackResizable && !DividerView.this.mIsInMinimizeInteraction) {
                    DividerSnapAlgorithm.SnapTarget snapTarget = snapTarget;
                    if (snapTarget.position < 0) {
                        snapTarget = DividerView.this.mSnapAlgorithm.getMiddleTarget();
                    }
                    if (snapTarget.position != DividerView.this.mSnapAlgorithm.getDismissEndTarget().position && snapTarget.position != DividerView.this.mSnapAlgorithm.getDismissStartTarget().position) {
                        DividerView.this.saveSnapTargetBeforeMinimized(snapTarget);
                    }
                }
            }
        };
        final AnonymousClass6 r9 = new Runnable() {
            public void run() {
                if (!DividerView.this.mDockedStackMinimized && DividerView.this.mIsInMinimizeInteraction) {
                    boolean unused = DividerView.this.mIsInMinimizeInteraction = false;
                }
            }
        };
        final DividerSnapAlgorithm.SnapTarget snapTarget2 = snapTarget;
        final long j2 = j;
        ofInt.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                int i;
                boolean z;
                DividerView dividerView = DividerView.this;
                DividerSnapAlgorithm.SnapTarget snapTarget = snapTarget2;
                int i2 = snapTarget.position;
                if (z) {
                    i = Integer.MAX_VALUE;
                } else {
                    i = snapTarget.taskPosition;
                }
                dividerView.resizeStack(i2, i, snapTarget2, true);
                if (j2 == 0 || z) {
                    if (!this.mCancelled) {
                        r9.run();
                    }
                    r10.run();
                    return;
                }
                if (!(z = this.mCancelled)) {
                    DividerView.this.mHandler.postDelayed(r9, j2);
                }
                DividerView.this.mHandler.postDelayed(r10, j2);
            }
        });
        this.mCurrentAnimator = ofInt;
        return ofInt;
    }

    private void cancelFlingAnimation() {
        ValueAnimator valueAnimator = this.mCurrentAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    /* access modifiers changed from: private */
    public void commitSnapFlags(DividerSnapAlgorithm.SnapTarget snapTarget) {
        int i;
        int i2;
        String str;
        int i3 = snapTarget.flag;
        if (i3 == 0) {
            if (this.mSnapAlgorithm.getFirstSplitTarget() == snapTarget) {
                str = "firstSplitTarget";
            } else if (this.mSnapAlgorithm.getMiddleTarget() == snapTarget) {
                str = "middleTarget";
            } else {
                str = this.mSnapAlgorithm.getLastSplitTarget() == snapTarget ? "lastSplitTarget" : "otherTarget";
            }
            RecentsPushEventHelper.sendResizeStackEvent(str);
            return;
        }
        boolean z = true;
        if (i3 != 1 ? !((i = this.mDockSide) == 3 || i == 4) : !((i2 = this.mDockSide) == 1 || i2 == 2)) {
            z = false;
        }
        if (z) {
            this.mWindowManagerProxy.dismissDockedStack();
        } else {
            this.mWindowManagerProxy.maximizeDockedStack();
        }
        RecentsEventBus.getDefault().send(new ExitMultiModeEvent());
        RecentsPushEventHelper.sendExitMultiWindowEvent(this.mUnDockByUndockingTaskEvent ? "exitMultiWindowButton" : "slippery");
        this.mUnDockByUndockingTaskEvent = false;
        this.mWindowManagerProxy.setResizeDimLayer(false, -1, 0, 0.0f);
    }

    private void liftBackground() {
        if (!this.mBackgroundLifted) {
            if (isHorizontalDivision()) {
                this.mBackground.animate().scaleY(1.4f);
            } else {
                this.mBackground.animate().scaleX(1.4f);
            }
            this.mBackground.animate().setInterpolator(Interpolators.TOUCH_RESPONSE).setDuration(150).translationZ((float) this.mTouchElevation).start();
            this.mHandle.animate().setInterpolator(Interpolators.TOUCH_RESPONSE).setDuration(150).translationZ((float) this.mTouchElevation).start();
            this.mBackgroundLifted = true;
        }
    }

    private void releaseBackground() {
        if (this.mBackgroundLifted) {
            this.mBackground.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(200).translationZ(0.0f).scaleX(1.0f).scaleY(1.0f).start();
            this.mHandle.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(200).translationZ(0.0f).start();
            this.mBackgroundLifted = false;
        }
    }

    public void setMinimizedDockStack(boolean z, boolean z2) {
        float f;
        if (Utilities.isAndroidNorNewer()) {
            this.mHomeStackResizable = z2;
            updateDockSide();
            float f2 = 0.0f;
            if (!z) {
                resetBackground();
            } else if (!z2) {
                int i = this.mDockSide;
                if (i == 2) {
                    this.mBackground.setPivotY(0.0f);
                    this.mBackground.setScaleY(0.0f);
                } else if (i == 1 || i == 3) {
                    View view = this.mBackground;
                    if (this.mDockSide == 1) {
                        f = 0.0f;
                    } else {
                        f = (float) view.getWidth();
                    }
                    view.setPivotX(f);
                    this.mBackground.setScaleX(0.0f);
                }
            }
            this.mMinimizedShadow.setAlpha(z ? 1.0f : 0.0f);
            if (!z2) {
                DividerHandleView dividerHandleView = this.mHandle;
                if (!z) {
                    f2 = 1.0f;
                }
                dividerHandleView.setAlpha(f2);
                this.mDockedStackMinimized = z;
            } else if (this.mDockedStackMinimized != z) {
                this.mDockedStackMinimized = z;
                if (this.mDisplayRotation != this.mDefaultDisplay.getRotation()) {
                    try {
                        SystemUICompat.getStableInsets(this.mStableInsets);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    repositionSnapTargetBeforeMinimized();
                    updateDisplayInfo();
                } else {
                    this.mMinimizedSnapAlgorithm = null;
                    initializeSnapAlgorithm();
                }
                if (this.mIsInMinimizeInteraction != z || this.mCurrentAnimator != null) {
                    cancelFlingAnimation();
                    if (z) {
                        requestLayout();
                        this.mIsInMinimizeInteraction = true;
                        resizeStack(this.mMinimizedSnapAlgorithm.getMiddleTarget());
                        return;
                    }
                    resizeStack(this.mSnapTargetBeforeMinimized);
                    this.mIsInMinimizeInteraction = false;
                }
            }
        }
    }

    public void setMinimizedDockStack(boolean z, long j, boolean z2) {
        int i;
        DividerSnapAlgorithm.SnapTarget snapTarget;
        float f;
        if (Utilities.isAndroidNorNewer()) {
            this.mHomeStackResizable = z2;
            updateDockSide();
            if (!z2) {
                float f2 = 1.0f;
                this.mMinimizedShadow.animate().alpha(z ? 1.0f : 0.0f).setInterpolator(Interpolators.ALPHA_IN).setDuration(j).start();
                this.mHandle.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(j).alpha(z ? 0.0f : 1.0f).start();
                int i2 = this.mDockSide;
                if (i2 == 2) {
                    this.mBackground.setPivotY(0.0f);
                    ViewPropertyAnimator animate = this.mBackground.animate();
                    if (z) {
                        f2 = 0.0f;
                    }
                    animate.scaleY(f2);
                } else if (i2 == 1 || i2 == 3) {
                    View view = this.mBackground;
                    if (this.mDockSide == 1) {
                        f = 0.0f;
                    } else {
                        f = (float) view.getWidth();
                    }
                    view.setPivotX(f);
                    ViewPropertyAnimator animate2 = this.mBackground.animate();
                    if (z) {
                        f2 = 0.0f;
                    }
                    animate2.scaleX(f2);
                }
                this.mDockedStackMinimized = z;
            } else if (this.mDockedStackMinimized != z) {
                this.mIsInMinimizeInteraction = true;
                this.mMinimizedSnapAlgorithm = null;
                this.mDockedStackMinimized = z;
                initializeSnapAlgorithm();
                if (z) {
                    i = this.mSnapTargetBeforeMinimized.position;
                } else {
                    i = getCurrentPosition();
                }
                int i3 = i;
                if (z) {
                    snapTarget = this.mMinimizedSnapAlgorithm.getMiddleTarget();
                } else {
                    snapTarget = this.mSnapTargetBeforeMinimized;
                }
                stopDragging(i3, snapTarget, j, Interpolators.FAST_OUT_SLOW_IN, 0);
                setAdjustedForIme(false, j);
            }
            if (!z) {
                this.mBackground.animate().withEndAction(this.mResetBackgroundRunnable);
            }
            this.mBackground.animate().setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setDuration(j).start();
        }
    }

    public void setAdjustedForIme(boolean z) {
        updateDockSide();
        this.mHandle.setAlpha(z ? 0.0f : 1.0f);
        if (!z) {
            resetBackground();
        } else if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            this.mBackground.setScaleY(0.5f);
        }
        this.mAdjustedForIme = z;
    }

    public void setAdjustedForIme(boolean z, long j) {
        updateDockSide();
        float f = 1.0f;
        this.mHandle.animate().setInterpolator(IME_ADJUST_INTERPOLATOR).setDuration(j).alpha(z ? 0.0f : 1.0f).start();
        if (this.mDockSide == 2) {
            this.mBackground.setPivotY(0.0f);
            ViewPropertyAnimator animate = this.mBackground.animate();
            if (z) {
                f = 0.5f;
            }
            animate.scaleY(f);
        }
        if (!z) {
            this.mBackground.animate().withEndAction(this.mResetBackgroundRunnable);
        }
        this.mBackground.animate().setInterpolator(IME_ADJUST_INTERPOLATOR).setDuration(j).start();
        this.mAdjustedForIme = z;
    }

    /* access modifiers changed from: private */
    public void saveSnapTargetBeforeMinimized(DividerSnapAlgorithm.SnapTarget snapTarget) {
        this.mSnapTargetBeforeMinimized = snapTarget;
        this.mState.mRatioPositionBeforeMinimized = ((float) snapTarget.position) / ((float) (isHorizontalDivision() ? this.mDisplayHeight : this.mDisplayWidth));
    }

    /* access modifiers changed from: private */
    public void resetBackground() {
        View view = this.mBackground;
        view.setPivotX((float) (view.getWidth() / 2));
        View view2 = this.mBackground;
        view2.setPivotY((float) (view2.getHeight() / 2));
        this.mBackground.setScaleX(1.0f);
        this.mBackground.setScaleY(1.0f);
        this.mMinimizedShadow.setAlpha(0.0f);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateDisplayInfo();
    }

    public void notifyDockSideChanged(int i) {
        int i2 = this.mDockSide;
        this.mDockSide = i;
        this.mMinimizedShadow.setDockSide(this.mDockSide);
        requestLayout();
        try {
            SystemUICompat.getStableInsets(this.mStableInsets);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.mMinimizedSnapAlgorithm = null;
        initializeSnapAlgorithm();
        if ((i2 == 1 && this.mDockSide == 3) || (i2 == 3 && this.mDockSide == 1)) {
            repositionSnapTargetBeforeMinimized();
        }
        if (this.mHomeStackResizable && this.mDockedStackMinimized) {
            resizeStack(this.mMinimizedSnapAlgorithm.getMiddleTarget());
        }
    }

    private void repositionSnapTargetBeforeMinimized() {
        float f = this.mState.mRatioPositionBeforeMinimized;
        int i = isHorizontalDivision() ? this.mDisplayHeight : this.mDisplayWidth;
        this.mSnapAlgorithm = null;
        initializeSnapAlgorithm();
        this.mSnapTargetBeforeMinimized = this.mSnapAlgorithm.calculateNonDismissingSnapTarget((int) (f * ((float) i)));
    }

    private void updateDisplayInfo() {
        this.mDisplayRotation = this.mDefaultDisplay.getRotation();
        DisplayInfo displayInfo = new DisplayInfo();
        this.mDefaultDisplay.getDisplayInfo(displayInfo);
        this.mDisplayWidth = displayInfo.logicalWidth;
        this.mDisplayHeight = displayInfo.logicalHeight;
        this.mSnapAlgorithm = null;
        this.mMinimizedSnapAlgorithm = null;
        initializeSnapAlgorithm();
    }

    private int calculatePosition(int i, int i2) {
        return isHorizontalDivision() ? calculateYPosition(i2) : calculateXPosition(i);
    }

    public boolean isHorizontalDivision() {
        return getResources().getConfiguration().orientation == 1;
    }

    private int calculateXPosition(int i) {
        return (this.mStartPosition + i) - this.mStartX;
    }

    private int calculateYPosition(int i) {
        return (this.mStartPosition + i) - this.mStartY;
    }

    private void alignTopLeft(Rect rect, Rect rect2) {
        int width = rect2.width();
        int height = rect2.height();
        int i = rect.left;
        int i2 = rect.top;
        rect2.set(i, i2, width + i, height + i2);
    }

    private void alignBottomRight(Rect rect, Rect rect2) {
        int width = rect2.width();
        int height = rect2.height();
        int i = rect.right;
        int i2 = rect.bottom;
        rect2.set(i - width, i2 - height, i, i2);
    }

    public void calculateBoundsForPosition(int i, int i2, Rect rect) {
        DockedDividerUtils.calculateBoundsForPosition(i, i2, rect, this.mDisplayWidth, this.mDisplayHeight, this.mDividerSize);
    }

    private void resizeStack(DividerSnapAlgorithm.SnapTarget snapTarget) {
        int i = snapTarget.position;
        resizeStack(i, i, snapTarget);
    }

    public void resizeStack(int i, int i2, DividerSnapAlgorithm.SnapTarget snapTarget) {
        resizeStack(i, i2, snapTarget, true);
    }

    public void resizeStack(int i, int i2, DividerSnapAlgorithm.SnapTarget snapTarget, boolean z) {
        DividerSnapAlgorithm.SnapTarget snapTarget2;
        int i3 = i;
        int i4 = i2;
        DividerSnapAlgorithm.SnapTarget snapTarget3 = snapTarget;
        if (!this.mRemoved) {
            boolean z2 = true;
            boolean z3 = z;
            if (!Utilities.isAndroidNorNewer()) {
                this.mWindowManager.update(i3);
            }
            calculateBoundsForPosition(i3, this.mDockSide, this.mDockedRect);
            if (!this.mDockedRect.equals(this.mLastResizeRect) || this.mEntranceAnimationRunning) {
                if (this.mBackground.getZ() > 0.0f) {
                    this.mBackground.invalidate();
                }
                if (z3) {
                    this.mLastResizeRect.set(this.mDockedRect);
                }
                if (!this.mHomeStackResizable || !this.mIsInMinimizeInteraction || (snapTarget2 = this.mSnapTargetBeforeMinimized) == null) {
                    if (this.mEntranceAnimationRunning && i4 != Integer.MAX_VALUE) {
                        if (this.mCurrentAnimator != null) {
                            calculateBoundsForPosition(i4, this.mDockSide, this.mDockedTaskRect);
                        } else {
                            calculateBoundsForPosition(isHorizontalDivision() ? this.mDisplayHeight : this.mDisplayWidth, this.mDockSide, this.mDockedTaskRect);
                        }
                        calculateBoundsForPosition(i4, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                        this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, (Rect) null, this.mOtherTaskRect, (Rect) null, z3);
                    } else if (this.mExitAnimationRunning && i4 != Integer.MAX_VALUE) {
                        calculateBoundsForPosition(i4, this.mDockSide, this.mDockedTaskRect);
                        calculateBoundsForPosition(this.mExitStartPosition, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                        this.mOtherInsetRect.set(this.mOtherTaskRect);
                        applyExitAnimationParallax(this.mOtherTaskRect, i3);
                        this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, (Rect) null, this.mOtherTaskRect, this.mOtherInsetRect, z3);
                    } else if (i4 != Integer.MAX_VALUE) {
                        calculateBoundsForPosition(i3, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherRect);
                        int invertDockSide = DockedDividerUtils.invertDockSide(this.mDockSide);
                        int restrictDismissingTaskPosition = restrictDismissingTaskPosition(i4, this.mDockSide, snapTarget3);
                        int restrictDismissingTaskPosition2 = restrictDismissingTaskPosition(i4, invertDockSide, snapTarget3);
                        calculateBoundsForPosition(restrictDismissingTaskPosition, this.mDockSide, this.mDockedTaskRect);
                        calculateBoundsForPosition(restrictDismissingTaskPosition2, invertDockSide, this.mOtherTaskRect);
                        this.mDisplayRect.set(0, 0, this.mDisplayWidth, this.mDisplayHeight);
                        alignTopLeft(this.mDockedRect, this.mDockedTaskRect);
                        alignTopLeft(this.mOtherRect, this.mOtherTaskRect);
                        this.mDockedInsetRect.set(this.mDockedTaskRect);
                        this.mOtherInsetRect.set(this.mOtherTaskRect);
                        if (dockSideTopLeft(this.mDockSide)) {
                            alignTopLeft(this.mDisplayRect, this.mDockedInsetRect);
                            alignBottomRight(this.mDisplayRect, this.mOtherInsetRect);
                        } else {
                            alignBottomRight(this.mDisplayRect, this.mDockedInsetRect);
                            alignTopLeft(this.mDisplayRect, this.mOtherInsetRect);
                        }
                        DividerSnapAlgorithm.SnapTarget snapTarget4 = snapTarget;
                        int i5 = i;
                        applyDismissingParallax(this.mDockedTaskRect, this.mDockSide, snapTarget4, i5, restrictDismissingTaskPosition);
                        applyDismissingParallax(this.mOtherTaskRect, invertDockSide, snapTarget4, i5, restrictDismissingTaskPosition2);
                        this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, this.mDockedTaskRect, this.mDockedInsetRect, this.mOtherTaskRect, this.mOtherInsetRect, z3);
                    } else {
                        this.mWindowManagerProxy.resizeDockedStack(this.mDockedRect, (Rect) null, (Rect) null, (Rect) null, (Rect) null, z3);
                    }
                    DividerSnapAlgorithm.SnapTarget closestDismissTarget = getSnapAlgorithm().getClosestDismissTarget(i3);
                    float dimFraction = getDimFraction(i3, closestDismissTarget);
                    WindowManagerProxy windowManagerProxy = this.mWindowManagerProxy;
                    if (dimFraction == 0.0f) {
                        z2 = false;
                    }
                    windowManagerProxy.setResizeDimLayer(z2, getStackIdForDismissTarget(closestDismissTarget), getWindowingModeForDismissTarget(closestDismissTarget), dimFraction);
                    return;
                }
                calculateBoundsForPosition(snapTarget2.position, this.mDockSide, this.mDockedTaskRect);
                calculateBoundsForPosition(this.mSnapTargetBeforeMinimized.position, DockedDividerUtils.invertDockSide(this.mDockSide), this.mOtherTaskRect);
                if (this.mDockSide == 3) {
                    this.mDockedTaskRect.offset((Math.max(i3, this.mStableInsets.left - this.mDividerSize) - this.mDockedTaskRect.left) + this.mDividerSize, 0);
                }
                WindowManagerProxy windowManagerProxy2 = this.mWindowManagerProxy;
                Rect rect = this.mDockedRect;
                Rect rect2 = this.mDockedTaskRect;
                windowManagerProxy2.resizeDockedStack(rect, rect2, rect2, this.mOtherTaskRect, (Rect) null, z3);
            }
        }
    }

    private void applyExitAnimationParallax(Rect rect, int i) {
        int i2 = this.mDockSide;
        if (i2 == 2) {
            rect.offset(0, (int) (((float) (i - this.mExitStartPosition)) * 0.25f));
        } else if (i2 == 1) {
            rect.offset((int) (((float) (i - this.mExitStartPosition)) * 0.25f), 0);
        } else if (i2 == 3) {
            rect.offset((int) (((float) (this.mExitStartPosition - i)) * 0.25f), 0);
        }
    }

    private float getDimFraction(int i, DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (this.mEntranceAnimationRunning) {
            return 0.0f;
        }
        float interpolation = DIM_INTERPOLATOR.getInterpolation(Math.max(0.0f, Math.min(getSnapAlgorithm().calculateDismissingFraction(i), 1.0f)));
        return hasInsetsAtDismissTarget(snapTarget) ? interpolation * 0.8f : interpolation;
    }

    private boolean hasInsetsAtDismissTarget(DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (isHorizontalDivision()) {
            if (snapTarget == getSnapAlgorithm().getDismissStartTarget()) {
                if (this.mStableInsets.top != 0) {
                    return true;
                }
                return false;
            } else if (this.mStableInsets.bottom != 0) {
                return true;
            } else {
                return false;
            }
        } else if (snapTarget == getSnapAlgorithm().getDismissStartTarget()) {
            if (this.mStableInsets.left != 0) {
                return true;
            }
            return false;
        } else if (this.mStableInsets.right != 0) {
            return true;
        } else {
            return false;
        }
    }

    private int restrictDismissingTaskPosition(int i, int i2, DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget.flag != 1 || !dockSideTopLeft(i2)) {
            return (snapTarget.flag != 2 || !dockSideBottomRight(i2)) ? i : Math.min(this.mSnapAlgorithm.getLastSplitTarget().position, this.mStartPosition);
        }
        return Math.max(this.mSnapAlgorithm.getFirstSplitTarget().position, this.mStartPosition);
    }

    private void applyDismissingParallax(Rect rect, int i, DividerSnapAlgorithm.SnapTarget snapTarget, int i2, int i3) {
        DividerSnapAlgorithm.SnapTarget snapTarget2;
        int i4;
        float min = Math.min(1.0f, Math.max(0.0f, this.mSnapAlgorithm.calculateDismissingFraction(i2)));
        DividerSnapAlgorithm.SnapTarget snapTarget3 = null;
        if (i2 <= this.mSnapAlgorithm.getLastSplitTarget().position && dockSideTopLeft(i)) {
            snapTarget3 = this.mSnapAlgorithm.getDismissStartTarget();
            i4 = i3;
            snapTarget2 = this.mSnapAlgorithm.getFirstSplitTarget();
        } else if (i2 < this.mSnapAlgorithm.getLastSplitTarget().position || !dockSideBottomRight(i)) {
            i4 = 0;
            snapTarget2 = null;
        } else {
            snapTarget3 = this.mSnapAlgorithm.getDismissEndTarget();
            snapTarget2 = this.mSnapAlgorithm.getLastSplitTarget();
            i4 = snapTarget2.position;
        }
        if (snapTarget3 != null && min > 0.0f && isDismissing(snapTarget2, i2, i)) {
            int calculateParallaxDismissingFraction = (int) (((float) i4) + (calculateParallaxDismissingFraction(min, i) * ((float) (snapTarget3.position - snapTarget2.position))));
            int width = rect.width();
            int height = rect.height();
            if (i == 1) {
                rect.left = calculateParallaxDismissingFraction - width;
                rect.right = calculateParallaxDismissingFraction;
            } else if (i == 2) {
                rect.top = calculateParallaxDismissingFraction - height;
                rect.bottom = calculateParallaxDismissingFraction;
            } else if (i == 3) {
                int i5 = this.mDividerSize;
                rect.left = calculateParallaxDismissingFraction + i5;
                rect.right = calculateParallaxDismissingFraction + width + i5;
            } else if (i == 4) {
                int i6 = this.mDividerSize;
                rect.top = calculateParallaxDismissingFraction + i6;
                rect.bottom = calculateParallaxDismissingFraction + height + i6;
            }
        }
    }

    private static float calculateParallaxDismissingFraction(float f, int i) {
        float interpolation = SLOWDOWN_INTERPOLATOR.getInterpolation(f) / 3.5f;
        return i == 2 ? interpolation / 2.0f : interpolation;
    }

    private static boolean isDismissing(DividerSnapAlgorithm.SnapTarget snapTarget, int i, int i2) {
        if (i2 != 2 && i2 != 1) {
            return i > snapTarget.position;
        }
        if (i < snapTarget.position) {
            return true;
        }
        return false;
    }

    private int getStackIdForDismissTarget(DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget.flag == 1 && dockSideTopLeft(this.mDockSide)) {
            return 3;
        }
        if (snapTarget.flag != 2 || !dockSideBottomRight(this.mDockSide)) {
            return Build.VERSION.SDK_INT >= 26 ? 5 : 0;
        }
        return 3;
    }

    private int getWindowingModeForDismissTarget(DividerSnapAlgorithm.SnapTarget snapTarget) {
        if (snapTarget.flag != 1 || !dockSideTopLeft(this.mDockSide)) {
            return (snapTarget.flag != 2 || !dockSideBottomRight(this.mDockSide)) ? 4 : 3;
        }
        return 3;
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        internalInsetsInfo.touchableRegion.set(this.mHandle.getLeft(), this.mHandle.getTop(), this.mHandle.getRight(), this.mHandle.getBottom());
        internalInsetsInfo.touchableRegion.op(this.mBackground.getLeft(), this.mBackground.getTop(), this.mBackground.getRight(), this.mBackground.getBottom(), Region.Op.UNION);
    }

    public int growsRecents() {
        if (this.mGrowRecents && this.mWindowManagerProxy.getDockSide() == 2 && getCurrentPosition() == getSnapAlgorithm().getLastSplitTarget().position) {
            return getSnapAlgorithm().getMiddleTarget().position;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void onRecentsActivityStarting() {
        if (this.mGrowRecents && getWindowManagerProxy().getDockSide() == 2 && getCurrentPosition() == getSnapAlgorithm().getLastSplitTarget().position) {
            this.mState.growAfterRecentsDrawn = true;
            startDragging(false, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDockedFirstAnimationFrame() {
        saveSnapTargetBeforeMinimized(this.mSnapAlgorithm.getMiddleTarget());
    }

    /* access modifiers changed from: package-private */
    public void onDockedTopTask(int i, Rect rect) {
        if (i == -1) {
            DividerState dividerState = this.mState;
            dividerState.growAfterRecentsDrawn = false;
            dividerState.animateAfterRecentsDrawn = true;
            startDragging(false, false);
        }
        updateDockSide();
        int calculatePositionForBounds = DockedDividerUtils.calculatePositionForBounds(rect, this.mDockSide, this.mDividerSize);
        this.mEntranceAnimationRunning = true;
        if (this.mStableInsets.isEmpty()) {
            SystemServicesProxy.getInstance(this.mContext).getStableInsets(this.mStableInsets);
            this.mSnapAlgorithm = null;
            this.mMinimizedSnapAlgorithm = null;
            initializeSnapAlgorithm();
        }
        resizeStack(calculatePositionForBounds, this.mSnapAlgorithm.getMiddleTarget().position, this.mSnapAlgorithm.getMiddleTarget());
    }

    /* access modifiers changed from: package-private */
    public void onRecentsDrawn() {
        DividerState dividerState = this.mState;
        if (dividerState.animateAfterRecentsDrawn) {
            dividerState.animateAfterRecentsDrawn = false;
            updateDockSide();
            this.mHandler.post(new Runnable() {
                public void run() {
                    DividerView dividerView = DividerView.this;
                    dividerView.stopDragging(dividerView.getCurrentPosition(), DividerView.this.mSnapAlgorithm.getMiddleTarget(), (long) DividerView.this.mLongPressEntraceAnimDuration, Interpolators.FAST_OUT_SLOW_IN, 200);
                }
            });
        }
        DividerState dividerState2 = this.mState;
        if (dividerState2.growAfterRecentsDrawn) {
            dividerState2.growAfterRecentsDrawn = false;
            updateDockSide();
            RecentsEventBus.getDefault().send(new RecentsGrowingEvent());
            stopDragging(getCurrentPosition(), this.mSnapAlgorithm.getMiddleTarget(), 336, Interpolators.FAST_OUT_SLOW_IN);
        }
    }

    /* access modifiers changed from: package-private */
    public void onUndockingTask(final boolean z) {
        this.mHandle.post(new Runnable() {
            public void run() {
                DividerSnapAlgorithm.SnapTarget snapTarget;
                int dockSide = DividerView.this.mWindowManagerProxy.getDockSide();
                if (dockSide == -1) {
                    return;
                }
                if (z || !DividerView.this.mDockedStackMinimized) {
                    DividerView.this.startDragging(false, false);
                    if (DividerView.dockSideTopLeft(dockSide)) {
                        snapTarget = DividerView.this.mSnapAlgorithm.getDismissEndTarget();
                    } else {
                        snapTarget = DividerView.this.mSnapAlgorithm.getDismissStartTarget();
                    }
                    DividerSnapAlgorithm.SnapTarget snapTarget2 = snapTarget;
                    boolean unused = DividerView.this.mExitAnimationRunning = true;
                    DividerView dividerView = DividerView.this;
                    int unused2 = dividerView.mExitStartPosition = dividerView.getCurrentPosition();
                    DividerView dividerView2 = DividerView.this;
                    dividerView2.stopDragging(dividerView2.mExitStartPosition, snapTarget2, 336, 100, 0, Interpolators.FAST_OUT_SLOW_IN);
                    boolean unused3 = DividerView.this.mUnDockByUndockingTaskEvent = true;
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void onMultiWindowStateChanged(boolean z) {
        if (z && !Utilities.isAndroidNorNewer()) {
            updateDockSide();
            resizeStack(this.mSnapAlgorithm.getMiddleTarget().position, Integer.MAX_VALUE, this.mSnapAlgorithm.getMiddleTarget());
        }
    }

    public int getPositionWhenHandleDockKey(boolean z) {
        DividerSnapAlgorithm.SnapTarget snapTarget;
        DividerSnapAlgorithm snapAlgorithm = getSnapAlgorithm();
        DividerSnapAlgorithm.SnapTarget calculateNonDismissingSnapTarget = snapAlgorithm.calculateNonDismissingSnapTarget(getCurrentPosition());
        if (z) {
            snapTarget = snapAlgorithm.getPreviousTarget(calculateNonDismissingSnapTarget);
        } else {
            snapTarget = snapAlgorithm.getNextTarget(calculateNonDismissingSnapTarget);
        }
        return snapTarget.position;
    }
}
