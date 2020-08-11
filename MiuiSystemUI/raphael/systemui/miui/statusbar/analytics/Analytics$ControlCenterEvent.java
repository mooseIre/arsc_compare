package com.android.systemui.miui.statusbar.analytics;

import android.content.Context;
import android.provider.Settings;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;

public class Analytics$ControlCenterEvent extends Analytics$Event {
    public static int getUseControlPanel(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "use_control_panel", ((ControlPanelController) Dependency.get(ControlPanelController.class)).getUseControlPanelSettingDefault());
    }

    public static int getExpandableUnderLockscreen(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "expandable_under_lock_screen", 1);
    }
}
