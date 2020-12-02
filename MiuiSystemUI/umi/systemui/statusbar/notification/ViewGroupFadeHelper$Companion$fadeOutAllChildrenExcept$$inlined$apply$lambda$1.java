package com.android.systemui.statusbar.notification;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0015R$id;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ViewGroupFadeHelper.kt */
final class ViewGroupFadeHelper$Companion$fadeOutAllChildrenExcept$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ ViewGroup $root$inlined;
    final /* synthetic */ Set $viewsToFadeOut$inlined;

    ViewGroupFadeHelper$Companion$fadeOutAllChildrenExcept$$inlined$apply$lambda$1(long j, ViewGroup viewGroup, Set set, Runnable runnable) {
        this.$root$inlined = viewGroup;
        this.$viewsToFadeOut$inlined = set;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        Float f = (Float) this.$root$inlined.getTag(C0015R$id.view_group_fade_helper_previous_value_tag);
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animation");
        Object animatedValue = valueAnimator.getAnimatedValue();
        if (animatedValue != null) {
            float floatValue = ((Float) animatedValue).floatValue();
            for (View view : this.$viewsToFadeOut$inlined) {
                if (!Intrinsics.areEqual(view.getAlpha(), f)) {
                    view.setTag(C0015R$id.view_group_fade_helper_restore_tag, Float.valueOf(view.getAlpha()));
                }
                view.setAlpha(floatValue);
            }
            this.$root$inlined.setTag(C0015R$id.view_group_fade_helper_previous_value_tag, Float.valueOf(floatValue));
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
    }
}
