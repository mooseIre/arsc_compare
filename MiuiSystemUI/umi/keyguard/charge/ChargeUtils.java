package com.android.keyguard.charge;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import com.android.systemui.Application;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.StatusBar;
import java.io.File;
import miui.os.Build;
import miui.util.FeatureParser;

public class ChargeUtils {
    private static String KEY_BATTERY_ENDURANCE_TIME = "battery_endurance_time";
    private static String KEY_LEFT_CHARGE_TIME = "left_charge_time";
    private static String METHOD_GET_BATTERY_INFO = "getBatteryInfo";
    private static String PROVIDER_POWER_CENTER = "content://com.miui.powercenter.provider";
    private static final boolean SUPPORT_WIRELESS_CHARGE = new File("/sys/class/power_supply/wireless/signal_strength").exists();
    private static boolean sChargeAnimationDisabled = false;
    private static boolean sNeedRepositionDevice = false;

    public static boolean isRapidCharge(int i) {
        return i == 1;
    }

    public static boolean isSuperRapidCharge(int i) {
        return i == 2 || i == 3;
    }

    public static boolean isWirelessCarMode(int i) {
        return i == 11;
    }

    public static boolean isWirelessSuperRapidCharge(int i) {
        return i >= 9;
    }

    public static boolean supportWirelessCharge() {
        return SUPPORT_WIRELESS_CHARGE;
    }

    public static boolean supportNewChargeAnimation() {
        return !FeatureParser.getBoolean("is_pad", false);
    }

    public static void disableChargeAnimation(boolean z) {
        sChargeAnimationDisabled = z;
    }

    public static boolean isChargeAnimationDisabled() {
        return sChargeAnimationDisabled;
    }

    public static boolean supportWaveChargeAnimation() {
        return "draco".equals(Build.DEVICE);
    }

    public static boolean supportVideoChargeAnimation() {
        return "cmi".equals(Build.DEVICE) || "umi".equals(Build.DEVICE) || "urd".equals(Build.DEVICE) || "toco".equals(Build.DEVICE) || "tocoin".equals(Build.DEVICE) || "lmi".equals(Build.DEVICE) || "lmiin".equals(Build.DEVICE) || "monet".equals(Build.DEVICE) || "monetin".equals(Build.DEVICE) || "vangogh".equals(Build.DEVICE) || "curtana".equals(Build.DEVICE) || "durandal".equals(Build.DEVICE) || "excalibur".equals(Build.DEVICE) || "joyeuse".equals(Build.DEVICE) || "gram".equals(Build.DEVICE);
    }

    public static void setNeedRepositionDevice(boolean z) {
        sNeedRepositionDevice = z;
    }

    public static String getChargingHintText(Context context, boolean z, int i) {
        String str = null;
        if (!z) {
            return null;
        }
        if (supportWirelessCharge() && sNeedRepositionDevice) {
            return context.getString(R.string.wireless_charge_reset_device);
        }
        Bundle batteryInfo = getBatteryInfo(context);
        Resources resources = context.getResources();
        if (batteryInfo != null && i == 100) {
            long j = batteryInfo.getLong(i == 100 ? KEY_BATTERY_ENDURANCE_TIME : KEY_LEFT_CHARGE_TIME);
            long hours = getHours(j);
            long mins = getMins(j);
            int i2 = (hours > 0 ? 1 : (hours == 0 ? 0 : -1));
            if (i2 > 0 && mins > 0) {
                str = resources.getQuantityString(R.plurals.keyguard_charging_info_could_use_time_text, (int) hours, new Object[]{Long.valueOf(hours), Long.valueOf(mins)});
            } else if (i2 > 0) {
                str = resources.getQuantityString(R.plurals.keyguard_charging_info_could_use_hour_time_text, (int) hours, new Object[]{Long.valueOf(hours)});
            } else if (mins > 0) {
                str = resources.getQuantityString(R.plurals.keyguard_charging_info_could_use_min_time_text, (int) mins, new Object[]{Long.valueOf(mins)});
            }
        }
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        if (i == 100) {
            return resources.getString(R.string.keyguard_charged);
        }
        if (isSuperQuickCharging()) {
            return resources.getString(R.string.keyguard_charging_super_quick_and_level_tip, new Object[]{Integer.valueOf(i)});
        } else if (isQuickCharging()) {
            return resources.getString(R.string.keyguard_charging_quick_and_level_tip, new Object[]{Integer.valueOf(i)});
        } else {
            return resources.getString(R.string.keyguard_charging_normal_and_level_tip, new Object[]{Integer.valueOf(i)});
        }
    }

    private static Bundle getBatteryInfo(Context context) {
        try {
            return context.getContentResolver().call(Uri.parse(PROVIDER_POWER_CENTER), METHOD_GET_BATTERY_INFO, (String) null, (Bundle) null);
        } catch (Exception unused) {
            Log.e("ChargeUtils", "cannot find the path getBatteryInfo of content://com.miui.powercenter.provider");
            return null;
        }
    }

    public static boolean isQuickCharging() {
        return ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).isQuickCharging();
    }

    public static ViewGroup getParentView(Context context) {
        return ((StatusBar) ((Application) context.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class)).getStatusBarWindow();
    }

    public static boolean isSuperQuickCharging() {
        return ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).isSuperQuickCharging();
    }

    public static long getHours(long j) {
        return j / 3600000;
    }

    public static long getMins(long j) {
        return (j % 3600000) / 60000;
    }
}
