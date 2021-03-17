package com.android.keyguard.charge;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0019R$plurals;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import miui.util.FeatureParser;

public class ChargeUtils {
    public static String KEY_QUICK_CHARGE = "quick_charge";
    public static String METHOD_GET_POWER_SUPPLY_INFO = "getPowerSupplyInfo";
    public static String PROVIDER_POWER_CENTER = "content://com.miui.powercenter.provider";
    private static final boolean SUPPORT_WIRELESS_CHARGE = new File("/sys/class/power_supply/wireless/signal_strength").exists();
    private static int WAVE_DELAY_TIME = 1000;
    private static List<String> mIsSupportStrongSuperRapidChargeList = new ArrayList();
    public static MiuiBatteryStatus sBatteryStatus = null;
    private static boolean sChargeAnimationDisabled = false;
    private static boolean sNeedRepositionDevice = SUPPORT_WIRELESS_CHARGE;

    public static boolean isRapidCharge(int i) {
        if (i == 1) {
            return true;
        }
        return SUPPORT_WIRELESS_CHARGE;
    }

    public static boolean isStrongSuperRapidCharge(int i) {
        if (i == 4) {
            return true;
        }
        return SUPPORT_WIRELESS_CHARGE;
    }

    public static boolean isSuperRapidCharge(int i) {
        if (i == 2 || i == 3) {
            return true;
        }
        return SUPPORT_WIRELESS_CHARGE;
    }

    public static boolean isWirelessCarMode(int i) {
        if (i == 11) {
            return true;
        }
        return SUPPORT_WIRELESS_CHARGE;
    }

    public static boolean isWirelessStrongSuperRapidCharge(int i) {
        if (i == 14 || i == 15) {
            return true;
        }
        return SUPPORT_WIRELESS_CHARGE;
    }

    public static boolean isWirelessSuperRapidCharge(int i) {
        if (i < 9 || i == 14 || i == 15) {
            return SUPPORT_WIRELESS_CHARGE;
        }
        return true;
    }

    public static boolean supportWirelessCharge() {
        return SUPPORT_WIRELESS_CHARGE;
    }

    public static boolean supportNewChargeAnimation() {
        return !FeatureParser.getBoolean("is_pad", (boolean) SUPPORT_WIRELESS_CHARGE);
    }

    public static void disableChargeAnimation(boolean z) {
        sChargeAnimationDisabled = z;
    }

    public static boolean isChargeAnimationDisabled() {
        return sChargeAnimationDisabled;
    }

    private static int getChargeAnimationType() {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (contextForUser != null) {
            return contextForUser.getResources().getInteger(C0016R$integer.keyguard_charge_animation_type);
        }
        return 0;
    }

    public static boolean supportWaveChargeAnimation() {
        if (getChargeAnimationType() == 2) {
            return true;
        }
        return SUPPORT_WIRELESS_CHARGE;
    }

    public static boolean supportVideoChargeAnimation() {
        if (getChargeAnimationType() == 1) {
            return true;
        }
        return SUPPORT_WIRELESS_CHARGE;
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
            return context.getString(C0021R$string.wireless_charge_reset_device);
        }
        Bundle batteryInfo = getBatteryInfo(context);
        Resources resources = context.getResources();
        if (batteryInfo != null && i == 100) {
            long j = batteryInfo.getLong(i == 100 ? "battery_endurance_time" : "left_charge_time");
            long hours = getHours(j);
            long mins = getMins(j);
            int i2 = (hours > 0 ? 1 : (hours == 0 ? 0 : -1));
            if (i2 > 0 && mins > 0) {
                str = resources.getQuantityString(C0019R$plurals.keyguard_charging_info_could_use_time_text, (int) hours, Long.valueOf(hours), Long.valueOf(mins));
            } else if (i2 > 0) {
                str = resources.getQuantityString(C0019R$plurals.keyguard_charging_info_could_use_hour_time_text, (int) hours, Long.valueOf(hours));
            } else if (mins > 0) {
                str = resources.getQuantityString(C0019R$plurals.keyguard_charging_info_could_use_min_time_text, (int) mins, Long.valueOf(mins));
            }
        }
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        if (i == 100) {
            return resources.getString(C0021R$string.keyguard_charged);
        }
        if (isStrongSuperQuickCharging()) {
            return resources.getString(C0021R$string.keyguard_charging_super_quick_and_level_tip, Integer.valueOf(i));
        } else if (isSuperQuickCharging()) {
            return resources.getString(C0021R$string.keyguard_charging_super_quick_and_level_tip, Integer.valueOf(i));
        } else if (isQuickCharging()) {
            return resources.getString(C0021R$string.keyguard_charging_quick_and_level_tip, Integer.valueOf(i));
        } else {
            return resources.getString(C0021R$string.keyguard_charging_normal_and_level_tip, Integer.valueOf(i));
        }
    }

    private static Bundle getBatteryInfo(Context context) {
        try {
            return context.getContentResolver().call(Uri.parse(PROVIDER_POWER_CENTER), "getBatteryInfo", (String) null, (Bundle) null);
        } catch (Exception unused) {
            Log.e("ChargeUtils", "cannot find the path getBatteryInfo of content://com.miui.powercenter.provider");
            return null;
        }
    }

    public static boolean isQuickCharging() {
        return ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).isQuickCharging();
    }

    public static ViewGroup getParentView() {
        return ((StatusBar) Dependency.get(StatusBar.class)).getNotificationShadeWindowView();
    }

    public static boolean isSuperQuickCharging() {
        return ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).isSuperQuickCharging();
    }

    public static boolean isStrongSuperQuickCharging() {
        return ((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).isStrongSuperQuickCharging();
    }

    public static boolean isSupportWirelessStrongChargeSsw() {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        boolean z = contextForUser != null ? contextForUser.getResources().getBoolean(C0010R$bool.keyguard_wireless_strong_charge_ssw) : false;
        if (((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).getCurrentChargeDeviceType() != 15 || !z) {
            return SUPPORT_WIRELESS_CHARGE;
        }
        return true;
    }

    public static long getHours(long j) {
        return j / 3600000;
    }

    public static long getMins(long j) {
        return (j % 3600000) / 60000;
    }

    public static void setBatteryStatus(MiuiBatteryStatus miuiBatteryStatus) {
        sBatteryStatus = miuiBatteryStatus;
    }

    private static boolean supportStrongSuperRapidCharge() {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (mIsSupportStrongSuperRapidChargeList.isEmpty() && contextForUser != null) {
            mIsSupportStrongSuperRapidChargeList = Arrays.asList(contextForUser.getResources().getStringArray(C0008R$array.config_charge_supportWirelessStrongSuper));
        }
        return mIsSupportStrongSuperRapidChargeList.contains(Build.DEVICE);
    }

    public static int getChargeSpeed(int i, int i2) {
        if (i != 10) {
            if (i == 11) {
                if (isStrongSuperRapidCharge(i2)) {
                    return 3;
                }
                if (!isSuperRapidCharge(i2)) {
                    if (isRapidCharge(i2)) {
                        return 1;
                    }
                }
            }
            return 0;
        } else if (!isWirelessSuperRapidCharge(i2)) {
            if (isWirelessStrongSuperRapidCharge(i2)) {
                if (supportStrongSuperRapidCharge()) {
                    return 3;
                }
            }
            return 0;
        }
        return 2;
    }

    public static int getWaveTextDelayTime() {
        if (supportWaveChargeAnimation()) {
            return WAVE_DELAY_TIME;
        }
        return 0;
    }

    public static int getWaveItemDelayTime() {
        if (supportWaveChargeAnimation()) {
            return WAVE_DELAY_TIME - 200;
        }
        return 0;
    }

    public static void showSystemOverlayToast(Context context, int i, int i2) {
        showSystemOverlayToast(context, context.getString(i), i2);
    }

    public static void showSystemOverlayToast(Context context, CharSequence charSequence, int i) {
        Toast.makeText(context, charSequence, i).show();
    }
}
