package com.android.systemui.statusbar;

import android.graphics.Paint;
import android.view.View;
import com.android.systemui.Interpolators;

public class CrossFadeHelper {
    public static void fadeOut(final View view, final Runnable runnable) {
        view.animate().cancel();
        view.animate().alpha(0.0f).setDuration(210).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
            public void run() {
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
                view.setVisibility(4);
            }
        });
        if (view.hasOverlappingRendering()) {
            view.animate().withLayer();
        }
    }

    public static void fadeOut(View view, float f) {
        fadeOut(view, f, true);
    }

    public static void fadeOut(View view, float f, boolean z) {
        view.animate().cancel();
        if (f == 1.0f) {
            view.setVisibility(4);
        } else if (view.getVisibility() == 4) {
            view.setVisibility(0);
        }
        if (z) {
            f = mapToFadeDuration(f);
        }
        float interpolation = Interpolators.ALPHA_OUT.getInterpolation(1.0f - f);
        view.setAlpha(interpolation);
        updateLayerType(view, interpolation);
    }

    private static float mapToFadeDuration(float f) {
        return Math.min(f / 0.5833333f, 1.0f);
    }

    private static void updateLayerType(View view, float f) {
        if (view.hasOverlappingRendering() && f > 0.0f && f < 1.0f) {
            view.setLayerType(2, (Paint) null);
        } else if (view.getLayerType() == 2) {
            view.setLayerType(0, (Paint) null);
        }
    }

    public static void fadeIn(View view) {
        view.animate().cancel();
        if (view.getVisibility() == 4) {
            view.setAlpha(0.0f);
            view.setVisibility(0);
        }
        view.animate().alpha(1.0f).setDuration(210).setInterpolator(Interpolators.ALPHA_IN).withEndAction((Runnable) null);
        if (view.hasOverlappingRendering()) {
            view.animate().withLayer();
        }
    }

    public static void fadeIn(View view, float f) {
        fadeIn(view, f, true);
    }

    public static void fadeIn(View view, float f, boolean z) {
        view.animate().cancel();
        if (view.getVisibility() == 4) {
            view.setVisibility(0);
        }
        if (z) {
            f = mapToFadeDuration(f);
        }
        float interpolation = Interpolators.ALPHA_IN.getInterpolation(f);
        view.setAlpha(interpolation);
        updateLayerType(view, interpolation);
    }
}
