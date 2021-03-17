package com.android.systemui.controlcenter.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.util.Utils;
import miuix.animation.Folme;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;

public class ControlCenterUtils extends Utils {
    public static void updateFsgState(Context context, String str, boolean z) {
        Intent intent = new Intent();
        intent.setAction("com.android.systemui.fsgesture");
        intent.putExtra("typeFrom", str);
        intent.putExtra("isEnter", z);
        intent.addFlags(67108864);
        context.sendBroadcast(intent);
    }

    public static float afterFriction(float f, float f2) {
        float min = Math.min(f / f2, 1.0f);
        float f3 = min * min;
        return ((((f3 * min) / 3.0f) - f3) + min) * f2;
    }

    public static float getTranslationY(int i, int i2, float f, float f2) {
        float f3 = 1.0f - (((float) i) / ((float) (i2 - 1)));
        return Math.max(0.0f, Math.max(0.0f, (afterFriction(f - 0.0f, f2) * (((1.0f - (f3 * f3)) * 0.15f) + 0.5f)) + 0.0f) - 0.0f);
    }

    public static void createButtonFolmeTouchStyle(View view) {
        ITouchStyle iTouchStyle = Folme.useAt(view).touch();
        iTouchStyle.setScale(1.0f, ITouchStyle.TouchType.DOWN);
        iTouchStyle.setScale(1.0f, ITouchStyle.TouchType.UP);
        iTouchStyle.handleTouchOf(view, new AnimConfig());
    }

    public static void createCardFolmeTouchStyle(View view) {
        Folme.useAt(view).touch().handleTouchOf(view, new AnimConfig());
    }

    public static void createIconFolmeTouchStyle(View view) {
        ITouchStyle iTouchStyle = Folme.useAt(view).touch();
        iTouchStyle.setAlpha(0.6f, ITouchStyle.TouchType.DOWN);
        iTouchStyle.setAlpha(1.0f, ITouchStyle.TouchType.UP);
        iTouchStyle.setScale(1.0f, ITouchStyle.TouchType.DOWN);
        iTouchStyle.setScale(1.0f, ITouchStyle.TouchType.UP);
        iTouchStyle.handleTouchOf(view, new AnimConfig());
    }

    public static String getCalendarPkg(Context context) {
        if (!Constants.IS_INTERNATIONAL) {
            return "com.android.calendar";
        }
        if (isAppInstalledForUser(context, "com.xiaomi.calendar", KeyguardUpdateMonitor.getCurrentUser())) {
            return "com.xiaomi.calendar";
        }
        if (isAppInstalledForUser(context, "com.android.calendar", KeyguardUpdateMonitor.getCurrentUser())) {
            return "com.android.calendar";
        }
        return "com.google.android.calendar";
    }

    public static boolean isAppInstalledForUser(Context context, String str, int i) {
        try {
            context.getPackageManager().getPackageInfo(str, 1);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public static Drawable getSmoothRoundDrawable(Context context, int i) {
        if (context == null || i <= 0) {
            return null;
        }
        if (!(context.getDrawable(i) instanceof SmoothRoundDrawable)) {
            return context.getDrawable(i);
        }
        try {
            Resources resources = context.getResources();
            return Drawable.createFromXml(resources, resources.getLayout(i));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean filterNearby(String str) {
        return str.equals("custom(com.google.android.gms/.nearby.sharing.SharingTileService)") && !Constants.IS_INTERNATIONAL;
    }
}
