package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.PropertyAnimator;
import kotlin.jvm.internal.Intrinsics;
import miui.maml.animation.interpolater.ElasticEaseOutInterpolater;
import miui.maml.animation.interpolater.QuartEaseInInterpolater;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class MiuiNotificationAnimations {
    @NotNull
    private static final ElasticEaseOutInterpolater HEADS_UP_APPEAR_INTERPOLATOR = new ElasticEaseOutInterpolater(2.0f, 1.4f);
    @NotNull
    private static final QuartEaseInInterpolater HEADS_UP_DISAPPEAR_INTERPOLATOR = new QuartEaseInInterpolater();
    public static final MiuiNotificationAnimations INSTANCE = new MiuiNotificationAnimations();
    @NotNull
    private static final AnimationFilter PANEL_APPEAR_DISAPPEAR_FILTER;
    @NotNull
    private static final AnimationFilter RELEASE_SPRING_FILTER;

    static {
        AnimationFilter animationFilter = new AnimationFilter();
        AnimatableProperty animatableProperty = MiuiNotificationAnimationExtensionsKt.PROPERTY_SPRING_Y_OFFSET;
        Intrinsics.checkExpressionValueIsNotNull(animatableProperty, "PROPERTY_SPRING_Y_OFFSET");
        animationFilter.animate(animatableProperty.getProperty());
        Intrinsics.checkExpressionValueIsNotNull(animationFilter, "AnimationFilter()\n      …SPRING_Y_OFFSET.property)");
        RELEASE_SPRING_FILTER = animationFilter;
        AnimationFilter animationFilter2 = new AnimationFilter();
        animationFilter2.animateAlpha();
        animationFilter2.animateScale();
        Intrinsics.checkExpressionValueIsNotNull(animationFilter2, "AnimationFilter()\n      …          .animateScale()");
        PANEL_APPEAR_DISAPPEAR_FILTER = animationFilter2;
    }

    private MiuiNotificationAnimations() {
    }

    @NotNull
    public final AnimationFilter getRELEASE_SPRING_FILTER() {
        return RELEASE_SPRING_FILTER;
    }

    @NotNull
    public final AnimationFilter getPANEL_APPEAR_DISAPPEAR_FILTER() {
        return PANEL_APPEAR_DISAPPEAR_FILTER;
    }

    @NotNull
    public final ElasticEaseOutInterpolater getHEADS_UP_APPEAR_INTERPOLATOR() {
        return HEADS_UP_APPEAR_INTERPOLATOR;
    }

    @NotNull
    public final QuartEaseInInterpolater getHEADS_UP_DISAPPEAR_INTERPOLATOR() {
        return HEADS_UP_DISAPPEAR_INTERPOLATOR;
    }

    public final void cancelSpringAnimations(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        PropertyAnimator.cancelAnimation(view, MiuiNotificationAnimationExtensionsKt.PROPERTY_SPRING_Y_OFFSET);
    }
}
