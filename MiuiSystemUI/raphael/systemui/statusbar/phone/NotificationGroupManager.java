package com.android.systemui.statusbar.phone;

import android.service.notification.StatusBarNotification;
import android.service.notification.StatusBarNotificationCompat;
import com.android.systemui.Constants;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class NotificationGroupManager implements OnHeadsUpChangedListener {
    private int mBarState = -1;
    private final HashMap<String, NotificationGroup> mGroupMap = new HashMap<>();
    private HeadsUpManager mHeadsUpManager;
    private boolean mIsUpdatingUnchangedGroup;
    private HashMap<String, StatusBarNotification> mIsolatedEntries = new HashMap<>();
    private OnGroupChangeListener mListener;

    public interface OnGroupChangeListener {
        void onGroupCreatedFromChildren(NotificationGroup notificationGroup);

        void onGroupExpansionChanged(ExpandableNotificationRow expandableNotificationRow, boolean z);

        void onGroupsChanged();
    }

    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    public void onHeadsUpPinnedModeChanged(boolean z) {
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    public void setOnGroupChangeListener(OnGroupChangeListener onGroupChangeListener) {
        this.mListener = onGroupChangeListener;
    }

    public boolean isGroupExpanded(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification));
        if (notificationGroup == null) {
            return false;
        }
        return notificationGroup.expanded;
    }

    public void setGroupExpanded(StatusBarNotification statusBarNotification, boolean z) {
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification));
        if (notificationGroup != null) {
            setGroupExpanded(notificationGroup, z);
        }
    }

    private void setGroupExpanded(NotificationGroup notificationGroup, boolean z) {
        notificationGroup.expanded = z;
        NotificationData.Entry entry = notificationGroup.summary;
        if (entry != null) {
            this.mListener.onGroupExpansionChanged(entry.row, z);
        }
    }

    public void onEntryRemoved(NotificationData.Entry entry) {
        onEntryRemovedInternal(entry, entry.notification);
        this.mIsolatedEntries.remove(entry.key);
    }

    private void onEntryRemovedInternal(NotificationData.Entry entry, StatusBarNotification statusBarNotification) {
        String groupKey = getGroupKey(statusBarNotification);
        NotificationGroup notificationGroup = this.mGroupMap.get(groupKey);
        if (notificationGroup != null) {
            if (isGroupChild(statusBarNotification)) {
                notificationGroup.children.remove(entry);
            } else {
                notificationGroup.summary = null;
            }
            updateSuppression(notificationGroup);
            if (notificationGroup.children.isEmpty() && notificationGroup.summary == null) {
                this.mGroupMap.remove(groupKey);
            }
        }
    }

    public boolean canRemove(NotificationGroup notificationGroup) {
        return notificationGroup != null && notificationGroup.summary != null && notificationGroup.children.isEmpty() && notificationGroup.summary.notification.getNotification().isGroupSummary() && !hasIsolatedChildren(notificationGroup);
    }

    public void onEntryAdded(NotificationData.Entry entry) {
        ExpandedNotification expandedNotification = entry.notification;
        boolean isGroupChild = isGroupChild(expandedNotification);
        String groupKey = getGroupKey(expandedNotification);
        NotificationGroup notificationGroup = this.mGroupMap.get(groupKey);
        if (notificationGroup == null) {
            notificationGroup = new NotificationGroup();
            this.mGroupMap.put(groupKey, notificationGroup);
        }
        if (isGroupChild) {
            notificationGroup.children.add(entry);
            updateSuppression(notificationGroup);
            return;
        }
        notificationGroup.summary = entry;
        notificationGroup.expanded = entry.row.areChildrenExpanded();
        updateSuppression(notificationGroup);
        if (!notificationGroup.children.isEmpty()) {
            Iterator it = ((HashSet) notificationGroup.children.clone()).iterator();
            while (it.hasNext()) {
                onEntryBecomingChild((NotificationData.Entry) it.next());
            }
            this.mListener.onGroupCreatedFromChildren(notificationGroup);
        }
    }

    private void onEntryBecomingChild(NotificationData.Entry entry) {
        if (entry.row.isHeadsUp()) {
            onHeadsUpStateChanged(entry, true);
        }
    }

    private void updateSuppression(NotificationGroup notificationGroup) {
        if (notificationGroup != null) {
            boolean z = notificationGroup.suppressed;
            notificationGroup.suppressed = shouldSuppressed(notificationGroup);
            boolean z2 = notificationGroup.suppressed;
            if (z != z2) {
                if (z2) {
                    handleSuppressedSummaryHeadsUpped(notificationGroup.summary);
                }
                if (!this.mIsUpdatingUnchangedGroup) {
                    this.mListener.onGroupsChanged();
                }
            }
        }
    }

    private boolean shouldSuppressed(NotificationGroup notificationGroup) {
        if (notificationGroup.summary == null || notificationGroup.expanded) {
            return false;
        }
        if (notificationGroup.children.size() == 1) {
            return true;
        }
        if ((notificationGroup.children.size() != 0 || !notificationGroup.summary.notification.getNotification().isGroupSummary() || (!hasIsolatedChildren(notificationGroup) && Constants.IS_INTERNATIONAL)) && !hasMediaOrCustomChildren(notificationGroup.children)) {
            return false;
        }
        return true;
    }

    private boolean hasMediaOrCustomChildren(Set<NotificationData.Entry> set) {
        return set.stream().filter($$Lambda$NotificationGroupManager$daLHCAj9OfzXKkD4bVk0C_kYg0.INSTANCE).count() > 0;
    }

    static /* synthetic */ boolean lambda$hasMediaOrCustomChildren$0(NotificationData.Entry entry) {
        return entry.isMediaNotification() || entry.isCustomViewNotification();
    }

    private boolean hasIsolatedChildren(NotificationGroup notificationGroup) {
        return getNumberOfIsolatedChildren(notificationGroup.summary.notification.getGroupKey()) != 0;
    }

    private int getNumberOfIsolatedChildren(String str) {
        return (int) this.mIsolatedEntries.values().stream().filter(new Predicate(str) {
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(Object obj) {
                return NotificationGroupManager.this.lambda$getNumberOfIsolatedChildren$1$NotificationGroupManager(this.f$1, (StatusBarNotification) obj);
            }
        }).count();
    }

    public /* synthetic */ boolean lambda$getNumberOfIsolatedChildren$1$NotificationGroupManager(String str, StatusBarNotification statusBarNotification) {
        return statusBarNotification.getGroupKey().equals(str) && isIsolated(statusBarNotification);
    }

    private NotificationData.Entry getIsolatedChild(String str) {
        for (StatusBarNotification next : this.mIsolatedEntries.values()) {
            if (next.getGroupKey().equals(str) && isIsolated(next)) {
                return this.mGroupMap.get(next.getKey()).summary;
            }
        }
        return null;
    }

    public void onEntryUpdated(NotificationData.Entry entry, StatusBarNotification statusBarNotification) {
        String groupKey = statusBarNotification.getGroupKey();
        String groupKey2 = entry.notification.getGroupKey();
        boolean z = true;
        boolean z2 = !groupKey.equals(groupKey2);
        boolean isGroupChild = isGroupChild(statusBarNotification);
        boolean isGroupChild2 = isGroupChild(entry.notification);
        if (z2 || isGroupChild != isGroupChild2) {
            z = false;
        }
        this.mIsUpdatingUnchangedGroup = z;
        if (this.mGroupMap.get(getGroupKey(statusBarNotification)) != null) {
            onEntryRemovedInternal(entry, statusBarNotification);
        }
        onEntryAdded(entry);
        this.mIsUpdatingUnchangedGroup = false;
        if (isIsolated(entry.notification)) {
            this.mIsolatedEntries.put(entry.key, entry.notification);
            if (z2) {
                updateSuppression(this.mGroupMap.get(groupKey));
                updateSuppression(this.mGroupMap.get(groupKey2));
            }
        } else if (!isGroupChild && isGroupChild2) {
            onEntryBecomingChild(entry);
        }
    }

    public boolean isSummaryHasChildren(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getNotification().isGroupSummary() && getTotalNumberOfChildren(statusBarNotification) > 0;
    }

    public boolean isSummaryOfSuppressedGroup(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getNotification().isGroupSummary() && isGroupSuppressed(getGroupKey(statusBarNotification));
    }

    private boolean isOnlyChild(StatusBarNotification statusBarNotification) {
        if (statusBarNotification.getNotification().isGroupSummary() || getTotalNumberOfChildren(statusBarNotification) != 1) {
            return false;
        }
        return true;
    }

    public boolean isOnlyChildInGroup(StatusBarNotification statusBarNotification) {
        ExpandableNotificationRow logicalGroupSummary;
        if (isOnlyChild(statusBarNotification) && (logicalGroupSummary = getLogicalGroupSummary(statusBarNotification)) != null && !logicalGroupSummary.getStatusBarNotification().equals(statusBarNotification)) {
            return true;
        }
        return false;
    }

    private int getTotalNumberOfChildren(StatusBarNotification statusBarNotification) {
        int numberOfIsolatedChildren = getNumberOfIsolatedChildren(statusBarNotification.getGroupKey());
        NotificationGroup notificationGroup = this.mGroupMap.get(statusBarNotification.getGroupKey());
        return numberOfIsolatedChildren + (notificationGroup != null ? notificationGroup.children.size() : 0);
    }

    private boolean isGroupSuppressed(String str) {
        NotificationGroup notificationGroup = this.mGroupMap.get(str);
        return notificationGroup != null && notificationGroup.suppressed;
    }

    public void setStatusBarState(int i) {
        if (this.mBarState != i) {
            this.mBarState = i;
            if (this.mBarState == 1) {
                collapseAllGroups();
            }
        }
    }

    public void collapseAllGroups() {
        ArrayList arrayList = new ArrayList(this.mGroupMap.values());
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            NotificationGroup notificationGroup = (NotificationGroup) arrayList.get(i);
            if (notificationGroup.expanded) {
                setGroupExpanded(notificationGroup, false);
            }
            updateSuppression(notificationGroup);
        }
    }

    public boolean isChildInGroupWithSummary(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup;
        if (isGroupChild(statusBarNotification) && (notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification))) != null && notificationGroup.summary != null && !notificationGroup.suppressed && !notificationGroup.children.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isSummaryOfGroup(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup;
        if (isGroupSummary(statusBarNotification) && (notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification))) != null) {
            return !notificationGroup.children.isEmpty();
        }
        return false;
    }

    public ExpandableNotificationRow getGroupSummary(StatusBarNotification statusBarNotification) {
        return getGroupSummary(getGroupKey(statusBarNotification));
    }

    public ExpandableNotificationRow getLogicalGroupSummary(StatusBarNotification statusBarNotification) {
        return getGroupSummary(statusBarNotification.getGroupKey());
    }

    private ExpandableNotificationRow getGroupSummary(String str) {
        NotificationData.Entry entry;
        NotificationGroup notificationGroup = this.mGroupMap.get(str);
        if (notificationGroup == null || (entry = notificationGroup.summary) == null) {
            return null;
        }
        return entry.row;
    }

    public NotificationGroup getNotificationGroup(String str) {
        return this.mGroupMap.get(str);
    }

    public boolean toggleGroupExpansion(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification));
        if (notificationGroup == null) {
            return false;
        }
        setGroupExpanded(notificationGroup, !notificationGroup.expanded);
        return notificationGroup.expanded;
    }

    private boolean isIsolated(StatusBarNotification statusBarNotification) {
        return this.mIsolatedEntries.containsKey(statusBarNotification.getKey());
    }

    private boolean isGroupSummary(StatusBarNotification statusBarNotification) {
        if (isIsolated(statusBarNotification)) {
            return true;
        }
        return statusBarNotification.getNotification().isGroupSummary();
    }

    private boolean isGroupChild(StatusBarNotification statusBarNotification) {
        if (!isIsolated(statusBarNotification) && StatusBarNotificationCompat.isGroup(statusBarNotification) && !statusBarNotification.getNotification().isGroupSummary()) {
            return true;
        }
        return false;
    }

    private String getGroupKey(StatusBarNotification statusBarNotification) {
        if (isIsolated(statusBarNotification)) {
            return statusBarNotification.getKey();
        }
        return statusBarNotification.getGroupKey();
    }

    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
        ExpandedNotification expandedNotification = entry.notification;
        if (entry.row.isHeadsUp()) {
            if (shouldIsolate(expandedNotification)) {
                onEntryRemovedInternal(entry, entry.notification);
                this.mIsolatedEntries.put(expandedNotification.getKey(), expandedNotification);
                onEntryAdded(entry);
                updateSuppression(this.mGroupMap.get(entry.notification.getGroupKey()));
                this.mListener.onGroupsChanged();
                return;
            }
            handleSuppressedSummaryHeadsUpped(entry);
        } else if (this.mIsolatedEntries.containsKey(expandedNotification.getKey())) {
            onEntryRemovedInternal(entry, entry.notification);
            this.mIsolatedEntries.remove(expandedNotification.getKey());
            onEntryAdded(entry);
            this.mListener.onGroupsChanged();
        }
    }

    private void handleSuppressedSummaryHeadsUpped(NotificationData.Entry entry) {
        ExpandedNotification expandedNotification = entry.notification;
        if (isGroupSuppressed(expandedNotification.getGroupKey()) && expandedNotification.getNotification().isGroupSummary() && entry.row.isHeadsUp()) {
            NotificationGroup notificationGroup = this.mGroupMap.get(expandedNotification.getGroupKey());
            if (notificationGroup != null) {
                Iterator<NotificationData.Entry> it = notificationGroup.children.iterator();
                NotificationData.Entry next = it.hasNext() ? it.next() : null;
                if (next == null) {
                    next = getIsolatedChild(expandedNotification.getGroupKey());
                }
                if (next != null) {
                    if (!next.row.keepInParent() && !next.row.isRemoved() && !next.row.isDismissed()) {
                        if (this.mHeadsUpManager.isHeadsUp(next.key)) {
                            this.mHeadsUpManager.updateNotification(next, true);
                        } else {
                            this.mHeadsUpManager.showNotification(next);
                        }
                    } else {
                        return;
                    }
                }
            }
            this.mHeadsUpManager.releaseImmediately(entry.key);
        }
    }

    private boolean shouldIsolate(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(statusBarNotification.getGroupKey());
        return StatusBarNotificationCompat.isGroup(statusBarNotification) && !statusBarNotification.getNotification().isGroupSummary() && (statusBarNotification.getNotification().fullScreenIntent != null || notificationGroup == null || !notificationGroup.expanded || isGroupNotFullyVisible(notificationGroup));
    }

    private boolean isGroupNotFullyVisible(NotificationGroup notificationGroup) {
        NotificationData.Entry entry = notificationGroup.summary;
        return entry == null || entry.row.getClipTopAmount() > 0 || notificationGroup.summary.row.getTranslationY() < 0.0f;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("GroupManager state:");
        printWriter.println("  number of groups: " + this.mGroupMap.size());
        for (Map.Entry next : this.mGroupMap.entrySet()) {
            printWriter.println("\n    key: " + ((String) next.getKey()));
            printWriter.println(next.getValue());
        }
        printWriter.println("\n    isolated entries: " + this.mIsolatedEntries.size());
        for (Map.Entry next2 : this.mIsolatedEntries.entrySet()) {
            printWriter.print("      ");
            printWriter.print((String) next2.getKey());
            printWriter.print(", ");
            printWriter.println(next2.getValue());
        }
    }

    public static class NotificationGroup {
        public final HashSet<NotificationData.Entry> children = new HashSet<>();
        public boolean expanded;
        public NotificationData.Entry summary;
        public boolean suppressed;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("    summary expanded=");
            sb.append(this.expanded);
            sb.append(" suppressed=");
            sb.append(this.suppressed);
            sb.append(":\n      ");
            NotificationData.Entry entry = this.summary;
            sb.append(entry != null ? entry.notification : "null");
            String str = sb.toString() + "\n    children size: " + this.children.size();
            Iterator<NotificationData.Entry> it = this.children.iterator();
            while (it.hasNext()) {
                str = str + "\n      " + it.next().notification;
            }
            return str;
        }
    }
}
