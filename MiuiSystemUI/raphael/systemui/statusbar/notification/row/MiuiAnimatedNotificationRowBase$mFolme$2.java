package com.android.systemui.statusbar.notification.row;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import miuix.animation.Folme;
import miuix.animation.IAnimTarget;
import miuix.animation.IStateStyle;
import miuix.animation.ViewTarget;

/* compiled from: MiuiAnimatedNotificationRowBase.kt */
final class MiuiAnimatedNotificationRowBase$mFolme$2 extends Lambda implements Function0<IStateStyle> {
    final /* synthetic */ MiuiAnimatedNotificationRowBase this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiAnimatedNotificationRowBase$mFolme$2(MiuiAnimatedNotificationRowBase miuiAnimatedNotificationRowBase) {
        super(0);
        this.this$0 = miuiAnimatedNotificationRowBase;
    }

    @Override // kotlin.jvm.functions.Function0
    public final IStateStyle invoke() {
        IAnimTarget target = Folme.getTarget(this.this$0, ViewTarget.sCreator);
        target.setDefaultMinVisibleChange(0.002f);
        return Folme.useAt(target).state();
    }
}
