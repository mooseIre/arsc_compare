package com.android.systemui.statusbar.policy;

public interface WirelessChargingController extends CallbackController<Callback> {

    public interface Callback {
        void onWirelessChargingChanged(boolean z);
    }

    boolean isWirelessChargingEnabled();

    boolean isWirelessChargingSupported();

    void setWirelessChargingEnabled(boolean z);
}
