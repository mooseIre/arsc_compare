package com.android.systemui.statusbar.notification.row;

import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.notification.FakeShadowView;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiExpandableNotificationRow.kt */
final class MiuiExpandableNotificationRow$mFakeShadowView$2 extends Lambda implements Function0<FakeShadowView> {
    final /* synthetic */ MiuiExpandableNotificationRow this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiExpandableNotificationRow$mFakeShadowView$2(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        super(0);
        this.this$0 = miuiExpandableNotificationRow;
    }

    @Override // kotlin.jvm.functions.Function0
    public final FakeShadowView invoke() {
        return (FakeShadowView) this.this$0.findViewById(C0015R$id.fake_shadow);
    }
}
