package com.android.keyguard;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.app.WallpaperColors;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextCompat;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.security.FingerprintIdUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.TouchDelegate;
import android.view.View;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.Constants;
import com.android.systemui.SystemUI;
import com.android.systemui.Util;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.miui.DrawableUtils;
import com.android.systemui.plugins.R;
import com.xiaomi.stat.MiStat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import miui.content.res.ThemeResources;
import miui.util.FeatureParser;

public class MiuiKeyguardUtils {
    public static final String AOD_MODE = (Build.VERSION.SDK_INT >= 28 ? "doze_always_on" : "aod_mode");
    private static final String CUSTOMIZED_REGION = SystemProperties.get("ro.miui.customized.region", "");
    private static final boolean FINGERPRINT_SIDE_CAP = SystemProperties.getBoolean("ro.hardware.fp.sideCap", false);
    public static final boolean IS_MTK_BUILD = "mediatek".equals(FeatureParser.getString("vendor"));
    public static final boolean IS_OPERATOR_CUSTOMIZATION_TEST = (miui.os.Build.IS_CM_CUSTOMIZATION_TEST || miui.os.Build.IS_CT_CUSTOMIZATION_TEST);
    private static final int PROCESS_USER_ID = Process.myUid();
    private static List<String> sDeviceSupportPickupByMTK = new ArrayList();
    private static FingerprintHelper sFingerprintHelper = null;
    private static boolean sHasNavigationBar;
    private static boolean sHasSetAuth;
    private static boolean sIsEllipticProximity;
    private static boolean sIsUserUnlocked = false;
    private static List<String> sKeepScreenOnWhenLargeAreaTouchList = new ArrayList();
    private static List<String> sRegionSupportMiHomeList = new ArrayList();
    private static IWindowManager sWindowManager;

    public static boolean isWeakenAimationEnable(Context context) {
        return false;
    }

    static {
        boolean z = true;
        if (!SystemProperties.getBoolean("ro.vendor.audio.us.proximity", false) && !SystemProperties.getBoolean("ro.audio.us.proximity", false)) {
            z = false;
        }
        sIsEllipticProximity = z;
    }

    public static boolean isDefaultLockScreenTheme() {
        return !ThemeResources.getSystem().containsAwesomeLockscreenEntry("manifest.xml");
    }

    public static boolean isIndianRegion(Context context) {
        return "IN".equals(KeyguardUpdateMonitor.getInstance(context).getCurrentRegion()) && miui.os.Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean isFingerprintHardwareAvailable(Context context) {
        if (sFingerprintHelper == null) {
            sFingerprintHelper = new FingerprintHelper(context);
        }
        return sFingerprintHelper.isHardwareDetected();
    }

    public static boolean uriHasUserId(Uri uri) {
        if (uri == null) {
            return false;
        }
        return !TextUtils.isEmpty(uri.getUserInfo());
    }

    public static Uri maybeAddUserId(Uri uri, int i) {
        if (uri == null) {
            return null;
        }
        if (i == -2 || !MiStat.Param.CONTENT.equals(uri.getScheme()) || uriHasUserId(uri)) {
            return uri;
        }
        Uri.Builder buildUpon = uri.buildUpon();
        buildUpon.encodedAuthority("" + i + "@" + uri.getEncodedAuthority());
        return buildUpon.build();
    }

    public static boolean hasNavigationBar(Context context) {
        if (sWindowManager == null) {
            sWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            try {
                sHasNavigationBar = IWindowManagerCompat.hasNavigationBar(sWindowManager, ContextCompat.getDisplayId(context));
            } catch (Exception unused) {
                Log.e("miui_keyguard", "no window manager to get navigation bar information");
                sWindowManager = null;
            }
        }
        return sHasNavigationBar;
    }

    public static int getFastBlurColor(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return -1;
        }
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            while (width > 1) {
                width /= 2;
                if (width < 1) {
                    width = 1;
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }
            while (height > 1) {
                height /= 2;
                if (height < 1) {
                    height = 1;
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }
            return bitmap.getPixel(0, 0);
        } catch (Exception e) {
            Log.e("miui_keyguard", "getFastBlurColor", e);
            return -1;
        } finally {
            bitmap.recycle();
        }
    }

    public static int getFastBlurColor(Context context, Drawable drawable) {
        return getFastBlurColor(context, DrawableUtils.drawable2Bitmap(drawable));
    }

    public static int addTwoColor(int i, int i2) {
        float alpha = ((float) Color.alpha(i)) / 255.0f;
        float alpha2 = ((float) Color.alpha(i2)) / 255.0f;
        float f = (alpha + alpha2) - (alpha * alpha2);
        float f2 = 1.0f - alpha2;
        return Color.argb((int) (255.0f * f), (int) ((((((float) Color.red(i)) * alpha) * f2) + (((float) Color.red(i2)) * alpha2)) / f), (int) ((((((float) Color.green(i)) * alpha) * f2) + (((float) Color.green(i2)) * alpha2)) / f), (int) ((((((float) Color.blue(i)) * alpha) * f2) + (((float) Color.blue(i2)) * alpha2)) / f));
    }

    public static boolean isSupportPickupByMTK(Context context) {
        if (sDeviceSupportPickupByMTK.isEmpty()) {
            sDeviceSupportPickupByMTK = Arrays.asList(context.getResources().getStringArray(R.array.device_support_pickup_by_MTK));
        }
        return sDeviceSupportPickupByMTK.contains(miui.os.Build.DEVICE);
    }

    public static boolean isPsensorDisabled(Context context) {
        return ((SensorManager) context.getSystemService("sensor")).getDefaultSensor(8) == null || sIsEllipticProximity;
    }

    public static boolean isNonUI() {
        return SystemProperties.getBoolean("sys.power.nonui", false);
    }

    public static boolean isSupportVerticalClock(int i, Context context) {
        return (i == 0 && context.getResources().getBoolean(R.bool.keyguard_show_vertical_time)) || i == 3;
    }

    public static int getDefaultKeyguardClockPosition(Context context) {
        return context.getResources().getInteger(R.integer.default_keyguard_clock_position);
    }

    public static boolean isPad() {
        return FeatureParser.getBoolean("is_pad", false);
    }

    public static boolean isGxzwSensor() {
        return MiuiGxzwManager.isGxzwSensor();
    }

    public static boolean isBroadSideFingerprint() {
        return FINGERPRINT_SIDE_CAP;
    }

    public static boolean isTopActivitySystemApp(Context context) {
        String topActivityPkg = Util.getTopActivityPkg(context);
        if (TextUtils.isEmpty(topActivityPkg)) {
            return false;
        }
        if ("com.miui.tsmclient".equalsIgnoreCase(topActivityPkg) || "com.miui.nextpay.global.app".equalsIgnoreCase(topActivityPkg)) {
            return true;
        }
        try {
            if ((context.getPackageManager().getPackageInfo(topActivityPkg, 0).applicationInfo.flags & 1) > 0) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isTopActivityCameraApp(Context context) {
        return PackageUtils.PACKAGE_NAME_CAMERA.equalsIgnoreCase(Util.getTopActivityPkg(context));
    }

    public static boolean isTopActivityLockScreenMagazine(Context context) {
        return PackageUtils.PACKAGE_NAME_LOCK_SCREEN_MAGAZINE.equalsIgnoreCase(Util.getTopActivityPkg(context));
    }

    public static boolean isTopActivityRemoteController(Context context) {
        return "com.duokan.phone.remotecontroller".equalsIgnoreCase(Util.getTopActivityPkg(context));
    }

    public static boolean isFullScreenGestureOpened(Context context) {
        return MiuiSettings.Global.getBoolean(context.getContentResolver(), "force_fsg_nav_bar");
    }

    public static String getCameraImageName(Context context, boolean z) {
        if (!hasNavigationBar(context)) {
            return "camera_preview";
        }
        return (!Constants.IS_NOTCH || !z) ? "camera_preview_nvirtualkey" : "camera_preview_notch_nvirtualkey";
    }

    public static void setUserAuthenticatedSinceBoot() {
        if (!sHasSetAuth) {
            SystemProperties.set("sys.miui.user_authenticated", "true");
            sHasSetAuth = true;
        }
    }

    public static boolean isNightMode(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService("uimode");
        return uiModeManager != null && uiModeManager.getNightMode() == 2;
    }

    public static boolean isGreenKidActive(Context context) {
        return MiuiSettings.Secure.isGreenKidActive(context.getContentResolver());
    }

    public static boolean isSuperPowerActive(Context context) {
        return MiuiSettings.System.isSuperSaveModeOpen(context, 0);
    }

    public static boolean canSwitchUser(Context context, int i) {
        if (i != 0 && !KeyguardUpdateMonitor.getInstance(context).getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot()) {
            return false;
        }
        if (isSuperPowerActive(context)) {
            Log.d("MiuiKeyguardUtils", "Can't switch user when super power active.");
            return false;
        } else if (isGreenKidActive(context)) {
            Log.d("MiuiKeyguardUtils", "Can't switch user when green kid active.");
            return false;
        } else if (!PhoneUtils.isInCall(context)) {
            return true;
        } else {
            Log.d("MiuiKeyguardUtils", "Can't switch user when phone calling.");
            return false;
        }
    }

    public static boolean isAodEnable(Context context) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), AOD_MODE, 0, -2) != 0;
    }

    public static boolean isAodUsingSuperWallpaperStyle(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "aod_using_super_wallpaper", 0) == 1;
    }

    public static boolean isInvertColorsEnable(Context context) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), "accessibility_display_inversion_enabled", 0, -2) != 0;
    }

    public static int getAuthUserId(Context context, int i) {
        HashMap userFingerprintIds;
        int intForUser = Settings.Secure.getIntForUser(context.getContentResolver(), "second_user_id", -10000, 0);
        if (intForUser == -10000 || (userFingerprintIds = FingerprintIdUtils.getUserFingerprintIds(context, intForUser)) == null || userFingerprintIds.size() == 0 || !userFingerprintIds.containsKey(String.valueOf(i))) {
            return 0;
        }
        return ((Integer) userFingerprintIds.get(String.valueOf(i))).intValue();
    }

    public static boolean keepScreenOnWhenLargeAreaTouch(Context context) {
        if (sKeepScreenOnWhenLargeAreaTouchList.isEmpty()) {
            sKeepScreenOnWhenLargeAreaTouchList = Arrays.asList(context.getResources().getStringArray(R.array.keep_screen_on_when_large_area_touch));
        }
        return isTopActivityPkgInList(context, sKeepScreenOnWhenLargeAreaTouchList);
    }

    public static boolean isTopActivityPkgInList(Context context, List<String> list) {
        String topActivityPkg = Util.getTopActivityPkg(context);
        return !TextUtils.isEmpty(topActivityPkg) && list.contains(topActivityPkg);
    }

    public static boolean isSupportAodAnimateDevice() {
        return Constants.SUPPORT_AOD;
    }

    public static int getKeyguardNotificationStatus(ContentResolver contentResolver) {
        boolean z = false;
        int i = 1;
        if (Settings.Global.getInt(contentResolver, "new_device_after_support_notification_animation", 0) != 0) {
            z = true;
        }
        if ("perseus".equals(miui.os.Build.DEVICE) || (z && isSupportAodAnimateDevice())) {
            i = 2;
        }
        return Settings.System.getIntForUser(contentResolver, "wakeup_for_keyguard_notification", i, KeyguardUpdateMonitor.getCurrentUser());
    }

    public static boolean isWakeupForNotification(ContentResolver contentResolver) {
        return getKeyguardNotificationStatus(contentResolver) == 1;
    }

    public static boolean showMXTelcelLockScreen(Context context) {
        return "mx_telcel".equals(CUSTOMIZED_REGION) && isAppRunning(context, "com.celltick.lockscreen");
    }

    public static boolean isAppRunning(Context context, String str) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses;
        if (!(TextUtils.isEmpty(str) || (runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) == null || runningAppProcesses.size() == 0)) {
            for (int i = 0; i < runningAppProcesses.size(); i++) {
                ActivityManager.RunningAppProcessInfo runningAppProcessInfo = runningAppProcesses.get(i);
                if (str.equals(runningAppProcessInfo.processName)) {
                    return true;
                }
                String[] strArr = runningAppProcessInfo.pkgList;
                if (strArr != null) {
                    for (String equals : strArr) {
                        if (str.equals(equals)) {
                            return true;
                        }
                    }
                    continue;
                }
            }
        }
        return false;
    }

    public static void setViewTouchDelegate(final View view, final int i) {
        if (view != null) {
            final View view2 = (View) view.getParent();
            view2.post(new Runnable() {
                public void run() {
                    TouchDelegate touchDelegate;
                    if (i != 0) {
                        Rect rect = new Rect();
                        view.getHitRect(rect);
                        int i = rect.top;
                        int i2 = i;
                        rect.top = i - i2;
                        rect.bottom += i2;
                        rect.left -= i2;
                        rect.right += i2;
                        touchDelegate = new TouchDelegate(rect, view);
                    } else {
                        touchDelegate = null;
                    }
                    view2.setTouchDelegate(touchDelegate);
                }
            });
        }
    }

    public static boolean isRegionSupportMiHome(Context context) {
        if (sRegionSupportMiHomeList.isEmpty()) {
            sRegionSupportMiHomeList = Arrays.asList(context.getResources().getStringArray(R.array.region_support_mihome));
        }
        return sRegionSupportMiHomeList.contains(miui.os.Build.getRegion());
    }

    public static int getBitmapColorMode(Bitmap bitmap) {
        return (WallpaperColors.fromBitmap(bitmap).getColorHints() & 1) == 1 ? 2 : 0;
    }

    public static boolean isDeviceProvisionedInSettingsDb(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0;
    }

    public static boolean isSupportGestureWakeup() {
        return FeatureParser.getBoolean("support_gesture_wakeup", false);
    }

    public static boolean isSystemProcess() {
        return PROCESS_USER_ID == 1000;
    }

    public static boolean isUserUnlocked(Context context) {
        if (!sIsUserUnlocked) {
            sIsUserUnlocked = UserManagerCompat.isUserUnlocked((UserManager) context.getSystemService(UserManager.class));
        }
        return sIsUserUnlocked;
    }

    public static void userActivity(Context context) {
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) SystemUI.getComponent(context, KeyguardViewMediator.class);
        if (keyguardViewMediator != null) {
            keyguardViewMediator.userActivity();
        }
    }

    public static String getMiuiVersionName() {
        return SystemProperties.get("ro.miui.ui.version.name");
    }

    public static boolean notifyThemeSetSuperWallpaper(Context context) {
        try {
            boolean z = ContentProviderUtils.getResultFromProvider(context, Uri.parse("content://com.android.thememanager.otaupdate.provider"), "getCurrentThemeState", (String) null, (Bundle) null).getBoolean("key_is_set_super_wallpaper");
            Slog.i("MiuiKeyguardUtils", "notifyThemeSetSuperWallpaper needSetSuperWallpaper = " + z);
            return z;
        } catch (Exception e) {
            Log.e("MiuiKeyguardUtils", "Failed to isThemeChange", e);
            return false;
        }
    }
}
