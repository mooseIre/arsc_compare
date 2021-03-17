package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import com.android.systemui.statusbar.views.DismissView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$updateDismissView$listener$1 extends Lambda implements Function1<Animator, Unit> {
    final /* synthetic */ boolean $showDismissView;
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationPanelViewController$updateDismissView$listener$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController, boolean z) {
        super(1);
        this.this$0 = miuiNotificationPanelViewController;
        this.$showDismissView = z;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Animator animator) {
        invoke(animator);
        return Unit.INSTANCE;
    }

    public final void invoke(@Nullable Animator animator) {
        DismissView dismissView = this.this$0.mDismissView;
        if (dismissView != null) {
            dismissView.stopAnimator();
        }
        if (this.$showDismissView) {
            DismissView dismissView2 = this.this$0.mDismissView;
            if (dismissView2 != null) {
                dismissView2.setVisibility(0);
                return;
            }
            return;
        }
        DismissView dismissView3 = this.this$0.mDismissView;
        if (dismissView3 != null) {
            dismissView3.setVisibility(8);
        }
    }
}
