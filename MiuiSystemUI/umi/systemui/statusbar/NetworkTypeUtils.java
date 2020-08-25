package com.android.systemui.statusbar;

import android.telephony.ServiceState;
import miui.os.Build;
import miui.util.FeatureParser;

public class NetworkTypeUtils {
    public static int getDataNetTypeFromServiceState(int i, ServiceState serviceState) {
        if ((i == 13 || i == 19) && serviceState != null) {
            return (serviceState.isUsingCarrierAggregation() || (FeatureParser.getBoolean("support_ca", false) && Build.IS_CT_CUSTOMIZATION_TEST)) ? 19 : 13;
        }
        return i;
    }
}
