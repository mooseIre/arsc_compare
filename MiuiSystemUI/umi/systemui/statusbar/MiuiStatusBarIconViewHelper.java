package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.drawable.Icon;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import java.util.ArrayList;

public class MiuiStatusBarIconViewHelper {
    public static final ArrayList<String> DRIP_END_BLOCKED_LIST;

    static {
        ArrayList<String> arrayList = new ArrayList<>();
        DRIP_END_BLOCKED_LIST = arrayList;
        arrayList.add("micphone");
        DRIP_END_BLOCKED_LIST.add("headset");
        DRIP_END_BLOCKED_LIST.add("mikey");
        DRIP_END_BLOCKED_LIST.add("call_record");
        DRIP_END_BLOCKED_LIST.add("privacy_mode");
        DRIP_END_BLOCKED_LIST.add("ble_unlock_mode");
        DRIP_END_BLOCKED_LIST.add("zen");
        DRIP_END_BLOCKED_LIST.add("gps");
        DRIP_END_BLOCKED_LIST.add("missed_call");
        DRIP_END_BLOCKED_LIST.add("ime");
        DRIP_END_BLOCKED_LIST.add("sync_failing");
        DRIP_END_BLOCKED_LIST.add("sync_active");
        DRIP_END_BLOCKED_LIST.add("cast");
        DRIP_END_BLOCKED_LIST.add("bluetooth_handsfree_battery");
        DRIP_END_BLOCKED_LIST.add("nfc");
        DRIP_END_BLOCKED_LIST.add("tty");
        DRIP_END_BLOCKED_LIST.add("speakerphone");
        DRIP_END_BLOCKED_LIST.add("volume");
        DRIP_END_BLOCKED_LIST.add("alarm_clock");
    }

    public static void transformIconIfNeed(Context context, StatusBarIcon statusBarIcon, ExpandedNotification expandedNotification, String str, boolean z, boolean z2) {
        if (needChangeIcon(statusBarIcon, str, expandedNotification)) {
            if (z) {
                statusBarIcon.icon = Icon.createWithResource(context, Icons.getTintDrawableId(statusBarIcon.icon.getResId()));
            } else if (z2) {
                statusBarIcon.icon = Icon.createWithResource(context, Icons.getDarkDrawableId(statusBarIcon.icon.getResId()));
            } else {
                statusBarIcon.icon = Icon.createWithResource(context, Icons.getLightDrawableId(statusBarIcon.icon.getResId()));
            }
        }
    }

    public static boolean needChangeIcon(StatusBarIcon statusBarIcon, String str, ExpandedNotification expandedNotification) {
        return str != null && "com.android.systemui".equals(statusBarIcon.pkg) && expandedNotification == null;
    }

    public static boolean canUseTint(String str, StatusBarIcon statusBarIcon) {
        if (str == null || statusBarIcon == null) {
            return true;
        }
        return "com.android.systemui".equals(statusBarIcon.pkg);
    }

    public static int transformResId(int i, boolean z, boolean z2) {
        if (z) {
            return Icons.getTintDrawableId(i);
        }
        return z2 ? Icons.getLightDrawableId(i) : Icons.getDarkDrawableId(i);
    }
}
