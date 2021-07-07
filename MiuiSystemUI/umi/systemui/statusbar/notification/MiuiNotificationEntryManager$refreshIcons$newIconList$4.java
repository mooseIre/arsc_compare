package com.android.systemui.statusbar.notification;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$refreshIcons$newIconList$4 extends Lambda implements Function1<Drawable, Boolean> {
    public static final MiuiNotificationEntryManager$refreshIcons$newIconList$4 INSTANCE = new MiuiNotificationEntryManager$refreshIcons$newIconList$4();

    MiuiNotificationEntryManager$refreshIcons$newIconList$4() {
        super(1);
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(Drawable drawable) {
        return Boolean.valueOf(invoke(drawable));
    }

    public final boolean invoke(Drawable drawable) {
        return (drawable instanceof ColorDrawable) && ((ColorDrawable) drawable).getColor() == 0;
    }
}
