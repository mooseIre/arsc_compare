package com.android.keyguard.faceunlock;

import android.content.Context;
import android.hardware.miuiface.IMiuiFaceManager;
import android.hardware.miuiface.MiuiFaceFactory;
import android.miui.Shell;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.systemui.C0018R$string;
import com.android.systemui.Dependency;

public class MiuiFaceUnlockUtils {
    private static IMiuiFaceManager mFaceManager = null;
    protected static int mHelpStringResId = 0;
    private static boolean sIsScreenTurnOnDelayed = false;

    public static boolean isSupportFaceUnlock(Context context) {
        IMiuiFaceManager faceManager = MiuiFaceFactory.getFaceManager(context, 0);
        mFaceManager = faceManager;
        return faceManager.isFaceFeatureSupport();
    }

    public static boolean isFaceFeatureEnabled(Context context) {
        IMiuiFaceManager faceManager = MiuiFaceFactory.getFaceManager(context, 0);
        mFaceManager = faceManager;
        return faceManager.isFaceFeatureEnabled();
    }

    public static boolean hasEnrolledFaces(Context context) {
        IMiuiFaceManager faceManager = MiuiFaceFactory.getFaceManager(context, 0);
        mFaceManager = faceManager;
        if (faceManager.hasEnrolledFaces() > 0) {
            return true;
        }
        return false;
    }

    public static boolean isSupportLiftingCamera(Context context) {
        try {
            Class<?> cls = Class.forName("miui.os.DeviceFeature");
            return ((Boolean) cls.getDeclaredMethod("hasPopupCameraSupport", (Class[]) null).invoke(cls, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.e("miui_face", "reflect error when get hasPopupCameraSupport state", e);
            return false;
        }
    }

    public static boolean isSupportScreenOnDelayed(Context context) {
        IMiuiFaceManager faceManager = MiuiFaceFactory.getFaceManager(context, 0);
        mFaceManager = faceManager;
        if (!faceManager.isSupportScreenOnDelayed() || ((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).isAodUsingSuperWallpaper()) {
            return false;
        }
        return true;
    }

    public static void setScreenTurnOnDelayed(boolean z) {
        sIsScreenTurnOnDelayed = z;
        Shell.setRuntimeSharedValue("KEYGUARD_TURN_ON_DELAYED", z ? 1 : 0);
    }

    public static boolean isScreenTurnOnDelayed() {
        return sIsScreenTurnOnDelayed;
    }

    public static void resetFaceUnlockSettingValues(Context context) {
        if (!hasEnrolledFaces(context)) {
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
        if (i != 3) {
            if (i != 4) {
                if (i != 5) {
                    switch (i) {
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                            break;
                        case 12:
                            mHelpStringResId = C0018R$string.face_unlock_check_failed;
                            return;
                        case 13:
                        case 14:
                            mHelpStringResId = C0018R$string.face_unlock_check_failed;
                            return;
                        default:
                            switch (i) {
                                case 21:
                                    mHelpStringResId = C0018R$string.face_unlock_reveal_eye;
                                    return;
                                case 22:
                                    mHelpStringResId = C0018R$string.face_unlock_open_eye;
                                    return;
                                case 23:
                                    mHelpStringResId = C0018R$string.face_unlock_reveal_mouth;
                                    return;
                                default:
                                    mHelpStringResId = C0018R$string.face_unlock_check_failed;
                                    return;
                            }
                    }
                } else {
                    mHelpStringResId = C0018R$string.face_unlock_not_found;
                    return;
                }
            }
            mHelpStringResId = C0018R$string.face_unlock_be_on_the_screen;
            return;
        }
        mHelpStringResId = C0018R$string.unlock_failed;
    }
}
