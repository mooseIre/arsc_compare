package com.android.systemui.statusbar.notification.icon;

import android.app.Notification;
import android.content.Context;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: IconBuilder.kt */
public final class IconBuilder {
    private final Context context;

    public IconBuilder(@NotNull Context context2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        this.context = context2;
    }

    @NotNull
    public final StatusBarIconView createIconView(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Context context2 = this.context;
        StringBuilder sb = new StringBuilder();
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        sb.append(sbn.getPackageName());
        sb.append("/0x");
        ExpandedNotification sbn2 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
        sb.append(Integer.toHexString(sbn2.getId()));
        return new StatusBarIconView(context2, sb.toString(), notificationEntry.getSbn());
    }

    @NotNull
    public final CharSequence getIconContentDescription(@NotNull Notification notification) {
        Intrinsics.checkParameterIsNotNull(notification, "n");
        String contentDescForNotification = StatusBarIconView.contentDescForNotification(this.context, notification);
        Intrinsics.checkExpressionValueIsNotNull(contentDescForNotification, "StatusBarIconView.conten…rNotification(context, n)");
        return contentDescForNotification;
    }
}
