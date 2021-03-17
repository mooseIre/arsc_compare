package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MiuiSettings;
import android.util.Log;

public class VirtualSimUtils {
    public static boolean isVirtualSim(Context context, int i) {
        return MiuiSettings.VirtualSim.isVirtualSimEnabled(context) && i == MiuiSettings.VirtualSim.getVirtualSimSlotId(context);
    }

    public static String getVirtualSimCarrierName(Context context) {
        Bundle bundle;
        try {
            bundle = context.getContentResolver().call(Uri.parse("content://com.miui.virtualsim.provider.virtualsimInfo"), "getCarrierName", (String) null, (Bundle) null);
        } catch (Exception e) {
            Log.d("VirtualSimUtils", "getVirtualSimCarrierName e" + e);
            bundle = null;
        }
        if (bundle == null) {
            return null;
        }
        return bundle.getString("carrierName");
    }
}
