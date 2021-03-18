package com.android.systemui.statusbar.phone;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
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

    public static final boolean shouldHideGroupSummary(@NotNull StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        return isAutoGroupSummary(statusBarNotification) && NotificationSettingsHelper.disableAutoGroupSummary(statusBarNotification.getPackageName());
    }

    private static final boolean isAutoGroupSummary(StatusBarNotification statusBarNotification) {
        if (statusBarNotification.getId() == Integer.MAX_VALUE && Intrinsics.areEqual("ranker_group", statusBarNotification.getTag())) {
            Notification notification = statusBarNotification.getNotification();
            Intrinsics.checkExpressionValueIsNotNull(notification, "sbn.notification");
            if (Intrinsics.areEqual("ranker_group", notification.getGroup())) {
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
