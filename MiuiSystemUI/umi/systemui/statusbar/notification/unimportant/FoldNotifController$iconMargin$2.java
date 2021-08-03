package com.android.systemui.statusbar.notification.unimportant;

import com.android.systemui.C0012R$dimen;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: FoldNotifController.kt */
public final class FoldNotifController$iconMargin$2 extends Lambda implements Function0<Integer> {
    final /* synthetic */ FoldNotifController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FoldNotifController$iconMargin$2(FoldNotifController foldNotifController) {
        super(0);
        this.this$0 = foldNotifController;
    }

    /* Return type fixed from 'int' to match base method */
    @Override // kotlin.jvm.functions.Function0
    public final Integer invoke() {
        return this.this$0.context.getResources().getDimensionPixelSize(C0012R$dimen.custom_notification_title_icon_margin_fold);
    }
}
