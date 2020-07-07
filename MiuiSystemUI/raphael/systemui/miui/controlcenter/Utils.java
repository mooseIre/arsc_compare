package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import miuix.animation.Folme;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;

public class Utils {
    public static float afterFriction(float f, float f2) {
        float min = Math.min(f / f2, 1.0f);
        float f3 = min * min;
        return ((((f3 * min) / 3.0f) - f3) + min) * f2;
    }

    public static float getTranslationY(int i, int i2, float f, float f2) {
        float f3 = 1.0f - (((float) i) / ((float) (i2 - 1)));
        return Math.max(0.0f, Math.max(0.0f, (afterFriction(f - 0.0f, f2) * (((1.0f - (f3 * f3)) * 0.15f) + 0.5f)) + 0.0f) - 0.0f);
    }

    public static float getTranslationY(float f, float f2) {
        if (f < 0.0f) {
            return afterFriction(-f, f2) * -1.0f;
        }
        return afterFriction(f, f2);
    }

    public static Point getScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        Display defaultDisplay = windowManager != null ? windowManager.getDefaultDisplay() : null;
        if (defaultDisplay == null) {
            return null;
        }
        Point point = new Point();
        defaultDisplay.getRealSize(point);
        return point;
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
}
