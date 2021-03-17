package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import com.android.systemui.statusbar.views.DismissView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewController$updateDismissView$listener$1 extends Lambda implements Function1<Animator, Unit> {
    final /* synthetic */ boolean $showDismissView;
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationPanelViewController$updateDismissView$listener$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController, boolean z) {
        super(1);
        this.this$0 = miuiNotificationPanelViewController;
        this.$showDismissView = z;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke((Animator) obj);
        return Unit.INSTANCE;
    }

    public final void invoke(@Nullable Animator animator) {
        DismissView access$getMDismissView$p = this.this$0.mDismissView;
        if (access$getMDismissView$p != null) {
            access$getMDismissView$p.stopAnimator();
        }
        if (this.$showDismissView) {
            DismissView access$getMDismissView$p2 = this.this$0.mDismissView;
            if (access$getMDismissView$p2 != null) {
                access$getMDismissView$p2.setVisibility(0);
                return;
            }
            return;
        }
        DismissView access$getMDismissView$p3 = this.this$0.mDismissView;
        if (access$getMDismissView$p3 != null) {
            access$getMDismissView$p3.setVisibility(8);
        }
    }
}
