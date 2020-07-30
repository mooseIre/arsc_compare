package com.android.systemui.statusbar.phone;

public class SignalClusterViewControllerDripImpl implements SignalClusterViewController {
    public boolean isDrip() {
        return true;
    }

    public boolean isEthernetAble() {
        return false;
    }

    public boolean isNotch() {
        return true;
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
