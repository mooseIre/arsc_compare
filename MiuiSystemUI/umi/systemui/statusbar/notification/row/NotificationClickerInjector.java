package com.android.systemui.statusbar.notification.row;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.unimportant.FoldNotifController;
import com.android.systemui.statusbar.phone.ShadeController;
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
        NotificationEntry entry = miuiExpandableNotificationRow.getEntry();
        Intrinsics.checkExpressionValueIsNotNull(entry, "row.entry");
        ExpandedNotification sbn = entry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "row.entry.sbn");
        String targetPackageName = sbn.getTargetPackageName();
        Intrinsics.checkExpressionValueIsNotNull(targetPackageName, "row.entry.sbn.targetPackageName");
        ((FoldNotifController) Dependency.get(FoldNotifController.class)).addClickCount(targetPackageName);
        if (!miuiExpandableNotificationRow.isSummaryWithChildren() || miuiExpandableNotificationRow.isGroupExpanded()) {
            NotificationEntry entry2 = miuiExpandableNotificationRow.getEntry();
            Intrinsics.checkExpressionValueIsNotNull(entry2, "row.entry");
            Boolean isFoldEntrance = NotificationUtil.isFoldEntrance(entry2.getSbn());
            Intrinsics.checkExpressionValueIsNotNull(isFoldEntrance, "NotificationUtil.isFoldEntrance(row.entry.sbn)");
            if (!isFoldEntrance.booleanValue()) {
                return false;
            }
            NotificationEntry entry3 = miuiExpandableNotificationRow.getEntry();
            Intrinsics.checkExpressionValueIsNotNull(entry3, "row.entry");
            if (NotificationUtil.getClickType(entry3.getSbn()) == 0) {
                NotificationEntry entry4 = miuiExpandableNotificationRow.getEntry();
                Intrinsics.checkExpressionValueIsNotNull(entry4, "row.entry");
                return ((FoldNotifController) Dependency.get(FoldNotifController.class)).jump2Fold(entry4.getSbn());
            }
            ((ShadeController) Dependency.get(ShadeController.class)).animateCollapsePanels(0);
            return ((FoldNotifController) Dependency.get(FoldNotifController.class)).jump2FoldSettings();
        }
        miuiExpandableNotificationRow.getExpandClickListener().onClick(expandableNotificationRow);
        return true;
    }
}
