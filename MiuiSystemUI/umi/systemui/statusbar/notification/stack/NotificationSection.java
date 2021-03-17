package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.view.View;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.row.ExpandableView;

public class NotificationSection {
    private ObjectAnimator mBottomAnimator = null;
    private Rect mBounds = new Rect();
    private int mBucket;
    private Rect mCurrentBounds = new Rect(-1, -1, -1, -1);
    private Rect mEndAnimationRect = new Rect();
    private ExpandableView mFirstVisibleChild;
    private ExpandableView mLastVisibleChild;
    private View mOwningView;
    private Rect mStartAnimationRect = new Rect();
    private ObjectAnimator mTopAnimator = null;

    NotificationSection(View view, int i) {
        this.mOwningView = view;
        this.mBucket = i;
    }

    public void cancelAnimators() {
        ObjectAnimator objectAnimator = this.mBottomAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        ObjectAnimator objectAnimator2 = this.mTopAnimator;
        if (objectAnimator2 != null) {
            objectAnimator2.cancel();
        }
    }

    public Rect getCurrentBounds() {
        return this.mCurrentBounds;
    }

    public Rect getBounds() {
        return this.mBounds;
    }

    public boolean didBoundsChange() {
        return !this.mCurrentBounds.equals(this.mBounds);
    }

    public boolean areBoundsAnimating() {
        return (this.mBottomAnimator == null && this.mTopAnimator == null) ? false : true;
    }

    public int getBucket() {
        return this.mBucket;
    }

    public void startBackgroundAnimation(boolean z, boolean z2) {
        Rect rect = this.mCurrentBounds;
        Rect rect2 = this.mBounds;
        rect.left = rect2.left;
        rect.right = rect2.right;
        startBottomAnimation(z2);
        startTopAnimation(z);
    }

    private void startTopAnimation(boolean z) {
        int i = this.mEndAnimationRect.top;
        int i2 = this.mBounds.top;
        ObjectAnimator objectAnimator = this.mTopAnimator;
        if (objectAnimator != null && i == i2) {
            return;
        }
        if (z) {
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "backgroundTop", this.mCurrentBounds.top, i2);
            ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofInt.setDuration(300L);
            ofInt.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.statusbar.notification.stack.NotificationSection.AnonymousClass1 */

                public void onAnimationEnd(Animator animator) {
                    NotificationSection.this.mStartAnimationRect.top = -1;
                    NotificationSection.this.mEndAnimationRect.top = -1;
                    NotificationSection.this.mTopAnimator = null;
                }
            });
            ofInt.start();
            this.mStartAnimationRect.top = this.mCurrentBounds.top;
            this.mEndAnimationRect.top = i2;
            this.mTopAnimator = ofInt;
        } else if (objectAnimator != null) {
            int i3 = this.mStartAnimationRect.top;
            objectAnimator.getValues()[0].setIntValues(i3, i2);
            this.mStartAnimationRect.top = i3;
            this.mEndAnimationRect.top = i2;
            objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
        } else {
            setBackgroundTop(i2);
        }
    }

    private void startBottomAnimation(boolean z) {
        int i = this.mStartAnimationRect.bottom;
        int i2 = this.mEndAnimationRect.bottom;
        int i3 = this.mBounds.bottom;
        ObjectAnimator objectAnimator = this.mBottomAnimator;
        if (objectAnimator != null && i2 == i3) {
            return;
        }
        if (z) {
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "backgroundBottom", this.mCurrentBounds.bottom, i3);
            ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofInt.setDuration(300L);
            ofInt.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.statusbar.notification.stack.NotificationSection.AnonymousClass2 */

                public void onAnimationEnd(Animator animator) {
                    NotificationSection.this.mStartAnimationRect.bottom = -1;
                    NotificationSection.this.mEndAnimationRect.bottom = -1;
                    NotificationSection.this.mBottomAnimator = null;
                }
            });
            ofInt.start();
            this.mStartAnimationRect.bottom = this.mCurrentBounds.bottom;
            this.mEndAnimationRect.bottom = i3;
            this.mBottomAnimator = ofInt;
        } else if (objectAnimator != null) {
            objectAnimator.getValues()[0].setIntValues(i, i3);
            this.mStartAnimationRect.bottom = i;
            this.mEndAnimationRect.bottom = i3;
            objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
        } else {
            setBackgroundBottom(i3);
        }
    }

    private void setBackgroundTop(int i) {
        this.mCurrentBounds.top = i;
        this.mOwningView.invalidate();
    }

    private void setBackgroundBottom(int i) {
        this.mCurrentBounds.bottom = i;
        this.mOwningView.invalidate();
    }

    public ExpandableView getFirstVisibleChild() {
        return this.mFirstVisibleChild;
    }

    public ExpandableView getLastVisibleChild() {
        return this.mLastVisibleChild;
    }

    public boolean setFirstVisibleChild(ExpandableView expandableView) {
        boolean z = this.mFirstVisibleChild != expandableView;
        this.mFirstVisibleChild = expandableView;
        return z;
    }

    public boolean setLastVisibleChild(ExpandableView expandableView) {
        boolean z = this.mLastVisibleChild != expandableView;
        this.mLastVisibleChild = expandableView;
        return z;
    }

    public void resetCurrentBounds() {
        this.mCurrentBounds.set(this.mBounds);
    }

    public boolean isTargetTop(int i) {
        return (this.mTopAnimator == null && this.mCurrentBounds.top == i) || (this.mTopAnimator != null && this.mEndAnimationRect.top == i);
    }

    public boolean isTargetBottom(int i) {
        return (this.mBottomAnimator == null && this.mCurrentBounds.bottom == i) || (this.mBottomAnimator != null && this.mEndAnimationRect.bottom == i);
    }

    public int updateBounds(int i, int i2, boolean z) {
        int i3;
        int i4;
        int i5;
        ExpandableView firstVisibleChild = getFirstVisibleChild();
        if (firstVisibleChild != null) {
            int ceil = (int) Math.ceil((double) ViewState.getFinalTranslationY(firstVisibleChild));
            if (isTargetTop(ceil)) {
                i5 = ceil;
            } else {
                i5 = (int) Math.ceil((double) firstVisibleChild.getTranslationY());
            }
            i3 = Math.max(i5, i);
            if (firstVisibleChild.showingPulsing()) {
                i4 = Math.max(i, ceil + ExpandableViewState.getFinalActualHeight(firstVisibleChild));
                if (z) {
                    Rect rect = this.mBounds;
                    rect.left = (int) (((float) rect.left) + Math.max(firstVisibleChild.getTranslation(), 0.0f));
                    Rect rect2 = this.mBounds;
                    rect2.right = (int) (((float) rect2.right) + Math.min(firstVisibleChild.getTranslation(), 0.0f));
                }
            } else {
                i4 = i;
            }
        } else {
            i4 = i;
            i3 = i4;
        }
        int max = Math.max(i, i3);
        ExpandableView lastVisibleChild = getLastVisibleChild();
        if (lastVisibleChild != null) {
            int floor = (int) Math.floor((double) ((ViewState.getFinalTranslationY(lastVisibleChild) + ((float) ExpandableViewState.getFinalActualHeight(lastVisibleChild))) - ((float) lastVisibleChild.getClipBottomAmount())));
            if (!isTargetBottom(floor)) {
                floor = (int) ((lastVisibleChild.getTranslationY() + ((float) lastVisibleChild.getActualHeight())) - ((float) lastVisibleChild.getClipBottomAmount()));
                i2 = (int) Math.min(lastVisibleChild.getTranslationY() + ((float) lastVisibleChild.getActualHeight()), (float) i2);
            }
            i4 = Math.max(i4, Math.max(floor, i2));
        }
        int max2 = Math.max(max, i4);
        Rect rect3 = this.mBounds;
        rect3.top = max;
        rect3.bottom = max2;
        return max2;
    }

    public boolean needsBackground() {
        return (this.mFirstVisibleChild == null || this.mBucket == 1) ? false : true;
    }
}
