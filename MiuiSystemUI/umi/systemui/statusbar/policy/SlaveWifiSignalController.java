package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.MiuiWifiManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import com.android.settingslib.wifi.SlaveWifiUtils;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBarIconController;

public class SlaveWifiSignalController extends BroadcastReceiver {
    private static final int[] SLAVE_WIFI_ACCESSIBILITY = {C0021R$string.accessibility_no_wifi, C0021R$string.accessibility_wifi_one_bar, C0021R$string.accessibility_wifi_two_bars, C0021R$string.accessibility_wifi_three_bars, C0021R$string.accessibility_wifi_signal_full};
    private static final int[] SLAVE_WIFI_ICONS = {C0013R$drawable.stat_sys_slave_wifi_signal_0, C0013R$drawable.stat_sys_slave_wifi_signal_1, C0013R$drawable.stat_sys_slave_wifi_signal_2, C0013R$drawable.stat_sys_slave_wifi_signal_3, C0013R$drawable.stat_sys_slave_wifi_signal_4};
    private BroadcastDispatcher mBroadcastDispatcher;
    private boolean mConnected;
    private Context mContext;
    private boolean mEnabled;
    private int mLevel;
    private Handler mMainHandle;
    private int mRssi;
    private StatusBarIconController mStatusBarIconController;
    private boolean mSupportSlaveWifi;

    public SlaveWifiSignalController(Context context, Handler handler, StatusBarIconController statusBarIconController, Handler handler2, BroadcastDispatcher broadcastDispatcher) {
        this.mContext = context;
        this.mStatusBarIconController = statusBarIconController;
        statusBarIconController.setIcon("slave_wifi", C0013R$drawable.stat_sys_slave_wifi_signal_0_tint, context.getString(C0021R$string.accessibility_no_wifi));
        this.mStatusBarIconController.setIconVisibility("slave_wifi", false);
        this.mMainHandle = handler2;
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    public void start() {
        SlaveWifiUtils slaveWifiUtils = new SlaveWifiUtils(this.mContext);
        this.mSupportSlaveWifi = slaveWifiUtils.supportDualWifi(this.mContext);
        if (slaveWifiUtils.supportDualWifi(this.mContext)) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.SLAVE_STATE_CHANGE");
            intentFilter.addAction("android.net.wifi.SLAVE_RSSI_CHANGED");
            intentFilter.addAction("android.net.wifi.WIFI_SLAVE_STATE_CHANGED");
            this.mBroadcastDispatcher.registerReceiver(this, intentFilter, this.mContext.getMainExecutor(), UserHandle.ALL);
            if (slaveWifiUtils.isSlaveWifiEnabled()) {
                this.mEnabled = slaveWifiUtils.isSlaveWifiEnabled();
                WifiInfo wifiSlaveConnectionInfo = slaveWifiUtils.getWifiSlaveConnectionInfo();
                if (wifiSlaveConnectionInfo == null) {
                    Log.d("SlaveWifiSignalController", "SlaveWifiSignalController: don't connected");
                    return;
                }
                this.mConnected = wifiSlaveConnectionInfo.getNetworkId() != -1;
                int rssi = wifiSlaveConnectionInfo.getRssi();
                this.mRssi = rssi;
                this.mLevel = WifiManager.calculateSignalLevel(rssi, 5);
                Log.d("SlaveWifiSignalController", "SlaveWifiSignalController: init, connected = true, rssi = " + this.mRssi + ", level = " + this.mLevel);
                updateIconState();
                return;
            }
            Log.d("SlaveWifiSignalController", "SlaveWifiSignalController: don't enable");
        }
    }

    public void onReceive(Context context, Intent intent) {
        String action;
        if (this.mSupportSlaveWifi && (action = intent.getAction()) != null) {
            char c = 65535;
            int hashCode = action.hashCode();
            boolean z = false;
            if (hashCode != -1689576611) {
                if (hashCode != -1647522833) {
                    if (hashCode == -908597123 && action.equals("android.net.wifi.WIFI_SLAVE_STATE_CHANGED")) {
                        c = 1;
                    }
                } else if (action.equals("android.net.wifi.SLAVE_STATE_CHANGE")) {
                    c = 2;
                }
            } else if (action.equals("android.net.wifi.SLAVE_RSSI_CHANGED")) {
                c = 0;
            }
            if (c == 0) {
                int intExtra = intent.getIntExtra("newRssi", -200);
                this.mRssi = intExtra;
                this.mLevel = MiuiWifiManager.calculateSignalLevel(intExtra, 5);
                Log.d("SlaveWifiSignalController", "handleBroadcast: rssi changed,  rssi = " + this.mRssi + ", level = " + this.mLevel);
            } else if (c == 1) {
                if (intent.getIntExtra("wifi_state", 18) == 17) {
                    z = true;
                }
                this.mEnabled = z;
                Log.d("SlaveWifiSignalController", "handleBroadcast: wifi slave state changed, enabled = " + this.mEnabled);
            } else if (c == 2) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (networkInfo != null && networkInfo.isConnected()) {
                    z = true;
                }
                this.mConnected = z;
                Log.d("SlaveWifiSignalController", "handleBroadcast: network_slave_state_change, connected = " + this.mConnected);
            }
            updateIconState();
        }
    }

    /* access modifiers changed from: protected */
    public void updateIconState() {
        this.mMainHandle.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.SlaveWifiSignalController.AnonymousClass1 */

            public void run() {
                boolean z = false;
                int max = Math.max(Math.min(4, SlaveWifiSignalController.this.mLevel), 0);
                SlaveWifiSignalController.this.mStatusBarIconController.setIcon("slave_wifi", SlaveWifiSignalController.SLAVE_WIFI_ICONS[max], SlaveWifiSignalController.this.mContext.getString(SlaveWifiSignalController.SLAVE_WIFI_ACCESSIBILITY[max]));
                StatusBarIconController statusBarIconController = SlaveWifiSignalController.this.mStatusBarIconController;
                if (SlaveWifiSignalController.this.mEnabled && SlaveWifiSignalController.this.mConnected) {
                    z = true;
                }
                statusBarIconController.setIconVisibility("slave_wifi", z);
            }
        });
    }
}
