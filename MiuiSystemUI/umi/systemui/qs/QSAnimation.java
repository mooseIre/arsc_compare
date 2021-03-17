package com.android.systemui.qs;

import android.animation.Animator;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.controlcenter.phone.widget.HideBeforeAnimatorListener;
import com.android.systemui.controlcenter.phone.widget.ShowBeforeAnimatorListener;
import com.miui.systemui.util.MiuiInterpolators;
import java.util.Arrays;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: QSAnimation.kt */
public final class QSAnimation {
    public static final QSAnimation INSTANCE = new QSAnimation();
    @NotNull
    private static final Interpolator INTERPOLATOR;

    static {
        Interpolator interpolator = MiuiInterpolators.CUBIC_EASE_IN_OUT;
        Intrinsics.checkExpressionValueIsNotNull(interpolator, "MiuiInterpolators.CUBIC_EASE_IN_OUT");
        INTERPOLATOR = interpolator;
    }

    private QSAnimation() {
    }

    @NotNull
    public final Interpolator getINTERPOLATOR() {
        return INTERPOLATOR;
    }

    /* compiled from: QSAnimation.kt */
    public static final class QsHideBeforeAnimatorListener extends HideBeforeAnimatorListener {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public QsHideBeforeAnimatorListener(@NotNull View... viewArr) {
            super((View[]) Arrays.copyOf(viewArr, viewArr.length));
            Intrinsics.checkParameterIsNotNull(viewArr, "views");
            animateAlpha(true);
            setAlphaDuration(420);
            setAlphaInterpolator(QSAnimation.INSTANCE.getINTERPOLATOR());
        }
    }

    /* compiled from: QSAnimation.kt */
    public static final class QsShowBeforeAnimatorListener extends ShowBeforeAnimatorListener {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public QsShowBeforeAnimatorListener(@NotNull View... viewArr) {
            super((View[]) Arrays.copyOf(viewArr, viewArr.length));
            Intrinsics.checkParameterIsNotNull(viewArr, "views");
            animateAlpha(true);
            setAlphaDuration(420);
            setAlphaInterpolator(QSAnimation.INSTANCE.getINTERPOLATOR());
        }

        public void onAnimationEnd(@Nullable Animator animator) {
            super.onAnimationEnd(animator);
            for (View view : this.mViews) {
                view.animate().cancel();
                Intrinsics.checkExpressionValueIsNotNull(view, "v");
                view.setAlpha(1.0f);
            }
        }
    }
}
