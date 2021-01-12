package com.android.keyguard.charge;

import android.os.SystemProperties;

public class Constants {
    public static final boolean IS_NOTCH = (SystemProperties.getInt("ro.miui.notch", 0) == 1);
    public static final boolean SUPPORT_BROADCAST_QUICK_CHARGE;

    static {
        boolean z = false;
        SystemProperties.getBoolean("debug.miuisystemui.staging", false);
        if (SystemProperties.getInt("persist.quick.charge.detect", 0) == 1 || SystemProperties.getInt("persist.vendor.quick.charge", 0) == 1) {
            z = true;
        }
        SUPPORT_BROADCAST_QUICK_CHARGE = z;
    }
}
