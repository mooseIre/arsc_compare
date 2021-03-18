package com.android.keyguard.fod;

import android.content.Context;
import android.graphics.Rect;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;

/* access modifiers changed from: package-private */
public class MiuiGxzwTouchHelper {
    private final Context mContext;
    private final MiuiGxzwIconView mMiuiGxzwIconView;
    private final MiuiGxzwQuickOpenView mMiuiGxzwQuickOpenView;
    private final PowerManager mPowerManager;
    private boolean mTouchDown = false;
    private int mValidRegionCount;

    MiuiGxzwTouchHelper(MiuiGxzwIconView miuiGxzwIconView, MiuiGxzwQuickOpenView miuiGxzwQuickOpenView) {
        Context context = miuiGxzwIconView.getContext();
        this.mContext = context;
        this.mMiuiGxzwIconView = miuiGxzwIconView;
        this.mMiuiGxzwQuickOpenView = miuiGxzwQuickOpenView;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
    }

    public boolean onTouch(MotionEvent motionEvent) {
        String str;
        int findFodTouchEventIndex = findFodTouchEventIndex(motionEvent);
        float x = motionEvent.getX(findFodTouchEventIndex);
        float y = motionEvent.getY(findFodTouchEventIndex);
        float pressure = motionEvent.getPressure(findFodTouchEventIndex) * 2048.0f;
        float toolMinor = motionEvent.getToolMinor(findFodTouchEventIndex);
        float orientation = motionEvent.getOrientation(findFodTouchEventIndex);
        float touchMajor = motionEvent.getTouchMajor(findFodTouchEventIndex);
        float touchMinor = motionEvent.getTouchMinor(findFodTouchEventIndex);
        int caculateAction = caculateAction(motionEvent, findFodTouchEventIndex);
        if (MiuiGxzwUtils.isLargeFod()) {
            str = String.format("onTouch: originalAction = %s, action = %s, x = %f, y = %f, pressure = %f, area = %f, angle = %f, major = %f, minor = %f", MotionEvent.actionToString(motionEvent.getAction()), MotionEvent.actionToString(caculateAction), Float.valueOf(x), Float.valueOf(y), Float.valueOf(pressure), Float.valueOf(toolMinor), Float.valueOf(orientation), Float.valueOf(touchMajor), Float.valueOf(touchMinor));
        } else {
            str = String.format("onTouch: originalAction = %s, action = %s, x = %f, y = %f, pressure = %f, area = %f", MotionEvent.actionToString(motionEvent.getAction()), MotionEvent.actionToString(caculateAction), Float.valueOf(x), Float.valueOf(y), Float.valueOf(pressure), Float.valueOf(toolMinor));
        }
        if (motionEvent.getAction() != 2 || (toolMinor > 0.0f && !this.mTouchDown)) {
            Slog.i("MiuiGxzwTouchHelper", str);
        } else {
            Log.i("MiuiGxzwTouchHelper", str);
        }
        if (this.mMiuiGxzwQuickOpenView.isShow()) {
            dispatchTouchEventForQuickOpenView(caculateAction, x, y);
        }
        if (!this.mMiuiGxzwIconView.isShowing()) {
            return false;
        }
        if (!this.mMiuiGxzwIconView.isDozing()) {
            userActivity();
        }
        if (caculateAction == 0) {
            handleActionDown(x, y, orientation, touchMajor, touchMinor, toolMinor);
        } else if (caculateAction == 1) {
            handleAciontUp();
        } else if (caculateAction == 2) {
            handleActionMove(x, y, orientation, touchMajor, touchMinor, toolMinor, pressure);
        }
        return true;
    }

    private void handleActionDown(float f, float f2, float f3, float f4, float f5, float f6) {
        if (!this.mTouchDown && this.mMiuiGxzwIconView.isShowing() && f6 > 0.0f) {
            this.mMiuiGxzwIconView.setCanvasInfo(f, f2, f3, f4, f5);
            onTouchDown();
        }
    }

    private void handleAciontUp() {
        if (this.mTouchDown && this.mMiuiGxzwIconView.isShowing()) {
            onTouchUp(true);
        }
    }

    private void handleActionMove(float f, float f2, float f3, float f4, float f5, float f6, float f7) {
        if (!this.mTouchDown && f6 > 0.0f) {
            this.mMiuiGxzwIconView.setCanvasInfo(f, f2, f3, f4, f5);
            onTouchDown();
        }
        if (!isInValidRegion(f, f2) && this.mTouchDown && this.mMiuiGxzwIconView.isShowing()) {
            onTouchUp(true);
        }
    }

    private int findFodTouchEventIndex(MotionEvent motionEvent) {
        if (motionEvent.getPointerCount() == 1) {
            return 0;
        }
        Rect region = this.mMiuiGxzwIconView.getRegion();
        float width = ((float) region.width()) / 2.0f;
        float height = ((float) region.height()) / 2.0f;
        float f = Float.MAX_VALUE;
        int pointerCount = motionEvent.getPointerCount();
        int i = 0;
        for (int i2 = 0; i2 < pointerCount; i2++) {
            float x = motionEvent.getX(i2) - width;
            float y = motionEvent.getY(i2) - height;
            float pow = (float) Math.pow((double) ((x * x) + (y * y)), 2.0d);
            if (pow < f) {
                i = i2;
                f = pow;
            }
        }
        return i;
    }

    private void dispatchTouchEventForQuickOpenView(int i, float f, float f2) {
        Rect region = this.mMiuiGxzwIconView.getRegion();
        if (i != 0) {
            if (i != 1) {
                if (i == 2) {
                    this.mMiuiGxzwQuickOpenView.onTouchMove(f + ((float) region.left), f2 + ((float) region.top));
                    return;
                } else if (i != 3) {
                    return;
                }
            }
            this.mMiuiGxzwQuickOpenView.onTouchUp(f + ((float) region.left), f2 + ((float) region.top));
            return;
        }
        this.mMiuiGxzwQuickOpenView.onTouchDown(f + ((float) region.left), f2 + ((float) region.top));
    }

    private boolean isInValidRegion(float f, float f2) {
        Rect region = this.mMiuiGxzwIconView.getRegion();
        if (f >= 0.0f && f2 >= 0.0f && f <= ((float) (region.width() + 0)) && f2 <= ((float) (region.height() + 0))) {
            this.mValidRegionCount = 0;
        } else {
            this.mValidRegionCount++;
        }
        return this.mValidRegionCount < 3;
    }

    private void userActivity() {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
    }

    private void onTouchDown() {
        if (!this.mTouchDown) {
            this.mValidRegionCount = 0;
            this.mTouchDown = true;
            this.mMiuiGxzwIconView.onTouchDown();
        }
    }

    /* access modifiers changed from: package-private */
    public void onTouchUp(boolean z) {
        if (this.mTouchDown) {
            this.mTouchDown = false;
            this.mValidRegionCount = 0;
            this.mMiuiGxzwIconView.onTouchUp(z);
        }
    }

    private int caculateAction(MotionEvent motionEvent, int i) {
        int action = motionEvent.getAction();
        if (action == 0) {
            return 0;
        }
        if (action == 1) {
            return 1;
        }
        if (action != 2) {
            if (action == 3) {
                return 1;
            }
            if ((action == 6 || action == 262 || action == 518) && i == motionEvent.getActionIndex()) {
                return 1;
            }
        }
        return 2;
    }
}
