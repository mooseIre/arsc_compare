package com.android.systemui.statusbar.notification.row;

import android.app.NotificationChannel;
import java.util.Comparator;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Comparisons.kt */
public final class ChannelEditorDialogController$getDisplayableChannels$$inlined$compareBy$1<T> implements Comparator<T> {
    public final int compare(T t, T t2) {
        String str;
        String str2;
        NotificationChannel notificationChannel = (NotificationChannel) t;
        Intrinsics.checkExpressionValueIsNotNull(notificationChannel, "it");
        CharSequence name = notificationChannel.getName();
        if (name == null || (str = name.toString()) == null) {
            str = notificationChannel.getId();
        }
        NotificationChannel notificationChannel2 = (NotificationChannel) t2;
        Intrinsics.checkExpressionValueIsNotNull(notificationChannel2, "it");
        CharSequence name2 = notificationChannel2.getName();
        if (name2 == null || (str2 = name2.toString()) == null) {
            str2 = notificationChannel2.getId();
        }
        return ComparisonsKt__ComparisonsKt.compareValues(str, str2);
    }
}
