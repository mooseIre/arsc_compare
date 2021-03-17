package com.android.systemui.statusbar.notification.row;

import android.view.View;
import com.android.systemui.C0015R$id;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiExpandableNotificationRow.kt */
final class MiuiExpandableNotificationRow$mMiniBar$2 extends Lambda implements Function0<View> {
    final /* synthetic */ MiuiExpandableNotificationRow this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiExpandableNotificationRow$mMiniBar$2(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        super(0);
        this.this$0 = miuiExpandableNotificationRow;
    }

    @Override // kotlin.jvm.functions.Function0
    public final View invoke() {
        return this.this$0.findViewById(C0015R$id.mini_window_bar);
    }
}
