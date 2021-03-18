package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.systemui.C0021R$string;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SignalController;
import java.util.Objects;

public class MiuiWifiSignalController extends SignalController<WifiState, SignalController.IconGroup> {
    private final WifiStatusTracker mWifiTracker;

    public MiuiWifiSignalController(Context context, boolean z, CallbackHandler callbackHandler, NetworkControllerImpl networkControllerImpl, WifiManager wifiManager, ConnectivityManager connectivityManager, NetworkScoreManager networkScoreManager) {
        super("WifiSignalController", context, 1, callbackHandler, networkControllerImpl);
        WifiStatusTracker wifiStatusTracker = new WifiStatusTracker(this.mContext, wifiManager, networkScoreManager, connectivityManager, new Runnable() {
            /* class com.android.systemui.statusbar.policy.$$Lambda$MiuiWifiSignalController$MUWAVC22i7B3vqU5A1N8W3Oo91w */

            public final void run() {
                MiuiWifiSignalController.lambda$MUWAVC22i7B3vqU5A1N8W3Oo91w(MiuiWifiSignalController.this);
            }
        });
        this.mWifiTracker = wifiStatusTracker;
        wifiStatusTracker.setListening(true);
        if (wifiManager != null) {
            wifiManager.registerTrafficStateCallback(context.getMainExecutor(), new WifiTrafficStateCallback());
        }
        SignalController.IconGroup iconGroup = new SignalController.IconGroup("Wi-Fi Icons", MiuiWifiIcons.WIFI_SIGNAL_STRENGTH, MiuiWifiIcons.QS_WIFI_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, MiuiWifiIcons.WIFI_NO_NETWORK, MiuiWifiIcons.QS_WIFI_NO_NETWORK, MiuiWifiIcons.WIFI_NO_NETWORK, MiuiWifiIcons.QS_WIFI_NO_NETWORK, AccessibilityContentDescriptions.WIFI_NO_CONNECTION);
        ((WifiState) this.mLastState).iconGroup = iconGroup;
        ((WifiState) this.mCurrentState).iconGroup = iconGroup;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.SignalController
    public WifiState cleanState() {
        return new WifiState();
    }

    /* access modifiers changed from: package-private */
    public void refreshLocale() {
        this.mWifiTracker.refreshLocale();
    }

    @Override // com.android.systemui.statusbar.policy.SignalController
    public void notifyListeners(NetworkController.SignalCallback signalCallback) {
        T t = this.mCurrentState;
        boolean z = ((WifiState) t).enabled && ((WifiState) t).connected;
        T t2 = this.mCurrentState;
        String str = ((WifiState) t2).connected ? ((WifiState) t2).ssid : null;
        boolean z2 = z && ((WifiState) this.mCurrentState).ssid != null;
        String charSequence = getTextIfExists(getContentDescription()).toString();
        if (((WifiState) this.mCurrentState).inetCondition == 0) {
            charSequence = charSequence + "," + this.mContext.getString(C0021R$string.data_connection_no_internet);
        }
        NetworkController.IconState iconState = new NetworkController.IconState(z, getCurrentIconId(), charSequence);
        NetworkController.IconState iconState2 = new NetworkController.IconState(((WifiState) this.mCurrentState).connected, getQsCurrentIconId(), charSequence);
        T t3 = this.mCurrentState;
        boolean z3 = ((WifiState) t3).enabled;
        boolean z4 = z2 && ((WifiState) t3).activityIn;
        boolean z5 = z2 && ((WifiState) this.mCurrentState).activityOut;
        T t4 = this.mCurrentState;
        signalCallback.setWifiIndicators(z3, iconState, iconState2, z4, z5, ((WifiState) t4).wifiStandard, str, ((WifiState) t4).isTransient, ((WifiState) t4).statusLabel, ((WifiState) t4).noNetwork);
    }

    public void fetchInitialState() {
        this.mWifiTracker.fetchInitialState();
        T t = this.mCurrentState;
        WifiStatusTracker wifiStatusTracker = this.mWifiTracker;
        ((WifiState) t).enabled = wifiStatusTracker.enabled;
        ((WifiState) t).connected = wifiStatusTracker.connected;
        ((WifiState) t).ssid = wifiStatusTracker.ssid;
        ((WifiState) t).rssi = wifiStatusTracker.rssi;
        ((WifiState) t).level = wifiStatusTracker.level;
        ((WifiState) t).statusLabel = wifiStatusTracker.statusLabel;
        notifyListenersIfNecessary();
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
        ((WifiState) t).statusLabel = wifiStatusTracker.statusLabel;
        ((WifiState) t).wifiStandard = wifiStatusTracker.wifiStandard;
        ((WifiState) t).isReady = wifiStatusTracker.vhtMax8SpatialStreamsSupport && wifiStatusTracker.he8ssCapableAp;
        if (intent != null && "android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
            updateWifiNoNetwork();
        }
        notifyListenersIfNecessary();
    }

    /* access modifiers changed from: protected */
    public void updateWifiNoNetwork() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        ((WifiState) this.mCurrentState).noNetwork = activeNetworkInfo != null && ((WifiState) this.mCurrentState).connected && !ConnectivityManager.isNetworkTypeWifi(activeNetworkInfo.getType());
    }

    /* access modifiers changed from: private */
    public void handleStatusUpdated() {
        ((WifiState) this.mCurrentState).statusLabel = this.mWifiTracker.statusLabel;
        notifyListenersIfNecessary();
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

    private class WifiTrafficStateCallback implements WifiManager.TrafficStateCallback {
        private WifiTrafficStateCallback() {
        }

        public void onStateChanged(int i) {
            MiuiWifiSignalController.this.setActivity(i);
        }
    }

    /* access modifiers changed from: package-private */
    public static class WifiState extends SignalController.State {
        boolean isReady;
        boolean isTransient;
        boolean noNetwork;
        String ssid;
        String statusLabel;
        int wifiStandard;

        WifiState() {
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void copyFrom(SignalController.State state) {
            super.copyFrom(state);
            WifiState wifiState = (WifiState) state;
            this.ssid = wifiState.ssid;
            this.wifiStandard = wifiState.wifiStandard;
            this.isReady = wifiState.isReady;
            this.isTransient = wifiState.isTransient;
            this.statusLabel = wifiState.statusLabel;
            this.noNetwork = wifiState.noNetwork;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public void toString(StringBuilder sb) {
            super.toString(sb);
            sb.append(",ssid=");
            sb.append(this.ssid);
            sb.append(",wifiStandard=");
            sb.append(this.wifiStandard);
            sb.append(",isReady=");
            sb.append(this.isReady);
            sb.append(",noNetwork=");
            sb.append(this.noNetwork);
            sb.append(",isTransient=");
            sb.append(this.isTransient);
            sb.append(",statusLabel=");
            sb.append(this.statusLabel);
        }

        @Override // com.android.systemui.statusbar.policy.SignalController.State
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            WifiState wifiState = (WifiState) obj;
            if (Objects.equals(wifiState.ssid, this.ssid) && wifiState.wifiStandard == this.wifiStandard && wifiState.isReady == this.isReady && wifiState.noNetwork == this.noNetwork && wifiState.isTransient == this.isTransient && TextUtils.equals(wifiState.statusLabel, this.statusLabel)) {
                return true;
            }
            return false;
        }
    }
}
