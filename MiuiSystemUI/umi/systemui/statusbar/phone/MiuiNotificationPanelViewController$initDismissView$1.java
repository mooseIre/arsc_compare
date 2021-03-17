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

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$initDismissView$1 implements View.OnClickListener {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    MiuiNotificationPanelViewController$initDismissView$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    public final void onClick(View view) {
        MetricsLogger.action(this.this$0.getPanelView().getContext(), 148);
        this.this$0.getMNotificationStackScroller().clearNotifications(0, true);
        DismissView dismissView = this.this$0.mDismissView;
        if (dismissView != null) {
            dismissView.animatorStart(new AnimatorListenerAdapter() {
                /* class com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController$initDismissView$1.AnonymousClass1 */

                public void onAnimationEnd(@NotNull Animator animator) {
                    Intrinsics.checkParameterIsNotNull(animator, "animation");
                }
            });
        }
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).clearAllNotifications();
    }
}
