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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.miui.systemui.util.ReflectUtil;
import java.math.BigDecimal;

/* access modifiers changed from: package-private */
public class MiuiGxzwUtils {
    private static int DENSITY_DPI = -1;
    public static int GXZW_ANIM_HEIGHT = 1028;
    public static int GXZW_ANIM_WIDTH = 1028;
    private static final float GXZW_ANIM_WIDTH_PRCENT = getPrcent(1028, 1080);
    public static float GXZW_HEIGHT_PRCENT = -1.0f;
    public static int GXZW_ICON_HEIGHT = 173;
    public static int GXZW_ICON_WIDTH = 173;
    public static int GXZW_ICON_X = 453;
    public static int GXZW_ICON_Y = 1640;
    private static final boolean GXZW_LOWLIGHT_SENSOR;
    public static float GXZW_WIDTH_PRCENT = -1.0f;
    private static float GXZW_X_PRCENT = -1.0f;
    private static float GXZW_Y_PRCENT = -1.0f;
    public static int PRIVATE_FLAG_IS_HBM_OVERLAY;
    private static int SCREEN_HEIGHT_DP = -1;
    public static int SCREEN_HEIGHT_PHYSICAL = -1;
    public static int SCREEN_HEIGHT_PX = -1;
    private static int SCREEN_WIDTH_DP = -1;
    public static int SCREEN_WIDTH_PHYSICAL = -1;
    public static int SCREEN_WIDTH_PX = -1;

    public static boolean isLargeFod() {
        return false;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt("persist.vendor.sys.fp.expolevel", 0) == 136) {
            z = true;
        }
        GXZW_LOWLIGHT_SENSOR = z;
        PRIVATE_FLAG_IS_HBM_OVERLAY = Integer.MIN_VALUE;
        try {
            PRIVATE_FLAG_IS_HBM_OVERLAY = Class.forName("android.view.WindowManager$LayoutParams").getDeclaredField("PRIVATE_FLAG_IS_HBM_OVERLAY").getInt(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (NoSuchFieldException unused) {
            Log.w("MiuiGxzwUtils", "WindowManager.LayoutParams does not have this field: PRIVATE_FLAG_IS_HBM_OVERLAY");
        }
    }

    public static boolean isSupportLowlight() {
        return GXZW_LOWLIGHT_SENSOR;
    }

    public static boolean isSupportNonuiSensor() {
        return (SystemProperties.getInt("ro.vendor.touchfeature.type", 0) & 128) != 0;
    }

    public static void caculateGxzwIconSize(Context context) {
        int i = context.getResources().getConfiguration().densityDpi;
        int i2 = context.getResources().getConfiguration().screenWidthDp;
        int i3 = context.getResources().getConfiguration().screenHeightDp;
        if (i != DENSITY_DPI || i2 != SCREEN_WIDTH_DP || i3 != SCREEN_HEIGHT_DP) {
            DENSITY_DPI = i;
            SCREEN_WIDTH_DP = i2;
            SCREEN_HEIGHT_DP = i3;
            if (SCREEN_WIDTH_PHYSICAL == -1) {
                phySicalScreenPx(context);
            }
            screenWhPx(context);
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
                GXZW_X_PRCENT = getPrcent(GXZW_ICON_X, SCREEN_WIDTH_PHYSICAL);
                GXZW_Y_PRCENT = getPrcent(GXZW_ICON_Y, SCREEN_HEIGHT_PHYSICAL);
                GXZW_WIDTH_PRCENT = getPrcent(GXZW_ICON_WIDTH, SCREEN_WIDTH_PHYSICAL);
                float prcent = getPrcent(GXZW_ICON_HEIGHT, SCREEN_HEIGHT_PHYSICAL);
                GXZW_HEIGHT_PRCENT = prcent;
                GXZW_ICON_X = (int) (((float) SCREEN_WIDTH_PX) * GXZW_X_PRCENT);
                GXZW_ICON_Y = (int) (((float) SCREEN_HEIGHT_PX) * GXZW_Y_PRCENT);
                GXZW_ICON_WIDTH = (int) (((float) SCREEN_WIDTH_PX) * GXZW_WIDTH_PRCENT);
                GXZW_ICON_HEIGHT = (int) (((float) SCREEN_HEIGHT_PX) * prcent);
                int i4 = (int) (((float) SCREEN_WIDTH_PX) * GXZW_ANIM_WIDTH_PRCENT);
                GXZW_ANIM_WIDTH = i4;
                GXZW_ANIM_HEIGHT = i4;
                int caculateCutoutHeightIfNeed = caculateCutoutHeightIfNeed(context);
                int prcent2 = (int) (((float) GXZW_ICON_Y) * getPrcent(SCREEN_HEIGHT_PHYSICAL, SCREEN_HEIGHT_PHYSICAL - caculateCutoutHeightIfNeed));
                GXZW_ICON_Y = prcent2;
                GXZW_ICON_Y = prcent2 - caculateCutoutHeightIfNeed;
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

    public static float getPrcent(int i, int i2) {
        if (i2 == 0 || i == 0) {
            return 1.0f;
        }
        return new BigDecimal(i).divide(new BigDecimal(i2), 10, 5).floatValue();
    }

    public static void phySicalScreenPx(Context context) {
        Display display = ((DisplayManager) context.getSystemService("display")).getDisplay(0);
        SCREEN_WIDTH_PHYSICAL = display.getMode().getPhysicalWidth();
        SCREEN_HEIGHT_PHYSICAL = display.getMode().getPhysicalHeight();
    }

    private static void screenWhPx(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        boolean z = false;
        ((DisplayManager) context.getSystemService("display")).getDisplay(0).getRealMetrics(displayMetrics);
        if (context.getResources().getConfiguration().orientation == 1) {
            z = true;
        }
        SCREEN_WIDTH_PX = z ? displayMetrics.widthPixels : displayMetrics.heightPixels;
        SCREEN_HEIGHT_PX = z ? displayMetrics.heightPixels : displayMetrics.widthPixels;
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
        return Settings.Secure.getIntForUser(context.getContentResolver(), "gxzw_icon_aod_show_enable", 1, 0) == 1 && !MiuiKeyguardUtils.isInvertColorsEnable(context);
    }

    public static boolean isFodAodLowlightShowEnable(Context context) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), "gxzw_icon_aod_lowlight_show_enable", 0, 0) == 1;
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
                service.transact(i, obtain, null, 0);
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
            Object callStaticObjectMethod = ReflectUtil.callStaticObjectMethod(Class.forName("miui.util.ITouchFeature"), "getInstance", null, new Object[0]);
            if (callStaticObjectMethod != null) {
                return ((Integer) ReflectUtil.callObjectMethod(callStaticObjectMethod, "getSupportTouchFeatureVersion", null, new Object[0])).intValue();
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
            Object callStaticObjectMethod = ReflectUtil.callStaticObjectMethod(Class.forName("miui.util.ITouchFeature"), "getInstance", null, new Object[0]);
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
            Object callStaticObjectMethod = ReflectUtil.callStaticObjectMethod(Class.forName("miui.util.ITouchFeature"), "getInstance", null, new Object[0]);
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

    public static boolean supportHalo(Context context) {
        return context.getResources().getBoolean(C0010R$bool.config_enableFodCircleHalo);
    }

    public static int getHaloRes() {
        return C0013R$drawable.gxzw_white_halo_light;
    }

    public static float getHaloResCircleRadius(Context context) {
        return (float) context.getResources().getDimensionPixelOffset(C0012R$dimen.gxzw_halo_res_circle_radius);
    }

    public static int getHealthHaloRes() {
        return C0013R$drawable.gxzw_green_halo_light;
    }
}
