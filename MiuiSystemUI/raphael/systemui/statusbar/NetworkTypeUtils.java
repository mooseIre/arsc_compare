package com.android.systemui.statusbar;

import android.telephony.ServiceState;
import android.util.Log;
import miui.os.Build;
import miui.util.FeatureParser;

public class NetworkTypeUtils {
    public static int getDataNetTypeFromServiceState(int i, ServiceState serviceState) {
        int i2 = 19;
        if ((i != 13 && i != 19) || serviceState == null) {
            i2 = i;
        } else if (!serviceState.isUsingCarrierAggregation() && (!FeatureParser.getBoolean("support_ca", false) || !Build.IS_CT_CUSTOMIZATION_TEST)) {
            i2 = 13;
        }
        Log.d("NetworkTypeUtils", "getDataNetTypeFromServiceState:srcDataNetType = " + i + ", destDataNetType " + i2);
        return i2;
    }
}
