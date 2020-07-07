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
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.policy.ScrollAdapter;

public class ExpandHelper {
    /* access modifiers changed from: private */
    public Callback mCallback;
    private Context mContext;
    /* access modifiers changed from: private */
    public float mCurrentHeight;
    private boolean mEnabled = true;
    private View mEventSource;
    /* access modifiers changed from: private */
    public boolean mExpanding;
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
    /* access modifiers changed from: private */
    public boolean mOnlyMovements;
    private float mPullGestureMinXSpan;
    /* access modifiers changed from: private */
    public ExpandableView mResizedView;
    private ScaleGestureDetector mSGD;
    /* access modifiers changed from: private */
    public ObjectAnimator mScaleAnimation;
    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
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
    /* access modifiers changed from: private */
    public ViewScaler mScaler;
    private ScrollAdapter mScrollAdapter;
    private int mSmallSize;
    private int mTouchSlop;
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

    private class ViewScaler {
        ExpandableView mView;

        public ViewScaler() {
        }

        public void setView(ExpandableView expandableView) {
            this.mView = expandableView;
        }

        public void setHeight(float f) {
            this.mView.setActualHeight((int) f);
            float unused = ExpandHelper.this.mCurrentHeight = f;
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
        this.mScaleAnimation = ObjectAnimator.ofFloat(viewScaler, "height", new float[]{0.0f});
        this.mPullGestureMinXSpan = this.mContext.getResources().getDimension(R.dimen.pull_span_min);
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        this.mSGD = new ScaleGestureDetector(context, this.mScaleGestureListener);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, 0.3f);
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
        if ((f3 < ((float) view.getWidth())) && (f4 < ((float) view.getHeight()))) {
            return true;
        }
        return false;
    }

    public void setEventSource(View view) {
        this.mEventSource = view;
    }

    public void setScrollAdapter(ScrollAdapter scrollAdapter) {
        this.mScrollAdapter = scrollAdapter;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0054, code lost:
        if (r0 != 3) goto L_0x010c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onInterceptTouchEvent(android.view.MotionEvent r8) {
        /*
            r7 = this;
            boolean r0 = r7.isEnabled()
            r1 = 0
            if (r0 != 0) goto L_0x0008
            return r1
        L_0x0008:
            r7.trackVelocity(r8)
            int r0 = r8.getAction()
            android.view.ScaleGestureDetector r2 = r7.mSGD
            r2.onTouchEvent(r8)
            android.view.ScaleGestureDetector r2 = r7.mSGD
            float r2 = r2.getFocusX()
            int r2 = (int) r2
            android.view.ScaleGestureDetector r3 = r7.mSGD
            float r3 = r3.getFocusY()
            int r3 = (int) r3
            float r3 = (float) r3
            r7.mInitialTouchFocusY = r3
            android.view.ScaleGestureDetector r4 = r7.mSGD
            float r4 = r4.getCurrentSpan()
            r7.mInitialTouchSpan = r4
            float r5 = r7.mInitialTouchFocusY
            r7.mLastFocusY = r5
            r7.mLastSpanY = r4
            boolean r4 = r7.mExpanding
            r5 = 1
            if (r4 == 0) goto L_0x0042
            float r0 = r8.getRawY()
            r7.mLastMotionY = r0
            r7.maybeRecycleVelocityTracker(r8)
            return r5
        L_0x0042:
            r4 = 2
            if (r0 != r4) goto L_0x004b
            int r6 = r7.mExpansionStyle
            r6 = r6 & r5
            if (r6 == 0) goto L_0x004b
            return r5
        L_0x004b:
            r0 = r0 & 255(0xff, float:3.57E-43)
            if (r0 == 0) goto L_0x00cf
            r2 = 3
            if (r0 == r5) goto L_0x00bd
            if (r0 == r4) goto L_0x0058
            if (r0 == r2) goto L_0x00bd
            goto L_0x010c
        L_0x0058:
            android.view.ScaleGestureDetector r0 = r7.mSGD
            float r0 = r0.getCurrentSpanX()
            float r2 = r7.mPullGestureMinXSpan
            int r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r2 <= 0) goto L_0x0079
            android.view.ScaleGestureDetector r2 = r7.mSGD
            float r2 = r2.getCurrentSpanY()
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 <= 0) goto L_0x0079
            boolean r0 = r7.mExpanding
            if (r0 != 0) goto L_0x0079
            com.android.systemui.statusbar.ExpandableView r0 = r7.mResizedView
            r7.startExpanding(r0, r4)
            r7.mWatchingForPull = r1
        L_0x0079:
            boolean r0 = r7.mWatchingForPull
            if (r0 == 0) goto L_0x010c
            float r0 = r8.getRawY()
            float r2 = r7.mInitialTouchY
            float r0 = r0 - r2
            float r2 = r8.getRawX()
            float r3 = r7.mInitialTouchX
            float r2 = r2 - r3
            int r3 = r7.mTouchSlop
            float r3 = (float) r3
            int r3 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r3 <= 0) goto L_0x010c
            float r2 = java.lang.Math.abs(r2)
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 <= 0) goto L_0x010c
            r7.mWatchingForPull = r1
            com.android.systemui.statusbar.ExpandableView r0 = r7.mResizedView
            if (r0 == 0) goto L_0x010c
            boolean r0 = r7.isFullyExpanded(r0)
            if (r0 != 0) goto L_0x010c
            com.android.systemui.statusbar.ExpandableView r0 = r7.mResizedView
            boolean r0 = r7.startExpanding(r0, r5)
            if (r0 == 0) goto L_0x010c
            float r0 = r8.getRawY()
            r7.mLastMotionY = r0
            float r0 = r8.getRawY()
            r7.mInitialTouchY = r0
            r7.mHasPopped = r1
            goto L_0x010c
        L_0x00bd:
            int r0 = r8.getActionMasked()
            if (r0 != r2) goto L_0x00c4
            r1 = r5
        L_0x00c4:
            float r0 = r7.getCurrentVelocity()
            r7.finishExpanding(r1, r0)
            r7.clearView()
            goto L_0x010c
        L_0x00cf:
            com.android.systemui.statusbar.policy.ScrollAdapter r0 = r7.mScrollAdapter
            if (r0 == 0) goto L_0x00e7
            android.view.View r0 = r0.getHostView()
            float r4 = (float) r2
            boolean r0 = r7.isInside(r0, r4, r3)
            if (r0 == 0) goto L_0x00e7
            com.android.systemui.statusbar.policy.ScrollAdapter r0 = r7.mScrollAdapter
            boolean r0 = r0.isScrolledToTop()
            if (r0 == 0) goto L_0x00e7
            goto L_0x00e8
        L_0x00e7:
            r5 = r1
        L_0x00e8:
            r7.mWatchingForPull = r5
            float r0 = (float) r2
            com.android.systemui.statusbar.ExpandableView r0 = r7.findView(r0, r3)
            r7.mResizedView = r0
            if (r0 == 0) goto L_0x0100
            com.android.systemui.ExpandHelper$Callback r2 = r7.mCallback
            boolean r0 = r2.canChildBeExpanded(r0)
            if (r0 != 0) goto L_0x0100
            r0 = 0
            r7.mResizedView = r0
            r7.mWatchingForPull = r1
        L_0x0100:
            float r0 = r8.getRawY()
            r7.mInitialTouchY = r0
            float r0 = r8.getRawX()
            r7.mInitialTouchX = r0
        L_0x010c:
            float r0 = r8.getRawY()
            r7.mLastMotionY = r0
            r7.maybeRecycleVelocityTracker(r8)
            boolean r7 = r7.mExpanding
            return r7
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
                        if (rawY > ((float) this.mTouchSlop) && rawY > Math.abs(rawX)) {
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
        final boolean z2;
        if (this.mExpanding) {
            float height = this.mScaler.getHeight();
            boolean z3 = true;
            boolean z4 = this.mOldHeight == ((float) this.mSmallSize);
            if (!z) {
                z2 = (!z4 ? height >= this.mOldHeight || f > 0.0f : height > this.mOldHeight && f >= 0.0f) | (this.mNaturalHeight == ((float) this.mSmallSize));
            } else {
                z2 = !z4;
            }
            if (this.mScaleAnimation.isRunning()) {
                this.mScaleAnimation.cancel();
            }
            this.mCallback.expansionStateChanged(false);
            int naturalHeight = this.mScaler.getNaturalHeight();
            if (!z2) {
                naturalHeight = this.mSmallSize;
            }
            float f2 = (float) naturalHeight;
            int i = (f2 > height ? 1 : (f2 == height ? 0 : -1));
            if (i == 0 || !this.mEnabled) {
                if (i != 0) {
                    this.mScaler.setHeight(f2);
                }
                this.mCallback.setUserExpandedChild(this.mResizedView, z2);
                this.mCallback.setUserLockedChild(this.mResizedView, false);
                this.mScaler.setView((ExpandableView) null);
            } else {
                this.mScaleAnimation.setFloatValues(new float[]{f2});
                this.mScaleAnimation.setupStartValues();
                final ExpandableView expandableView = this.mResizedView;
                this.mScaleAnimation.addListener(new AnimatorListenerAdapter() {
                    public boolean mCancelled;

                    public void onAnimationEnd(Animator animator) {
                        if (!this.mCancelled) {
                            ExpandHelper.this.mCallback.setUserExpandedChild(expandableView, z2);
                            if (!ExpandHelper.this.mExpanding) {
                                ExpandHelper.this.mScaler.setView((ExpandableView) null);
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
                    z3 = false;
                }
                if (z2 != z3) {
                    f = 0.0f;
                }
                this.mFlingAnimationUtils.apply((Animator) this.mScaleAnimation, height, f2, f);
                this.mScaleAnimation.start();
            }
            this.mExpanding = false;
            this.mExpansionStyle = 0;
        }
    }

    private void clearView() {
        this.mResizedView = null;
    }

    public void cancel() {
        finishExpanding(true, 0.0f);
        clearView();
        this.mSGD = new ScaleGestureDetector(this.mContext, this.mScaleGestureListener);
    }

    public void onlyObserveMovements(boolean z) {
        this.mOnlyMovements = z;
    }
}
