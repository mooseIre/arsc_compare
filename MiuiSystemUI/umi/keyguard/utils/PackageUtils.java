package com.android.keyguard.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import miui.os.Build;

public class PackageUtils {
    public static final String CLASS_NAME_CAMERA;
    public static final boolean IS_VELA_CAMERA;
    public static final String PACKAGE_NAME_CAMERA = ("vela".equals(Build.DEVICE) ? "com.mlab.cam" : "com.android.camera");

    static {
        boolean equals = "vela".equals(Build.DEVICE);
        IS_VELA_CAMERA = equals;
        CLASS_NAME_CAMERA = equals ? "com.mtlab.camera.CameraActivity" : "com.android.camera.Camera";
    }

    public static boolean isAppInstalledForUser(Context context, String str, int i) {
        try {
            context.getPackageManager().getPackageInfoAsUser(str, 1, i);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            Log.e("miui_keyguard", "name not found pkg=" + str);
            return false;
        }
    }

    public static Drawable getDrawableFromPackage(Context context, String str, String str2) {
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(str);
            return resourcesForApplication.getDrawable(resourcesForApplication.getIdentifier(str2, "drawable", str));
        } catch (Exception unused) {
            Log.e("miui_keyguard", "something wrong when get image from" + str);
            return null;
        }
    }

    public static ResolveInfo resolveIntent(Context context, Intent intent) {
        return resolveIntent(context, intent, 0);
    }

    public static ResolveInfo resolveIntent(Context context, Intent intent, int i) {
        if (intent == null) {
            return null;
        }
        try {
            return context.getPackageManager().resolveActivityAsUser(intent, i, KeyguardUpdateMonitor.getCurrentUser());
        } catch (Exception e) {
            Log.e("PackageUtils", "resolveIntent exception" + e.getMessage());
            return null;
        }
    }

    public static Intent getTSMClientIntent() {
        Intent intent = new Intent();
        intent.setAction("com.miui.intent.action.DOUBLE_CLICK");
        intent.putExtra("event_source", "shortcut_of_all_cards");
        intent.addFlags(268435456);
        return intent;
    }

    public static Intent getSmartHomeMainIntent() {
        Intent intent = new Intent();
        intent.setPackage("com.xiaomi.smarthome");
        intent.setData(Uri.parse("http://home.mi.com/main"));
        intent.putExtra("source", 11);
        intent.setAction("android.intent.action.VIEW");
        intent.addFlags(268435456);
        return intent;
    }

    public static Intent getMarketDownloadIntent(String str) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("market://details?id=" + str + "&back=true&ref=keyguard"));
        intent.setAction("android.intent.action.VIEW");
        intent.addFlags(268435456);
        return intent;
    }

    public static Intent getCameraIntent() {
        Intent intent = new Intent();
        intent.setFlags(276856832);
        intent.putExtra("ShowCameraWhenLocked", true);
        intent.putExtra("StartActivityWhenLocked", true);
        intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
        intent.setComponent(new ComponentName(PACKAGE_NAME_CAMERA, CLASS_NAME_CAMERA));
        return intent;
    }

    public static boolean supportTSMClient(Context context) {
        try {
            if (!isAppInstalledForUser(context, "com.miui.tsmclient", KeyguardUpdateMonitor.getCurrentUser()) || context.getPackageManager().getPackageInfo("com.miui.tsmclient", 0).versionCode < 18) {
                return false;
            }
            return true;
        } catch (Exception unused) {
            Log.e("PackageUtils", "cannot find TSMClient Package");
            return false;
        }
    }
}
