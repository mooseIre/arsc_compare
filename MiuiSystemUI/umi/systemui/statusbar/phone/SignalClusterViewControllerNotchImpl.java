package com.android.systemui.statusbar.phone;

public class SignalClusterViewControllerNotchImpl extends SignalClusterViewControllerImpl {
    public boolean isNotch() {
        return true;
    }

    public boolean isNotchEarDualEnable() {
        return true;
    }

    public boolean isVoWifiEnableInEar() {
        return false;
    }

    public boolean isVpnEnableInEar() {
        return false;
    }

    public boolean isWifiNoNetworkEnableInEar() {
        return false;
    }
}
