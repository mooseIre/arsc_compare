package com.android.systemui.statusbar.policy;

import com.android.systemui.plugins.R;

public class WifiIcons {
    static final int[][] QS_SLAVE_WIFI_SIGNAL_STRENGTH = {new int[]{R.drawable.stat_sys_slave_wifi_signal_0, R.drawable.stat_sys_slave_wifi_signal_1, R.drawable.stat_sys_slave_wifi_signal_2, R.drawable.stat_sys_slave_wifi_signal_3, R.drawable.stat_sys_slave_wifi_signal_4}, new int[]{R.drawable.stat_sys_slave_wifi_signal_0, R.drawable.stat_sys_slave_wifi_signal_1, R.drawable.stat_sys_slave_wifi_signal_2, R.drawable.stat_sys_slave_wifi_signal_3, R.drawable.stat_sys_slave_wifi_signal_4}};
    public static final int[][] QS_WIFI_SIGNAL_STRENGTH = {new int[]{R.drawable.ic_qs_wifi_0, R.drawable.ic_qs_wifi_1, R.drawable.ic_qs_wifi_2, R.drawable.ic_qs_wifi_3, R.drawable.ic_qs_wifi_4}, new int[]{R.drawable.ic_qs_wifi_full_0, R.drawable.ic_qs_wifi_full_1, R.drawable.ic_qs_wifi_full_2, R.drawable.ic_qs_wifi_full_3, R.drawable.ic_qs_wifi_full_4}};
    static final int[][] SB_SLAVE_WIFI_SIGNAL_STRENGTH = {new int[]{R.drawable.stat_sys_slave_wifi_signal_0, R.drawable.stat_sys_slave_wifi_signal_1, R.drawable.stat_sys_slave_wifi_signal_2, R.drawable.stat_sys_slave_wifi_signal_3, R.drawable.stat_sys_slave_wifi_signal_4}, new int[]{R.drawable.stat_sys_slave_wifi_signal_0, R.drawable.stat_sys_slave_wifi_signal_1, R.drawable.stat_sys_slave_wifi_signal_2, R.drawable.stat_sys_slave_wifi_signal_3, R.drawable.stat_sys_slave_wifi_signal_4}};
    static final int WIFI_LEVEL_COUNT;
    public static final int[][] WIFI_SIGNAL_STRENGTH;

    static {
        int[][] iArr = {new int[]{R.drawable.stat_sys_wifi_signal_0, R.drawable.stat_sys_wifi_signal_1, R.drawable.stat_sys_wifi_signal_2, R.drawable.stat_sys_wifi_signal_3, R.drawable.stat_sys_wifi_signal_4}, new int[]{R.drawable.stat_sys_wifi_signal_0, R.drawable.stat_sys_wifi_signal_1, R.drawable.stat_sys_wifi_signal_2, R.drawable.stat_sys_wifi_signal_3, R.drawable.stat_sys_wifi_signal_4}};
        WIFI_SIGNAL_STRENGTH = iArr;
        WIFI_LEVEL_COUNT = iArr[0].length;
    }
}
