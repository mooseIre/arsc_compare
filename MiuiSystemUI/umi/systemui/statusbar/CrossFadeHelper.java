package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;

public class CrossFadeHelper {
    public static void fadeOut(View view) {
        fadeOut(view, (Runnable) null);
    }

    public static void fadeOut(View view, Runnable runnable) {
        fadeOut(view, 210, 0, runnable);
    }

    public static void fadeOut(final View view, long j, int i, final Runnable runnable) {
        view.animate().cancel();
        view.animate().alpha(0.0f).setDuration(j).setInterpolator(Interpolators.ALPHA_OUT).setStartDelay((long) i).withEndAction(new Runnable() {
            /* class com.android.systemui.statusbar.CrossFadeHelper.AnonymousClass1 */

            public void run() {
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
                if (view.getVisibility() != 8) {
                    view.setVisibility(4);
                }
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
        if (f == 1.0f && view.getVisibility() != 8) {
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
        return Math.min(f / 0.7f, 1.0f);
    }

    private static void updateLayerType(View view, float f) {
        if (!view.hasOverlappingRendering() || f <= 0.0f || f >= 1.0f) {
            if (view.getLayerType() == 2 && view.getTag(C0015R$id.cross_fade_layer_type_changed_tag) != null && view.getTag(C0015R$id.cross_fade_layer_type_changed_tag) != null) {
                view.setLayerType(0, null);
            }
        } else if (view.getLayerType() != 2) {
            view.setLayerType(2, null);
            view.setTag(C0015R$id.cross_fade_layer_type_changed_tag, Boolean.TRUE);
        }
    }

    public static void fadeIn(View view) {
        fadeIn(view, 210, 0);
    }

    public static void fadeIn(final View view, long j, int i) {
        view.animate().cancel();
        if (view.getVisibility() == 4) {
            view.setAlpha(0.0f);
            view.setVisibility(0);
        }
        view.animate().alpha(1.0f).setDuration(j).setStartDelay((long) i).setInterpolator(Interpolators.ALPHA_IN).setListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.CrossFadeHelper.AnonymousClass2 */

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                view.setAlpha(1.0f);
            }
        }).withEndAction(null);
        if (view.hasOverlappingRendering() && view.getLayerType() != 2) {
            view.animate().withLayer();
        }
    }

    public static void fadeIn(View view, float f) {
        fadeIn(view, f, false);
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
