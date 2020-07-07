package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.MiuiWifiManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.android.settingslib.wifi.SlaveWifiUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalController;

public class SlaveWifiSignalController extends SignalController<SlaveWifiState, SignalController.IconGroup> {
    private boolean mSupportSlaveWifi;

    public SlaveWifiSignalController(Context context, boolean z, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl) {
        super("SlaveWifiSignalController", context, 1, callbackHandler, networkControllerImpl);
        this.mSupportSlaveWifi = z;
        if (z) {
            SignalController.IconGroup iconGroup = new SignalController.IconGroup("Slave WiFi Icons", WifiIcons.SB_SLAVE_WIFI_SIGNAL_STRENGTH, WifiIcons.QS_SLAVE_WIFI_SIGNAL_STRENGTH, AccessibilityContentDescriptions.SLAVE_WIFI_CONNECTION_STRENGTH, R.drawable.stat_sys_wifi_signal_null, R.drawable.ic_qs_wifi_no_network, R.drawable.stat_sys_wifi_signal_null, R.drawable.ic_qs_wifi_no_network, R.string.accessibility_status_bar_no_slave_wifi);
            ((SlaveWifiState) this.mLastState).iconGroup = iconGroup;
            ((SlaveWifiState) this.mCurrentState).iconGroup = iconGroup;
            SlaveWifiUtils slaveWifiUtils = new SlaveWifiUtils(context);
            if (slaveWifiUtils.isSlaveWifiEnabled()) {
                ((SlaveWifiState) this.mLastState).enabled = true;
                ((SlaveWifiState) this.mCurrentState).enabled = true;
                WifiInfo wifiSlaveConnectionInfo = slaveWifiUtils.getWifiSlaveConnectionInfo();
                if (wifiSlaveConnectionInfo == null) {
                    Log.d("SlaveWifiSignalController", "SlaveWifiSignalController: don't connected");
                    return;
                }
                T t = this.mCurrentState;
                T t2 = this.mLastState;
                ((SlaveWifiState) t2).connected = true;
                ((SlaveWifiState) t).connected = true;
                int rssi = wifiSlaveConnectionInfo.getRssi();
                ((SlaveWifiState) t2).rssi = rssi;
                ((SlaveWifiState) t).rssi = rssi;
                T t3 = this.mCurrentState;
                int calculateSignalLevel = WifiManager.calculateSignalLevel(((SlaveWifiState) t3).rssi, 5);
                ((SlaveWifiState) this.mLastState).level = calculateSignalLevel;
                ((SlaveWifiState) t3).level = calculateSignalLevel;
                Log.d("SlaveWifiSignalController", "SlaveWifiSignalController: init, connected = true, rssi = " + ((SlaveWifiState) this.mCurrentState).rssi + ", level = " + ((SlaveWifiState) this.mCurrentState).level);
                return;
            }
            Log.d("SlaveWifiSignalController", "SlaveWifiSignalController: don't enable");
        }
    }

    /* access modifiers changed from: protected */
    public void handleBroadcast(Intent intent) {
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
                ((SlaveWifiState) this.mCurrentState).rssi = intent.getIntExtra("newRssi", -200);
                T t = this.mCurrentState;
                ((SlaveWifiState) t).level = MiuiWifiManager.calculateSignalLevel(((SlaveWifiState) t).rssi, 5);
                Log.d("SlaveWifiSignalController", "handleBroadcast: rssi changed,  mCurrentState.rssi = " + ((SlaveWifiState) this.mCurrentState).rssi + ", mCurrentState.level = " + ((SlaveWifiState) this.mCurrentState).level);
            } else if (c == 1) {
                SlaveWifiState slaveWifiState = (SlaveWifiState) this.mCurrentState;
                if (intent.getIntExtra("wifi_state", 18) == 17) {
                    z = true;
                }
                slaveWifiState.enabled = z;
                Log.d("SlaveWifiSignalController", "handleBroadcast: wifi slave state changed, mCurrentState.enabled = " + ((SlaveWifiState) this.mCurrentState).enabled);
            } else if (c == 2) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                SlaveWifiState slaveWifiState2 = (SlaveWifiState) this.mCurrentState;
                if (networkInfo != null && networkInfo.isConnected()) {
                    z = true;
                }
                slaveWifiState2.connected = z;
                Log.d("SlaveWifiSignalController", "handleBroadcast: network_slave_state_change, mCurrentState.connected = " + ((SlaveWifiState) this.mCurrentState).connected);
            }
            notifyListenersIfNecessary();
        }
    }

    public void notifyListeners(NetworkController.SignalCallback signalCallback) {
        if (this.mSupportSlaveWifi) {
            T t = this.mCurrentState;
            boolean z = ((SlaveWifiState) t).enabled && ((SlaveWifiState) t).connected;
            Log.d("SlaveWifiSignalController", "notifyListeners: " + z);
            String stringIfExists = getStringIfExists(getContentDescription());
            signalCallback.setSlaveWifiIndicators(((SlaveWifiState) this.mCurrentState).enabled, new NetworkController.IconState(z, getCurrentIconId(), stringIfExists), new NetworkController.IconState(z, getQsCurrentIconId(), stringIfExists));
        }
    }

    /* access modifiers changed from: protected */
    public SlaveWifiState cleanState() {
        return new SlaveWifiState();
    }

    static class SlaveWifiState extends SignalController.State {
        SlaveWifiState() {
        }
    }
}
