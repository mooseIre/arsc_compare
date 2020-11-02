package com.android.keyguard.fod;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.utils.ReflectUtil;
import com.android.systemui.plugins.R;
import java.util.Arrays;
import java.util.HashSet;
import miui.os.Build;

class MiuiGxzwUtils {
    private static int DENSITY_DPI = -1;
    public static int GXZW_ANIM_HEIGHT = 1008;
    public static int GXZW_ANIM_WIDTH = 1008;
    public static int GXZW_ICON_HEIGHT = 173;
    public static int GXZW_ICON_WIDTH = 173;
    public static int GXZW_ICON_X = 453;
    public static int GXZW_ICON_Y = 1640;
    private static final boolean IS_SPECIAL_CEPHEUS = ("cepheus".equals(Build.DEVICE) && new HashSet(Arrays.asList(new String[]{"1.12.2", "1.2.2", "1.9.2", "1.19.2"})).contains(SystemProperties.get("ro.boot.hwversion", "null")));
    public static int PRIVATE_FLAG_IS_HBM_OVERLAY = Integer.MIN_VALUE;
    private static int SCREEN_HEIGHT_DP = -1;
    private static int SCREEN_WIDTH_DP = -1;
    private static int sPreShowTouches = 0;
    private static int sPreShowTouchesUser = -10000;

    public static int getHaloRes() {
        return R.drawable.gxzw_white_halo_light;
    }

    public static boolean isLargeFod() {
        return false;
    }

    static {
        try {
            PRIVATE_FLAG_IS_HBM_OVERLAY = Class.forName("android.view.WindowManager$LayoutParams").getDeclaredField("PRIVATE_FLAG_IS_HBM_OVERLAY").getInt((Object) null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (NoSuchFieldException unused) {
            Log.w("MiuiGxzwUtils", "WindowManager.LayoutParams does not have this field: PRIVATE_FLAG_IS_HBM_OVERLAY");
        }
    }

    public static void caculateGxzwIconSize(Context context) {
        int i = context.getResources().getConfiguration().densityDpi;
        int i2 = context.getResources().getConfiguration().screenWidthDp;
        int i3 = context.getResources().getConfiguration().screenHeightDp;
        if (i != DENSITY_DPI || i2 != SCREEN_WIDTH_DP || i3 != SCREEN_HEIGHT_DP) {
            DENSITY_DPI = i;
            SCREEN_WIDTH_DP = i2;
            SCREEN_HEIGHT_DP = i3;
            String str = SystemProperties.get("persist.vendor.sys.fp.fod.location.X_Y", "");
            String str2 = SystemProperties.get("persist.vendor.sys.fp.fod.size.width_height", "");
            if (str.isEmpty() || str2.isEmpty()) {
                resetDefaultValue();
                return;
            }
            try {
                GXZW_ICON_X = Integer.parseInt(str.split(",")[0]);
                GXZW_ICON_Y = Integer.parseInt(str.split(",")[1]);
                GXZW_ICON_WIDTH = Integer.parseInt(str2.split(",")[0]);
                GXZW_ICON_HEIGHT = Integer.parseInt(str2.split(",")[1]);
                GXZW_ICON_Y -= caculateCutoutHeightIfNeed(context);
            } catch (Exception e) {
                e.printStackTrace();
                resetDefaultValue();
            }
            if (isLargeFod()) {
                GXZW_ANIM_WIDTH += GXZW_ICON_WIDTH;
                GXZW_ANIM_HEIGHT += GXZW_ICON_HEIGHT;
            }
        }
    }

    public static int caculateCutoutHeightIfNeed(Context context) {
        int i = 0;
        if (!MiuiSettings.Global.getBoolean(context.getContentResolver(), "force_black_v2")) {
            return 0;
        }
        Display display = ((DisplayManager) context.getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        DisplayCutout fromResourcesRectApproximation = DisplayCutout.fromResourcesRectApproximation(context.getResources(), point.x, point.y);
        if (fromResourcesRectApproximation != null) {
            i = fromResourcesRectApproximation.getSafeInsetTop();
        }
        return i % 2 != 0 ? i + 1 : i;
    }

    public static boolean isFodAodShowEnable(Context context) {
        if (Settings.Secure.getIntForUser(context.getContentResolver(), "gxzw_icon_aod_show_enable", 1, 0) != 1 || MiuiKeyguardUtils.isInvertColorsEnable(context)) {
            return false;
        }
        return true;
    }

    public static boolean isFodAodLowlightShowEnable(Context context) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), "gxzw_icon_aod_lowlight_show_enable", 1, 0) == 1;
    }

    public static void vibrateLight(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        if (vibrator != null) {
            vibrator.vibrate(12);
        }
    }

    public static void vibrateNormal(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        if (vibrator != null) {
            vibrator.vibrate(24);
        }
    }

    public static void notifySurfaceFlinger(int i, int i2) {
        IBinder service = ServiceManager.getService("SurfaceFlinger");
        if (service != null) {
            Parcel obtain = Parcel.obtain();
            obtain.writeInterfaceToken("android.ui.ISurfaceComposer");
            obtain.writeInt(i2);
            try {
                service.transact(i, obtain, (Parcel) null, 0);
            } catch (RemoteException e) {
                Log.e("MiuiGxzwUtils", "Failed to notifySurfaceFlinger", e);
            } catch (Throwable th) {
                obtain.recycle();
                throw th;
            }
            obtain.recycle();
        }
    }

    private static int getSupportTouchFeatureVersion() {
        try {
            Object callStaticObjectMethod = ReflectUtil.callStaticObjectMethod(Class.forName("miui.util.ITouchFeature"), "getInstance", (Class<?>[]) null, new Object[0]);
            if (callStaticObjectMethod != null) {
                return ((Integer) ReflectUtil.callObjectMethod(callStaticObjectMethod, "getSupportTouchFeatureVersion", (Class<?>[]) null, new Object[0])).intValue();
            }
            return 1;
        } catch (Exception e) {
            Log.i("MiuiGxzwUtils", e.toString());
            return 1;
        }
    }

    public static boolean setTouchMode(int i, int i2) {
        if (1 == getSupportTouchFeatureVersion()) {
            return setTouchModelV1(i, i2);
        }
        return setTouchModelV2(i, i2);
    }

    private static boolean setTouchModelV2(int i, int i2) {
        try {
            Log.i("MiuiGxzwUtils", "setTouchmode v2 mode:" + i + " value" + i2);
            Object callStaticObjectMethod = ReflectUtil.callStaticObjectMethod(Class.forName("miui.util.ITouchFeature"), "getInstance", (Class<?>[]) null, new Object[0]);
            if (callStaticObjectMethod != null) {
                return ((Boolean) ReflectUtil.callObjectMethod(callStaticObjectMethod, "setTouchMode", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE}, 0, Integer.valueOf(i), Integer.valueOf(i2))).booleanValue();
            }
        } catch (Exception e) {
            Log.i("MiuiGxzwUtils", e.toString());
        }
        return false;
    }

    private static boolean setTouchModelV1(int i, int i2) {
        try {
            Log.i("MiuiGxzwUtils", "setTouchMode v1 mode:" + i + " value" + i2);
            Object callStaticObjectMethod = ReflectUtil.callStaticObjectMethod(Class.forName("miui.util.ITouchFeature"), "getInstance", (Class<?>[]) null, new Object[0]);
            if (callStaticObjectMethod != null) {
                return ((Boolean) ReflectUtil.callObjectMethod(callStaticObjectMethod, "setTouchMode", new Class[]{Integer.TYPE, Integer.TYPE}, Integer.valueOf(i), Integer.valueOf(i2))).booleanValue();
            }
        } catch (Exception e) {
            Log.i("MiuiGxzwUtils", e.toString());
        }
        return false;
    }

    private static void resetDefaultValue() {
        GXZW_ICON_X = 453;
        GXZW_ICON_Y = 1640;
        GXZW_ICON_WIDTH = 173;
        GXZW_ICON_HEIGHT = 173;
    }

    public static boolean isSpecialCepheus() {
        return IS_SPECIAL_CEPHEUS;
    }

    public static boolean supportHalo(Context context) {
        return context.getResources().getBoolean(R.bool.config_enableFodCircleHalo);
    }

    public static float getHaloResCircleRadius(Context context) {
        return (float) context.getResources().getDimensionPixelOffset(R.dimen.gxzw_halo_res_circle_radius);
    }

    public static void saveShowTouchesState(Context context) {
        sPreShowTouchesUser = KeyguardUpdateMonitor.getCurrentUser();
        sPreShowTouches = Settings.System.getIntForUser(context.getContentResolver(), "show_touches", 0, sPreShowTouchesUser);
        if (sPreShowTouches != 0) {
            Settings.System.putIntForUser(context.getContentResolver(), "show_touches", 0, sPreShowTouchesUser);
        }
    }

    public static void restoreShowTouchesState(Context context) {
        if (sPreShowTouches != 0) {
            Settings.System.putIntForUser(context.getContentResolver(), "show_touches", sPreShowTouches, sPreShowTouchesUser);
            sPreShowTouches = 0;
            sPreShowTouchesUser = -10000;
        }
    }
}
