package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.views.DismissView;
import com.miui.systemui.util.HapticFeedBackImpl;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewController$initDismissView$1 implements View.OnClickListener {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$initDismissView$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public final void onClick(View view) {
        MetricsLogger.action(this.this$0.getPanelView().getContext(), 148);
        this.this$0.getMNotificationStackScroller().clearNotifications(0, true);
        DismissView access$getMDismissView$p = this.this$0.mDismissView;
        if (access$getMDismissView$p != null) {
            access$getMDismissView$p.animatorStart(new AnimatorListenerAdapter() {
                public void onAnimationEnd(@NotNull Animator animator) {
                    Intrinsics.checkParameterIsNotNull(animator, "animation");
                }
            });
        }
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).clearAllNotifications();
    }
}
