package com.android.systemui;

import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.WifiStatusTracker;

public class SettingsLibCompat {
    public static int getWifiStandard(AccessPoint accessPoint) {
        return accessPoint.getWifiStandard();
    }

    public static int getWifiStandard(WifiStatusTracker wifiStatusTracker) {
        return wifiStatusTracker.wifiStandard;
    }
}
