package com.android.systemui.pip.phone;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import com.android.systemui.pip.PipCompat;
import java.io.PrintWriter;

public class PipTouchState {
    private int mActivePointerId;
    private boolean mAllowDraggingOffscreen = false;
    private boolean mAllowTouches = true;
    private final PointF mDownDelta = new PointF();
    private final PointF mDownTouch = new PointF();
    private boolean mIsDragging = false;
    private boolean mIsUserInteracting = false;
    private final PointF mLastDelta = new PointF();
    private final PointF mLastTouch = new PointF();
    private boolean mStartedDragging = false;
    private final PointF mVelocity = new PointF();
    private VelocityTracker mVelocityTracker;
    private ViewConfiguration mViewConfig;

    public PipTouchState(ViewConfiguration viewConfiguration) {
        this.mViewConfig = viewConfiguration;
    }

    public void reset() {
        this.mAllowDraggingOffscreen = false;
        this.mIsDragging = false;
        this.mStartedDragging = false;
        this.mIsUserInteracting = false;
    }

    public void onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        int i = 0;
        if (action != 0) {
            if (action != 1) {
                if (action != 2) {
                    if (action != 3) {
                        if (action == 6 && this.mIsUserInteracting) {
                            this.mVelocityTracker.addMovement(motionEvent);
                            int actionIndex = motionEvent.getActionIndex();
                            if (motionEvent.getPointerId(actionIndex) == this.mActivePointerId) {
                                if (actionIndex == 0) {
                                    i = 1;
                                }
                                this.mActivePointerId = motionEvent.getPointerId(i);
                                Log.e("PipTouchHandler", "Relinquish active pointer id on POINTER_UP: " + this.mActivePointerId);
                                this.mLastTouch.set(PipCompat.getTouchXForPointId(motionEvent, i), PipCompat.getTouchYForPointId(motionEvent, i));
                                return;
                            }
                            return;
                        }
                        return;
                    }
                } else if (this.mIsUserInteracting) {
                    this.mVelocityTracker.addMovement(motionEvent);
                    int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex == -1) {
                        Log.e("PipTouchHandler", "Invalid active pointer id on MOVE: " + this.mActivePointerId);
                        return;
                    }
                    float touchXForPointId = PipCompat.getTouchXForPointId(motionEvent, findPointerIndex);
                    float touchYForPointId = PipCompat.getTouchYForPointId(motionEvent, findPointerIndex);
                    PointF pointF = this.mLastDelta;
                    PointF pointF2 = this.mLastTouch;
                    pointF.set(touchXForPointId - pointF2.x, touchYForPointId - pointF2.y);
                    PointF pointF3 = this.mDownDelta;
                    PointF pointF4 = this.mDownTouch;
                    pointF3.set(touchXForPointId - pointF4.x, touchYForPointId - pointF4.y);
                    boolean z = this.mDownDelta.length() > ((float) this.mViewConfig.getScaledTouchSlop());
                    if (this.mIsDragging) {
                        this.mStartedDragging = false;
                    } else if (z) {
                        this.mIsDragging = true;
                        this.mStartedDragging = true;
                    }
                    this.mLastTouch.set(touchXForPointId, touchYForPointId);
                    return;
                } else {
                    return;
                }
            } else if (this.mIsUserInteracting) {
                this.mVelocityTracker.addMovement(motionEvent);
                this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mViewConfig.getScaledMaximumFlingVelocity());
                this.mVelocity.set(this.mVelocityTracker.getXVelocity(), this.mVelocityTracker.getYVelocity());
                int findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                if (findPointerIndex2 == -1) {
                    Log.e("PipTouchHandler", "Invalid active pointer id on UP: " + this.mActivePointerId);
                    return;
                }
                this.mLastTouch.set(PipCompat.getTouchXForPointId(motionEvent, findPointerIndex2), PipCompat.getTouchYForPointId(motionEvent, findPointerIndex2));
            } else {
                return;
            }
            recycleVelocityTracker();
        } else if (this.mAllowTouches) {
            initOrResetVelocityTracker();
            this.mActivePointerId = motionEvent.getPointerId(0);
            Log.e("PipTouchHandler", "Setting active pointer id on DOWN: " + this.mActivePointerId);
            this.mLastTouch.set(PipCompat.getTouchX(motionEvent), PipCompat.getTouchY(motionEvent));
            this.mDownTouch.set(this.mLastTouch);
            this.mAllowDraggingOffscreen = true;
            this.mIsUserInteracting = true;
        }
    }

    public PointF getVelocity() {
        return this.mVelocity;
    }

    public PointF getLastTouchPosition() {
        return this.mLastTouch;
    }

    public PointF getLastTouchDelta() {
        return this.mLastDelta;
    }

    public PointF getDownTouchPosition() {
        return this.mDownTouch;
    }

    public boolean isDragging() {
        return this.mIsDragging;
    }

    public boolean isUserInteracting() {
        return this.mIsUserInteracting;
    }

    public boolean startedDragging() {
        return this.mStartedDragging;
    }

    public void setAllowTouches(boolean z) {
        this.mAllowTouches = z;
        if (this.mIsUserInteracting) {
            reset();
        }
    }

    public boolean allowDraggingOffscreen() {
        return this.mAllowDraggingOffscreen;
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipTouchHandler");
        printWriter.println(str2 + "mAllowTouches=" + this.mAllowTouches);
        printWriter.println(str2 + "mActivePointerId=" + this.mActivePointerId);
        printWriter.println(str2 + "mDownTouch=" + this.mDownTouch);
        printWriter.println(str2 + "mDownDelta=" + this.mDownDelta);
        printWriter.println(str2 + "mLastTouch=" + this.mLastTouch);
        printWriter.println(str2 + "mLastDelta=" + this.mLastDelta);
        printWriter.println(str2 + "mVelocity=" + this.mVelocity);
        printWriter.println(str2 + "mIsUserInteracting=" + this.mIsUserInteracting);
        printWriter.println(str2 + "mIsDragging=" + this.mIsDragging);
        printWriter.println(str2 + "mStartedDragging=" + this.mStartedDragging);
        printWriter.println(str2 + "mAllowDraggingOffscreen=" + this.mAllowDraggingOffscreen);
    }
}
