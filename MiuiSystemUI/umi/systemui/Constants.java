package com.android.systemui;

import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import java.io.File;
import miui.os.Build;
import miui.util.FeatureParser;

public class Constants {
    public static final boolean DEBUG = SystemProperties.getBoolean("debug.miuisystemui.staging", false);
    public static final String HOME_LAUCNHER_PACKAGE_NAME = SystemProperties.get("ro.miui.product.home", "com.miui.home");
    public static final boolean IS_CUST_SINGLE_SIM = (SystemProperties.getInt("ro.miui.singlesim", 0) == 1);
    public static final boolean IS_INDIA_REGION;
    public static final boolean IS_INTERNATIONAL;
    public static final boolean IS_MEDIATEK = FeatureParser.getBoolean("is_mediatek", false);
    public static final boolean IS_NOTCH = (SystemProperties.getInt("ro.miui.notch", 0) == 1);
    public static final boolean IS_OLED_SCREEN = ("oled".equals(SystemProperties.get("ro.vendor.display.type")) || "oled".equals(SystemProperties.get("ro.display.type")));
    public static final boolean IS_SUPPORT_LINEAR_MOTOR_VIBRATE = "linear".equals(SystemProperties.get("sys.haptic.motor"));
    public static final boolean IS_TABLET = Build.IS_TABLET;
    public static final String SILENT_MODE_ACTION;
    public static final File SOUND_SCREENSHOT = new File("/system/media/audio/ui/screenshot.ogg");
    public static final File SOUND_SCREENSHOT_KR = new File("/system/media/audio/ui/screenshot_kr.ogg");
    public static final boolean SUPPORT_ANDROID_FLASHLIGHT = FeatureParser.getBoolean("support_android_flashlight", false);
    public static final boolean SUPPORT_AOD = FeatureParser.getBoolean("support_aod", false);
    public static final boolean SUPPORT_BROADCAST_QUICK_CHARGE = (SystemProperties.getInt("persist.quick.charge.detect", 0) == 1 || SystemProperties.getInt("persist.vendor.quick.charge", 0) == 1);
    public static final boolean SUPPORT_DISABLE_USB_BY_SIM = (Build.IS_CM_CUSTOMIZATION_TEST || Build.IS_CM_CUSTOMIZATION);
    public static final boolean SUPPORT_DUAL_GPS = FeatureParser.getBoolean("support_dual_gps", false);
    public static final boolean SUPPORT_EXTREME_BATTERY_SAVER = FeatureParser.getBoolean("support_extreme_battery_saver", false);
    public static final boolean SUPPORT_FPS_DYNAMIC_ACCOMMODATION = SystemProperties.getBoolean("ro.vendor.smart_dfps.enable", false);
    public static final boolean SUPPORT_LAB_GESTURE;
    public static final boolean SUPPORT_SCREEN_PAPER_MODE = FeatureParser.getBoolean("support_screen_paper_mode", false);

    static {
        int i = Build.VERSION.SDK_INT;
        boolean z = true;
        "clover".equals(miui.os.Build.DEVICE);
        boolean z2 = miui.os.Build.IS_INTERNATIONAL_BUILD;
        IS_INTERNATIONAL = z2;
        IS_INDIA_REGION = z2 && miui.os.Build.getRegion().endsWith("IN");
        "fr_orange".equals(SystemProperties.get("ro.miui.customized.region"));
        boolean z3 = miui.os.Build.IS_TABLET;
        boolean z4 = miui.os.Build.IS_INTERNATIONAL_BUILD;
        if (!"sagit".equals(miui.os.Build.DEVICE) || miui.os.Build.IS_STABLE_VERSION) {
            z = false;
        }
        SUPPORT_LAB_GESTURE = z;
        new File("/system/media/audio/ui/charging.ogg");
        new File("/system/media/audio/ui/charge_wireless.ogg");
        new File("/system/media/audio/ui/disconnect.ogg");
        new File("/system/media/audio/ui/flashlight.ogg");
        new File("/data/system/theme/com.android.systemui");
        SILENT_MODE_ACTION = i < 30 ? "com.android.settings/com.android.settings.Settings$MiuiSilentModeAcivity" : "com.android.settings/com.android.settings.Settings$SoundSettingsActivity";
    }

    public static boolean isIndiaDevice() {
        String str = SystemProperties.get("ro.boot.hwc");
        return !TextUtils.isEmpty(str) && str.toLowerCase().contains("india");
    }
}
