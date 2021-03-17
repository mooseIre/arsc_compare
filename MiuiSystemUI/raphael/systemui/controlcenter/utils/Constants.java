package com.android.systemui.controlcenter.utils;

import com.miui.systemui.DebugConfig;
import miui.os.Build;
import miui.util.FeatureParser;

public class Constants {
    public static final boolean DEBUG = DebugConfig.DEBUG_QUICK_SETTINGS;
    public static final boolean IS_INTERNATIONAL = Build.IS_INTERNATIONAL_BUILD;
    public static final boolean IS_TABLET = Build.IS_TABLET;
    public static final boolean SUPPORT_ANDROID_FLASHLIGHT = FeatureParser.getBoolean("support_android_flashlight", false);
    public static final boolean SUPPORT_EXTREME_BATTERY_SAVER = FeatureParser.getBoolean("support_extreme_battery_saver", false);
}
