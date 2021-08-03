package com.android.systemui.statusbar.notification.unimportant;

import android.app.NotificationManager;
import com.android.systemui.plugins.miui.controls.MiPlayPlugin;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: FoldNotifController.kt */
public final class FoldNotifController$mNm$2 extends Lambda implements Function0<NotificationManager> {
    final /* synthetic */ FoldNotifController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FoldNotifController$mNm$2(FoldNotifController foldNotifController) {
        super(0);
        this.this$0 = foldNotifController;
    }

    @Override // kotlin.jvm.functions.Function0
    @NotNull
    public final NotificationManager invoke() {
        Object systemService = this.this$0.context.getSystemService(MiPlayPlugin.REF_NOTIFICATION);
        if (systemService != null) {
            return (NotificationManager) systemService;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.app.NotificationManager");
    }
}
