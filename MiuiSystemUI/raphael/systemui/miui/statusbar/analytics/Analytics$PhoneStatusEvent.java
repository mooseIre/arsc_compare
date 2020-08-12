package com.android.systemui.miui.statusbar.analytics;

import android.content.Context;
import android.content.ContextCompat;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.IWindowManagerCompat;
import android.view.WindowManagerGlobal;
import com.android.internal.view.RotationPolicy;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import miui.telephony.TelephonyManager;
import miui.util.AudioManagerHelper;

public class Analytics$PhoneStatusEvent extends Analytics$Event {
    public static int getIsDualCardValue() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        int i = 0;
        for (int i2 = 0; i2 < phoneCount; i2++) {
            if (TelephonyManager.getDefault().hasIccCard(i2)) {
                i++;
            }
        }
        return i > 1 ? 1 : 0;
    }

    public static int getIsAlarmSetValue() {
        return ((NextAlarmController) Dependency.get(NextAlarmController.class)).hasAlarm() ? 1 : 0;
    }

    public static int getIsMuteTurnedOnValue(Context context) {
        return AudioManagerHelper.isSilentEnabled(context) ? 1 : 0;
    }

    public static int getIsWifiTurnedOnValue(Context context) {
        return ((WifiManager) context.getSystemService("wifi")).isWifiEnabled() ? 1 : 0;
    }

    public static int getIsBluetoothTurnedOnValue() {
        return ((BluetoothController) Dependency.get(BluetoothController.class)).isBluetoothEnabled() ? 1 : 0;
    }

    public static int getIsAutoBrightnessTurnedOnValue(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "screen_brightness_mode", 0);
    }

    public static int getIsGpsTurnedOnValue(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "location_mode", 0) != 0 ? 1 : 0;
    }

    public static int getIsRotationLockTurnedOnValue(Context context) {
        return RotationPolicy.isRotationLocked(context) ? 1 : 0;
    }

    public static int getIsFullScreen(Context context) {
        try {
            return IWindowManagerCompat.hasNavigationBar(WindowManagerGlobal.getWindowManagerService(), ContextCompat.getDisplayId(context)) ? 1 : 0;
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getIsNotchScreen() {
        return Constants.IS_NOTCH ? 1 : 0;
    }
}
