package com.android.keyguard.fod;

import android.content.Context;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.FrameLayout;

public abstract class GxzwWindowFrameLayout extends FrameLayout {
    private boolean mAdded = false;
    public final Handler mHandler = new Handler();
    protected final WindowManager mWindowManager = ((WindowManager) getContext().getSystemService("window"));

    /* access modifiers changed from: protected */
    public abstract WindowManager.LayoutParams generateLayoutParams();

    public GxzwWindowFrameLayout(Context context) {
        super(context);
    }

    public void addViewToWindow() {
        this.mAdded = true;
        if (!isAttachedToWindow() && getParent() == null) {
            this.mWindowManager.addView(this, generateLayoutParams());
        }
    }

    public void removeViewFromWindow() {
        this.mAdded = false;
        if (isAttachedToWindow()) {
            this.mWindowManager.removeView(this);
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$GxzwWindowFrameLayout$zcil8p0vmCZMbI4psM5rDbfIPg */

            public final void run() {
                GxzwWindowFrameLayout.m6lambda$zcil8p0vmCZMbI4psM5rDbfIPg(GxzwWindowFrameLayout.this);
            }
        });
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$GxzwWindowFrameLayout$zcil8p0vmCZMbI4psM5rDbfIPg */

            public final void run() {
                GxzwWindowFrameLayout.m6lambda$zcil8p0vmCZMbI4psM5rDbfIPg(GxzwWindowFrameLayout.this);
            }
        });
    }

    /* access modifiers changed from: public */
    private void updateViewAddState() {
        if (this.mAdded && !isAttachedToWindow()) {
            addViewToWindow();
        } else if (!this.mAdded && isAttachedToWindow()) {
            removeViewFromWindow();
        }
    }
}
