package com.android.keyguard.utils;

import android.util.Log;
import miui.os.Build;

public class DeviceLevelUtils {
    private static int DEVICE_LOW_END = 0;
    public static String TAG = "DeviceLevelUtils";
    private static int sDeviceLevel;

    static {
        try {
            sDeviceLevel = ((Integer) ReflectUtil.callStaticObjectMethod(Build.class, Integer.TYPE, "getDeviceLevelForAnimation", (Class<?>[]) null, new Object[0])).intValue();
        } catch (Exception unused) {
            sDeviceLevel = -1;
            Log.e(TAG, "reflect failed when get device level");
        }
        try {
            DEVICE_LOW_END = ((Integer) ReflectUtil.getStaticObjectField(Build.class, "DEVICE_LOW_END", Integer.TYPE)).intValue();
        } catch (Exception unused2) {
            DEVICE_LOW_END = -2;
            Log.e(TAG, "reflect failed when get device_low_end");
        }
    }

    public static boolean isLowEndDevice() {
        return Build.IS_MIUI_LITE_VERSION || sDeviceLevel == DEVICE_LOW_END;
    }

    public static float getAnimationDurationRatio() {
        return isLowEndDevice() ? 0.6f : 1.0f;
    }
}
