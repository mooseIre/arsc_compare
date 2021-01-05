package com.android.systemui.util;

import android.view.View;
import miuix.animation.Folme;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;

public class ViewAnimUtils {
    public static void mouse(View view) {
        boolean isClickable = view.isClickable();
        createMouseAnim(view).handleTouchOf(view, new AnimConfig[0]);
        view.setClickable(isClickable);
    }

    public static ITouchStyle createMouseAnim(View view) {
        ITouchStyle iTouchStyle = Folme.useAt(view).touch();
        iTouchStyle.setTint(0.0f, 0.0f, 0.0f, 0.0f);
        return iTouchStyle;
    }
}
