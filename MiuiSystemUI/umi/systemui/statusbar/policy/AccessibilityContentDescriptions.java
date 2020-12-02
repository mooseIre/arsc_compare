package com.android.systemui.statusbar.policy;

import com.android.systemui.C0021R$string;

public class AccessibilityContentDescriptions {
    static final int[] ETHERNET_CONNECTION_VALUES = {C0021R$string.accessibility_ethernet_disconnected, C0021R$string.accessibility_ethernet_connected};
    static final int[] PHONE_SIGNAL_STRENGTH;
    static final int[] WIFI_CONNECTION_STRENGTH = {C0021R$string.accessibility_no_wifi, C0021R$string.accessibility_wifi_one_bar, C0021R$string.accessibility_wifi_two_bars, C0021R$string.accessibility_wifi_three_bars, C0021R$string.accessibility_wifi_signal_full};
    static final int WIFI_NO_CONNECTION = C0021R$string.accessibility_no_wifi;

    static {
        int i = C0021R$string.accessibility_phone_signal_full;
        PHONE_SIGNAL_STRENGTH = new int[]{C0021R$string.accessibility_no_phone, C0021R$string.accessibility_phone_one_bar, C0021R$string.accessibility_phone_two_bars, C0021R$string.accessibility_phone_three_bars, i, i};
    }
}
