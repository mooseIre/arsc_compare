package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.Constants;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.BatteryController;

public class BatteryIndicator extends ImageView implements DemoMode, BatteryController.BatteryStateChangeCallback {
    private int mClipWidth;
    private boolean mDemoMode;
    protected boolean mDisabled = false;
    protected int mDisplayWidth;
    protected boolean mIsCharging;
    protected boolean mIsExtremePowerSave;
    protected boolean mIsPowerSave;
    protected final int mLowLevel = this.mContext.getResources().getInteger(285868035);
    protected int mPowerLevel;
    private boolean mShowBatteryIndicator;

    public BatteryIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        updateDisplaySize();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((BatteryController) Dependency.get(BatteryController.class)).addCallback(this);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((BatteryController) Dependency.get(BatteryController.class)).removeCallback(this);
    }

    public void onPowerSaveChanged(boolean z) {
        if (z != this.mIsPowerSave) {
            this.mIsPowerSave = z;
            update();
        }
    }

    public void onExtremePowerSaveChanged(boolean z) {
        if (z != this.mIsExtremePowerSave) {
            this.mIsExtremePowerSave = z;
            update();
        }
    }

    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        if (z2 != this.mIsCharging || this.mPowerLevel != i) {
            this.mIsCharging = z2;
            this.mPowerLevel = i;
            update();
        }
    }

    public void onBatteryStyleChanged(int i) {
        this.mShowBatteryIndicator = i == 2;
        update();
    }

    public void update() {
        updateVisibility();
        if (getVisibility() == 0) {
            updateDrawable();
        }
    }

    /* access modifiers changed from: protected */
    public void updateDrawable() {
        if (!this.mDemoMode) {
            int i = (this.mDisplayWidth * this.mPowerLevel) / 100;
            if (this.mClipWidth != i) {
                this.mClipWidth = i;
                invalidate();
            }
            int i2 = R.drawable.battery_indicator;
            if (!this.mIsCharging) {
                if (this.mIsPowerSave || this.mIsExtremePowerSave) {
                    i2 = R.drawable.battery_indicator_power_save;
                } else if (this.mPowerLevel < this.mLowLevel) {
                    i2 = R.drawable.battery_indicator_low;
                }
            }
            setImageResource(i2);
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            if (Util.showCtsSpecifiedColor()) {
                drawable.setColorFilter(getResources().getColor(R.color.status_bar_icon_text_color_dark_mode_cts), PorterDuff.Mode.SRC_IN);
            } else {
                drawable.setColorFilter((ColorFilter) null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDisplaySize() {
        this.mDisplayWidth = getMeasuredWidth();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (z) {
            updateDisplaySize();
            postUpdate();
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateDisplaySize();
        postUpdate();
    }

    public void onDraw(Canvas canvas) {
        canvas.save();
        updateCanvas(canvas);
        super.onDraw(canvas);
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    public void updateCanvas(Canvas canvas) {
        if (getLayoutDirection() == 0) {
            canvas.clipRect(this.mLeft, this.mTop, this.mLeft + this.mClipWidth, this.mBottom);
        } else {
            canvas.clipRect(this.mRight - this.mClipWidth, this.mTop, this.mRight, this.mBottom);
        }
    }

    /* access modifiers changed from: protected */
    public void updateVisibility() {
        if (!this.mDemoMode) {
            if (!this.mShowBatteryIndicator || this.mDisabled || Constants.IS_NOTCH) {
                setVisibility(8);
                clearAnimation();
                return;
            }
            setVisibility(0);
        }
    }

    private void postUpdate() {
        post(new Runnable() {
            public void run() {
                BatteryIndicator.this.update();
            }
        });
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            setVisibility(8);
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            update();
        }
    }
}
