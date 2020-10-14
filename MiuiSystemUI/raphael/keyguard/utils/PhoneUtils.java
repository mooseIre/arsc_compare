package com.android.keyguard.utils;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.KeyguardUpdateMonitor;
import miui.telephony.TelephonyManager;

public class PhoneUtils {
    private static final Intent INTENT_EMERGENCY_DIAL = new Intent().setAction("com.android.phone.EmergencyDialer.DIAL").setPackage("com.android.phone").setFlags(343932928);

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

    public static void takeEmergencyCallAction(Context context, EmergencyButton.EmergencyButtonCallback emergencyButtonCallback) {
        if (isInCall(context)) {
            resumeCall(context);
            if (emergencyButtonCallback != null) {
                emergencyButtonCallback.onEmergencyButtonClickedWhenInCall();
                return;
            }
            return;
        }
        KeyguardUpdateMonitor.getInstance(context).reportEmergencyCallAction(true);
        context.startActivityAsUser(INTENT_EMERGENCY_DIAL, ActivityOptions.makeCustomAnimation(context, 0, 0).toBundle(), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
    }
}
