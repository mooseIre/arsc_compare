package com.android.systemui.pip.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.IActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.view.IPinnedStackController;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.PipSnapAlgorithm;
import com.android.systemui.pip.PipCompat;
import com.android.systemui.pip.phone.InputConsumerController;
import com.android.systemui.pip.phone.PipAccessibilityInteractionConnection;
import com.android.systemui.pip.phone.PipMenuActivityController;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.io.PrintWriter;

public class PipTouchHandler {
    private final AccessibilityManager mAccessibilityManager;
    private final IActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    private PipTouchGesture mDefaultMovementGesture = new PipTouchGesture() {
        private final PointF mDelta = new PointF();
        private final Point mStartPosition = new Point();
        private boolean mStartedOnLeft;

        public void onDown(PipTouchState pipTouchState) {
            if (pipTouchState.isUserInteracting()) {
                Rect bounds = PipTouchHandler.this.mMotionHelper.getBounds();
                boolean z = false;
                this.mStartedOnLeft = bounds.left < PipTouchHandler.this.mMovementBounds.centerX();
                boolean unused = PipTouchHandler.this.mMovementWithinMinimize = true;
                PipTouchHandler pipTouchHandler = PipTouchHandler.this;
                if (pipTouchState.getDownTouchPosition().y >= ((float) PipTouchHandler.this.mMovementBounds.bottom)) {
                    z = true;
                }
                boolean unused2 = pipTouchHandler.mMovementWithinDismiss = z;
                this.mDelta.set(0.0f, 0.0f);
                this.mStartPosition.set(bounds.left, bounds.top);
                if (PipTouchHandler.this.mMenuState != 0 && !PipTouchHandler.this.mIsMinimized) {
                    PipTouchHandler.this.mMenuController.pokeMenu();
                }
                PipTouchHandler.this.mDismissViewController.createDismissTarget();
                PipTouchHandler.this.mHandler.postDelayed(PipTouchHandler.this.mShowDismissAffordance, 225);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean onMove(PipTouchState pipTouchState) {
            boolean z = false;
            if (!pipTouchState.isUserInteracting()) {
                return false;
            }
            if (pipTouchState.startedDragging()) {
                float unused = PipTouchHandler.this.mSavedSnapFraction = -1.0f;
                PipTouchHandler.this.mHandler.removeCallbacks(PipTouchHandler.this.mShowDismissAffordance);
                PipTouchHandler.this.mDismissViewController.showDismissTarget();
            }
            if (!pipTouchState.isDragging()) {
                return false;
            }
            PipTouchHandler.this.mTmpBounds.set(PipTouchHandler.this.mMotionHelper.getBounds());
            PointF lastTouchDelta = pipTouchState.getLastTouchDelta();
            Point point = this.mStartPosition;
            PointF pointF = this.mDelta;
            float f = ((float) point.x) + pointF.x;
            float f2 = ((float) point.y) + pointF.y;
            float pipBoundsLeft = PipCompat.getPipBoundsLeft(PipTouchHandler.this.mTmpBounds, f, lastTouchDelta.x);
            float pipBoundsTop = PipCompat.getPipBoundsTop(PipTouchHandler.this.mTmpBounds, f2, lastTouchDelta.y);
            pipTouchState.allowDraggingOffscreen();
            float max = Math.max((float) PipTouchHandler.this.mMovementBounds.left, Math.min((float) PipTouchHandler.this.mMovementBounds.right, pipBoundsLeft));
            float max2 = Math.max((float) PipTouchHandler.this.mMovementBounds.top, pipBoundsTop);
            PointF pointF2 = this.mDelta;
            pointF2.x += max - f;
            pointF2.y += max2 - f2;
            PipTouchHandler.this.mTmpBounds.offsetTo((int) max, (int) max2);
            PipTouchHandler.this.mMotionHelper.movePip(PipTouchHandler.this.mTmpBounds);
            PipTouchHandler.this.updateDismissFraction();
            PointF lastTouchPosition = pipTouchState.getLastTouchPosition();
            if (PipTouchHandler.this.mMovementWithinMinimize) {
                PipTouchHandler pipTouchHandler = PipTouchHandler.this;
                boolean unused2 = pipTouchHandler.mMovementWithinMinimize = !this.mStartedOnLeft ? lastTouchPosition.x >= ((float) pipTouchHandler.mMovementBounds.right) : lastTouchPosition.x <= ((float) (pipTouchHandler.mMovementBounds.left + PipTouchHandler.this.mTmpBounds.width()));
            }
            if (PipTouchHandler.this.mMovementWithinDismiss) {
                PipTouchHandler pipTouchHandler2 = PipTouchHandler.this;
                if (lastTouchPosition.y >= ((float) pipTouchHandler2.mMovementBounds.bottom)) {
                    z = true;
                }
                boolean unused3 = pipTouchHandler2.mMovementWithinDismiss = z;
            }
            return true;
        }

        public boolean onUp(PipTouchState pipTouchState) {
            AnonymousClass1 r12;
            PipTouchHandler.this.cleanUpDismissTarget();
            if (!pipTouchState.isUserInteracting()) {
                return false;
            }
            PointF velocity = pipTouchState.getVelocity();
            boolean z = Math.abs(velocity.x) > Math.abs(velocity.y);
            float length = PointF.length(velocity.x, velocity.y);
            boolean z2 = length > PipTouchHandler.this.mFlingAnimationUtils.getMinVelocityPxPerSecond();
            boolean z3 = z2 && velocity.y > 0.0f && !z && PipTouchHandler.this.mMovementWithinDismiss;
            if (PipTouchHandler.this.mMotionHelper.shouldDismissPip() || z3) {
                PipTouchHandler.this.mMotionHelper.animateDismiss(PipTouchHandler.this.mMotionHelper.getBounds(), velocity.x, velocity.y, PipTouchHandler.this.mUpdateScrimListener);
                MetricsLogger.action(PipTouchHandler.this.mContext, 822, 1);
                return true;
            }
            if (pipTouchState.isDragging()) {
                if (z2 && z && PipTouchHandler.this.mMovementWithinMinimize) {
                    if (this.mStartedOnLeft) {
                        int i = (velocity.x > 0.0f ? 1 : (velocity.x == 0.0f ? 0 : -1));
                    } else {
                        int i2 = (velocity.x > 0.0f ? 1 : (velocity.x == 0.0f ? 0 : -1));
                    }
                }
                if (PipTouchHandler.this.mIsMinimized) {
                    PipTouchHandler.this.setMinimizedStateInternal(false);
                }
                if (PipTouchHandler.this.mMenuState != 0) {
                    PipTouchHandler.this.mMenuController.showMenu(PipTouchHandler.this.mMenuState, PipTouchHandler.this.mMotionHelper.getBounds(), PipTouchHandler.this.mMovementBounds, true);
                    r12 = null;
                } else {
                    r12 = new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            PipTouchHandler.this.mMenuController.hideMenu();
                        }
                    };
                }
                if (z2) {
                    PipTouchHandler.this.mMotionHelper.flingToSnapTarget(length, velocity.x, velocity.y, PipTouchHandler.this.mMovementBounds, PipTouchHandler.this.mUpdateScrimListener, r12);
                } else {
                    PipTouchHandler.this.mMotionHelper.animateToClosestSnapTarget(PipTouchHandler.this.mMovementBounds, PipTouchHandler.this.mUpdateScrimListener, r12);
                }
            } else if (PipTouchHandler.this.mIsMinimized) {
                PipTouchHandler.this.mMotionHelper.animateToClosestSnapTarget(PipTouchHandler.this.mMovementBounds, (ValueAnimator.AnimatorUpdateListener) null, (Animator.AnimatorListener) null);
                PipTouchHandler.this.setMinimizedStateInternal(false);
            } else if (PipTouchHandler.this.mMenuState != 2) {
                PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), PipTouchHandler.this.mMovementBounds, true);
            } else {
                PipTouchHandler.this.mMenuController.hideMenu();
                PipTouchHandler.this.mMotionHelper.expandPip();
            }
            return true;
        }
    };
    private int mDeferResizeToNormalBoundsUntilRotation = -1;
    /* access modifiers changed from: private */
    public final PipDismissViewController mDismissViewController;
    private int mDisplayRotation;
    private Rect mExpandedBounds = new Rect();
    private Rect mExpandedMovementBounds = new Rect();
    private int mExpandedShortestEdgeSize;
    /* access modifiers changed from: private */
    public final FlingAnimationUtils mFlingAnimationUtils;
    private final PipTouchGesture[] mGestures;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private int mImeHeight;
    private boolean mIsImeShowing;
    /* access modifiers changed from: private */
    public boolean mIsMinimized;
    /* access modifiers changed from: private */
    public final PipMenuActivityController mMenuController;
    private final PipMenuListener mMenuListener = new PipMenuListener();
    /* access modifiers changed from: private */
    public int mMenuState;
    /* access modifiers changed from: private */
    public final PipMotionHelper mMotionHelper;
    /* access modifiers changed from: private */
    public Rect mMovementBounds = new Rect();
    /* access modifiers changed from: private */
    public boolean mMovementWithinDismiss;
    /* access modifiers changed from: private */
    public boolean mMovementWithinMinimize;
    private Rect mNormalBounds = new Rect();
    private Rect mNormalMovementBounds = new Rect();
    private IPinnedStackController mPinnedStackController;
    /* access modifiers changed from: private */
    public float mSavedSnapFraction = -1.0f;
    private boolean mSendingHoverAccessibilityEvents;
    /* access modifiers changed from: private */
    public Runnable mShowDismissAffordance = new Runnable() {
        public void run() {
            PipTouchHandler.this.mDismissViewController.showDismissTarget();
        }
    };
    private boolean mShowPipMenuOnAnimationEnd = false;
    private final PipSnapAlgorithm mSnapAlgorithm;
    /* access modifiers changed from: private */
    public final Rect mTmpBounds = new Rect();
    private final PipTouchState mTouchState;
    /* access modifiers changed from: private */
    public ValueAnimator.AnimatorUpdateListener mUpdateScrimListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            PipTouchHandler.this.updateDismissFraction();
        }
    };
    private final ViewConfiguration mViewConfig;

    /* access modifiers changed from: package-private */
    public void setMinimizedState(boolean z, boolean z2) {
    }

    /* access modifiers changed from: package-private */
    public void setMinimizedStateInternal(boolean z) {
    }

    private class PipMenuListener implements PipMenuActivityController.Listener {
        private PipMenuListener() {
        }

        public void onPipMenuStateChanged(int i, boolean z) {
            PipTouchHandler.this.setMenuState(i, z);
        }

        public void onPipExpand() {
            if (!PipTouchHandler.this.mIsMinimized) {
                PipTouchHandler.this.mMotionHelper.expandPip();
            }
        }

        public void onPipMinimize() {
            PipTouchHandler.this.setMinimizedStateInternal(true);
            PipTouchHandler.this.mMotionHelper.animateToClosestMinimizedState(PipTouchHandler.this.mMovementBounds, (ValueAnimator.AnimatorUpdateListener) null);
        }

        public void onPipDismiss() {
            PipTouchHandler.this.mMotionHelper.dismissPip();
            MetricsLogger.action(PipTouchHandler.this.mContext, 822, 0);
        }

        public void onPipShowMenu() {
            PipTouchHandler.this.mMenuController.showMenu(2, PipTouchHandler.this.mMotionHelper.getBounds(), PipTouchHandler.this.mMovementBounds, true);
        }
    }

    public PipTouchHandler(Context context, IActivityManager iActivityManager, PipMenuActivityController pipMenuActivityController, InputConsumerController inputConsumerController) {
        this.mContext = context;
        this.mActivityManager = iActivityManager;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mViewConfig = ViewConfiguration.get(context);
        this.mMenuController = pipMenuActivityController;
        this.mMenuController.addListener(this.mMenuListener);
        this.mDismissViewController = new PipDismissViewController(context);
        this.mSnapAlgorithm = new PipSnapAlgorithm(this.mContext);
        this.mTouchState = new PipTouchState(this.mViewConfig);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 2.0f);
        this.mGestures = new PipTouchGesture[]{this.mDefaultMovementGesture};
        this.mMotionHelper = new PipMotionHelper(this.mContext, this.mActivityManager, this.mMenuController, this.mSnapAlgorithm, this.mFlingAnimationUtils);
        this.mExpandedShortestEdgeSize = context.getResources().getDimensionPixelSize(R.dimen.pip_expanded_shortest_edge_size);
        inputConsumerController.setTouchListener(new InputConsumerController.TouchListener() {
            public final boolean onTouchEvent(MotionEvent motionEvent) {
                return PipTouchHandler.this.handleTouchEvent(motionEvent);
            }
        });
        inputConsumerController.setRegistrationListener(new InputConsumerController.RegistrationListener() {
            public final void onRegistrationChanged(boolean z) {
                PipTouchHandler.this.onRegistrationChanged(z);
            }
        });
        onRegistrationChanged(inputConsumerController.isRegistered());
    }

    public void setTouchEnabled(boolean z) {
        this.mTouchState.setAllowTouches(z);
    }

    public void showPictureInPictureMenu() {
        if (!this.mTouchState.isUserInteracting()) {
            this.mMenuController.showMenu(2, this.mMotionHelper.getBounds(), this.mMovementBounds, false);
        }
    }

    public void onActivityPinned() {
        this.mMenuState = 0;
        if (this.mIsMinimized) {
            setMinimizedStateInternal(false);
        }
        cleanUpDismissTarget();
        this.mShowPipMenuOnAnimationEnd = true;
    }

    public void onPinnedStackAnimationEnded() {
        this.mMotionHelper.synchronizePinnedStackBounds();
        if (this.mShowPipMenuOnAnimationEnd) {
            this.mMenuController.showMenu(1, this.mMotionHelper.getBounds(), this.mMovementBounds, true);
            this.mShowPipMenuOnAnimationEnd = false;
        }
    }

    public void onConfigurationChanged() {
        this.mMotionHelper.onConfigurationChanged();
        this.mMotionHelper.synchronizePinnedStackBounds();
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mIsImeShowing = z;
        this.mImeHeight = i;
    }

    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, int i) {
        this.mNormalBounds = rect2;
        Rect rect4 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mNormalBounds, rect, rect4, this.mIsImeShowing ? this.mImeHeight : 0);
        float width = ((float) rect2.width()) / ((float) rect2.height());
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        Size sizeForAspectRatio = this.mSnapAlgorithm.getSizeForAspectRatio(width, (float) this.mExpandedShortestEdgeSize, point.x, point.y);
        this.mExpandedBounds.set(0, 0, sizeForAspectRatio.getWidth(), sizeForAspectRatio.getHeight());
        Rect rect5 = new Rect();
        this.mSnapAlgorithm.getMovementBounds(this.mExpandedBounds, rect, rect5, this.mIsImeShowing ? this.mImeHeight : 0);
        if (z && !this.mTouchState.isUserInteracting()) {
            Rect rect6 = new Rect(rect3);
            Rect rect7 = this.mMenuState == 2 ? rect5 : rect4;
            if (this.mIsImeShowing) {
                int i2 = rect6.top;
                if (i2 == this.mMovementBounds.bottom) {
                    rect6.offsetTo(rect6.left, rect7.bottom);
                } else {
                    rect6.offset(0, Math.min(0, rect7.bottom - i2));
                }
            } else if (rect6.top == this.mMovementBounds.bottom) {
                rect6.offsetTo(rect6.left, rect7.bottom);
            }
            this.mMotionHelper.animateToIMEOffset(rect6);
        }
        this.mNormalMovementBounds = rect4;
        this.mExpandedMovementBounds = rect5;
        this.mDisplayRotation = i;
        updateMovementBounds(this.mMenuState);
        if (this.mDeferResizeToNormalBoundsUntilRotation == i) {
            this.mMotionHelper.animateToUnexpandedState(rect2, this.mSavedSnapFraction, this.mNormalMovementBounds, this.mMovementBounds, this.mIsMinimized, true);
            this.mSavedSnapFraction = -1.0f;
            this.mDeferResizeToNormalBoundsUntilRotation = -1;
        }
    }

    /* access modifiers changed from: private */
    public void onRegistrationChanged(boolean z) {
        this.mAccessibilityManager.setPictureInPictureActionReplacingConnection(z ? new PipAccessibilityInteractionConnection(this.mMotionHelper, new PipAccessibilityInteractionConnection.AccessibilityCallbacks() {
            public final void onAccessibilityShowMenu() {
                PipTouchHandler.this.onAccessibilityShowMenu();
            }
        }, this.mHandler) : null);
        if (!z && this.mTouchState.isUserInteracting()) {
            cleanUpDismissTarget();
        }
    }

    /* access modifiers changed from: private */
    public void onAccessibilityShowMenu() {
        this.mMenuController.showMenu(2, this.mMotionHelper.getBounds(), this.mMovementBounds, false);
    }

    /* access modifiers changed from: private */
    public boolean handleTouchEvent(MotionEvent motionEvent) {
        if (this.mPinnedStackController == null) {
            return true;
        }
        this.mTouchState.onTouchEvent(motionEvent);
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action == 1) {
                updateMovementBounds(this.mMenuState);
                PipTouchGesture[] pipTouchGestureArr = this.mGestures;
                int length = pipTouchGestureArr.length;
                int i = 0;
                while (i < length && !pipTouchGestureArr[i].onUp(this.mTouchState)) {
                    i++;
                }
            } else if (action == 2) {
                PipTouchGesture[] pipTouchGestureArr2 = this.mGestures;
                int length2 = pipTouchGestureArr2.length;
                int i2 = 0;
                while (i2 < length2 && !pipTouchGestureArr2[i2].onMove(this.mTouchState)) {
                    i2++;
                }
            } else if (action != 3) {
                if (action == 7 || action == 9) {
                    if (this.mAccessibilityManager.isEnabled() && !this.mSendingHoverAccessibilityEvents) {
                        AccessibilityEvent obtain = AccessibilityEvent.obtain(128);
                        PipAccessibilityInteractionConnection.obtainRootAccessibilityNodeInfo().recycle();
                        this.mAccessibilityManager.sendAccessibilityEvent(obtain);
                        this.mSendingHoverAccessibilityEvents = true;
                    }
                } else if (action == 10 && this.mAccessibilityManager.isEnabled() && this.mSendingHoverAccessibilityEvents) {
                    AccessibilityEvent obtain2 = AccessibilityEvent.obtain(256);
                    PipAccessibilityInteractionConnection.obtainRootAccessibilityNodeInfo().recycle();
                    this.mAccessibilityManager.sendAccessibilityEvent(obtain2);
                    this.mSendingHoverAccessibilityEvents = false;
                }
            }
            this.mTouchState.reset();
            cleanUpDismissTarget();
        } else {
            this.mMotionHelper.synchronizePinnedStackBounds();
            for (PipTouchGesture onDown : this.mGestures) {
                onDown.onDown(this.mTouchState);
            }
        }
        if (this.mMenuState == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateDismissFraction() {
        if (this.mMenuController != null) {
            Rect bounds = this.mMotionHelper.getBounds();
            float height = (float) (this.mMovementBounds.bottom + bounds.height());
            int i = bounds.bottom;
            float min = ((float) i) > height ? Math.min((((float) i) - height) / ((float) bounds.height()), 1.0f) : 0.0f;
            if (Float.compare(min, 0.0f) != 0 || this.mMenuState != 0) {
                this.mMenuController.setDismissFraction(min);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPinnedStackController(IPinnedStackController iPinnedStackController) {
        this.mPinnedStackController = iPinnedStackController;
    }

    /* access modifiers changed from: package-private */
    public void setMenuState(int i, boolean z) {
        if (i == 2) {
            Rect rect = new Rect(this.mExpandedBounds);
            if (z) {
                this.mSavedSnapFraction = this.mMotionHelper.animateToExpandedState(rect, this.mMovementBounds, this.mExpandedMovementBounds);
            }
        } else if (i == 0) {
            if (z) {
                if (this.mDeferResizeToNormalBoundsUntilRotation == -1) {
                    try {
                        int displayRotation = this.mPinnedStackController.getDisplayRotation();
                        if (this.mDisplayRotation != displayRotation) {
                            this.mDeferResizeToNormalBoundsUntilRotation = displayRotation;
                        }
                    } catch (RemoteException unused) {
                        Log.e("PipTouchHandler", "Could not get display rotation from controller");
                    }
                }
                if (this.mDeferResizeToNormalBoundsUntilRotation == -1) {
                    this.mMotionHelper.animateToUnexpandedState(new Rect(this.mNormalBounds), this.mSavedSnapFraction, this.mNormalMovementBounds, this.mMovementBounds, this.mIsMinimized, false);
                    this.mSavedSnapFraction = -1.0f;
                }
            } else {
                setTouchEnabled(false);
                this.mSavedSnapFraction = -1.0f;
            }
        }
        this.mMenuState = i;
        updateMovementBounds(i);
        boolean z2 = true;
        if (i != 1) {
            Context context = this.mContext;
            if (i != 2) {
                z2 = false;
            }
            MetricsLogger.visibility(context, 823, z2);
        }
    }

    public PipMotionHelper getMotionHelper() {
        return this.mMotionHelper;
    }

    private void updateMovementBounds(int i) {
        Rect rect;
        int i2 = 0;
        boolean z = i == 2;
        if (z) {
            rect = this.mExpandedMovementBounds;
        } else {
            rect = this.mNormalMovementBounds;
        }
        this.mMovementBounds = rect;
        try {
            IPinnedStackController iPinnedStackController = this.mPinnedStackController;
            if (z) {
                i2 = this.mExpandedShortestEdgeSize;
            }
            iPinnedStackController.setMinEdgeSize(i2);
        } catch (RemoteException e) {
            Log.e("PipTouchHandler", "Could not set minimized state", e);
        }
    }

    /* access modifiers changed from: private */
    public void cleanUpDismissTarget() {
        this.mHandler.removeCallbacks(this.mShowDismissAffordance);
        this.mDismissViewController.destroyDismissTarget();
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipTouchHandler");
        printWriter.println(str2 + "mMovementBounds=" + this.mMovementBounds);
        printWriter.println(str2 + "mNormalBounds=" + this.mNormalBounds);
        printWriter.println(str2 + "mNormalMovementBounds=" + this.mNormalMovementBounds);
        printWriter.println(str2 + "mExpandedBounds=" + this.mExpandedBounds);
        printWriter.println(str2 + "mExpandedMovementBounds=" + this.mExpandedMovementBounds);
        printWriter.println(str2 + "mMenuState=" + this.mMenuState);
        printWriter.println(str2 + "mIsMinimized=" + this.mIsMinimized);
        printWriter.println(str2 + "mIsImeShowing=" + this.mIsImeShowing);
        printWriter.println(str2 + "mImeHeight=" + this.mImeHeight);
        printWriter.println(str2 + "mSavedSnapFraction=" + this.mSavedSnapFraction);
        printWriter.println(str2 + "mEnableDragToEdgeDismiss=" + true);
        printWriter.println(str2 + "mEnableMinimize=" + false);
        this.mSnapAlgorithm.dump(printWriter, str2);
        this.mTouchState.dump(printWriter, str2);
        this.mMotionHelper.dump(printWriter, str2);
    }
}
