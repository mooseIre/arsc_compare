package com.android.systemui.statusbar.phone;

import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class NotificationGroupManager implements OnHeadsUpChangedListener, StatusBarStateController.StateListener {
    private int mBarState = -1;
    private BubbleController mBubbleController = null;
    private final HashMap<String, NotificationGroup> mGroupMap = new HashMap<>();
    private HeadsUpManager mHeadsUpManager;
    private boolean mIsUpdatingUnchangedGroup;
    private HashMap<String, StatusBarNotification> mIsolatedEntries = new HashMap<>();
    private final ArraySet<OnGroupChangeListener> mListeners = new ArraySet<>();
    private final Lazy<PeopleNotificationIdentifier> mPeopleNotificationIdentifier;

    public interface OnGroupChangeListener {
        default void onGroupCreated(NotificationGroup notificationGroup, String str) {
        }

        default void onGroupCreatedFromChildren(NotificationGroup notificationGroup) {
        }

        default void onGroupExpansionChanged(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        }

        default void onGroupRemoved(NotificationGroup notificationGroup, String str) {
        }

        default void onGroupSuppressionChanged(NotificationGroup notificationGroup, boolean z) {
        }

        default void onGroupsChanged() {
        }
    }

    public NotificationGroupManager(StatusBarStateController statusBarStateController, Lazy<PeopleNotificationIdentifier> lazy) {
        statusBarStateController.addCallback(this);
        this.mPeopleNotificationIdentifier = lazy;
    }

    private BubbleController getBubbleController() {
        if (this.mBubbleController == null) {
            this.mBubbleController = (BubbleController) Dependency.get(BubbleController.class);
        }
        return this.mBubbleController;
    }

    public void addOnGroupChangeListener(OnGroupChangeListener onGroupChangeListener) {
        this.mListeners.add(onGroupChangeListener);
    }

    public boolean isGroupExpanded(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification));
        if (notificationGroup == null) {
            return false;
        }
        return notificationGroup.expanded;
    }

    public boolean isLogicalGroupExpanded(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(statusBarNotification.getGroupKey());
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
        if (notificationGroup.summary != null) {
            Iterator<OnGroupChangeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onGroupExpansionChanged(notificationGroup.summary.getRow(), z);
            }
        }
    }

    public void onEntryRemoved(NotificationEntry notificationEntry) {
        onEntryRemovedInternal(notificationEntry, notificationEntry.getSbn());
        this.mIsolatedEntries.remove(notificationEntry.getKey());
    }

    private void onEntryRemovedInternal(NotificationEntry notificationEntry, StatusBarNotification statusBarNotification) {
        onEntryRemovedInternal(notificationEntry, statusBarNotification.getGroupKey(), statusBarNotification.isGroup(), statusBarNotification.getNotification().isGroupSummary());
    }

    private void onEntryRemovedInternal(NotificationEntry notificationEntry, String str, boolean z, boolean z2) {
        String groupKey = getGroupKey(notificationEntry.getKey(), str);
        NotificationGroup notificationGroup = this.mGroupMap.get(groupKey);
        if (notificationGroup != null) {
            if (isGroupChild(notificationEntry.getKey(), z, z2)) {
                notificationGroup.children.remove(notificationEntry.getKey());
            } else {
                notificationGroup.summary = null;
            }
            updateSuppression(notificationGroup);
            if (notificationGroup.children.isEmpty() && notificationGroup.summary == null) {
                this.mGroupMap.remove(groupKey);
                Iterator<OnGroupChangeListener> it = this.mListeners.iterator();
                while (it.hasNext()) {
                    it.next().onGroupRemoved(notificationGroup, groupKey);
                }
            }
        }
    }

    public void onEntryAdded(NotificationEntry notificationEntry) {
        updateIsolation(notificationEntry);
        onEntryAddedInternal(notificationEntry);
    }

    private void onEntryAddedInternal(NotificationEntry notificationEntry) {
        String str;
        if (notificationEntry.isRowRemoved()) {
            notificationEntry.setDebugThrowable(new Throwable());
        }
        ExpandedNotification sbn = notificationEntry.getSbn();
        boolean isGroupChild = isGroupChild(sbn);
        String groupKey = getGroupKey(sbn);
        NotificationGroup notificationGroup = this.mGroupMap.get(groupKey);
        if (notificationGroup == null) {
            notificationGroup = new NotificationGroup();
            this.mGroupMap.put(groupKey, notificationGroup);
            Iterator<OnGroupChangeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onGroupCreated(notificationGroup, groupKey);
            }
        }
        if (isGroupChild) {
            NotificationEntry notificationEntry2 = notificationGroup.children.get(notificationEntry.getKey());
            if (!(notificationEntry2 == null || notificationEntry2 == notificationEntry)) {
                Throwable debugThrowable = notificationEntry2.getDebugThrowable();
                StringBuilder sb = new StringBuilder();
                sb.append("Inconsistent entries found with the same key ");
                sb.append(notificationEntry.getKey());
                sb.append("existing removed: ");
                sb.append(notificationEntry2.isRowRemoved());
                if (debugThrowable != null) {
                    str = Log.getStackTraceString(debugThrowable) + "\n";
                } else {
                    str = "";
                }
                sb.append(str);
                sb.append(" added removed");
                sb.append(notificationEntry.isRowRemoved());
                Log.wtf("NotificationGroupManager", sb.toString(), new Throwable());
            }
            notificationGroup.children.put(notificationEntry.getKey(), notificationEntry);
            updateSuppression(notificationGroup);
            return;
        }
        notificationGroup.summary = notificationEntry;
        notificationGroup.expanded = notificationEntry.areChildrenExpanded();
        updateSuppression(notificationGroup);
        if (!notificationGroup.children.isEmpty()) {
            Iterator it2 = new ArrayList(notificationGroup.children.values()).iterator();
            while (it2.hasNext()) {
                onEntryBecomingChild((NotificationEntry) it2.next());
            }
            Iterator<OnGroupChangeListener> it3 = this.mListeners.iterator();
            while (it3.hasNext()) {
                it3.next().onGroupCreatedFromChildren(notificationGroup);
            }
        }
    }

    private void onEntryBecomingChild(NotificationEntry notificationEntry) {
        updateIsolation(notificationEntry);
    }

    private void updateSuppression(NotificationGroup notificationGroup) {
        if (notificationGroup != null) {
            boolean z = false;
            int i = 0;
            boolean z2 = false;
            for (NotificationEntry notificationEntry : notificationGroup.children.values()) {
                if (!getBubbleController().isBubbleNotificationSuppressedFromShade(notificationEntry)) {
                    i++;
                } else {
                    z2 = true;
                }
            }
            boolean z3 = notificationGroup.suppressed;
            NotificationEntry notificationEntry2 = notificationGroup.summary;
            if (notificationEntry2 != null && !notificationGroup.expanded && (i == 1 || (i == 0 && notificationEntry2.getSbn().getNotification().isGroupSummary() && (hasIsolatedChildren(notificationGroup) || z2)))) {
                z = true;
            }
            notificationGroup.suppressed = z;
            boolean shouldSuppressed = z | NotificationGroupManagerInjectorKt.shouldSuppressed(notificationGroup, i);
            notificationGroup.suppressed = shouldSuppressed;
            if (z3 != shouldSuppressed) {
                Iterator<OnGroupChangeListener> it = this.mListeners.iterator();
                while (it.hasNext()) {
                    OnGroupChangeListener next = it.next();
                    if (!this.mIsUpdatingUnchangedGroup) {
                        next.onGroupSuppressionChanged(notificationGroup, notificationGroup.suppressed);
                        next.onGroupsChanged();
                    }
                }
            }
        }
    }

    private boolean hasIsolatedChildren(NotificationGroup notificationGroup) {
        return getNumberOfIsolatedChildren(notificationGroup.summary.getSbn().getGroupKey()) != 0;
    }

    private int getNumberOfIsolatedChildren(String str) {
        int i = 0;
        for (StatusBarNotification statusBarNotification : this.mIsolatedEntries.values()) {
            if (statusBarNotification.getGroupKey().equals(str) && isIsolated(statusBarNotification.getKey())) {
                i++;
            }
        }
        return i;
    }

    public void onEntryUpdated(NotificationEntry notificationEntry, StatusBarNotification statusBarNotification) {
        onEntryUpdated(notificationEntry, statusBarNotification.getGroupKey(), statusBarNotification.isGroup(), statusBarNotification.getNotification().isGroupSummary());
    }

    public void onEntryUpdated(NotificationEntry notificationEntry, String str, boolean z, boolean z2) {
        String groupKey = notificationEntry.getSbn().getGroupKey();
        boolean z3 = true;
        boolean z4 = !str.equals(groupKey);
        boolean isGroupChild = isGroupChild(notificationEntry.getKey(), z, z2);
        boolean isGroupChild2 = isGroupChild(notificationEntry.getSbn());
        if (z4 || isGroupChild != isGroupChild2) {
            z3 = false;
        }
        this.mIsUpdatingUnchangedGroup = z3;
        if (this.mGroupMap.get(getGroupKey(notificationEntry.getKey(), str)) != null) {
            onEntryRemovedInternal(notificationEntry, str, z, z2);
        }
        onEntryAddedInternal(notificationEntry);
        this.mIsUpdatingUnchangedGroup = false;
        if (isIsolated(notificationEntry.getSbn().getKey())) {
            this.mIsolatedEntries.put(notificationEntry.getKey(), notificationEntry.getSbn());
            if (z4) {
                updateSuppression(this.mGroupMap.get(str));
                updateSuppression(this.mGroupMap.get(groupKey));
            }
        } else if (!isGroupChild && isGroupChild2) {
            onEntryBecomingChild(notificationEntry);
        }
    }

    public boolean isSummaryOfSuppressedGroup(StatusBarNotification statusBarNotification) {
        return isGroupSuppressed(getGroupKey(statusBarNotification)) && statusBarNotification.getNotification().isGroupSummary();
    }

    private boolean isOnlyChild(StatusBarNotification statusBarNotification) {
        if (statusBarNotification.getNotification().isGroupSummary() || getTotalNumberOfChildren(statusBarNotification) != 1) {
            return false;
        }
        return true;
    }

    public boolean isOnlyChildInGroup(StatusBarNotification statusBarNotification) {
        NotificationEntry logicalGroupSummary;
        if (isOnlyChild(statusBarNotification) && (logicalGroupSummary = getLogicalGroupSummary(statusBarNotification)) != null && !logicalGroupSummary.getSbn().equals(statusBarNotification)) {
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

    private void setStatusBarState(int i) {
        this.mBarState = i;
        if (i == 1) {
            collapseAllGroups();
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
        if (isGroupSummary(statusBarNotification) && (notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification))) != null && notificationGroup.summary != null && !notificationGroup.children.isEmpty() && Objects.equals(notificationGroup.summary.getSbn(), statusBarNotification)) {
            return true;
        }
        return false;
    }

    public NotificationEntry getGroupSummary(StatusBarNotification statusBarNotification) {
        return getGroupSummary(getGroupKey(statusBarNotification));
    }

    public NotificationEntry getLogicalGroupSummary(StatusBarNotification statusBarNotification) {
        return getGroupSummary(statusBarNotification.getGroupKey());
    }

    private NotificationEntry getGroupSummary(String str) {
        NotificationGroup notificationGroup = this.mGroupMap.get(str);
        if (notificationGroup == null) {
            return null;
        }
        return notificationGroup.summary;
    }

    public ArrayList<NotificationEntry> getLogicalChildren(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(statusBarNotification.getGroupKey());
        if (notificationGroup == null) {
            return null;
        }
        ArrayList<NotificationEntry> arrayList = new ArrayList<>(notificationGroup.children.values());
        for (StatusBarNotification statusBarNotification2 : this.mIsolatedEntries.values()) {
            if (statusBarNotification2.getGroupKey().equals(statusBarNotification.getGroupKey())) {
                arrayList.add(this.mGroupMap.get(statusBarNotification2.getKey()).summary);
            }
        }
        return arrayList;
    }

    public ArrayList<NotificationEntry> getChildren(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(statusBarNotification.getGroupKey());
        if (notificationGroup == null) {
            return null;
        }
        return new ArrayList<>(notificationGroup.children.values());
    }

    public void updateSuppression(NotificationEntry notificationEntry) {
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(notificationEntry.getSbn()));
        if (notificationGroup != null) {
            updateSuppression(notificationGroup);
        }
    }

    public String getGroupKey(StatusBarNotification statusBarNotification) {
        return getGroupKey(statusBarNotification.getKey(), statusBarNotification.getGroupKey());
    }

    private String getGroupKey(String str, String str2) {
        return isIsolated(str) ? str : str2;
    }

    public boolean toggleGroupExpansion(StatusBarNotification statusBarNotification) {
        NotificationGroup notificationGroup = this.mGroupMap.get(getGroupKey(statusBarNotification));
        if (notificationGroup == null) {
            return false;
        }
        setGroupExpanded(notificationGroup, !notificationGroup.expanded);
        return notificationGroup.expanded;
    }

    private boolean isIsolated(String str) {
        return this.mIsolatedEntries.containsKey(str);
    }

    public boolean isGroupSummary(StatusBarNotification statusBarNotification) {
        if (isIsolated(statusBarNotification.getKey())) {
            return true;
        }
        return statusBarNotification.getNotification().isGroupSummary();
    }

    public boolean isGroupChild(StatusBarNotification statusBarNotification) {
        return isGroupChild(statusBarNotification.getKey(), statusBarNotification.isGroup(), statusBarNotification.getNotification().isGroupSummary());
    }

    private boolean isGroupChild(String str, boolean z, boolean z2) {
        return !isIsolated(str) && z && !z2;
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        updateIsolation(notificationEntry);
    }

    private boolean shouldIsolate(NotificationEntry notificationEntry) {
        ExpandedNotification sbn = notificationEntry.getSbn();
        if (!sbn.isGroup() || sbn.getNotification().isGroupSummary()) {
            return false;
        }
        if (this.mPeopleNotificationIdentifier.get().getPeopleNotificationType(notificationEntry.getSbn(), notificationEntry.getRanking()) == 3) {
            return true;
        }
        HeadsUpManager headsUpManager = this.mHeadsUpManager;
        if (headsUpManager != null && !headsUpManager.isAlerting(notificationEntry.getKey())) {
            return false;
        }
        NotificationGroup notificationGroup = this.mGroupMap.get(sbn.getGroupKey());
        if (sbn.getNotification().fullScreenIntent != null || notificationGroup == null || !notificationGroup.expanded || isGroupNotFullyVisible(notificationGroup)) {
            return true;
        }
        return false;
    }

    private void isolateNotification(NotificationEntry notificationEntry) {
        ExpandedNotification sbn = notificationEntry.getSbn();
        onEntryRemovedInternal(notificationEntry, notificationEntry.getSbn());
        this.mIsolatedEntries.put(sbn.getKey(), sbn);
        onEntryAddedInternal(notificationEntry);
        updateSuppression(this.mGroupMap.get(notificationEntry.getSbn().getGroupKey()));
        Iterator<OnGroupChangeListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onGroupsChanged();
        }
    }

    public void updateIsolation(NotificationEntry notificationEntry) {
        boolean isIsolated = isIsolated(notificationEntry.getSbn().getKey());
        if (shouldIsolate(notificationEntry)) {
            if (!isIsolated) {
                isolateNotification(notificationEntry);
            }
        } else if (isIsolated) {
            stopIsolatingNotification(notificationEntry);
        }
    }

    private void stopIsolatingNotification(NotificationEntry notificationEntry) {
        ExpandedNotification sbn = notificationEntry.getSbn();
        if (isIsolated(sbn.getKey())) {
            onEntryRemovedInternal(notificationEntry, notificationEntry.getSbn());
            this.mIsolatedEntries.remove(sbn.getKey());
            onEntryAddedInternal(notificationEntry);
            Iterator<OnGroupChangeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onGroupsChanged();
            }
        }
    }

    private boolean isGroupNotFullyVisible(NotificationGroup notificationGroup) {
        NotificationEntry notificationEntry = notificationGroup.summary;
        return notificationEntry == null || notificationEntry.isGroupNotFullyVisible();
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("GroupManager state:");
        printWriter.println("  number of groups: " + this.mGroupMap.size());
        for (Map.Entry<String, NotificationGroup> entry : this.mGroupMap.entrySet()) {
            printWriter.println("\n    key: " + entry.getKey());
            printWriter.println(entry.getValue());
        }
        printWriter.println("\n    isolated entries: " + this.mIsolatedEntries.size());
        for (Map.Entry<String, StatusBarNotification> entry2 : this.mIsolatedEntries.entrySet()) {
            printWriter.print("      ");
            printWriter.print(entry2.getKey());
            printWriter.print(", ");
            printWriter.println(entry2.getValue());
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        setStatusBarState(i);
    }

    public static class NotificationGroup {
        public final HashMap<String, NotificationEntry> children = new HashMap<>();
        public boolean expanded;
        public NotificationEntry summary;
        public boolean suppressed;

        public String toString() {
            String str;
            String str2;
            StringBuilder sb = new StringBuilder();
            sb.append("    summary:\n      ");
            NotificationEntry notificationEntry = this.summary;
            sb.append(notificationEntry != null ? notificationEntry.getSbn() : "null");
            NotificationEntry notificationEntry2 = this.summary;
            if (notificationEntry2 == null || notificationEntry2.getDebugThrowable() == null) {
                str = "";
            } else {
                str = Log.getStackTraceString(this.summary.getDebugThrowable());
            }
            sb.append(str);
            String str3 = sb.toString() + "\n    children size: " + this.children.size();
            for (NotificationEntry notificationEntry3 : this.children.values()) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str3);
                sb2.append("\n      ");
                sb2.append(notificationEntry3.getSbn());
                if (notificationEntry3.getDebugThrowable() != null) {
                    str2 = Log.getStackTraceString(notificationEntry3.getDebugThrowable());
                } else {
                    str2 = "";
                }
                sb2.append(str2);
                str3 = sb2.toString();
            }
            return str3 + "\n    summary suppressed: " + this.suppressed;
        }
    }
}
