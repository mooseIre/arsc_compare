package com.android.systemui.util;

import android.graphics.PointF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: RelativeTouchListener.kt */
public abstract class RelativeTouchListener implements View.OnTouchListener {
    private final Handler handler = new Handler();
    private boolean movedEnough;
    private boolean performedLongClick;
    private final PointF touchDown = new PointF();
    private int touchSlop = -1;
    private final VelocityTracker velocityTracker = VelocityTracker.obtain();
    private final PointF viewPositionOnTouchDown = new PointF();

    public abstract boolean onDown(@NotNull View view, @NotNull MotionEvent motionEvent);

    public abstract void onMove(@NotNull View view, @NotNull MotionEvent motionEvent, float f, float f2, float f3, float f4);

    public abstract void onUp(@NotNull View view, @NotNull MotionEvent motionEvent, float f, float f2, float f3, float f4, float f5, float f6);

    public boolean onTouch(@NotNull View view, @NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        Intrinsics.checkParameterIsNotNull(motionEvent, "ev");
        addMovement(motionEvent);
        float rawX = motionEvent.getRawX() - this.touchDown.x;
        float rawY = motionEvent.getRawY() - this.touchDown.y;
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action == 1) {
                if (this.movedEnough) {
                    this.velocityTracker.computeCurrentVelocity(1000);
                    PointF pointF = this.viewPositionOnTouchDown;
                    float f = pointF.x;
                    float f2 = pointF.y;
                    VelocityTracker velocityTracker2 = this.velocityTracker;
                    Intrinsics.checkExpressionValueIsNotNull(velocityTracker2, "velocityTracker");
                    float xVelocity = velocityTracker2.getXVelocity();
                    VelocityTracker velocityTracker3 = this.velocityTracker;
                    Intrinsics.checkExpressionValueIsNotNull(velocityTracker3, "velocityTracker");
                    onUp(view, motionEvent, f, f2, rawX, rawY, xVelocity, velocityTracker3.getYVelocity());
                } else if (!this.performedLongClick) {
                    view.performClick();
                } else {
                    this.handler.removeCallbacksAndMessages(null);
                }
                this.velocityTracker.clear();
                this.movedEnough = false;
            } else if (action == 2) {
                if (!this.movedEnough && ((float) Math.hypot((double) rawX, (double) rawY)) > ((float) this.touchSlop) && !this.performedLongClick) {
                    this.movedEnough = true;
                    this.handler.removeCallbacksAndMessages(null);
                }
                if (this.movedEnough) {
                    PointF pointF2 = this.viewPositionOnTouchDown;
                    onMove(view, motionEvent, pointF2.x, pointF2.y, rawX, rawY);
                }
            }
        } else if (!onDown(view, motionEvent)) {
            return false;
        } else {
            ViewConfiguration viewConfiguration = ViewConfiguration.get(view.getContext());
            Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(v.context)");
            this.touchSlop = viewConfiguration.getScaledTouchSlop();
            this.touchDown.set(motionEvent.getRawX(), motionEvent.getRawY());
            this.viewPositionOnTouchDown.set(view.getTranslationX(), view.getTranslationY());
            this.performedLongClick = false;
            this.handler.postDelayed(new RelativeTouchListener$onTouch$1(this, view), (long) ViewConfiguration.getLongPressTimeout());
        }
        return true;
    }

    private final void addMovement(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        this.velocityTracker.addMovement(motionEvent);
        motionEvent.offsetLocation(-rawX, -rawY);
    }
}
