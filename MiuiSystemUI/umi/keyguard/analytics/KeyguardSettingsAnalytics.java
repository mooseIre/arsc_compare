package com.android.keyguard.analytics;

import android.content.Context;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.security.MiuiLockPatternUtils;
import android.text.TextUtils;
import com.android.keyguard.FingerprintHelper;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.systemui.statusbar.phone.UnlockMethodCache;
import com.xiaomi.stat.MiStatParams;
import java.util.Calendar;
import miui.os.Build;
import miui.util.FeatureParser;

public class KeyguardSettingsAnalytics {
    private static final boolean SUPPORT_STAR_ANIMATION = "cepheus".equals(Build.DEVICE);
    private static int sDayOfStatistics = -1;
    public static boolean sFrontFingerprintSensor = FeatureParser.getBoolean("front_fingerprint_sensor", false);
    public static boolean sSupportHallSensor = FeatureParser.getBoolean("support_hall_sensor", false);
    private static int sYearOfStatistics = -1;

    private static MiStatParams getKeyguardSettingState(Context context, String str, int i, int i2, String str2, int i3, int i4, int i5, long j, int i6, int i7, int i8, int i9, String str3, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        MiStatParams miStatParams = new MiStatParams();
        String str4 = str;
        miStatParams.putString("secure_type", str);
        if (MiuiKeyguardUtils.isFingerprintHardwareAvailable(context)) {
            int i10 = i;
            miStatParams.putInt("fingerprint_num", i);
            miStatParams.putString("fingerprint_type", getFingerPrintType());
        }
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            int i11 = i2;
            miStatParams.putInt("gxzw_anim", i2);
        }
        if (MiuiFaceUnlockUtils.isSupportFaceUnlock(context)) {
            String str5 = str2;
            miStatParams.putString("face_unlock_state", str2);
            if (MiuiFaceUnlockUtils.hasEnrolledFaces(context) && FaceUnlockManager.getInstance().isFaceUnlockApplyForKeyguard()) {
                int i12 = i4;
                miStatParams.putInt("face_unlock_success_stay_screen", i4);
                int i13 = i5;
                miStatParams.putInt("face_unlock_success_show_message", i5);
                int i14 = i3;
                miStatParams.putInt("face_unlock_notification_toggle", i3);
            }
        }
        long j2 = j;
        miStatParams.putLong("screen_off_time", j);
        int i15 = i6;
        miStatParams.putInt("screen_on_by_notification_toggle", i6);
        int i16 = i7;
        miStatParams.putInt("screen_on_by_volume_toggle", i7);
        miStatParams.putInt("quick_camera_toggle", i8);
        miStatParams.putInt("lunar_calendar_toggle", i9);
        miStatParams.putString("keyguard_notification_state", str3);
        AnalyticsHelper.booleanToInt(z);
        miStatParams.putInt("owner_info_toggle", z ? 1 : 0);
        AnalyticsHelper.booleanToInt(z2);
        miStatParams.putInt("blue_unlock_state", z2 ? 1 : 0);
        if (sSupportHallSensor) {
            AnalyticsHelper.booleanToInt(z3);
            miStatParams.putInt("smart_cover_unlock_toggle", z3 ? 1 : 0);
        }
        AnalyticsHelper.booleanToInt(z4);
        miStatParams.putInt("pickup_wakeup_toggle", z4 ? 1 : 0);
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            AnalyticsHelper.booleanToInt(z5);
            miStatParams.putInt("fod_quick_open_toggle", z5 ? 1 : 0);
        }
        return miStatParams;
    }

    public static boolean isCurrentDay() {
        Calendar instance = Calendar.getInstance();
        if (sYearOfStatistics == instance.get(1) && sDayOfStatistics == instance.get(6)) {
            return true;
        }
        sYearOfStatistics = instance.get(1);
        sDayOfStatistics = instance.get(6);
        return false;
    }

    public static MiStatParams getKeyguardSettingsStatParams(Context context) {
        int i;
        int i2;
        int i3;
        Context context2 = context;
        MiuiLockPatternUtils miuiLockPatternUtils = new MiuiLockPatternUtils(context2);
        boolean z = UnlockMethodCache.getInstance(context).isMethodSecure() || KeyguardUpdateMonitor.getInstance(context).isSimPinSecure();
        String str = "unsecure";
        if (z) {
            int activePasswordQuality = miuiLockPatternUtils.getActivePasswordQuality(KeyguardUpdateMonitor.getCurrentUser());
            if (activePasswordQuality == 65536) {
                str = "pattern";
            } else if (activePasswordQuality == 131072 || activePasswordQuality == 196608) {
                str = "numeric";
            } else if (activePasswordQuality == 262144 || activePasswordQuality == 327680 || activePasswordQuality == 393216) {
                str = "mixed";
            }
        }
        int size = (!MiuiKeyguardUtils.isFingerprintHardwareAvailable(context) || !z) ? 0 : new FingerprintHelper(context2).getFingerprintIds().size();
        int intForUser = MiuiKeyguardUtils.isGxzwSensor() ? Settings.System.getIntForUser(context.getContentResolver(), "fod_animation_type", SUPPORT_STAR_ANIMATION ^ true ? 1 : 0, 0) : -1;
        String str2 = "no_data";
        if (MiuiFaceUnlockUtils.isSupportFaceUnlock(context)) {
            if (MiuiFaceUnlockUtils.hasEnrolledFaces(context) && FaceUnlockManager.getInstance().isFaceUnlockApplyForKeyguard()) {
                str2 = "enabled";
            } else if (MiuiFaceUnlockUtils.hasEnrolledFaces(context)) {
                str2 = "disabled";
            }
            i3 = Settings.Secure.getIntForUser(context.getContentResolver(), "face_unlock_success_stay_screen", 0, KeyguardUpdateMonitor.getCurrentUser());
            i2 = Settings.Secure.getIntForUser(context.getContentResolver(), "face_unlock_success_show_message", 0, KeyguardUpdateMonitor.getCurrentUser());
            i = Settings.Secure.getIntForUser(context.getContentResolver(), "face_unlock_by_notification_screen_on", 0, KeyguardUpdateMonitor.getCurrentUser());
        } else {
            i3 = 0;
            i2 = 0;
            i = 0;
        }
        return getKeyguardSettingState(context, str, size, intForUser, str2, i3, i2, i, Settings.System.getLongForUser(context.getContentResolver(), "screen_off_timeout", 30000, KeyguardUpdateMonitor.getCurrentUser()), MiuiKeyguardUtils.getKeyguardNotificationStatus(context.getContentResolver()), Settings.System.getIntForUser(context.getContentResolver(), "volumekey_wake_screen", 0, KeyguardUpdateMonitor.getCurrentUser()), Settings.System.getIntForUser(context.getContentResolver(), "volumekey_launch_camera", 0, KeyguardUpdateMonitor.getCurrentUser()), Settings.System.getIntForUser(context.getContentResolver(), "show_lunar_calendar", 0, KeyguardUpdateMonitor.getCurrentUser()), Settings.Secure.getIntForUser(context.getContentResolver(), "lock_screen_show_notifications", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0 ? Settings.Secure.getIntForUser(context.getContentResolver(), "lock_screen_allow_private_notifications", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0 ? "show_content" : "hide_content" : "hide_notification", miuiLockPatternUtils.isOwnerInfoEnabled(KeyguardUpdateMonitor.getCurrentUser()) && !TextUtils.isEmpty(miuiLockPatternUtils.getOwnerInfo(UserHandle.myUserId())), new MiuiLockPatternUtils(context2).getBluetoothUnlockEnabled(), SystemProperties.getInt("persist.sys.smartcover_mode", -1) != 0, MiuiSettings.System.getBooleanForUser(context.getContentResolver(), "pick_up_gesture_wakeup_mode", false, KeyguardUpdateMonitor.getCurrentUser()), MiuiGxzwManager.isQuickOpenEnable(context));
    }

    private static String getFingerPrintType() {
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            return "fod";
        }
        return sFrontFingerprintSensor ? "front" : "back";
    }
}
