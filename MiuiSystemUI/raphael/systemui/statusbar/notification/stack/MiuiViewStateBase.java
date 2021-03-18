package com.android.systemui.statusbar.notification.stack;

import android.util.FloatProperty;
import android.view.View;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.PropertyAnimator;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public class MiuiViewStateBase {
    private boolean animatingAddRemove;
    private int springYOffset;
    private boolean touchAnimating;

    public final int getSpringYOffset() {
        return this.springYOffset;
    }

    public final void setSpringYOffset(int i) {
        this.springYOffset = i;
    }

    public final void setAnimatingAddRemove(boolean z) {
        this.animatingAddRemove = z;
    }

    public final boolean getTouchAnimating() {
        return this.touchAnimating;
    }

    public final void setTouchAnimating(boolean z) {
        this.touchAnimating = z;
    }

    private final void animateSpringYOffset(View view, AnimationProperties animationProperties) {
        AnimationFilter animationFilter;
        if (view instanceof ExpandableView) {
            AnimatableProperty access$getPROPERTY_SPRING_Y_OFFSET$p = MiuiNotificationAnimationExtensionsKt.access$getPROPERTY_SPRING_Y_OFFSET$p();
            Intrinsics.checkExpressionValueIsNotNull(access$getPROPERTY_SPRING_Y_OFFSET$p, "PROPERTY_SPRING_Y_OFFSET");
            if ((access$getPROPERTY_SPRING_Y_OFFSET$p.getProperty() instanceof FloatProperty) && animationProperties != null && (animationFilter = animationProperties.getAnimationFilter()) != null) {
                AnimatableProperty access$getPROPERTY_SPRING_Y_OFFSET$p2 = MiuiNotificationAnimationExtensionsKt.access$getPROPERTY_SPRING_Y_OFFSET$p();
                Intrinsics.checkExpressionValueIsNotNull(access$getPROPERTY_SPRING_Y_OFFSET$p2, "PROPERTY_SPRING_Y_OFFSET");
                if (animationFilter.shouldAnimateProperty(access$getPROPERTY_SPRING_Y_OFFSET$p2.getProperty())) {
                    ExpandableViewState viewState = ((ExpandableView) view).getViewState();
                    int i = 0;
                    if (!Intrinsics.areEqual(viewState != null ? Float.valueOf((float) viewState.getSpringYOffset()) : 0, Float.valueOf(0.0f))) {
                        Object tag = view.getTag(C0015R$id.miui_child_index_hint);
                        if (tag instanceof Integer) {
                            i = ((Number) tag).intValue();
                        }
                        long j = animationProperties.duration;
                        animationProperties.duration = SpringAnimationEvent.Companion.getDurationForIndex$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(i);
                        PropertyAnimator.startAnimation(view, MiuiNotificationAnimationExtensionsKt.access$getPROPERTY_SPRING_Y_OFFSET$p(), 0.0f, animationProperties);
                        animationProperties.duration = j;
                    }
                }
            }
        }
    }

    public void animateTo(@Nullable View view, @Nullable AnimationProperties animationProperties) {
        animateSpringYOffset(view, animationProperties);
    }

    public final boolean isAnimating() {
        return this.animatingAddRemove || this.touchAnimating;
    }
}
