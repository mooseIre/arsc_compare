package com.android.keyguard.utils;

import android.content.Context;
import android.telecom.TelecomManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import miui.telephony.TelephonyManager;

public class PhoneUtils {
    public static void resumeCall(Context context) {
        getTelecommManager(context).showInCallScreen(false);
    }

    public static boolean isInCall(Context context) {
        int phoneState = KeyguardUpdateMonitor.getInstance(context).getPhoneState();
        return phoneState == 1 || phoneState == 2;
    }

    public static int getPhoneCount() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        if (phoneCount <= 0) {
            return 1;
        }
        return phoneCount;
    }

    private static TelecomManager getTelecommManager(Context context) {
        return (TelecomManager) context.getSystemService("telecom");
    }
}
