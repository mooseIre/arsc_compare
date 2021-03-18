package com.android.systemui.statusbar.phone;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$initializeFolmeAnimations$2 extends Lambda implements Function1<Float, Unit> {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationPanelViewController$initializeFolmeAnimations$2(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        super(1);
        this.this$0 = miuiNotificationPanelViewController;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Float f) {
        invoke(f.floatValue());
        return Unit.INSTANCE;
    }

    public final void invoke(float f) {
        this.this$0.setMSpringLength(f);
    }
}
