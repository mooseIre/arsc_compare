package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.NotificationInteractionTracker;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.Arrays;
import java.util.List;

public class ListDumper {
    public static String dumpTree(List<ListEntry> list, NotificationInteractionTracker notificationInteractionTracker, boolean z, String str) {
        StringBuilder sb = new StringBuilder();
        String str2 = str + "  ";
        for (int i = 0; i < list.size(); i++) {
            ListEntry listEntry = list.get(i);
            dumpEntry(listEntry, Integer.toString(i), str, sb, true, z, notificationInteractionTracker.hasUserInteractedWith(listEntry.getKey()));
            if (listEntry instanceof GroupEntry) {
                List<NotificationEntry> children = ((GroupEntry) listEntry).getChildren();
                for (int i2 = 0; i2 < children.size(); i2++) {
                    dumpEntry(children.get(i2), Integer.toString(i) + "." + Integer.toString(i2), str2, sb, true, z, notificationInteractionTracker.hasUserInteractedWith(listEntry.getKey()));
                }
            }
        }
        return sb.toString();
    }

    public static String dumpList(List<NotificationEntry> list, boolean z, String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            dumpEntry(list.get(i), Integer.toString(i), str, sb, false, z, false);
        }
        return sb.toString();
    }

    private static void dumpEntry(ListEntry listEntry, String str, String str2, StringBuilder sb, boolean z, boolean z2, boolean z3) {
        sb.append(str2);
        sb.append("[");
        sb.append(str);
        sb.append("] ");
        sb.append(listEntry.getKey());
        if (z) {
            sb.append(" (parent=");
            sb.append(listEntry.getParent() != null ? listEntry.getParent().getKey() : null);
            sb.append(")");
        }
        if (listEntry.getNotifSection() != null) {
            sb.append(" sectionIndex=");
            sb.append(listEntry.getSection());
            sb.append(" sectionName=");
            sb.append(listEntry.getNotifSection().getName());
        }
        if (z2) {
            NotificationEntry representativeEntry = listEntry.getRepresentativeEntry();
            StringBuilder sb2 = new StringBuilder();
            if (!representativeEntry.mLifetimeExtenders.isEmpty()) {
                int size = representativeEntry.mLifetimeExtenders.size();
                String[] strArr = new String[size];
                for (int i = 0; i < size; i++) {
                    strArr[i] = representativeEntry.mLifetimeExtenders.get(i).getName();
                }
                sb2.append("lifetimeExtenders=");
                sb2.append(Arrays.toString(strArr));
                sb2.append(" ");
            }
            if (!representativeEntry.mDismissInterceptors.isEmpty()) {
                int size2 = representativeEntry.mDismissInterceptors.size();
                String[] strArr2 = new String[size2];
                for (int i2 = 0; i2 < size2; i2++) {
                    strArr2[i2] = representativeEntry.mDismissInterceptors.get(i2).getName();
                }
                sb2.append("dismissInterceptors=");
                sb2.append(Arrays.toString(strArr2));
                sb2.append(" ");
            }
            if (representativeEntry.getExcludingFilter() != null) {
                sb2.append("filter=");
                sb2.append(representativeEntry.getExcludingFilter().getName());
                sb2.append(" ");
            }
            if (representativeEntry.getNotifPromoter() != null) {
                sb2.append("promoter=");
                sb2.append(representativeEntry.getNotifPromoter().getName());
                sb2.append(" ");
            }
            if (representativeEntry.mCancellationReason != -1) {
                sb2.append("cancellationReason=");
                sb2.append(representativeEntry.mCancellationReason);
                sb2.append(" ");
            }
            if (representativeEntry.getDismissState() != NotificationEntry.DismissState.NOT_DISMISSED) {
                sb2.append("dismissState=");
                sb2.append(representativeEntry.getDismissState());
                sb2.append(" ");
            }
            sb2.append("interacted=");
            sb2.append(z3 ? "yes" : "no");
            sb2.append(" ");
            String sb3 = sb2.toString();
            if (!sb3.isEmpty()) {
                sb.append("\n\t");
                sb.append(str2);
                sb.append(sb3);
            }
        }
        sb.append("\n");
    }
}
