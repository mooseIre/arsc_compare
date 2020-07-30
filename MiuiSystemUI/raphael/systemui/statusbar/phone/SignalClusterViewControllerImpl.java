package com.android.systemui.statusbar.phone;

public class SignalClusterViewControllerImpl implements SignalClusterViewController {
    public boolean isDrip() {
        return false;
    }

    public boolean isEthernetAble() {
        return true;
    }

    public boolean isNotch() {
        return false;
    }

    public boolean isNotchEarDualEnable() {
        return false;
    }

    public boolean isVoWifiEnableInEar() {
        return true;
    }

    public boolean isVpnEnableInEar() {
        return true;
    }

    public boolean isWifiNoNetworkEnableInEar() {
        return true;
    }
}
