package com.android.systemui.statusbar.notification.row;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiExpandableNotificationRow.kt */
public final class NotificationClickerInjector {
    public static final NotificationClickerInjector INSTANCE = new NotificationClickerInjector();

    private NotificationClickerInjector() {
    }

    public final boolean onClick(@NotNull ExpandableNotificationRow expandableNotificationRow) {
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        if (!(expandableNotificationRow instanceof MiuiExpandableNotificationRow)) {
            return false;
        }
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = (MiuiExpandableNotificationRow) expandableNotificationRow;
        if (!miuiExpandableNotificationRow.isSummaryWithChildren() || miuiExpandableNotificationRow.isGroupExpanded()) {
            return false;
        }
        miuiExpandableNotificationRow.getExpandClickListener().onClick(expandableNotificationRow);
        return true;
    }
}
