package com.android.systemui.statusbar.policy;

import android.telephony.ServiceState;
import com.miui.systemui.DeviceConfig;

public class FiveGStatus {
    public static boolean isNr5G(ServiceState serviceState) {
        if (DeviceConfig.IS_MEDIATEK && serviceState != null) {
            int nrState = serviceState.getNrState();
            if (nrState == 3 || nrState == 2) {
                return true;
            }
            if (nrState == 1) {
            }
        }
        return false;
    }
}
