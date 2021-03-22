package com.android.keyguard.utils;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.app.WallpaperColors;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.security.FingerprintIdUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Toast;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.util.CommonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import miui.content.res.ThemeResources;
import miui.util.FeatureParser;
import miui.util.HapticFeedbackUtil;

public class MiuiKeyguardUtils {
    public static final String AOD_MODE = (Build.VERSION.SDK_INT >= 28 ? "doze_always_on" : "aod_mode");
    private static final boolean FINGERPRINT_SIDE_CAP = SystemProperties.getBoolean("ro.hardware.fp.sideCap", false);
    public static final String HOME_LAUCNHER_PACKAGE_NAME = SystemProperties.get("ro.miui.product.home", "com.miui.home");
    public static final boolean IS_OPERATOR_CUSTOMIZATION_TEST = (miui.os.Build.IS_CM_CUSTOMIZATION_TEST || miui.os.Build.IS_CT_CUSTOMIZATION_TEST);
    private static final int PROCESS_USER_ID = Process.myUid();
    public static final boolean SUPPORT_LINEAR_MOTOR_VIBRATE = HapticFeedbackUtil.isSupportLinearMotorVibrate();
    private static boolean sExpandableUnderKeyguard;
    private static boolean sHasNavigationBar;
    private static List<String> sKeepScreenOnWhenLargeAreaTouchList = new ArrayList();
    private static boolean sOpenDoubleTapSleep;
    private static List<String> sRegionSupportMiHomeList = new ArrayList();
    private static IWindowManager sWindowManager;

    public static boolean isNonUI() {
        return SystemProperties.getBoolean("sys.power.nonui", false);
    }

    public static boolean isNightMode(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService("uimode");
        return uiModeManager != null && uiModeManager.getNightMode() == 2;
    }

    public static boolean isDefaultLockScreenTheme() {
        return !ThemeResources.getSystem().containsAwesomeLockscreenEntry("manifest.xml") && !ThemeResources.getSystem().containsSuperWallpaperLockscreenEntry("manifest.xml");
    }

    public static boolean isIndianRegion() {
        return "IN".equals(CommonUtil.getCurrentRegion()) && miui.os.Build.IS_INTERNATIONAL_BUILD;
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
        if (i == -2 || !"content".equals(uri.getScheme()) || uriHasUserId(uri)) {
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
                sHasNavigationBar = sWindowManager.hasNavigationBar(context.getDisplayId());
            } catch (Exception unused) {
                Log.e("miui_keyguard", "no window manager to get navigation bar information");
                sWindowManager = null;
            }
        }
        return sHasNavigationBar;
    }

    public static boolean isBlackGoldenTheme(Context context) {
        if (context == null) {
            return false;
        }
        return context.getResources().getBoolean(C0010R$bool.keyguard_show_vertical_time);
    }

    public static boolean isSupportVerticalClock(int i, Context context) {
        return (i == 0 && isBlackGoldenTheme(context)) || i == 3;
    }

    public static int getDefaultKeyguardClockPosition(Context context) {
        return context.getResources().getInteger(C0016R$integer.default_keyguard_clock_position);
    }

    public static boolean isPad() {
        return FeatureParser.getBoolean("is_pad", false);
    }

    public static boolean isGxzwSensor() {
        return MiuiGxzwManager.isGxzwSensor();
    }

    public static boolean isGlobalAndFingerprintEnable() {
        return miui.os.Build.IS_INTERNATIONAL_BUILD && ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
    }

    public static boolean isFullScreenGestureOpened() {
        return CommonUtil.isFullScreenGestureEnabled();
    }

    public static String getCameraImageName(Context context, boolean z) {
        if (!hasNavigationBar(context)) {
            return "camera_preview";
        }
        return (!DeviceConfig.IS_NOTCH || !z) ? "camera_preview_nvirtualkey" : "camera_preview_notch_nvirtualkey";
    }

    public static void setViewTouchDelegate(final View view, final int i) {
        if (view != null) {
            final View view2 = (View) view.getParent();
            view2.post(new Runnable() {
                /* class com.android.keyguard.utils.MiuiKeyguardUtils.AnonymousClass1 */

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
            sRegionSupportMiHomeList = Arrays.asList(context.getResources().getStringArray(C0008R$array.region_support_mihome));
        }
        return sRegionSupportMiHomeList.contains(miui.os.Build.getRegion());
    }

    public static boolean isDeviceProvisionedInSettingsDb(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) != 0;
    }

    public static boolean isSupportGestureWakeup() {
        return FeatureParser.getBoolean("support_gesture_wakeup", false);
    }

    public static int getBitmapColorMode(Bitmap bitmap) {
        return (WallpaperColors.fromBitmap(bitmap).getColorHints() & 1) == 1 ? 2 : 0;
    }

    public static boolean keepScreenOnWhenLargeAreaTouch(Context context) {
        if (sKeepScreenOnWhenLargeAreaTouchList.isEmpty()) {
            sKeepScreenOnWhenLargeAreaTouchList = Arrays.asList(context.getResources().getStringArray(C0008R$array.keep_screen_on_when_large_area_touch));
        }
        return isTopActivityPkgInList(context, sKeepScreenOnWhenLargeAreaTouchList);
    }

    public static boolean isTopActivityPkgInList(Context context, List<String> list) {
        String topActivityPkg = CommonUtil.getTopActivityPkg(context);
        return !TextUtils.isEmpty(topActivityPkg) && list.contains(topActivityPkg);
    }

    public static void setExpandableStatusbarUnderKeyguard(boolean z) {
        sExpandableUnderKeyguard = z;
    }

    public static boolean supportExpandableStatusbarUnderKeyguard() {
        return sExpandableUnderKeyguard;
    }

    public static void setContentObserverForGestureWakeup(boolean z) {
        sOpenDoubleTapSleep = z;
    }

    public static boolean supportDoubleTapSleep() {
        return sOpenDoubleTapSleep;
    }

    public static boolean isTopActivityMiPay(Context context) {
        String topActivityPkg = CommonUtil.getTopActivityPkg(context, false);
        if (TextUtils.isEmpty(topActivityPkg)) {
            return false;
        }
        if ("com.miui.tsmclient".equalsIgnoreCase(topActivityPkg) || "com.miui.nextpay.global.app".equalsIgnoreCase(topActivityPkg)) {
            return true;
        }
        return false;
    }

    public static boolean isTopActivityCameraApp(Context context) {
        return PackageUtils.PACKAGE_NAME_CAMERA.equalsIgnoreCase(CommonUtil.getTopActivityPkg(context, false));
    }

    public static boolean isBrowserSearchExist(Context context) {
        Intent intent = new Intent("com.android.browser.browser_search");
        intent.setPackage(isBrowserGlobalEnabled(context) ? "com.mi.globalbrowser" : "com.android.browser");
        return isIntentActivityExist(context, intent);
    }

    public static boolean isIntentActivityExist(Context context, Intent intent) {
        if (!(context == null || intent == null)) {
            try {
                List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(intent, 786432);
                if (queryIntentActivities == null || queryIntentActivities.size() <= 0) {
                    return false;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isBrowserGlobalEnabled(Context context) {
        if (!miui.os.Build.IS_INTERNATIONAL_BUILD || !PackageUtils.isAppInstalledForUser(context, "com.mi.globalbrowser", 0)) {
            return false;
        }
        return true;
    }

    public static boolean isBroadSideFingerprint() {
        return FINGERPRINT_SIDE_CAP;
    }

    public static boolean isAodEnable(Context context) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), AOD_MODE, 0, -2) != 0;
    }

    public static boolean isInvertColorsEnable(Context context) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), "accessibility_display_inversion_enabled", 0, -2) != 0;
    }

    public static boolean isSystemProcess() {
        return PROCESS_USER_ID == 1000;
    }

    public static int getAuthUserId(Context context, int i) {
        HashMap userFingerprintIds;
        int intForUser = Settings.Secure.getIntForUser(context.getContentResolver(), "second_user_id", -10000, 0);
        if (intForUser == -10000 || (userFingerprintIds = FingerprintIdUtils.getUserFingerprintIds(context, intForUser)) == null || userFingerprintIds.size() == 0 || !userFingerprintIds.containsKey(String.valueOf(i))) {
            return 0;
        }
        return ((Integer) userFingerprintIds.get(String.valueOf(i))).intValue();
    }

    public static boolean canSwitchUser(Context context, int i) {
        if (i != 0 && !((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot()) {
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

    public static boolean isGreenKidActive(Context context) {
        return MiuiSettings.Secure.isGreenKidActive(context.getContentResolver());
    }

    public static boolean isSuperPowerActive(Context context) {
        return MiuiSettings.System.isSuperSaveModeOpen(context, 0);
    }

    public static boolean isLargeScreen(Context context) {
        return miui.os.Build.DEVICE.equals("cetus") && (context.getResources().getConfiguration().screenLayout & 15) >= 3;
    }

    public static boolean isTopActivityLauncher(Context context) {
        try {
            List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
            if (runningTasks != null && !runningTasks.isEmpty()) {
                return TextUtils.equals(HOME_LAUCNHER_PACKAGE_NAME, runningTasks.get(0).topActivity.getPackageName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void handleBleUnlockSucceed(Context context) {
        Toast.makeText(context, C0021R$string.miui_keyguard_ble_unlock_succeed_msg, 0).show();
    }
}
