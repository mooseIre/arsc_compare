package com.android.systemui.statusbar.notification.row;

import com.android.systemui.C0012R$dimen;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiExpandableNotificationRow.kt */
final class MiuiExpandableNotificationRow$mMiniBarMarginBottom$2 extends Lambda implements Function0<Float> {
    final /* synthetic */ MiuiExpandableNotificationRow this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiExpandableNotificationRow$mMiniBarMarginBottom$2(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        super(0);
        this.this$0 = miuiExpandableNotificationRow;
    }

    /* Return type fixed from 'float' to match base method */
    @Override // kotlin.jvm.functions.Function0
    public final Float invoke() {
        return this.this$0.getResources().getDimension(C0012R$dimen.mini_window_bar_marginBottom);
    }
}
