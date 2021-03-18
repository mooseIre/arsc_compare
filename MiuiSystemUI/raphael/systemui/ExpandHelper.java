package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.policy.ScrollAdapter;

public class ExpandHelper implements Gefingerpoken {
    private Callback mCallback;
    private Context mContext;
    private float mCurrentHeight;
    private boolean mEnabled = true;
    private View mEventSource;
    private boolean mExpanding;
    private int mExpansionStyle = 0;
    private FlingAnimationUtils mFlingAnimationUtils;
    private int mGravity;
    private boolean mHasPopped;
    private float mInitialTouchFocusY;
    private float mInitialTouchSpan;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastFocusY;
    private float mLastMotionY;
    private float mLastSpanY;
    private float mNaturalHeight;
    private float mOldHeight;
    private boolean mOnlyMovements;
    private float mPullGestureMinXSpan;
    private ExpandableView mResizedView;
    private ScaleGestureDetector mSGD;
    private ObjectAnimator mScaleAnimation;
    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        /* class com.android.systemui.ExpandHelper.AnonymousClass1 */

        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        }

        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            if (!ExpandHelper.this.mOnlyMovements) {
                ExpandHelper expandHelper = ExpandHelper.this;
                expandHelper.startExpanding(expandHelper.mResizedView, 4);
            }
            return ExpandHelper.this.mExpanding;
        }
    };
    private ViewScaler mScaler;
    private ScrollAdapter mScrollAdapter;
    private final float mSlopMultiplier;
    private int mSmallSize;
    private final int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private boolean mWatchingForPull;

    public interface Callback {
        boolean canChildBeExpanded(View view);

        void expansionStateChanged(boolean z);

        ExpandableView getChildAtPosition(float f, float f2);

        ExpandableView getChildAtRawPosition(float f, float f2);

        int getMaxExpandHeight(ExpandableView expandableView);

        void setExpansionCancelled(View view);

        void setUserExpandedChild(View view, boolean z);

        void setUserLockedChild(View view, boolean z);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ObjectAnimator getScaleAnimation() {
        return this.mScaleAnimation;
    }

    /* access modifiers changed from: private */
    public class ViewScaler {
        ExpandableView mView;

        public ViewScaler() {
        }

        public void setView(ExpandableView expandableView) {
            this.mView = expandableView;
        }

        public void setHeight(float f) {
            this.mView.setActualHeight((int) f);
            ExpandHelper.this.mCurrentHeight = f;
        }

        public float getHeight() {
            return (float) this.mView.getActualHeight();
        }

        public int getNaturalHeight() {
            return ExpandHelper.this.mCallback.getMaxExpandHeight(this.mView);
        }
    }

    public ExpandHelper(Context context, Callback callback, int i, int i2) {
        this.mSmallSize = i;
        this.mContext = context;
        this.mCallback = callback;
        ViewScaler viewScaler = new ViewScaler();
        this.mScaler = viewScaler;
        this.mGravity = 48;
        this.mScaleAnimation = ObjectAnimator.ofFloat(viewScaler, "height", 0.0f);
        this.mPullGestureMinXSpan = this.mContext.getResources().getDimension(C0012R$dimen.pull_span_min);
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        this.mSlopMultiplier = ViewConfiguration.getAmbiguousGestureMultiplier();
        this.mSGD = new ScaleGestureDetector(context, this.mScaleGestureListener);
        this.mFlingAnimationUtils = new FlingAnimationUtils(this.mContext.getResources().getDisplayMetrics(), 0.3f);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateExpansion() {
        float currentSpan = (this.mSGD.getCurrentSpan() - this.mInitialTouchSpan) * 1.0f;
        float focusY = (this.mSGD.getFocusY() - this.mInitialTouchFocusY) * 1.0f * (this.mGravity == 80 ? -1.0f : 1.0f);
        float abs = Math.abs(focusY) + Math.abs(currentSpan) + 1.0f;
        this.mScaler.setHeight(clamp(((focusY * Math.abs(focusY)) / abs) + ((currentSpan * Math.abs(currentSpan)) / abs) + this.mOldHeight));
        this.mLastFocusY = this.mSGD.getFocusY();
        this.mLastSpanY = this.mSGD.getCurrentSpan();
    }

    private float clamp(float f) {
        int i = this.mSmallSize;
        if (f < ((float) i)) {
            f = (float) i;
        }
        float f2 = this.mNaturalHeight;
        return f > f2 ? f2 : f;
    }

    private ExpandableView findView(float f, float f2) {
        View view = this.mEventSource;
        if (view == null) {
            return this.mCallback.getChildAtPosition(f, f2);
        }
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        return this.mCallback.getChildAtRawPosition(f + ((float) iArr[0]), f2 + ((float) iArr[1]));
    }

    private boolean isInside(View view, float f, float f2) {
        if (view == null) {
            return false;
        }
        View view2 = this.mEventSource;
        if (view2 != null) {
            int[] iArr = new int[2];
            view2.getLocationOnScreen(iArr);
            f += (float) iArr[0];
            f2 += (float) iArr[1];
        }
        int[] iArr2 = new int[2];
        view.getLocationOnScreen(iArr2);
        float f3 = f - ((float) iArr2[0]);
        float f4 = f2 - ((float) iArr2[1]);
        if (f3 <= 0.0f || f4 <= 0.0f) {
            return false;
        }
        return ((f3 > ((float) view.getWidth()) ? 1 : (f3 == ((float) view.getWidth()) ? 0 : -1)) < 0) & ((f4 > ((float) view.getHeight()) ? 1 : (f4 == ((float) view.getHeight()) ? 0 : -1)) < 0);
    }

    public void setEventSource(View view) {
        this.mEventSource = view;
    }

    public void setScrollAdapter(ScrollAdapter scrollAdapter) {
        this.mScrollAdapter = scrollAdapter;
    }

    private float getTouchSlop(MotionEvent motionEvent) {
        if (motionEvent.getClassification() == 1) {
            return ((float) this.mTouchSlop) * this.mSlopMultiplier;
        }
        return (float) this.mTouchSlop;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0054, code lost:
        if (r0 != 3) goto L_0x010d;
     */
    @Override // com.android.systemui.Gefingerpoken
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onInterceptTouchEvent(android.view.MotionEvent r8) {
        /*
        // Method dump skipped, instructions count: 281
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.ExpandHelper.onInterceptTouchEvent(android.view.MotionEvent):boolean");
    }

    private void trackVelocity(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            } else {
                velocityTracker.clear();
            }
            this.mVelocityTracker.addMovement(motionEvent);
        } else if (actionMasked == 2) {
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }
            this.mVelocityTracker.addMovement(motionEvent);
        }
    }

    private void maybeRecycleVelocityTracker(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            return;
        }
        if (motionEvent.getActionMasked() == 3 || motionEvent.getActionMasked() == 1) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private float getCurrentVelocity() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            return 0.0f;
        }
        velocityTracker.computeCurrentVelocity(1000);
        return this.mVelocityTracker.getYVelocity();
    }

    public void setEnabled(boolean z) {
        this.mEnabled = z;
    }

    private boolean isEnabled() {
        return this.mEnabled;
    }

    private boolean isFullyExpanded(ExpandableView expandableView) {
        return expandableView.getIntrinsicHeight() == expandableView.getMaxContentHeight() && (!expandableView.isSummaryWithChildren() || expandableView.areChildrenExpanded());
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled() && !this.mExpanding) {
            return false;
        }
        trackVelocity(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        this.mSGD.onTouchEvent(motionEvent);
        int focusX = (int) this.mSGD.getFocusX();
        int focusY = (int) this.mSGD.getFocusY();
        if (this.mOnlyMovements) {
            this.mLastMotionY = motionEvent.getRawY();
            return false;
        }
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (this.mWatchingForPull) {
                        float rawY = motionEvent.getRawY() - this.mInitialTouchY;
                        float rawX = motionEvent.getRawX() - this.mInitialTouchX;
                        if (rawY > getTouchSlop(motionEvent) && rawY > Math.abs(rawX)) {
                            this.mWatchingForPull = false;
                            ExpandableView expandableView = this.mResizedView;
                            if (expandableView != null && !isFullyExpanded(expandableView) && startExpanding(this.mResizedView, 1)) {
                                this.mInitialTouchY = motionEvent.getRawY();
                                this.mLastMotionY = motionEvent.getRawY();
                                this.mHasPopped = false;
                            }
                        }
                    }
                    if (this.mExpanding && (this.mExpansionStyle & 1) != 0) {
                        float rawY2 = (motionEvent.getRawY() - this.mLastMotionY) + this.mCurrentHeight;
                        float clamp = clamp(rawY2);
                        boolean z = rawY2 > this.mNaturalHeight;
                        if (rawY2 < ((float) this.mSmallSize)) {
                            z = true;
                        }
                        if (!this.mHasPopped) {
                            View view = this.mEventSource;
                            if (view != null) {
                                view.performHapticFeedback(1);
                            }
                            this.mHasPopped = true;
                        }
                        this.mScaler.setHeight(clamp);
                        this.mLastMotionY = motionEvent.getRawY();
                        if (z) {
                            this.mCallback.expansionStateChanged(false);
                        } else {
                            this.mCallback.expansionStateChanged(true);
                        }
                        return true;
                    } else if (this.mExpanding) {
                        updateExpansion();
                        this.mLastMotionY = motionEvent.getRawY();
                        return true;
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 5 || actionMasked == 6) {
                        this.mInitialTouchY += this.mSGD.getFocusY() - this.mLastFocusY;
                        this.mInitialTouchSpan += this.mSGD.getCurrentSpan() - this.mLastSpanY;
                    }
                }
            }
            finishExpanding(!isEnabled() || motionEvent.getActionMasked() == 3, getCurrentVelocity());
            clearView();
        } else {
            ScrollAdapter scrollAdapter = this.mScrollAdapter;
            this.mWatchingForPull = scrollAdapter != null && isInside(scrollAdapter.getHostView(), (float) focusX, (float) focusY);
            this.mResizedView = findView((float) focusX, (float) focusY);
            this.mInitialTouchX = motionEvent.getRawX();
            this.mInitialTouchY = motionEvent.getRawY();
        }
        this.mLastMotionY = motionEvent.getRawY();
        maybeRecycleVelocityTracker(motionEvent);
        if (this.mResizedView != null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean startExpanding(ExpandableView expandableView, int i) {
        if (!(expandableView instanceof ExpandableNotificationRow)) {
            return false;
        }
        this.mExpansionStyle = i;
        if (this.mExpanding && expandableView == this.mResizedView) {
            return true;
        }
        this.mExpanding = true;
        this.mCallback.expansionStateChanged(true);
        this.mCallback.setUserLockedChild(expandableView, true);
        this.mScaler.setView(expandableView);
        float height = this.mScaler.getHeight();
        this.mOldHeight = height;
        this.mCurrentHeight = height;
        if (this.mCallback.canChildBeExpanded(expandableView)) {
            this.mNaturalHeight = (float) this.mScaler.getNaturalHeight();
            this.mSmallSize = expandableView.getCollapsedHeight();
        } else {
            this.mNaturalHeight = this.mOldHeight;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void finishExpanding(boolean z, float f) {
        finishExpanding(z, f, true);
    }

    private void finishExpanding(boolean z, float f, boolean z2) {
        final boolean z3;
        if (this.mExpanding) {
            float height = this.mScaler.getHeight();
            boolean z4 = true;
            boolean z5 = this.mOldHeight == ((float) this.mSmallSize);
            if (!z) {
                z3 = (!z5 ? height >= this.mOldHeight || f > 0.0f : height > this.mOldHeight && f >= 0.0f) | (this.mNaturalHeight == ((float) this.mSmallSize));
            } else {
                z3 = !z5;
            }
            if (this.mScaleAnimation.isRunning()) {
                this.mScaleAnimation.cancel();
            }
            this.mCallback.expansionStateChanged(false);
            int naturalHeight = this.mScaler.getNaturalHeight();
            if (!z3) {
                naturalHeight = this.mSmallSize;
            }
            float f2 = (float) naturalHeight;
            int i = (f2 > height ? 1 : (f2 == height ? 0 : -1));
            if (i == 0 || !this.mEnabled || !z2) {
                if (i != 0) {
                    this.mScaler.setHeight(f2);
                }
                this.mCallback.setUserExpandedChild(this.mResizedView, z3);
                this.mCallback.setUserLockedChild(this.mResizedView, false);
                this.mScaler.setView(null);
            } else {
                this.mScaleAnimation.setFloatValues(f2);
                this.mScaleAnimation.setupStartValues();
                final ExpandableView expandableView = this.mResizedView;
                this.mScaleAnimation.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.systemui.ExpandHelper.AnonymousClass2 */
                    public boolean mCancelled;

                    public void onAnimationEnd(Animator animator) {
                        if (!this.mCancelled) {
                            ExpandHelper.this.mCallback.setUserExpandedChild(expandableView, z3);
                            if (!ExpandHelper.this.mExpanding) {
                                ExpandHelper.this.mScaler.setView(null);
                            }
                        } else {
                            ExpandHelper.this.mCallback.setExpansionCancelled(expandableView);
                        }
                        ExpandHelper.this.mCallback.setUserLockedChild(expandableView, false);
                        ExpandHelper.this.mScaleAnimation.removeListener(this);
                    }

                    public void onAnimationCancel(Animator animator) {
                        this.mCancelled = true;
                    }
                });
                if (f < 0.0f) {
                    z4 = false;
                }
                if (z3 != z4) {
                    f = 0.0f;
                }
                this.mFlingAnimationUtils.apply(this.mScaleAnimation, height, f2, f);
                this.mScaleAnimation.start();
            }
            this.mExpanding = false;
            this.mExpansionStyle = 0;
        }
    }

    private void clearView() {
        this.mResizedView = null;
    }

    public void cancelImmediately() {
        cancel(false);
    }

    public void cancel() {
        cancel(true);
    }

    private void cancel(boolean z) {
        finishExpanding(true, 0.0f, z);
        clearView();
        this.mSGD = new ScaleGestureDetector(this.mContext, this.mScaleGestureListener);
    }

    public void onlyObserveMovements(boolean z) {
        this.mOnlyMovements = z;
    }
}
