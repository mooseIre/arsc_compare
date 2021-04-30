package com.android.systemui.statusbar.notification;

import android.text.TextUtils;
import android.view.View;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import java.util.List;

public class NotificationVisualHelper {
    public static boolean isVisualView(NotificationEntry notificationEntry, NotificationStackScrollLayout notificationStackScrollLayout, boolean z) {
        boolean z2;
        if (notificationEntry == null || notificationStackScrollLayout == null || notificationEntry.getRow() == null) {
            return false;
        }
        ExpandableNotificationRow row = notificationEntry.getRow();
        if (z) {
            z2 = justVisibleWhenPanelExpanded(row, notificationStackScrollLayout);
        } else {
            z2 = row.isHeadsUp();
        }
        if (z2) {
            notificationEntry.visualPosition = getVisualPosition(notificationEntry);
            notificationEntry.isVisualInFloat = row.isHeadsUp();
        }
        notificationEntry.isVisual = z2;
        return z2;
    }

    private static boolean justVisibleWhenPanelExpanded(ExpandableNotificationRow expandableNotificationRow, NotificationStackScrollLayout notificationStackScrollLayout) {
        float translationY = expandableNotificationRow.getTranslationY() + ((float) expandableNotificationRow.getClipTopAmount());
        float translationY2 = (expandableNotificationRow.getTranslationY() + ((float) expandableNotificationRow.getActualHeight())) - ((float) expandableNotificationRow.getClipBottomAmount());
        int topPadding = notificationStackScrollLayout.getTopPadding();
        int height = notificationStackScrollLayout.getHeight();
        if (expandableNotificationRow.isChildInGroup()) {
            ExpandableNotificationRow notificationParent = expandableNotificationRow.getNotificationParent();
            int positionInGroup = getPositionInGroup(expandableNotificationRow);
            if (((float) notificationParent.getClipTopAmount()) >= translationY2 || (!notificationParent.isGroupExpanded() && positionInGroup > 3)) {
                translationY = -1.0f;
                translationY2 = -1.0f;
            } else {
                float translationY3 = notificationParent.getTranslationY() + ((float) notificationParent.getClipTopAmount());
                translationY += translationY3;
                translationY2 += translationY3;
            }
        }
        return translationY < ((float) height) && translationY2 > ((float) topPadding);
    }

    public static int getVisualPosition(NotificationEntry notificationEntry) {
        int i;
        int i2;
        if (!(notificationEntry == null || notificationEntry.getRow() == null)) {
            ExpandableNotificationRow row = notificationEntry.getRow();
            if (row.isChildInGroup()) {
                i2 = getPositionInTop(row.getNotificationParent());
                i = getPositionInGroup(row);
            } else {
                i2 = getPositionInTop(row);
                i = 0;
            }
            if (!(i2 == -1 || i == -1)) {
                return (i2 * 100) + i;
            }
        }
        return -1;
    }

    private static int getPositionInTop(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow == null || !expandableNotificationRow.isTopLevelChild()) {
            return -1;
        }
        NotificationStackScrollLayout notificationStackScrollLayout = (NotificationStackScrollLayout) expandableNotificationRow.getParent();
        int i = 0;
        for (int i2 = 0; i2 < notificationStackScrollLayout.getChildCount(); i2++) {
            View childAt = notificationStackScrollLayout.getChildAt(i2);
            if ((childAt instanceof ExpandableNotificationRow) && childAt.getVisibility() != 8) {
                i++;
                if (equalsRow(expandableNotificationRow, (ExpandableNotificationRow) childAt)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int getPositionInGroup(ExpandableNotificationRow expandableNotificationRow) {
        List<ExpandableNotificationRow> attachedChildren;
        if (expandableNotificationRow == null || !expandableNotificationRow.isChildInGroup() || (attachedChildren = expandableNotificationRow.getNotificationParent().getAttachedChildren()) == null || attachedChildren.size() <= 0) {
            return -1;
        }
        int i = 0;
        for (ExpandableNotificationRow expandableNotificationRow2 : attachedChildren) {
            if (expandableNotificationRow2.getVisibility() != 8) {
                i++;
                if (equalsRow(expandableNotificationRow, expandableNotificationRow2)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean equalsRow(ExpandableNotificationRow expandableNotificationRow, ExpandableNotificationRow expandableNotificationRow2) {
        if (expandableNotificationRow == null || expandableNotificationRow2 == null) {
            return false;
        }
        return TextUtils.equals(expandableNotificationRow.getEntry().getKey(), expandableNotificationRow2.getEntry().getKey());
    }
}
