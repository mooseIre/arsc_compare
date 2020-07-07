package com.android.systemui.statusbar.phone;

import android.content.res.Configuration;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.ConfigurationController;

public class KeyguardStatusBarViewControllerNarrowNotchImpl extends KeyguardStatusBarViewControllerImpl {
    /* access modifiers changed from: private */
    public View mCarrierSuperContainer;
    /* access modifiers changed from: private */
    public ConfigurationController.ConfigurationListener mConfigurationListener;
    /* access modifiers changed from: private */
    public int mExtraSpace;
    /* access modifiers changed from: private */
    public View mSystemIconArea;
    /* access modifiers changed from: private */
    public ViewGroup mSystemIconContainer;

    public boolean isNotch() {
        return true;
    }

    public boolean isPromptCenter() {
        return false;
    }

    public void init(KeyguardStatusBarView keyguardStatusBarView) {
        super.init(keyguardStatusBarView);
        this.mSystemIconArea = this.mStatusBarView.findViewById(R.id.system_icons);
        this.mCarrierSuperContainer = this.mStatusBarView.findViewById(R.id.keyguard_carrier_super_container);
        AnonymousClass1 r2 = new ConfigurationController.ConfigurationListener() {
            public void onConfigChanged(Configuration configuration) {
            }

            public void onDensityOrFontScaleChanged() {
                DripStatusBarUtils.updateContainerWidth(KeyguardStatusBarViewControllerNarrowNotchImpl.this.mCarrierSuperContainer, true, false, 0);
                DripStatusBarUtils.updateContainerWidth(KeyguardStatusBarViewControllerNarrowNotchImpl.this.mSystemIconArea, false, false, KeyguardStatusBarViewControllerNarrowNotchImpl.this.mExtraSpace);
            }
        };
        this.mConfigurationListener = r2;
        r2.onDensityOrFontScaleChanged();
        this.mSystemIconContainer = (ViewGroup) this.mStatusBarView.findViewById(R.id.system_icons_container);
        ((BatteryMeterView) this.mSystemIconArea.findViewById(R.id.battery)).setBatteryMeterViewDelegate(new BatteryMeterView.BatteryMeterViewDelegate() {
            public void onNumberToIconChanged(boolean z) {
                KeyguardStatusBarViewControllerNarrowNotchImpl keyguardStatusBarViewControllerNarrowNotchImpl = KeyguardStatusBarViewControllerNarrowNotchImpl.this;
                int unused = keyguardStatusBarViewControllerNarrowNotchImpl.mExtraSpace = z ? keyguardStatusBarViewControllerNarrowNotchImpl.mSystemIconContainer.getContext().getResources().getDimensionPixelSize(R.dimen.battery_percent_mark_view_width) : 0;
                DripStatusBarUtils.updateContainerEndMargin(KeyguardStatusBarViewControllerNarrowNotchImpl.this.mSystemIconContainer, KeyguardStatusBarViewControllerNarrowNotchImpl.this.mExtraSpace);
                KeyguardStatusBarViewControllerNarrowNotchImpl.this.mConfigurationListener.onDensityOrFontScaleChanged();
            }
        });
    }

    public void showStatusIcons() {
        StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager(this.mStatusBarView.mStatusIcons, true);
        this.mDarkIconManager = darkIconManager;
        darkIconManager.mWhiteList = new ArraySet<>();
        this.mDarkIconManager.mWhiteList.add("volume");
        this.mDarkIconManager.mWhiteList.add("quiet");
        this.mDarkIconManager.mWhiteList.add("alarm_clock");
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDarkIconManager);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.mConfigurationListener);
    }

    public void hideStatusIcons() {
        super.hideStatusIcons();
        if (this.mConfigurationListener != null) {
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this.mConfigurationListener);
        }
    }
}
