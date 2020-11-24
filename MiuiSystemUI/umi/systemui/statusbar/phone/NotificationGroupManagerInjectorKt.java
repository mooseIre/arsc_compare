package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.miui.systemui.BuildConfig;
import java.util.Collection;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationGroupManagerInjector.kt */
public final class NotificationGroupManagerInjectorKt {
    public static final boolean shouldSuppressed(@NotNull NotificationGroupManager.NotificationGroup notificationGroup, int i) {
        Intrinsics.checkParameterIsNotNull(notificationGroup, "group");
        if (notificationGroup.summary != null && !notificationGroup.expanded) {
            if (suppressEmpty(i, notificationGroup)) {
                return true;
            }
            Collection<NotificationEntry> values = notificationGroup.children.values();
            Intrinsics.checkExpressionValueIsNotNull(values, "group.children.values");
            if (hasMediaOrCustomChildren(values)) {
                return true;
            }
        }
        return false;
    }

    private static final boolean suppressEmpty(int i, NotificationGroupManager.NotificationGroup notificationGroup) {
        if (i == 0) {
            NotificationEntry notificationEntry = notificationGroup.summary;
            Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "group.summary");
            ExpandedNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "group.summary.sbn");
            return sbn.getNotification().isGroupSummary() && !BuildConfig.IS_INTERNATIONAL;
        }
    }

    private static final boolean hasMediaOrCustomChildren(Collection<NotificationEntry> collection) {
        return collection.stream().filter(NotificationGroupManagerInjectorKt$hasMediaOrCustomChildren$1.INSTANCE).count() > 0;
    }
}
