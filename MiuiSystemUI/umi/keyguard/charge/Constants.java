package com.android.keyguard.charge;

import android.os.SystemProperties;

public class Constants {
    public static final boolean IS_NOTCH;

    static {
        boolean z = false;
        SystemProperties.getBoolean("debug.miuisystemui.staging", false);
        if (SystemProperties.getInt("ro.miui.notch", 0) == 1) {
            z = true;
        }
        IS_NOTCH = z;
    }
}
