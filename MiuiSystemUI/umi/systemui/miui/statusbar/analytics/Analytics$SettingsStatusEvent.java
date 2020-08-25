package com.android.systemui.miui.statusbar.analytics;

import android.app.MiuiStatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.systemui.Constants;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.xiaomi.stat.MiStat;
import java.util.List;
import miui.os.SystemProperties;
import miui.telephony.TelephonyManager;

public class Analytics$SettingsStatusEvent extends Analytics$Event {
    public static int getAppsCount(Context context, boolean z) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        int i = 0;
        List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 0);
        if (queryIntentActivities == null) {
            return 0;
        }
        for (ResolveInfo resolveInfo : queryIntentActivities) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            String str = activityInfo.packageName;
            String str2 = activityInfo.name;
            if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2) && z == NotificationSettingsHelper.isNotificationsBanned(context, str)) {
                i++;
            }
        }
        return i;
    }

    public static int getShowNotificationIconValue(Context context) {
        return MiuiStatusBarManager.isShowNotificationIcon(context) ? 1 : 0;
    }

    public static int getShowNetworkSpeedValue(Context context) {
        return MiuiStatusBarManager.isShowNetworkSpeed(context) ? 1 : 0;
    }

    public static int getShowCarrierUnderKeyguardValue(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1);
    }

    public static String getCustomCarrierValue(Context context) {
        int phoneCount = Constants.IS_CUST_SINGLE_SIM ? 1 : TelephonyManager.getDefault().getPhoneCount();
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < phoneCount; i3++) {
            if (TelephonyManager.getDefault().hasIccCard(i3)) {
                i2++;
                if (!TextUtils.isEmpty(MiuiSettings.System.getString(context.getContentResolver(), "status_bar_custom_carrier" + i3, (String) null))) {
                    i++;
                }
            }
        }
        if (i == 0) {
            return "none";
        }
        if (i == 1) {
            return i2 == 1 ? "single-1" : "dual-1";
        }
        return "dual-2";
    }

    public static String getBatteryIndicator(Context context) {
        int i = Settings.System.getInt(context.getContentResolver(), "battery_indicator_style", 1);
        if (i == 0) {
            return "graphic";
        }
        if (i == 1) {
            return "number";
        }
        return i == 2 ? "top" : "unknown";
    }

    public static int getToggleCollapseAfterClickedValue(Context context) {
        return MiuiStatusBarManager.isCollapseAfterClicked(context) ? 1 : 0;
    }

    public static int getExpandableUnderKeyguardValue(Context context) {
        return MiuiStatusBarManager.isExpandableUnderKeyguard(context) ? 1 : 0;
    }

    public static String getNotificationShortcut(Context context) {
        int i = Settings.Secure.getInt(context.getContentResolver(), "status_bar_notification_shade_shortcut", 1);
        if (i == 0) {
            return MiStat.Event.SEARCH;
        }
        return i == 1 ? "settings" : "unknown";
    }

    public static String getNotificationStyle(Context context) {
        return NotificationUtil.showMiuiStyle() ? "miui" : "google";
    }

    public static int getExpandSelectedInfo(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "control_center_expand_info_type", 0);
    }

    public static int getBucket(Context context) {
        return SystemProperties.getInt("persist.sys.notification_rank", 0);
    }

    public static int getUserFold(Context context) {
        return NotificationUtil.getUserFold(context);
    }

    public static int getUserAggregate(Context context) {
        return NotificationUtil.getUserAggregate(context);
    }
}
