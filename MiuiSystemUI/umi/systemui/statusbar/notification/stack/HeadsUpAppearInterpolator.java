package com.android.systemui.statusbar.notification.stack;

import android.graphics.Path;
import android.view.animation.PathInterpolator;

public class HeadsUpAppearInterpolator extends PathInterpolator {
    private static float X1 = 250.0f;
    private static float X2 = 200.0f;
    private static float XTOT = (250.0f + 200.0f);

    public HeadsUpAppearInterpolator() {
        super(getAppearPath());
    }

    private static Path getAppearPath() {
        float f = XTOT;
        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        float f2 = X1;
        path.cubicTo((f2 * 0.8f) / f, 1.125f, (0.8f * f2) / f, 1.125f, f2 / f, 1.125f);
        float f3 = X1;
        float f4 = X2;
        path.cubicTo(((0.4f * f4) + f3) / f, 1.125f, (f3 + (f4 * 0.2f)) / f, 1.0f, 1.0f, 1.0f);
        return path;
    }

    public static float getFractionUntilOvershoot() {
        return X1 / XTOT;
    }
}
