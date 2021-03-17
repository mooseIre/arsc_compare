package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.charge.MiuiBatteryStatus;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DemoModeController;
import miui.util.CustomizeUtil;

public class BatteryIndicator extends ImageView implements DemoMode, BatteryController.BatteryStateChangeCallback {
    private int mBottom;
    private int mClipWidth;
    private Context mContext;
    private boolean mDemoMode;
    protected int mDisplayWidth;
    protected boolean mIsCharging;
    protected boolean mIsExtremePowerSave;
    protected boolean mIsPowerSave;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.systemui.statusbar.phone.BatteryIndicator.AnonymousClass2 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(MiuiBatteryStatus miuiBatteryStatus) {
            super.onRefreshBatteryInfo(miuiBatteryStatus);
            BatteryIndicator.this.mPowerLevel = miuiBatteryStatus.getLevel();
            BatteryIndicator.this.mIsCharging = miuiBatteryStatus.isCharging();
            BatteryIndicator.this.update();
        }
    };
    private int mLeft;
    protected final int mLowLevel;
    protected int mPowerLevel;
    private int mRight;
    private boolean mShowBatteryIndicator;
    private int mTop;

    public BatteryIndicator(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mLowLevel = context.getResources().getInteger(285868035);
        updateDisplaySize();
    }

    private void updateDisplaySize() {
        this.mDisplayWidth = getMeasuredWidth();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        if (this.mIsPowerSave != z) {
            this.mIsPowerSave = z;
            update();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void update() {
        updateVisiblity();
        if (getVisibility() == 0) {
            updateDrawable();
        }
    }

    private void updateDrawable() {
        int i = 100;
        int i2 = (this.mDisplayWidth * (this.mDemoMode ? 100 : this.mPowerLevel)) / 100;
        if (this.mClipWidth != i2) {
            this.mClipWidth = i2;
            invalidate();
        }
        int i3 = C0013R$drawable.battery_indicator;
        if (!this.mIsCharging) {
            if (this.mIsPowerSave || this.mIsExtremePowerSave) {
                i3 = C0013R$drawable.battery_indicator_power_save;
            } else {
                if (!this.mDemoMode) {
                    i = this.mPowerLevel;
                }
                if (i < this.mLowLevel) {
                    i3 = C0013R$drawable.battery_indicator_low;
                }
            }
        }
        setImageResource(i3);
    }

    private void updateVisiblity() {
        if (!this.mShowBatteryIndicator || CustomizeUtil.HAS_NOTCH) {
            setVisibility(8);
            clearAnimation();
            return;
        }
        setVisibility(0);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryStyleChanged(int i) {
        this.mShowBatteryIndicator = i == 2;
        update();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onExtremePowerSaveChanged(boolean z) {
        if (this.mIsExtremePowerSave != z) {
            this.mIsExtremePowerSave = z;
            update();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback, com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            update();
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            update();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((BatteryController) Dependency.get(BatteryController.class)).addCallback(this);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mKeyguardUpdateMonitorCallback);
        ((DemoModeController) Dependency.get(DemoModeController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((DemoModeController) Dependency.get(DemoModeController.class)).removeCallback(this);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mKeyguardUpdateMonitorCallback);
        ((BatteryController) Dependency.get(BatteryController.class)).removeCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mLeft = i;
        this.mTop = i2;
        this.mRight = i3;
        this.mBottom = i4;
        if (z) {
            updateDisplaySize();
            postUpdate();
        }
    }

    private void postUpdate() {
        post(new Runnable() {
            /* class com.android.systemui.statusbar.phone.BatteryIndicator.AnonymousClass1 */

            public void run() {
                BatteryIndicator.this.update();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        canvas.save();
        updateCanvas(canvas);
        super.onDraw(canvas);
        canvas.restore();
    }

    private void updateCanvas(Canvas canvas) {
        if (getLayoutDirection() == 0) {
            int i = this.mLeft;
            canvas.clipRect(i, this.mTop, this.mClipWidth + i, this.mBottom);
            return;
        }
        int i2 = this.mRight;
        canvas.clipRect(i2 - this.mClipWidth, this.mTop, i2, this.mBottom);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateDisplaySize();
        postUpdate();
    }
}
