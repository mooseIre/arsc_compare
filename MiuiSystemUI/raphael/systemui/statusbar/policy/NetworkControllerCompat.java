package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;

public class NetworkControllerCompat {
    public static boolean supportSlaveWifi(Context context) {
        if (!"off".equals(Settings.System.getString(context.getContentResolver(), "cloud_slave_wifi_support")) && SystemProperties.getInt("ro.vendor.net.enable_dual_wifi", 0) == 1) {
            return true;
        }
        return false;
    }

    public static void addSlaveWifiBroadcast(IntentFilter intentFilter) {
        intentFilter.addAction("android.net.wifi.SLAVE_STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.SLAVE_RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.WIFI_SLAVE_STATE_CHANGED");
    }

    public static boolean isSlaveWifiBroadcast(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return false;
        }
        if ("android.net.wifi.SLAVE_RSSI_CHANGED".equals(action) || "android.net.wifi.WIFI_SLAVE_STATE_CHANGED".equals(action) || "android.net.wifi.SLAVE_STATE_CHANGE".equals(action)) {
            return true;
        }
        return false;
    }
}
