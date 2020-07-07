package com.android.systemui;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.miui.anim.PhysicBasedInterpolator;
import miui.maml.animation.interpolater.ElasticEaseOutInterpolater;
import miui.maml.animation.interpolater.QuartEaseInInterpolater;
import miui.view.animation.CubicEaseInOutInterpolator;
import miui.view.animation.CubicEaseOutInterpolator;
import miui.view.animation.QuinticEaseOutInterpolator;
import miui.view.animation.SineEaseInOutInterpolator;
import miui.view.animation.SineEaseOutInterpolator;

public class Interpolators {
    public static final Interpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
    public static final Interpolator ALPHA_IN = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
    public static final Interpolator ALPHA_OUT = new PathInterpolator(0.0f, 0.0f, 0.8f, 1.0f);
    public static final Interpolator APPEAR_DISAPPEAR = new PhysicBasedInterpolator(0.85f, 0.67f);
    public static final Interpolator CUBIC_EASE_IN_OUT = new CubicEaseInOutInterpolator();
    public static final Interpolator CUBIC_EASE_OUT = new CubicEaseOutInterpolator();
    public static final Interpolator CUSTOM_40_40 = new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f);
    public static final Interpolator DECELERATE = new DecelerateInterpolator();
    public static final Interpolator DECELERATE_CUBIC = new DecelerateInterpolator(1.5f);
    public static final Interpolator DECELERATE_QUART = new DecelerateInterpolator(2.0f);
    public static final Interpolator DECELERATE_QUINT = new DecelerateInterpolator(2.5f);
    public static final Interpolator EASE_IN_OUT = new SineEaseInOutInterpolator();
    public static final Interpolator FAST_OUT_LINEAR_IN = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
    public static final Interpolator FAST_OUT_SLOW_IN = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    public static final Interpolator HEADS_UP_APPEAR = new ElasticEaseOutInterpolater(2.0f, 1.4f);
    public static final Interpolator HEADS_UP_DISAPPEAR = new QuartEaseInInterpolater();
    public static final Interpolator ICON_OVERSHOT = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.4f);
    public static final Interpolator LINEAR = new LinearInterpolator();
    public static final Interpolator LINEAR_OUT_SLOW_IN = new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f);
    public static final Interpolator MIUI_ALPHA_IN = new SineEaseOutInterpolator();
    public static final Interpolator MIUI_ALPHA_OUT = new SineEaseOutInterpolator();
    public static final Interpolator PANEL_CLOSE_ACCELERATED = new PathInterpolator(0.3f, 0.0f, 0.5f, 1.0f);
    public static final Interpolator QUINTIC_EASE_OUT = new QuinticEaseOutInterpolator();
    public static final Interpolator TOUCH_RESPONSE = new PathInterpolator(0.3f, 0.0f, 0.1f, 1.0f);

    static {
        new AccelerateInterpolator();
        new PathInterpolator(0.9f, 0.0f, 0.7f, 1.0f);
    }
}
