package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.statusbar.phone.ForceBlackObserver;

public class MiuiLightBarController extends LightBarController implements ForceBlackObserver.Callback, ConfigurationController.ConfigurationListener {
    private boolean mForceBlack;
    private int mOrientation;
    protected boolean mSmartDarkEnable;
    protected boolean mSmartDarkLight;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiLightBarController(Context context, DarkIconDispatcher darkIconDispatcher, BatteryController batteryController, NavigationModeController navigationModeController) {
        super(context, darkIconDispatcher, batteryController, navigationModeController);
        Class cls = ForceBlackObserver.class;
        ((ForceBlackObserver) Dependency.get(cls)).addCallback(this);
        this.mForceBlack = ((ForceBlackObserver) Dependency.get(cls)).isForceBlack();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mOrientation = context.getResources().getConfiguration().orientation;
    }

    public void onForceBlackChange(boolean z, boolean z2) {
        this.mForceBlack = z;
        updateStatus();
    }

    public void onConfigChanged(Configuration configuration) {
        this.mOrientation = configuration.orientation;
        updateStatus();
    }

    public void setSmartDarkEnable(boolean z) {
        if (this.mSmartDarkEnable != z) {
            this.mSmartDarkEnable = z;
            updateStatus();
        }
    }

    public void setSmartDarkLight(boolean z) {
        if (this.mSmartDarkLight != z) {
            this.mSmartDarkLight = z;
            updateStatus();
        }
    }

    /* access modifiers changed from: protected */
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
}
