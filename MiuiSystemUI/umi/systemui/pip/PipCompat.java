package com.android.systemui.pip;

import android.graphics.Rect;
import android.view.MotionEvent;

public class PipCompat {
    public static float getPipBoundsLeft(Rect rect, float f, float f2) {
        return f + f2;
    }

    public static float getPipBoundsTop(Rect rect, float f, float f2) {
        return f + f2;
    }

    public static float getTouchX(MotionEvent motionEvent) {
        return motionEvent.getRawX();
    }

    public static float getTouchY(MotionEvent motionEvent) {
        return motionEvent.getRawY();
    }

    public static float getTouchXForPointId(MotionEvent motionEvent, int i) {
        return motionEvent.getRawX(i);
    }

    public static float getTouchYForPointId(MotionEvent motionEvent, int i) {
        return motionEvent.getRawY(i);
    }
}
