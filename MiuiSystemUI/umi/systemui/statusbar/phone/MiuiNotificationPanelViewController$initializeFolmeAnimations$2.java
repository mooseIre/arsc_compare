package com.android.systemui.statusbar.phone;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewController$initializeFolmeAnimations$2 extends Lambda implements Function1<Float, Unit> {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationPanelViewController$initializeFolmeAnimations$2(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        super(1);
        this.this$0 = miuiNotificationPanelViewController;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke(((Number) obj).floatValue());
        return Unit.INSTANCE;
    }

    public final void invoke(float f) {
        this.this$0.setMSpringLength(f);
    }
}
