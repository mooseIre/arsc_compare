package com.android.keyguard.fod;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.view.WindowManager;

/* access modifiers changed from: package-private */
public abstract class GxzwNoRotateFrameLayout extends GxzwWindowFrameLayout {
    protected boolean mKeyguardAuthen = false;
    private boolean mPortraitOrientation = true;
    protected Rect mRegion = caculateRegion();
    protected boolean mShowing = false;

    /* access modifiers changed from: protected */
    public abstract Rect caculateRegion();

    public GxzwNoRotateFrameLayout(Context context) {
        super(context);
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public Rect getRegion() {
        return this.mRegion;
    }

    /* access modifiers changed from: protected */
    public void show() {
        boolean z = true;
        this.mShowing = true;
        if (getContext().getResources().getConfiguration().orientation != 1 && !this.mKeyguardAuthen) {
            z = false;
        }
        this.mPortraitOrientation = z;
        this.mRegion = caculateRegion();
        updateLpByOrientation();
    }

    /* access modifiers changed from: protected */
    public void dismiss() {
        this.mShowing = false;
    }

    /* access modifiers changed from: protected */
    public void onKeyguardAuthen(boolean z) {
        this.mKeyguardAuthen = z;
        boolean z2 = true;
        if (getContext().getResources().getConfiguration().orientation != 1 && !this.mKeyguardAuthen) {
            z2 = false;
        }
        updateOrientation(z2);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        boolean z = true;
        if (configuration.orientation != 1 && !this.mKeyguardAuthen) {
            z = false;
        }
        updateOrientation(z);
    }

    private void updateOrientation(boolean z) {
        if (z != this.mPortraitOrientation && isAttachedToWindow()) {
            this.mPortraitOrientation = z;
            updateLpByOrientation();
            this.mWindowManager.updateViewLayout(this, generateLayoutParams());
        }
    }

    private void updateLpByOrientation() {
        int i;
        int i2;
        int i3;
        int i4;
        WindowManager.LayoutParams generateLayoutParams = generateLayoutParams();
        if (this.mPortraitOrientation) {
            Rect rect = this.mRegion;
            i3 = rect.left;
            i2 = rect.top;
            i4 = rect.width();
            i = this.mRegion.height();
        } else {
            Rect rect2 = this.mRegion;
            i2 = rect2.left;
            i3 = rect2.top;
            i = rect2.width();
            i4 = this.mRegion.height();
        }
        Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        int i5 = point.x;
        int i6 = point.y;
        int rotation = display.getRotation();
        if (!this.mKeyguardAuthen && (rotation == 2 || rotation == 3)) {
            int i7 = this.mPortraitOrientation ? i5 : i6;
            if (this.mPortraitOrientation) {
                i5 = i6;
            }
            i3 = (i7 - i3) - i4;
            i2 = (i5 - i2) - i;
        }
        generateLayoutParams.width = i4;
        generateLayoutParams.height = i;
        generateLayoutParams.x = i3;
        generateLayoutParams.y = i2;
    }
}
