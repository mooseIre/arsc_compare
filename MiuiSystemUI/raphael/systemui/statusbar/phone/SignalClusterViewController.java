package com.android.systemui.statusbar.phone;

public interface SignalClusterViewController {
    boolean isDrip();

    boolean isEthernetAble();

    boolean isNotch();

    boolean isNotchEarDualEnable();

    boolean isVoWifiEnableInEar();

    boolean isVpnEnableInEar();

    boolean isWifiNoNetworkEnableInEar();
}
