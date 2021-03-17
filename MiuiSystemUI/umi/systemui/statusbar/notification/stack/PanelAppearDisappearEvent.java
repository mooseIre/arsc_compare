package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.miui.systemui.animation.PhysicBasedInterpolator;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class PanelAppearDisappearEvent extends NotificationStackScrollLayout.AnimationEvent {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final PhysicBasedInterpolator INTERPOLATOR;

    /* compiled from: MiuiNotificationAnimationExtensions.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final PhysicBasedInterpolator getINTERPOLATOR() {
            return PanelAppearDisappearEvent.INTERPOLATOR;
        }

        public final void animateAppearDisappear$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(@NotNull View view, boolean z, @Nullable Animator.AnimatorListener animatorListener) {
            Intrinsics.checkParameterIsNotNull(view, "$this$animateAppearDisappear");
            ViewPropertyAnimator animate = view.animate();
            animate.setDuration(450);
            animate.setInterpolator(PanelAppearDisappearEvent.Companion.getINTERPOLATOR());
            float f = 0.8f;
            float f2 = 1.0f;
            animate.scaleX(z ? 1.0f : 0.8f);
            if (z) {
                f = 1.0f;
            }
            animate.scaleY(f);
            if (!z) {
                f2 = 0.0f;
            }
            animate.alpha(f2);
            if (animatorListener != null) {
                animate.setListener(animatorListener);
            }
            animate.start();
        }

        public final void animateAppearDisappear$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(@NotNull View view, boolean z) {
            Intrinsics.checkParameterIsNotNull(view, "$this$animateAppearDisappear");
            animateAppearDisappear$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(view, z, null);
        }
    }

    public PanelAppearDisappearEvent() {
        super(null, 17, 450, MiuiNotificationAnimations.INSTANCE.getPANEL_APPEAR_DISAPPEAR_FILTER());
    }

    static {
        PhysicBasedInterpolator.Builder builder = new PhysicBasedInterpolator.Builder();
        builder.setDamping(0.85f);
        builder.setResponse(0.67f);
        INTERPOLATOR = builder.build();
    }
}
