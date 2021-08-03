package com.android.systemui.statusbar.notification.unimportant;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: FoldNotifController.kt */
public final class FoldNotifController$cache$2 extends Lambda implements Function0<PackageScoreCache> {
    final /* synthetic */ FoldNotifController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FoldNotifController$cache$2(FoldNotifController foldNotifController) {
        super(0);
        this.this$0 = foldNotifController;
    }

    @Override // kotlin.jvm.functions.Function0
    @NotNull
    public final PackageScoreCache invoke() {
        return new PackageScoreCache(this.this$0.context);
    }
}
