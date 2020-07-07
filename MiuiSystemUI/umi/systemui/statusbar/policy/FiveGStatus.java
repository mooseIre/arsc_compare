package com.android.systemui.statusbar.policy;

import android.telephony.ServiceState;
import com.android.systemui.Constants;

public class FiveGStatus {
    public static boolean isNr5G(ServiceState serviceState) {
        if (Constants.IS_MEDIATEK && serviceState != null) {
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
