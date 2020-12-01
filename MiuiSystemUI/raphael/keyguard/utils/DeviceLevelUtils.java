package com.android.keyguard.utils;

import android.content.Context;
import android.security.KeyStore;
import android.util.Log;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import miui.os.Build;

public class DeviceLevelUtils {
    private static int DEVICE_LOW_END = 0;
    public static String TAG = "DeviceLevelUtils";
    private static Constructor<Class> sConstructor;
    private static int sDeviceLevel;
    private static Method sGetDeviceLevel;
    private static int sGpu;
    private static Object sObj;
    private static Class sPerfClass;
    private static PathClassLoader sPerfClassLoader;

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
        sConstructor = null;
        sGetDeviceLevel = null;
        sObj = null;
        sGpu = 0;
        try {
            sPerfClassLoader = new PathClassLoader("/system/framework/MiuiBooster.jar", ClassLoader.getSystemClassLoader());
            sPerfClass = sPerfClassLoader.loadClass("com.miui.performance.DeviceLevelUtils");
            sConstructor = sPerfClass.getConstructor(new Class[]{Context.class});
            sObj = sConstructor.newInstance(new Object[]{KeyStore.getApplicationContext()});
            sGetDeviceLevel = sPerfClass.getDeclaredMethod("getDeviceLevel", new Class[]{Integer.TYPE, Integer.TYPE});
            sGpu = ((Integer) sGetDeviceLevel.invoke(sObj, new Object[]{1, 3})).intValue();
            String str = TAG;
            Log.e(str, "gpu level: " + sGpu);
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "get gpu level exception:" + e);
        }
    }

    public static boolean isLowEndDevice() {
        return Build.IS_MIUI_LITE_VERSION || sDeviceLevel == DEVICE_LOW_END;
    }

    public static float getAnimationDurationRatio() {
        return isLowEndDevice() ? 0.6f : 1.0f;
    }

    public static boolean isLowGpuDevice() {
        return sGpu == 1;
    }
}
