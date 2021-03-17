package com.android.systemui.statusbar.phone;

import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.AlertingNotificationManager;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.RowContentBindParams;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.ArrayList;
import java.util.Objects;

public class NotificationGroupAlertTransferHelper implements OnHeadsUpChangedListener, StatusBarStateController.StateListener {
    private NotificationEntryManager mEntryManager;
    private final ArrayMap<String, GroupAlertEntry> mGroupAlertEntries = new ArrayMap<>();
    private final NotificationGroupManager mGroupManager = ((NotificationGroupManager) Dependency.get(NotificationGroupManager.class));
    private HeadsUpManager mHeadsUpManager;
    private boolean mIsDozing;
    private final NotificationEntryListener mNotificationEntryListener = new NotificationEntryListener() {
        /* class com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onPendingEntryAdded(NotificationEntry notificationEntry) {
            GroupAlertEntry groupAlertEntry = (GroupAlertEntry) NotificationGroupAlertTransferHelper.this.mGroupAlertEntries.get(NotificationGroupAlertTransferHelper.this.mGroupManager.getGroupKey(notificationEntry.getSbn()));
            if (groupAlertEntry != null) {
                NotificationGroupAlertTransferHelper.this.checkShouldTransferBack(groupAlertEntry);
            }
        }

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
            NotificationGroupAlertTransferHelper.this.mPendingAlerts.remove(notificationEntry.getKey());
        }
    };
    private final NotificationGroupManager.OnGroupChangeListener mOnGroupChangeListener = new NotificationGroupManager.OnGroupChangeListener() {
        /* class com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
        public void onGroupCreated(NotificationGroupManager.NotificationGroup notificationGroup, String str) {
            NotificationGroupAlertTransferHelper.this.mGroupAlertEntries.put(str, new GroupAlertEntry(notificationGroup));
        }

        @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
        public void onGroupRemoved(NotificationGroupManager.NotificationGroup notificationGroup, String str) {
            NotificationGroupAlertTransferHelper.this.mGroupAlertEntries.remove(str);
        }

        @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
        public void onGroupSuppressionChanged(NotificationGroupManager.NotificationGroup notificationGroup, boolean z) {
            if (z) {
                if (NotificationGroupAlertTransferHelper.this.mHeadsUpManager.isAlerting(notificationGroup.summary.getKey())) {
                    NotificationGroupAlertTransferHelper notificationGroupAlertTransferHelper = NotificationGroupAlertTransferHelper.this;
                    notificationGroupAlertTransferHelper.handleSuppressedSummaryAlerted(notificationGroup.summary, notificationGroupAlertTransferHelper.mHeadsUpManager);
                }
            } else if (notificationGroup.summary != null) {
                GroupAlertEntry groupAlertEntry = (GroupAlertEntry) NotificationGroupAlertTransferHelper.this.mGroupAlertEntries.get(NotificationGroupAlertTransferHelper.this.mGroupManager.getGroupKey(notificationGroup.summary.getSbn()));
                if (groupAlertEntry.mAlertSummaryOnNextAddition) {
                    if (!NotificationGroupAlertTransferHelper.this.mHeadsUpManager.isAlerting(notificationGroup.summary.getKey())) {
                        NotificationGroupAlertTransferHelper notificationGroupAlertTransferHelper2 = NotificationGroupAlertTransferHelper.this;
                        notificationGroupAlertTransferHelper2.alertNotificationWhenPossible(notificationGroup.summary, notificationGroupAlertTransferHelper2.mHeadsUpManager);
                    }
                    groupAlertEntry.mAlertSummaryOnNextAddition = false;
                    return;
                }
                NotificationGroupAlertTransferHelper.this.checkShouldTransferBack(groupAlertEntry);
            }
        }
    };
    private final ArrayMap<String, PendingAlertInfo> mPendingAlerts = new ArrayMap<>();
    private final RowContentBindStage mRowContentBindStage;

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    public NotificationGroupAlertTransferHelper(RowContentBindStage rowContentBindStage) {
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this);
        this.mRowContentBindStage = rowContentBindStage;
    }

    public void bind(NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager) {
        if (this.mEntryManager == null) {
            this.mEntryManager = notificationEntryManager;
            notificationEntryManager.addNotificationEntryListener(this.mNotificationEntryListener);
            notificationGroupManager.addOnGroupChangeListener(this.mOnGroupChangeListener);
            return;
        }
        throw new IllegalStateException("Already bound.");
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        if (this.mIsDozing != z) {
            for (GroupAlertEntry groupAlertEntry : this.mGroupAlertEntries.values()) {
                groupAlertEntry.mLastAlertTransferTime = 0;
                groupAlertEntry.mAlertSummaryOnNextAddition = false;
            }
        }
        this.mIsDozing = z;
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        onAlertStateChanged(notificationEntry, z, this.mHeadsUpManager);
    }

    private void onAlertStateChanged(NotificationEntry notificationEntry, boolean z, AlertingNotificationManager alertingNotificationManager) {
        if (z && this.mGroupManager.isSummaryOfSuppressedGroup(notificationEntry.getSbn())) {
            handleSuppressedSummaryAlerted(notificationEntry, alertingNotificationManager);
        }
    }

    private int getPendingChildrenNotAlerting(NotificationGroupManager.NotificationGroup notificationGroup) {
        NotificationEntryManager notificationEntryManager = this.mEntryManager;
        int i = 0;
        if (notificationEntryManager == null) {
            return 0;
        }
        for (NotificationEntry notificationEntry : notificationEntryManager.getPendingNotificationsIterator()) {
            if (isPendingNotificationInGroup(notificationEntry, notificationGroup) && onlySummaryAlerts(notificationEntry)) {
                i++;
            }
        }
        return i;
    }

    private boolean pendingInflationsWillAddChildren(NotificationGroupManager.NotificationGroup notificationGroup) {
        NotificationEntryManager notificationEntryManager = this.mEntryManager;
        if (notificationEntryManager == null) {
            return false;
        }
        for (NotificationEntry notificationEntry : notificationEntryManager.getPendingNotificationsIterator()) {
            if (isPendingNotificationInGroup(notificationEntry, notificationGroup)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPendingNotificationInGroup(NotificationEntry notificationEntry, NotificationGroupManager.NotificationGroup notificationGroup) {
        return this.mGroupManager.isGroupChild(notificationEntry.getSbn()) && Objects.equals(this.mGroupManager.getGroupKey(notificationEntry.getSbn()), this.mGroupManager.getGroupKey(notificationGroup.summary.getSbn())) && !notificationGroup.children.containsKey(notificationEntry.getKey());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSuppressedSummaryAlerted(NotificationEntry notificationEntry, AlertingNotificationManager alertingNotificationManager) {
        ArrayList<NotificationEntry> logicalChildren;
        NotificationEntry next;
        ExpandedNotification sbn = notificationEntry.getSbn();
        GroupAlertEntry groupAlertEntry = this.mGroupAlertEntries.get(this.mGroupManager.getGroupKey(sbn));
        if (this.mGroupManager.isSummaryOfSuppressedGroup(notificationEntry.getSbn()) && alertingNotificationManager.isAlerting(sbn.getKey()) && groupAlertEntry != null && !pendingInflationsWillAddChildren(groupAlertEntry.mGroup) && (logicalChildren = this.mGroupManager.getLogicalChildren(notificationEntry.getSbn())) != null && !logicalChildren.isEmpty() && (next = logicalChildren.iterator().next()) != null && !next.getRow().keepInParent() && !next.isRowRemoved() && !next.isRowDismissed()) {
            if (!alertingNotificationManager.isAlerting(next.getKey()) && onlySummaryAlerts(notificationEntry)) {
                groupAlertEntry.mLastAlertTransferTime = SystemClock.elapsedRealtime();
            }
            transferAlertState(notificationEntry, next, alertingNotificationManager);
        }
    }

    private void transferAlertState(NotificationEntry notificationEntry, NotificationEntry notificationEntry2, AlertingNotificationManager alertingNotificationManager) {
        alertingNotificationManager.removeNotification(notificationEntry.getKey(), true);
        alertNotificationWhenPossible(notificationEntry2, alertingNotificationManager);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkShouldTransferBack(GroupAlertEntry groupAlertEntry) {
        ArrayList<NotificationEntry> logicalChildren;
        int pendingChildrenNotAlerting;
        int size;
        if (SystemClock.elapsedRealtime() - groupAlertEntry.mLastAlertTransferTime < 300) {
            NotificationEntry notificationEntry = groupAlertEntry.mGroup.summary;
            if (onlySummaryAlerts(notificationEntry) && (size = (logicalChildren = this.mGroupManager.getLogicalChildren(notificationEntry.getSbn())).size() + (pendingChildrenNotAlerting = getPendingChildrenNotAlerting(groupAlertEntry.mGroup))) > 1) {
                boolean z = false;
                boolean z2 = false;
                for (int i = 0; i < logicalChildren.size(); i++) {
                    NotificationEntry notificationEntry2 = logicalChildren.get(i);
                    if (onlySummaryAlerts(notificationEntry2) && this.mHeadsUpManager.isAlerting(notificationEntry2.getKey())) {
                        this.mHeadsUpManager.removeNotification(notificationEntry2.getKey(), true);
                        z2 = true;
                    }
                    if (this.mPendingAlerts.containsKey(notificationEntry2.getKey())) {
                        this.mPendingAlerts.get(notificationEntry2.getKey()).mAbortOnInflation = true;
                        z2 = true;
                    }
                }
                if (z2 && !this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
                    if (size - pendingChildrenNotAlerting > 1) {
                        z = true;
                    }
                    if (z) {
                        alertNotificationWhenPossible(notificationEntry, this.mHeadsUpManager);
                    } else {
                        groupAlertEntry.mAlertSummaryOnNextAddition = true;
                    }
                    groupAlertEntry.mLastAlertTransferTime = 0;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void alertNotificationWhenPossible(NotificationEntry notificationEntry, AlertingNotificationManager alertingNotificationManager) {
        int contentFlag = alertingNotificationManager.getContentFlag();
        RowContentBindParams rowContentBindParams = (RowContentBindParams) this.mRowContentBindStage.getStageParams(notificationEntry);
        if ((rowContentBindParams.getContentViews() & contentFlag) == 0) {
            this.mPendingAlerts.put(notificationEntry.getKey(), new PendingAlertInfo(this, notificationEntry));
            rowContentBindParams.requireContentViews(contentFlag);
            this.mRowContentBindStage.requestRebind(notificationEntry, new NotifBindPipeline.BindCallback(notificationEntry, contentFlag) {
                /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationGroupAlertTransferHelper$eMYMUXNB2yOw4q9wL9gYe0M0Ark */
                public final /* synthetic */ NotificationEntry f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                public final void onBindFinished(NotificationEntry notificationEntry) {
                    NotificationGroupAlertTransferHelper.this.lambda$alertNotificationWhenPossible$0$NotificationGroupAlertTransferHelper(this.f$1, this.f$2, notificationEntry);
                }
            });
        } else if (alertingNotificationManager.isAlerting(notificationEntry.getKey())) {
            alertingNotificationManager.updateNotification(notificationEntry.getKey(), true);
        } else {
            alertingNotificationManager.showNotification(notificationEntry);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$alertNotificationWhenPossible$0 */
    public /* synthetic */ void lambda$alertNotificationWhenPossible$0$NotificationGroupAlertTransferHelper(NotificationEntry notificationEntry, int i, NotificationEntry notificationEntry2) {
        PendingAlertInfo remove = this.mPendingAlerts.remove(notificationEntry.getKey());
        if (remove == null) {
            return;
        }
        if (remove.isStillValid()) {
            alertNotificationWhenPossible(notificationEntry, this.mHeadsUpManager);
            return;
        }
        ((RowContentBindParams) this.mRowContentBindStage.getStageParams(notificationEntry)).markContentViewsFreeable(i);
        this.mRowContentBindStage.requestRebind(notificationEntry, null);
    }

    private boolean onlySummaryAlerts(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getNotification().getGroupAlertBehavior() == 1;
    }

    /* access modifiers changed from: private */
    public class PendingAlertInfo {
        boolean mAbortOnInflation;
        final NotificationEntry mEntry;
        final StatusBarNotification mOriginalNotification;

        PendingAlertInfo(NotificationGroupAlertTransferHelper notificationGroupAlertTransferHelper, NotificationEntry notificationEntry) {
            this.mOriginalNotification = notificationEntry.getSbn();
            this.mEntry = notificationEntry;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isStillValid() {
            if (!this.mAbortOnInflation && this.mEntry.getSbn().getGroupKey() == this.mOriginalNotification.getGroupKey() && this.mEntry.getSbn().getNotification().isGroupSummary() == this.mOriginalNotification.getNotification().isGroupSummary()) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class GroupAlertEntry {
        boolean mAlertSummaryOnNextAddition;
        final NotificationGroupManager.NotificationGroup mGroup;
        long mLastAlertTransferTime;

        GroupAlertEntry(NotificationGroupManager.NotificationGroup notificationGroup) {
            this.mGroup = notificationGroup;
        }
    }
}
