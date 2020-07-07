package com.android.systemui.miui.statusbar.analytics;

import android.content.Context;
import android.provider.Settings;

public class Analytics$ControlCenterEvent extends Analytics$Event {
    public static int getUseControlPanel(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "use_control_panel", 1);
    }

    public static int getExpandableUnderLockscreen(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "expandable_under_lock_screen", 1);
    }
}
