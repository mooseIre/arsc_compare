package com.android.systemui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;

public class ChargingView extends ImageView implements BatteryController.BatteryStateChangeCallback, ConfigurationController.ConfigurationListener {
    private BatteryController mBatteryController;
    private boolean mCharging;
    private boolean mDark;
    private int mImageResource;

    public void onBatteryStyleChanged(int i) {
    }

    public void onConfigChanged(Configuration configuration) {
    }

    public void onExtremePowerSaveChanged(boolean z) {
    }

    public void onPowerSaveChanged(boolean z) {
    }

    public ChargingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, new int[]{16843033});
        int resourceId = obtainStyledAttributes.getResourceId(0, 0);
        if (resourceId != 0) {
            this.mImageResource = resourceId;
        }
        obtainStyledAttributes.recycle();
        updateVisibility();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mBatteryController.addCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBatteryController.removeCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        this.mCharging = z2;
        updateVisibility();
    }

    public void onDensityOrFontScaleChanged() {
        setImageResource(this.mImageResource);
    }

    private void updateVisibility() {
        setVisibility((!this.mCharging || !this.mDark) ? 4 : 0);
    }
}
