package com.android.systemui.statusbar.notification.row;

import android.widget.ImageView;
import com.android.systemui.C0015R$id;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiExpandableNotificationRow.kt */
final class MiuiExpandableNotificationRow$mMiniWindowIcon$2 extends Lambda implements Function0<ImageView> {
    final /* synthetic */ MiuiExpandableNotificationRow this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiExpandableNotificationRow$mMiniWindowIcon$2(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        super(0);
        this.this$0 = miuiExpandableNotificationRow;
    }

    @Override // kotlin.jvm.functions.Function0
    public final ImageView invoke() {
        return (ImageView) this.this$0.findViewById(C0015R$id.mini_window_icon);
    }
}
