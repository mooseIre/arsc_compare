package com.android.systemui.fsgesture;

import android.util.Log;
import android.view.MotionEvent;

public class GesturesBackController implements IPointerEventListener {
    private GesturesBackCallback mCallback;
    private long mContinuousBackFinishTime;
    private float mDownX;
    private float mDownY;
    private int mDragDirection = -1;
    private int mGestureEdgeLeft;
    private int mGestureEdgeRight;
    private volatile boolean mIsGestureAnimationEnabled = true;
    private int mSwipeStatus = 4;
    private float mWithoutAnimatingDownX;
    private int mWithoutAnimatingDragDirection = -1;

    public interface GesturesBackCallback {
        void onSwipeProcess(boolean z, float f);

        void onSwipeStart(boolean z, float f);

        void onSwipeStop(boolean z, float f);

        void onSwipeStopDirect();
    }

    public GesturesBackController(GesturesBackCallback gesturesBackCallback, int i, int i2) {
        this.mCallback = gesturesBackCallback;
        this.mGestureEdgeLeft = i;
        this.mGestureEdgeRight = i2;
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        Log.d("GesturesBackController", "onPointerEvent swipeStatus:" + this.mSwipeStatus);
        if (this.mSwipeStatus != 16) {
            processPointerEvent(motionEvent);
            Log.d("GesturesBackController", "mSwipeStatus != SWIPE_STATUS_ANIMATING, processPointerEvent");
        } else if (motionEvent.getEventTime() - this.mContinuousBackFinishTime >= 300) {
            this.mSwipeStatus = 4;
            if (motionEvent.getActionMasked() == 0) {
                processPointerEvent(motionEvent);
                Log.d("GesturesBackController", "mSwipeStatus == SWIPE_STATUS_ANIMATING, processPointerEvent");
            }
        } else {
            processPointerEventWithoutAnimating(motionEvent);
            Log.d("GesturesBackController", "mSwipeStatus == SWIPE_STATUS_ANIMATING, processPointerEventWithoutAnimating");
        }
    }

    private void processPointerEventWithoutAnimating(MotionEvent motionEvent) {
        int i;
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        this.mContinuousBackFinishTime = motionEvent.getEventTime();
        Log.d("GesturesBackController", "processPointerEventWithoutAnimating currX:" + rawX + " currY:" + rawY + " mDragDirection:" + this.mDragDirection);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mWithoutAnimatingDownX = rawX;
            if (rawX <= ((float) this.mGestureEdgeLeft)) {
                this.mWithoutAnimatingDragDirection = 1;
            } else if (rawX >= ((float) this.mGestureEdgeRight)) {
                this.mWithoutAnimatingDragDirection = 2;
            }
        } else if ((actionMasked == 1 || actionMasked == 3) && (i = this.mWithoutAnimatingDragDirection) != -1) {
            float f = i == 1 ? rawX - this.mWithoutAnimatingDownX : this.mWithoutAnimatingDownX - rawX;
            int eventTime = (int) (motionEvent.getEventTime() - motionEvent.getDownTime());
            float f2 = f / ((float) eventTime);
            int i2 = (int) f2;
            if (f2 > 2.0f) {
                this.mCallback.onSwipeStopDirect();
            }
            Log.d("GesturesBackController", "processPointerEventWithoutAnimating MotionEvent.ACTION_UP offsetX:" + f + " diffTime:" + eventTime + " speed:" + i2);
            this.mWithoutAnimatingDragDirection = -1;
        }
    }

    private void processPointerEvent(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked != 2) {
                    if (actionMasked != 3) {
                        return;
                    }
                } else if (this.mSwipeStatus != 1) {
                    float f = this.mDragDirection == 1 ? rawX - this.mDownX : this.mDownX - rawX;
                    float abs = Math.abs(rawY - this.mDownY);
                    if (this.mSwipeStatus == 8 && f >= 20.0f && f >= abs / 2.0f) {
                        this.mSwipeStatus = 2;
                        this.mCallback.onSwipeStart(this.mIsGestureAnimationEnabled, rawY);
                    }
                    if (this.mSwipeStatus == 2) {
                        Log.d("GesturesBackController", "onPointerEvent MotionEvent.ACTION_MOVE processMiuiGestures");
                        if (this.mIsGestureAnimationEnabled) {
                            this.mCallback.onSwipeProcess(isFinished(f, (int) (f / ((float) ((int) (motionEvent.getEventTime() - motionEvent.getDownTime()))))), f);
                            return;
                        }
                        return;
                    }
                    return;
                } else {
                    return;
                }
            }
            if (this.mSwipeStatus == 2) {
                int eventTime = (int) (motionEvent.getEventTime() - motionEvent.getDownTime());
                float f2 = this.mDragDirection == 1 ? rawX - this.mDownX : this.mDownX - rawX;
                int i = (int) (f2 / ((float) eventTime));
                boolean isFinished = isFinished(f2, i);
                if (this.mIsGestureAnimationEnabled) {
                    this.mSwipeStatus = 16;
                    this.mCallback.onSwipeStop(isFinished, f2);
                } else if (isFinished) {
                    this.mCallback.onSwipeStopDirect();
                }
                this.mContinuousBackFinishTime = motionEvent.getEventTime();
                Log.d("GesturesBackController", "onPointerEvent MotionEvent.ACTION_UP stopGestures isFinish:" + isFinished + " speed:" + i);
            }
            this.mDragDirection = -1;
            return;
        }
        this.mDownX = rawX;
        this.mDownY = rawY;
        if (rawX <= ((float) this.mGestureEdgeLeft)) {
            this.mSwipeStatus = 8;
            this.mDragDirection = 1;
        } else if (rawX >= ((float) this.mGestureEdgeRight)) {
            this.mSwipeStatus = 8;
            this.mDragDirection = 2;
        } else {
            this.mSwipeStatus = 1;
        }
    }

    /* access modifiers changed from: package-private */
    public void setGestureEdgeWidth(int i, int i2) {
        this.mGestureEdgeLeft = i;
        this.mGestureEdgeRight = i2;
    }

    /* access modifiers changed from: package-private */
    public void enableGestureBackAnimation(boolean z) {
        this.mIsGestureAnimationEnabled = z;
    }

    static float convertOffset(float f) {
        if (f < 0.0f) {
            return 0.0f;
        }
        return (float) (10.0d - (Math.sin((((double) ((Math.min(f, 360.0f) / 2.0f) + 90.0f)) * 3.141592653589793d) / 180.0d) * 10.0d));
    }

    static boolean isFinished(float f, int i) {
        return f >= 0.0f && ((Math.min(f, 360.0f) / 2.0f) + 90.0f > 180.0f || i > 2);
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        this.mSwipeStatus = 1;
    }
}
