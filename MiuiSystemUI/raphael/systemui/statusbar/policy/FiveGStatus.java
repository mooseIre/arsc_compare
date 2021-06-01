package com.android.systemui.statusbar.policy;

import android.telephony.ServiceState;
import com.miui.systemui.DeviceConfig;

public class FiveGStatus {
    public static boolean isNr5G(ServiceState serviceState, String str) {
        if (DeviceConfig.IS_MEDIATEK && serviceState != null) {
            int nrState = serviceState.getNrState();
            if (nrState == 3) {
                return true;
            }
            if (nrState == 2) {
                if ("26001".equals(str) || "26012".equals(str) || "26015".equals(str) || "26017".equals(str)) {
                    return false;
                }
                return true;
            } else if (nrState == 1) {
            }
        }
        return false;
    }
}
