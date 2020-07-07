package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManagerCompat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.AsyncChannel;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.systemui.SettingsLibCompat;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalController;
import java.util.Objects;

public class WifiSignalController extends SignalController<WifiState, SignalController.IconGroup> {
    private boolean mShowWifiGeneraion;
    /* access modifiers changed from: private */
    public final AsyncChannel mWifiChannel;
    private int mWifiGeneration;
    private final WifiStatusTracker mWifiTracker;

    public WifiSignalController(Context context, boolean z, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, NetworkScoreManager networkScoreManager) {
        this(context, z, callbackHandler, networkControllerImpl, (WifiManager) context.getSystemService("wifi"));
    }

    public WifiSignalController(Context context, boolean z, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, WifiManager wifiManager) {
        super("WifiSignalController", context, 1, callbackHandler, networkControllerImpl);
        WifiStatusTracker wifiStatusTracker = new WifiStatusTracker(this.mContext, wifiManager, (NetworkScoreManager) context.getSystemService(NetworkScoreManager.class), (ConnectivityManager) context.getSystemService(ConnectivityManager.class), new Runnable() {
            public final void run() {
                WifiSignalController.this.handleStatusUpdated();
            }
        });
        this.mWifiTracker = wifiStatusTracker;
        wifiStatusTracker.setListening(true);
        WifiHandler wifiHandler = new WifiHandler(Looper.getMainLooper());
        this.mWifiChannel = new AsyncChannel();
        Messenger wifiServiceMessenger = WifiManagerCompat.getWifiServiceMessenger(wifiManager);
        if (wifiServiceMessenger != null) {
            this.mWifiChannel.connect(context, wifiHandler, wifiServiceMessenger);
        }
        if (wifiManager != null) {
            WifiManagerCompat.registerTrafficStateCallback(context, wifiManager, new WifiTrafficStateCallback(), (Handler) null);
        }
        SignalController.IconGroup iconGroup = new SignalController.IconGroup("Wi-Fi Icons", WifiIcons.WIFI_SIGNAL_STRENGTH, WifiIcons.QS_WIFI_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, R.drawable.stat_sys_wifi_signal_null, R.drawable.ic_qs_wifi_no_network, R.drawable.stat_sys_wifi_signal_null, R.drawable.ic_qs_wifi_no_network, R.string.accessibility_no_wifi);
        ((WifiState) this.mLastState).iconGroup = iconGroup;
        ((WifiState) this.mCurrentState).iconGroup = iconGroup;
    }

    /* access modifiers changed from: protected */
    public WifiState cleanState() {
        return new WifiState();
    }

    public void notifyListeners(NetworkController.SignalCallback signalCallback) {
        T t = this.mCurrentState;
        boolean z = ((WifiState) t).enabled && ((WifiState) t).connected;
        String str = z ? ((WifiState) this.mCurrentState).ssid : null;
        boolean z2 = z && ((WifiState) this.mCurrentState).ssid != null;
        String stringIfExists = getStringIfExists(getContentDescription());
        if (((WifiState) this.mCurrentState).inetCondition == 0) {
            stringIfExists = stringIfExists + "," + this.mContext.getString(R.string.data_connection_no_internet);
        }
        NetworkController.IconState iconState = new NetworkController.IconState(z, getCurrentIconId(), stringIfExists);
        NetworkController.IconState iconState2 = new NetworkController.IconState(((WifiState) this.mCurrentState).connected, getQsCurrentIconId(), stringIfExists);
        signalCallback.updateWifiGeneration(this.mShowWifiGeneraion, this.mWifiGeneration);
        T t2 = this.mCurrentState;
        signalCallback.setWifiIndicators(((WifiState) t2).enabled, iconState, iconState2, z2 && ((WifiState) t2).activityIn, z2 && ((WifiState) this.mCurrentState).activityOut, str, ((WifiState) this.mCurrentState).isTransient);
    }

    public int getCurrentIconId() {
        if (((WifiState) this.mCurrentState).noNetwork) {
            return R.drawable.stat_sys_wifi_signal_null;
        }
        return super.getCurrentIconId();
    }

    public void handleBroadcast(Intent intent) {
        this.mWifiTracker.handleBroadcast(intent);
        T t = this.mCurrentState;
        WifiStatusTracker wifiStatusTracker = this.mWifiTracker;
        ((WifiState) t).enabled = wifiStatusTracker.enabled;
        ((WifiState) t).connected = wifiStatusTracker.connected;
        ((WifiState) t).ssid = wifiStatusTracker.ssid;
        ((WifiState) t).rssi = wifiStatusTracker.rssi;
        ((WifiState) t).level = wifiStatusTracker.level;
        int wifiStandard = SettingsLibCompat.getWifiStandard(wifiStatusTracker);
        this.mWifiGeneration = wifiStandard;
        boolean z = true;
        this.mShowWifiGeneraion = wifiStandard == 6;
        T t2 = this.mCurrentState;
        WifiStatusTracker wifiStatusTracker2 = this.mWifiTracker;
        ((WifiState) t2).statusLabel = wifiStatusTracker2.statusLabel;
        WifiState wifiState = (WifiState) t2;
        int i = wifiStatusTracker2.state;
        if (!(i == 2 || i == 0)) {
            z = false;
        }
        wifiState.isTransient = z;
        if (intent != null && "android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
            updateWifiNoNetwork();
        }
        notifyListenersIfNecessary();
    }

    /* access modifiers changed from: private */
    public void handleStatusUpdated() {
        ((WifiState) this.mCurrentState).statusLabel = this.mWifiTracker.statusLabel;
        notifyListenersIfNecessary();
    }

    public void updateWifiNoNetwork() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        ((WifiState) this.mCurrentState).noNetwork = activeNetworkInfo != null && ((WifiState) this.mCurrentState).connected && !ConnectivityManager.isNetworkTypeWifi(activeNetworkInfo.getType());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setActivity(int i) {
        boolean z = false;
        ((WifiState) this.mCurrentState).activityIn = i == 3 || i == 1;
        WifiState wifiState = (WifiState) this.mCurrentState;
        if (i == 3 || i == 2) {
            z = true;
        }
        wifiState.activityOut = z;
        notifyListenersIfNecessary();
    }

    private class WifiHandler extends Handler {
        WifiHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                WifiSignalController.this.setActivity(message.arg1);
            } else if (i == 69632) {
                if (message.arg1 == 0) {
                    WifiSignalController.this.mWifiChannel.sendMessage(Message.obtain(this, 69633));
                } else {
                    Log.e(WifiSignalController.this.mTag, "Failed to connect to wifi");
                }
            }
        }
    }

    private class WifiTrafficStateCallback implements WifiManagerCompat.TrafficStateCallback {
        private WifiTrafficStateCallback() {
        }

        public void onStateChanged(int i) {
            WifiSignalController.this.setActivity(i);
        }
    }

    static class WifiState extends SignalController.State {
        boolean isTransient;
        boolean noNetwork;
        String ssid;
        String statusLabel;

        WifiState() {
        }

        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            WifiState wifiState = (WifiState) state;
            this.ssid = wifiState.ssid;
            this.isTransient = wifiState.isTransient;
            this.statusLabel = wifiState.statusLabel;
            this.noNetwork = wifiState.noNetwork;
        }

        /* access modifiers changed from: protected */
        public void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(",ssid=");
            sb.append(this.ssid);
            sb.append(",isTransient=");
            sb.append(this.isTransient);
            sb.append(",statusLabel=");
            sb.append(this.statusLabel);
            sb.append(",noNetwork=");
            sb.append(this.noNetwork);
        }

        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            WifiState wifiState = (WifiState) obj;
            if (!Objects.equals(wifiState.ssid, this.ssid) || wifiState.isTransient != this.isTransient || !TextUtils.equals(wifiState.statusLabel, this.statusLabel) || wifiState.noNetwork != this.noNetwork) {
                return false;
            }
            return true;
        }
    }
}
