package com.android.keyguard.charge;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0019R$plurals;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import miui.os.Build;
import miui.util.FeatureParser;

public class ChargeUtils {
    public static String KEY_QUICK_CHARGE = "quick_charge";
    public static String METHOD_GET_POWER_SUPPLY_INFO = "getPowerSupplyInfo";
    public static String PROVIDER_POWER_CENTER = "content://com.miui.powercenter.provider";
    private static final boolean SUPPORT_WIRELESS_CHARGE = new File("/sys/class/power_supply/wireless/signal_strength").exists();
    private static List<String> mIsExceptLiteChargeList = new ArrayList();
    private static List<String> mIsSupportLiteChargeList = new ArrayList();
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

    static {
        new ArrayList();
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
        int i = 1;
        boolean z = (Build.IS_MIUI_LITE_VERSION || supportLiteChargeAnimation()) && !exceptLiteChargeAnimation();
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (contextForUser != null) {
            i = contextForUser.getResources().getInteger(C0016R$integer.keyguard_charge_animation_type);
        }
        if (z) {
            return 0;
        }
        return i;
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

    public static boolean supportParticleChargeAnimation() {
        if (getChargeAnimationType() == 3) {
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
            String string = resources.getString(C0021R$string.keyguard_charging_super_quick_and_level_tip, Integer.valueOf(i));
            if (!isDoubleFastCharge()) {
                return string;
            }
            return String.format(resources.getString(C0021R$string.keyguard_charging_double_fast_and_level_tip), NumberFormat.getPercentInstance().format((double) (((float) i) / 100.0f)));
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

    public static boolean isDoubleFastCharge() {
        return ((MiuiChargeController) Dependency.get(MiuiChargeController.class)).isFastCharge();
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

    private static boolean supportLiteChargeAnimation() {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (mIsSupportLiteChargeList.isEmpty() && contextForUser != null) {
            mIsSupportLiteChargeList = Arrays.asList(contextForUser.getResources().getStringArray(C0008R$array.config_charge_support_lite));
        }
        return mIsSupportLiteChargeList.contains(android.os.Build.DEVICE);
    }

    private static boolean exceptLiteChargeAnimation() {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (mIsExceptLiteChargeList.isEmpty() && contextForUser != null) {
            mIsExceptLiteChargeList = Arrays.asList(contextForUser.getResources().getStringArray(C0008R$array.config_except_charge_support_lite));
        }
        return mIsExceptLiteChargeList.contains(android.os.Build.DEVICE);
    }

    public static int getChargeSpeed(int i, int i2) {
        if (i == 10 || i == 11) {
            if (isStrongSuperRapidCharge(i2)) {
                return 3;
            }
            if (isSuperRapidCharge(i2)) {
                return 2;
            }
            if (isRapidCharge(i2)) {
                return 1;
            }
        }
        return 0;
    }

    public static int getTextDelayTime() {
        if (supportWaveChargeAnimation()) {
            return 1000;
        }
        return supportParticleChargeAnimation() ? 1300 : 0;
    }

    public static int getWaveItemDelayTime() {
        return supportWaveChargeAnimation() ? 800 : 0;
    }

    public static void showSystemOverlayToast(Context context, int i, int i2) {
        showSystemOverlayToast(context, context.getString(i), i2);
    }

    public static void showSystemOverlayToast(Context context, CharSequence charSequence, int i) {
        Toast.makeText(context, charSequence, i).show();
    }

    public static boolean isOrientationLocked(Context context) {
        if (Settings.System.getInt(context.getContentResolver(), "accelerometer_rotation", 0) == 0) {
            return true;
        }
        return SUPPORT_WIRELESS_CHARGE;
    }
}
