package com.android.systemui.statusbar.policy;

import com.android.systemui.C0013R$drawable;

public class MiuiWifiIcons {
    public static final int QS_WIFI_NO_NETWORK;
    public static final int[][] QS_WIFI_SIGNAL_STRENGTH;
    static final int[] WIFI_FULL_ICONS = {C0013R$drawable.stat_sys_wifi_signal_0, C0013R$drawable.stat_sys_wifi_signal_1, C0013R$drawable.stat_sys_wifi_signal_2, C0013R$drawable.stat_sys_wifi_signal_3, C0013R$drawable.stat_sys_wifi_signal_4};
    static final int WIFI_LEVEL_COUNT = WIFI_SIGNAL_STRENGTH[0].length;
    private static final int[] WIFI_NO_INTERNET_ICONS;
    static final int WIFI_NO_NETWORK;
    static final int[][] WIFI_SIGNAL_STRENGTH;

    static {
        int[] iArr = {C0013R$drawable.stat_sys_wifi_signal_0, C0013R$drawable.stat_sys_wifi_signal_1, C0013R$drawable.stat_sys_wifi_signal_2, C0013R$drawable.stat_sys_wifi_signal_3, C0013R$drawable.stat_sys_wifi_signal_4};
        WIFI_NO_INTERNET_ICONS = iArr;
        int[][] iArr2 = {iArr, WIFI_FULL_ICONS};
        QS_WIFI_SIGNAL_STRENGTH = iArr2;
        WIFI_SIGNAL_STRENGTH = iArr2;
        int i = C0013R$drawable.stat_sys_wifi_signal_null;
        QS_WIFI_NO_NETWORK = i;
        WIFI_NO_NETWORK = i;
    }
}
