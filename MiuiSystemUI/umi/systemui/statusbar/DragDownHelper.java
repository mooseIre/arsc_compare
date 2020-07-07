package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.ExpandHelper;
import com.android.systemui.Interpolators;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.plugins.R;

public class DragDownHelper {
    /* access modifiers changed from: private */
    public ExpandHelper.Callback mCallback;
    /* access modifiers changed from: private */
    public DragDownCallback mDragDownCallback;
    private boolean mDraggedFarEnough;
    private boolean mDraggingDown;
    private FalsingManager mFalsingManager;
    private View mHost;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastHeight;
    private int mMinDragDistance;
    private ExpandableView mStartingChild;
    private final int[] mTemp2 = new int[2];
    private float mTouchSlop;

    public interface DragDownCallback {
        void onCrossedThreshold(boolean z);

        void onDragDownReset();

        boolean onDraggedDown(View view, int i);

        void onTouchSlopExceeded();

        void setEmptyDragAmount(float f);
    }

    public DragDownHelper(Context context, View view, ExpandHelper.Callback callback, DragDownCallback dragDownCallback) {
        this.mMinDragDistance = context.getResources().getDimensionPixelSize(R.dimen.keyguard_drag_down_min_distance);
        this.mTouchSlop = (float) ViewConfiguration.get(context).getScaledTouchSlop();
        this.mCallback = callback;
        this.mDragDownCallback = dragDownCallback;
        this.mHost = view;
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mDraggedFarEnough = false;
            this.mDraggingDown = false;
            this.mStartingChild = null;
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
        } else if (actionMasked == 2) {
            float f = y - this.mInitialTouchY;
            if (f > this.mTouchSlop && f > Math.abs(x - this.mInitialTouchX)) {
                this.mFalsingManager.onNotificatonStartDraggingDown();
                this.mDraggingDown = true;
                captureStartingChild(this.mInitialTouchX, this.mInitialTouchY);
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                this.mDragDownCallback.onTouchSlopExceeded();
                return true;
            }
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mDraggingDown) {
            return false;
        }
        motionEvent.getX();
        float y = motionEvent.getY();
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                float f = this.mInitialTouchY;
                this.mLastHeight = y - f;
                captureStartingChild(this.mInitialTouchX, f);
                ExpandableView expandableView = this.mStartingChild;
                if (expandableView != null) {
                    handleExpansion(this.mLastHeight, expandableView);
                } else {
                    this.mDragDownCallback.setEmptyDragAmount(this.mLastHeight);
                }
                if (this.mLastHeight > ((float) this.mMinDragDistance)) {
                    if (!this.mDraggedFarEnough) {
                        this.mDraggedFarEnough = true;
                        this.mDragDownCallback.onCrossedThreshold(true);
                    }
                } else if (this.mDraggedFarEnough) {
                    this.mDraggedFarEnough = false;
                    this.mDragDownCallback.onCrossedThreshold(false);
                }
                return true;
            } else if (actionMasked == 3) {
                stopDragging();
                return false;
            }
        } else if (isFalseTouch() || !this.mDragDownCallback.onDraggedDown(this.mStartingChild, (int) (y - this.mInitialTouchY))) {
            stopDragging();
            return false;
        } else {
            ExpandableView expandableView2 = this.mStartingChild;
            if (expandableView2 == null) {
                this.mDragDownCallback.setEmptyDragAmount(0.0f);
            } else {
                this.mCallback.setUserLockedChild(expandableView2, false);
                this.mStartingChild = null;
            }
            this.mDraggingDown = false;
        }
        return false;
    }

    private boolean isFalseTouch() {
        return this.mFalsingManager.isFalseTouch() || !this.mDraggedFarEnough;
    }

    private void captureStartingChild(float f, float f2) {
        if (this.mStartingChild == null) {
            ExpandableView findView = findView(f, f2);
            this.mStartingChild = findView;
            if (findView != null) {
                this.mCallback.setUserLockedChild(findView, true);
            }
        }
    }

    private void handleExpansion(float f, ExpandableView expandableView) {
        if (f < 0.0f) {
            f = 0.0f;
        }
        boolean isContentExpandable = expandableView.isContentExpandable();
        float f2 = f * (isContentExpandable ? 0.5f : 0.15f);
        if (isContentExpandable && ((float) expandableView.getCollapsedHeight()) + f2 > ((float) expandableView.getMaxContentHeight())) {
            f2 -= ((((float) expandableView.getCollapsedHeight()) + f2) - ((float) expandableView.getMaxContentHeight())) * 0.85f;
        }
        expandableView.setActualHeight((int) (((float) expandableView.getCollapsedHeight()) + f2));
    }

    private void cancelExpansion(final ExpandableView expandableView) {
        if (expandableView.getActualHeight() == expandableView.getCollapsedHeight()) {
            this.mCallback.setUserLockedChild(expandableView, false);
            return;
        }
        ObjectAnimator ofInt = ObjectAnimator.ofInt(expandableView, "actualHeight", new int[]{expandableView.getActualHeight(), expandableView.getCollapsedHeight()});
        ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofInt.setDuration(375);
        ofInt.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                DragDownHelper.this.mCallback.setUserLockedChild(expandableView, false);
            }
        });
        ofInt.start();
    }

    private void cancelExpansion() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mLastHeight, 0.0f});
        ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofFloat.setDuration(375);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                DragDownHelper.this.mDragDownCallback.setEmptyDragAmount(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ofFloat.start();
    }

    private void stopDragging() {
        this.mFalsingManager.onNotificatonStopDraggingDown();
        ExpandableView expandableView = this.mStartingChild;
        if (expandableView != null) {
            cancelExpansion(expandableView);
            this.mStartingChild = null;
        } else {
            cancelExpansion();
        }
        this.mDraggingDown = false;
        this.mDragDownCallback.onDragDownReset();
    }

    private ExpandableView findView(float f, float f2) {
        this.mHost.getLocationOnScreen(this.mTemp2);
        int[] iArr = this.mTemp2;
        return this.mCallback.getChildAtRawPosition(f + ((float) iArr[0]), f2 + ((float) iArr[1]));
    }

    public boolean isDraggingDown() {
        return this.mDraggingDown;
    }
}
