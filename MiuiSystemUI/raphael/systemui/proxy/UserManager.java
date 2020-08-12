package com.android.systemui.proxy;

import android.content.Context;

public class UserManager {
    public static boolean isDeviceInDemoMode(Context context) {
        return Settings$Global.getInt(context.getContentResolver(), "device_demo_mode", 0) > 0;
    }
}
