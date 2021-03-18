package com.android.keyguard.utils;

import android.app.ActivityOptions;
import android.content.Context;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import miui.telephony.TelephonyManager;

public class PhoneUtils {
    public static void resumeCall(Context context) {
        getTelecommManager(context).showInCallScreen(false);
    }

    public static boolean isInCall(Context context) {
        int phoneState = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getPhoneState();
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

    public static void takeEmergencyCallAction(Context context) {
        TelecomManager telecommManager = getTelecommManager(context);
        if (telecommManager == null) {
            Log.wtf("PhoneUtils", "TelecomManager was null, cannot launch emergency dialer");
        } else if (telecommManager.isInCall()) {
            telecommManager.showInCallScreen(false);
        } else {
            KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
            if (keyguardUpdateMonitor != null) {
                keyguardUpdateMonitor.reportEmergencyCallAction(true);
            } else {
                Log.w("PhoneUtils", "KeyguardUpdateMonitor was null, launching intent anyway.");
            }
            context.startActivityAsUser(telecommManager.createLaunchEmergencyDialerIntent(null).setFlags(343932928).putExtra("com.android.phone.EmergencyDialer.extra.ENTRY_TYPE", 1), ActivityOptions.makeCustomAnimation(context, 0, 0).toBundle(), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
        }
    }
}
