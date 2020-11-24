package com.android.systemui.statusbar;

import android.content.Context;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.MiuiLightBarController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.BatteryController;

public class MiuiStatusBarModule {
    public LightBarController provideLightBarController(Context context, DarkIconDispatcher darkIconDispatcher, BatteryController batteryController, NavigationModeController navigationModeController) {
        return new MiuiLightBarController(context, darkIconDispatcher, batteryController, navigationModeController);
    }
}
