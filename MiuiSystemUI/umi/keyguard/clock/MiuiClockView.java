package com.android.keyguard.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import miui.keyguard.clock.KeyguardClockController;

public class MiuiClockView extends FrameLayout {
    private Context mContext;
    private KeyguardClockController mMiuiClockController;

    public MiuiClockView(Context context) {
        this(context, null, 0, 0);
    }

    public MiuiClockView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public MiuiClockView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public MiuiClockView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mContext = context;
        this.mMiuiClockController = new KeyguardClockController(this.mContext, this);
    }

    public void setClockStyle(int i) {
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.setClockStyle(i);
        }
    }

    public void setShowLunarCalendar(int i) {
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.setShowLunarCalendar(i);
        }
    }

    public void setScaleRatio(float f) {
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.setScaleRatio(f);
        }
    }

    public void setTextColorDark(boolean z) {
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.setTextColorDark(z);
        }
    }

    public void setHasTopMargin(boolean z) {
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.setHasTopMargin(z);
        }
    }

    public void setOwnerInfo(String str) {
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.setOwnerInfo(str);
        }
    }

    public void setAutoDualClock(boolean z) {
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.setAutoDualClock(z);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.onAddToWindow();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardClockController keyguardClockController = this.mMiuiClockController;
        if (keyguardClockController != null) {
            keyguardClockController.onRemoveFromWindow();
        }
    }
}
