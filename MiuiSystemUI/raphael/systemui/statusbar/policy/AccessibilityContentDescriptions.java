package com.android.systemui.statusbar.policy;

import com.android.systemui.plugins.R;

public class AccessibilityContentDescriptions {
    static final int[] DATA_CONNECTION_STRENGTH = {R.string.accessibility_no_data, R.string.accessibility_data_one_bar, R.string.accessibility_data_two_bars, R.string.accessibility_data_three_bars, R.string.accessibility_data_signal_full};
    static final int[] ETHERNET_CONNECTION_VALUES = {R.string.accessibility_ethernet_disconnected, R.string.accessibility_ethernet_connected};
    static final int[] PHONE_SIGNAL_STRENGTH = {R.string.accessibility_no_phone, R.string.accessibility_phone_one_bar, R.string.accessibility_phone_two_bars, R.string.accessibility_phone_three_bars, R.string.accessibility_phone_signal_full};
    public static final int[] SLAVE_WIFI_CONNECTION_STRENGTH = {R.string.accessibility_status_bar_no_slave_wifi, R.string.accessibility_status_bar_slave_wifi_one_bar, R.string.accessibility_status_bar_slave_wifi_two_bars, R.string.accessibility_status_bar_slave_wifi_three_bars, R.string.accessibility_status_bar_slave_wifi_four_bars};
    static final int[] WIFI_CONNECTION_STRENGTH = {R.string.accessibility_no_wifi, R.string.accessibility_wifi_one_bar, R.string.accessibility_wifi_two_bars, R.string.accessibility_wifi_three_bars, R.string.accessibility_wifi_signal_full};
    static final int[] WIMAX_CONNECTION_STRENGTH = {R.string.accessibility_no_wimax, R.string.accessibility_wimax_one_bar, R.string.accessibility_wimax_two_bars, R.string.accessibility_wimax_three_bars, R.string.accessibility_wimax_signal_full};
}
