package com.android.systemui.bubbles;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.Dependency;

class BubbleTouchHandler implements View.OnTouchListener {
    private final BubbleData mBubbleData;
    private BubbleController mController = ((BubbleController) Dependency.get(BubbleController.class));
    private boolean mInDismissTarget;
    private boolean mMovedEnough;
    private final BubbleStackView mStack;
    private final PointF mTouchDown = new PointF();
    private int mTouchSlopSquared;
    private View mTouchedView;
    private VelocityTracker mVelocityTracker;
    private final PointF mViewPositionOnTouchDown = new PointF();

    BubbleTouchHandler(BubbleStackView bubbleStackView, BubbleData bubbleData, Context context) {
        new Handler();
        int scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mTouchSlopSquared = scaledTouchSlop * scaledTouchSlop;
        this.mBubbleData = bubbleData;
        this.mStack = bubbleStackView;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        View view2;
        String str;
        MotionEvent motionEvent2 = motionEvent;
        int actionMasked = motionEvent.getActionMasked();
        if (this.mTouchedView == null) {
            this.mTouchedView = this.mStack.getTargetView(motionEvent2);
        }
        if (actionMasked == 4 || (view2 = this.mTouchedView) == null) {
            this.mBubbleData.setExpanded(false);
            resetForNextGesture();
            return false;
        }
        boolean equals = this.mStack.equals(view2);
        boolean equals2 = this.mStack.getFlyoutView().equals(this.mTouchedView);
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        PointF pointF = this.mViewPositionOnTouchDown;
        PointF pointF2 = this.mTouchDown;
        float f = (pointF.x + rawX) - pointF2.x;
        float f2 = (pointF.y + rawY) - pointF2.y;
        if (actionMasked == 0) {
            trackMovement(motionEvent2);
            this.mTouchDown.set(rawX, rawY);
            this.mStack.onGestureStart();
            if (equals) {
                this.mViewPositionOnTouchDown.set(this.mStack.getStackPosition());
                this.mStack.onDragStart();
            } else if (equals2) {
                this.mStack.onFlyoutDragStart();
            } else {
                this.mViewPositionOnTouchDown.set(this.mTouchedView.getTranslationX(), this.mTouchedView.getTranslationY());
                this.mStack.onBubbleDragStart(this.mTouchedView);
            }
        } else if (actionMasked == 1) {
            trackMovement(motionEvent2);
            this.mVelocityTracker.computeCurrentVelocity(1000);
            float xVelocity = this.mVelocityTracker.getXVelocity();
            float yVelocity = this.mVelocityTracker.getYVelocity();
            boolean z = !equals ? this.mInDismissTarget || yVelocity > 6000.0f : this.mInDismissTarget || isFastFlingTowardsDismissTarget(rawX, rawY, xVelocity, yVelocity);
            if (equals2 && this.mMovedEnough) {
                this.mStack.onFlyoutDragFinished(rawX - this.mTouchDown.x, xVelocity);
            } else if (z) {
                if (equals) {
                    str = null;
                } else {
                    str = ((BubbleView) this.mTouchedView).getKey();
                }
                this.mStack.magnetToStackIfNeededThenAnimateDismissal(this.mTouchedView, xVelocity, yVelocity, new Runnable(equals, str) {
                    public final /* synthetic */ boolean f$1;
                    public final /* synthetic */ String f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        BubbleTouchHandler.this.lambda$onTouch$0$BubbleTouchHandler(this.f$1, this.f$2);
                    }
                });
            } else if (equals2) {
                if (!this.mBubbleData.isExpanded() && !this.mMovedEnough) {
                    this.mBubbleData.setExpanded(true);
                }
            } else if (this.mMovedEnough) {
                if (equals) {
                    this.mStack.onDragFinish(f, f2, xVelocity, yVelocity);
                } else {
                    this.mStack.onBubbleDragFinish(this.mTouchedView, f, f2, xVelocity, yVelocity);
                }
            } else if (this.mTouchedView == this.mStack.getExpandedBubbleView()) {
                this.mBubbleData.setExpanded(false);
            } else if (equals || equals2) {
                BubbleData bubbleData = this.mBubbleData;
                bubbleData.setExpanded(!bubbleData.isExpanded());
            } else {
                String key = ((BubbleView) this.mTouchedView).getKey();
                BubbleData bubbleData2 = this.mBubbleData;
                bubbleData2.setSelectedBubble(bubbleData2.getBubbleWithKey(key));
            }
            resetForNextGesture();
        } else if (actionMasked == 2) {
            trackMovement(motionEvent2);
            PointF pointF3 = this.mTouchDown;
            float f3 = rawX - pointF3.x;
            float f4 = rawY - pointF3.y;
            if ((f3 * f3) + (f4 * f4) > ((float) this.mTouchSlopSquared) && !this.mMovedEnough) {
                this.mMovedEnough = true;
            }
            if (this.mMovedEnough) {
                if (equals) {
                    this.mStack.onDragged(f, f2);
                } else if (equals2) {
                    this.mStack.onFlyoutDragged(f3);
                } else {
                    this.mStack.onBubbleDragged(this.mTouchedView, f, f2);
                }
            }
            boolean isInDismissTarget = this.mStack.isInDismissTarget(motionEvent2);
            if (isInDismissTarget != this.mInDismissTarget) {
                this.mInDismissTarget = isInDismissTarget;
                this.mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity2 = this.mVelocityTracker.getXVelocity();
                float yVelocity2 = this.mVelocityTracker.getYVelocity();
                if (!equals2) {
                    this.mStack.animateMagnetToDismissTarget(this.mTouchedView, this.mInDismissTarget, f, f2, xVelocity2, yVelocity2);
                }
            }
        } else if (actionMasked == 3) {
            resetForNextGesture();
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onTouch$0 */
    public /* synthetic */ void lambda$onTouch$0$BubbleTouchHandler(boolean z, String str) {
        if (z) {
            this.mController.dismissStack(1);
        } else {
            this.mController.removeBubble(str, 1);
        }
    }

    private boolean isFastFlingTowardsDismissTarget(float f, float f2, float f3, float f4) {
        if (f4 <= 0.0f) {
            return false;
        }
        if (f3 != 0.0f) {
            float f5 = f4 / f3;
            f = (((float) this.mStack.getHeight()) - (f2 - (f * f5))) / f5;
        }
        float width = ((float) this.mStack.getWidth()) * 0.5f;
        if (f4 <= 4000.0f) {
            return false;
        }
        float f6 = width / 2.0f;
        if (f <= f6 || f >= ((float) this.mStack.getWidth()) - f6) {
            return false;
        }
        return true;
    }

    private void resetForNextGesture() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
        this.mTouchedView = null;
        this.mMovedEnough = false;
        this.mInDismissTarget = false;
        this.mStack.onGestureFinished();
    }

    private void trackMovement(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
    }
}
