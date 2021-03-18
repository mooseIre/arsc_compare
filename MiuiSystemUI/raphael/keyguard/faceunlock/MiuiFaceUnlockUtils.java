package com.android.keyguard.faceunlock;

import android.content.Context;
import android.hardware.miuiface.BaseMiuiFaceManager;
import android.miui.Shell;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import java.lang.ref.WeakReference;
import miui.util.FeatureParser;

public class MiuiFaceUnlockUtils {
    protected static int mHelpStringResId = 0;
    private static WeakReference<BaseMiuiFaceManager> mWeakReferenceFaceManager = null;
    private static boolean sIsScreenTurnOnDelayed = false;

    private static void getFaceManager(Context context) {
        WeakReference<BaseMiuiFaceManager> weakReference = mWeakReferenceFaceManager;
        if (weakReference == null || weakReference.get() == null) {
            mWeakReferenceFaceManager = new WeakReference<>((BaseMiuiFaceManager) context.getSystemService("miui_face"));
        }
    }

    public static boolean isHardwareDetected(Context context) {
        getFaceManager(context);
        return mWeakReferenceFaceManager.get().isHardwareDetected();
    }

    public static boolean isFaceFeatureEnabled(Context context) {
        getFaceManager(context);
        return mWeakReferenceFaceManager.get().isFaceFeatureEnabled();
    }

    public static boolean hasEnrolledTemplates(Context context) {
        getFaceManager(context);
        return mWeakReferenceFaceManager.get().hasEnrolledTemplates();
    }

    public static boolean isSupportLiftingCamera(Context context) {
        try {
            Class<?> cls = Class.forName("miui.os.DeviceFeature");
            return ((Boolean) cls.getDeclaredMethod("hasPopupCameraSupport", null).invoke(cls, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.e("miui_face", "reflect error when get hasPopupCameraSupport state", e);
            return false;
        }
    }

    public static boolean isSupportScreenOnDelayed(Context context) {
        getFaceManager(context);
        return mWeakReferenceFaceManager.get().isSupportScreenOnDelayed() && !((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).isAodUsingSuperWallpaper();
    }

    public static boolean isSupportTeeFaceunlock() {
        return FeatureParser.getBoolean("support_tee_face_unlock", false);
    }

    public static void setScreenTurnOnDelayed(boolean z) {
        sIsScreenTurnOnDelayed = z;
        Shell.setRuntimeSharedValue("KEYGUARD_TURN_ON_DELAYED", z ? 1 : 0);
    }

    public static boolean isScreenTurnOnDelayed() {
        return sIsScreenTurnOnDelayed;
    }

    public static void resetFaceUnlockSettingValues(Context context) {
        if (!hasEnrolledTemplates(context)) {
            Settings.Secure.putIntForUser(context.getContentResolver(), "face_unlcok_apply_for_lock", 0, UserHandle.myUserId());
            Settings.Secure.putIntForUser(context.getContentResolver(), "face_unlock_success_stay_screen", 0, UserHandle.myUserId());
            Settings.Secure.putIntForUser(context.getContentResolver(), "face_unlock_success_show_message", 0, UserHandle.myUserId());
            Settings.Secure.putIntForUser(context.getContentResolver(), "face_unlock_by_notification_screen_on", 0, UserHandle.myUserId());
        }
    }

    public static String getFaceHelpInfo(Context context, int i) {
        updateRgbAuthenticationHelpInfo(i);
        return context.getResources().getString(mHelpStringResId);
    }

    private static void updateRgbAuthenticationHelpInfo(int i) {
        if (i == 0) {
            mHelpStringResId = C0021R$string.face_unlock_success;
        } else if (i != 3) {
            if (i != 4) {
                if (i != 5) {
                    switch (i) {
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                            break;
                        case 12:
                            mHelpStringResId = C0021R$string.face_unlock_check_failed;
                            return;
                        case 13:
                        case 14:
                            mHelpStringResId = C0021R$string.face_unlock_check_failed;
                            return;
                        default:
                            switch (i) {
                                case 21:
                                    mHelpStringResId = C0021R$string.face_unlock_reveal_eye;
                                    return;
                                case 22:
                                    mHelpStringResId = C0021R$string.face_unlock_open_eye;
                                    return;
                                case 23:
                                    mHelpStringResId = C0021R$string.face_unlock_reveal_mouth;
                                    return;
                                default:
                                    mHelpStringResId = C0021R$string.face_unlock_check_failed;
                                    return;
                            }
                    }
                } else {
                    mHelpStringResId = C0021R$string.face_unlock_not_found;
                    return;
                }
            }
            mHelpStringResId = C0021R$string.face_unlock_be_on_the_screen;
        } else {
            mHelpStringResId = C0021R$string.unlock_failed;
        }
    }
}
