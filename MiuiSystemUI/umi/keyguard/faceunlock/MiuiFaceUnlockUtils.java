package com.android.keyguard.faceunlock;

import android.content.Context;
import android.hardware.miuiface.IMiuiFaceManager;
import android.hardware.miuiface.MiuiFaceFactory;
import android.miui.Shell;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import miui.os.Build;

public class MiuiFaceUnlockUtils {
    private static IMiuiFaceManager mFaceManager = null;
    protected static int mHelpStringResId;
    private static List<String> sDeviceSupportSlideCamera = new ArrayList();
    private static boolean sIsScreenTurnOnDelayed = false;
    private static List<String> sSCSlideNotOpenCameraList = new ArrayList();

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

    public static boolean isSupportSlideCamera(Context context) {
        if (sDeviceSupportSlideCamera.isEmpty()) {
            sDeviceSupportSlideCamera = Arrays.asList(context.getResources().getStringArray(R.array.device_support_slide_camera));
        }
        return sDeviceSupportSlideCamera.contains(Build.DEVICE);
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

    public static boolean isStLightFaceUnlockType(Context context) {
        return MiuiFaceFactory.getCurrentAuthType(context) == 2;
    }

    public static boolean isSupportScreenOnDelayed(Context context) {
        mFaceManager = MiuiFaceFactory.getFaceManager(context, 0);
        if (sDeviceSupportSlideCamera.isEmpty()) {
            sDeviceSupportSlideCamera = Arrays.asList(context.getResources().getStringArray(R.array.device_support_slide_camera));
        }
        if (!sDeviceSupportSlideCamera.contains(Build.DEVICE) || !mFaceManager.isSupportScreenOnDelayed()) {
            if (!mFaceManager.isSupportScreenOnDelayed() || KeyguardUpdateMonitor.getInstance(context).isAodUsingSuperWallpaper()) {
                return false;
            }
            return true;
        } else if (Settings.System.getIntForUser(context.getContentResolver(), "sc_status", 0, KeyguardUpdateMonitor.getCurrentUser()) == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void setScreenTurnOnDelayed(boolean z) {
        sIsScreenTurnOnDelayed = z;
        Shell.setRuntimeSharedValue("KEYGUARD_TURN_ON_DELAYED", z ? 1 : 0);
    }

    public static boolean isScreenTurnOnDelayed() {
        return sIsScreenTurnOnDelayed;
    }

    public static boolean isSCSlideNotOpenCamera(Context context) {
        if (sSCSlideNotOpenCameraList.isEmpty()) {
            sSCSlideNotOpenCameraList = Arrays.asList(context.getResources().getStringArray(R.array.lockscreen_sc_slide_not_open_camera));
        }
        String topActivityPkg = Util.getTopActivityPkg(context);
        return !TextUtils.isEmpty(topActivityPkg) && sSCSlideNotOpenCameraList.contains(topActivityPkg);
    }

    public static boolean isSlideCoverOpened(Context context) {
        return !isSupportSlideCamera(context) || Settings.System.getIntForUser(context.getContentResolver(), "sc_status", 0, KeyguardUpdateMonitor.getCurrentUser()) == 0;
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
        if (isStLightFaceUnlockType(context)) {
            updateStLightAuthenticationHelpInfo(i);
        } else {
            updateRgbAuthenticationHelpInfo(i);
        }
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
                            mHelpStringResId = R.string.face_unlock_check_failed;
                            return;
                        case 13:
                        case 14:
                            mHelpStringResId = R.string.face_unlock_check_failed;
                            return;
                        default:
                            switch (i) {
                                case 21:
                                    mHelpStringResId = R.string.face_unlock_reveal_eye;
                                    return;
                                case 22:
                                    mHelpStringResId = R.string.face_unlock_open_eye;
                                    return;
                                case 23:
                                    mHelpStringResId = R.string.face_unlock_reveal_mouth;
                                    return;
                                default:
                                    mHelpStringResId = R.string.face_unlock_check_failed;
                                    return;
                            }
                    }
                } else {
                    mHelpStringResId = R.string.face_unlock_not_found;
                    return;
                }
            }
            mHelpStringResId = R.string.face_unlock_be_on_the_screen;
            return;
        }
        mHelpStringResId = R.string.unlock_failed;
    }

    private static void updateStLightAuthenticationHelpInfo(int i) {
        if (i == 20) {
            mHelpStringResId = R.string.face_unlock_not_found;
        } else if (i == 70) {
            mHelpStringResId = R.string.face_unlock_check_failed;
        } else if (i == 32) {
            mHelpStringResId = R.string.structure_face_data_input_error_overly_light;
        } else if (i != 33) {
            switch (i) {
                case 22:
                case 23:
                    mHelpStringResId = R.string.face_unlock_reveal_eye;
                    return;
                case 24:
                    mHelpStringResId = R.string.face_unlock_reveal_mouth;
                    return;
                case 25:
                case 29:
                case 30:
                    mHelpStringResId = R.string.face_unlock_open_eye;
                    return;
                case 26:
                case 27:
                case 28:
                    mHelpStringResId = R.string.structure_face_data_input_error_expose_features;
                    return;
                default:
                    mHelpStringResId = R.string.face_unlock_check_failed;
                    return;
            }
        } else {
            mHelpStringResId = R.string.structure_face_data_input_error_away_screen;
        }
    }
}
