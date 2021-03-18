package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Predicate;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: NotificationGroupManagerInjector.kt */
public final class NotificationGroupManagerInjectorKt$hasMediaOrCustomChildren$1<T> implements Predicate<NotificationEntry> {
    public static final NotificationGroupManagerInjectorKt$hasMediaOrCustomChildren$1 INSTANCE = new NotificationGroupManagerInjectorKt$hasMediaOrCustomChildren$1();

    NotificationGroupManagerInjectorKt$hasMediaOrCustomChildren$1() {
    }

    public final boolean test(NotificationEntry notificationEntry) {
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "it");
        return notificationEntry.isMediaNotification() || NotificationUtil.isCustomViewNotification(notificationEntry.getSbn());
    }
}
