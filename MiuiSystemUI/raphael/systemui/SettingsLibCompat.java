package com.android.systemui;

import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.WifiStatusTracker;

public class SettingsLibCompat {
    public static int getWifiStandard(AccessPoint accessPoint) {
        return accessPoint.getWifiGeneration();
    }

    public static int getWifiStandard(WifiStatusTracker wifiStatusTracker) {
        return wifiStatusTracker.wifiGeneration;
    }
}
