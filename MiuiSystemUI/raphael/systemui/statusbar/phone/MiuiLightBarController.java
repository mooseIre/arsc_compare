package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.statusbar.phone.ForceBlackObserver;

public class MiuiLightBarController extends LightBarController implements ForceBlackObserver.Callback, ConfigurationController.ConfigurationListener {
    private BarModeChangeListener mBarModeChangeListener;
    private boolean mForceBlack = ((ForceBlackObserver) Dependency.get(ForceBlackObserver.class)).isForceBlack();
    private int mOrientation;
    protected boolean mSmartDarkEnable;
    protected boolean mSmartDarkLight;

    public interface BarModeChangeListener {
        void onBarModeChanged(int i);
    }

    public MiuiLightBarController(Context context, DarkIconDispatcher darkIconDispatcher, BatteryController batteryController, NavigationModeController navigationModeController) {
        super(context, darkIconDispatcher, batteryController, navigationModeController);
        ((ForceBlackObserver) Dependency.get(ForceBlackObserver.class)).addCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mOrientation = context.getResources().getConfiguration().orientation;
    }

    @Override // com.miui.systemui.statusbar.phone.ForceBlackObserver.Callback
    public void onForceBlackChange(boolean z, boolean z2) {
        this.mForceBlack = z;
        updateStatus();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        this.mOrientation = configuration.orientation;
        updateStatus();
    }

    public void setSmartDarkEnable(boolean z) {
        this.mSmartDarkEnable = z;
        updateStatus();
    }

    public void setSmartDarkLight(boolean z) {
        this.mSmartDarkLight = z;
        updateStatus();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.LightBarController
    public void updateStatus() {
        if (this.mForceBlack && this.mOrientation == 1) {
            this.mStatusBarIconController.getTransitionsController().setIconsDark(false, animateChange());
        } else if (this.mStatusBarMode == 1) {
            this.mStatusBarIconController.getTransitionsController().setIconsDark(false, animateChange());
        } else if (this.mSmartDarkEnable) {
            this.mStatusBarIconController.getTransitionsController().setIconsDark(this.mSmartDarkLight, animateChange());
        } else {
            super.updateStatus();
        }
    }

    public void setBarModeChangeListener(BarModeChangeListener barModeChangeListener) {
        this.mBarModeChangeListener = barModeChangeListener;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.statusbar.phone.LightBarController
    public void onStatusBarModeChanged(int i) {
        super.onStatusBarModeChanged(i);
        BarModeChangeListener barModeChangeListener = this.mBarModeChangeListener;
        if (barModeChangeListener != null) {
            barModeChangeListener.onBarModeChanged(i);
        }
    }
}
