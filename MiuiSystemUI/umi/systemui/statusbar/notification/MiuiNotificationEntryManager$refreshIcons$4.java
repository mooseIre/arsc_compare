package com.android.systemui.statusbar.notification;

import android.graphics.drawable.Drawable;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$refreshIcons$4 extends Lambda implements Function1<Drawable, Boolean> {
    final /* synthetic */ MiuiNotificationEntryManager this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationEntryManager$refreshIcons$4(MiuiNotificationEntryManager miuiNotificationEntryManager) {
        super(1);
        this.this$0 = miuiNotificationEntryManager;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(Drawable drawable) {
        return Boolean.valueOf(invoke(drawable));
    }

    public final boolean invoke(Drawable drawable) {
        return !Intrinsics.areEqual(drawable, MiuiNotificationEntryManager.access$getTRANSPARENT_DRAWABLE$p(this.this$0));
    }
}
