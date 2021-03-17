package com.android.systemui.statusbar.notification.collection.coordinator;

import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.GroupEntry;
import com.android.systemui.statusbar.notification.collection.ListEntry;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotifViewBarn;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.inflation.NotifInflater;
import com.android.systemui.statusbar.notification.collection.listbuilder.OnBeforeFinalizeFilterListener;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PreparationCoordinator implements Coordinator {
    private final int mChildBindCutoff;
    /* access modifiers changed from: private */
    public final Set<NotificationEntry> mInflatingNotifs;
    private final NotifInflationErrorManager.NotifInflationErrorListener mInflationErrorListener;
    /* access modifiers changed from: private */
    public final Map<NotificationEntry, Integer> mInflationStates;
    private final PreparationCoordinatorLogger mLogger;
    private final NotifCollectionListener mNotifCollectionListener;
    private final NotifInflationErrorManager mNotifErrorManager;
    private final NotifInflater mNotifInflater;
    private final NotifFilter mNotifInflatingFilter;
    /* access modifiers changed from: private */
    public final NotifFilter mNotifInflationErrorFilter;
    private final OnBeforeFinalizeFilterListener mOnBeforeFinalizeFilterListener;
    /* access modifiers changed from: private */
    public final IStatusBarService mStatusBarService;
    /* access modifiers changed from: private */
    public final NotifViewBarn mViewBarn;

    public PreparationCoordinator(PreparationCoordinatorLogger preparationCoordinatorLogger, NotifInflaterImpl notifInflaterImpl, NotifInflationErrorManager notifInflationErrorManager, NotifViewBarn notifViewBarn, IStatusBarService iStatusBarService) {
        this(preparationCoordinatorLogger, notifInflaterImpl, notifInflationErrorManager, notifViewBarn, iStatusBarService, 9);
    }

    @VisibleForTesting
    PreparationCoordinator(PreparationCoordinatorLogger preparationCoordinatorLogger, NotifInflaterImpl notifInflaterImpl, NotifInflationErrorManager notifInflationErrorManager, NotifViewBarn notifViewBarn, IStatusBarService iStatusBarService, int i) {
        this.mInflationStates = new ArrayMap();
        this.mInflatingNotifs = new ArraySet();
        this.mNotifCollectionListener = new NotifCollectionListener() {
            public void onEntryInit(NotificationEntry notificationEntry) {
                PreparationCoordinator.this.mInflationStates.put(notificationEntry, 0);
            }

            public void onEntryUpdated(NotificationEntry notificationEntry) {
                PreparationCoordinator.this.abortInflation(notificationEntry, "entryUpdated");
                PreparationCoordinator.this.mInflatingNotifs.remove(notificationEntry);
                int access$300 = PreparationCoordinator.this.getInflationState(notificationEntry);
                if (access$300 == 1) {
                    PreparationCoordinator.this.mInflationStates.put(notificationEntry, 2);
                } else if (access$300 == -1) {
                    PreparationCoordinator.this.mInflationStates.put(notificationEntry, 0);
                }
            }

            public void onEntryRemoved(NotificationEntry notificationEntry, int i) {
                PreparationCoordinator preparationCoordinator = PreparationCoordinator.this;
                preparationCoordinator.abortInflation(notificationEntry, "entryRemoved reason=" + i);
            }

            public void onEntryCleanUp(NotificationEntry notificationEntry) {
                PreparationCoordinator.this.mInflationStates.remove(notificationEntry);
                PreparationCoordinator.this.mInflatingNotifs.remove(notificationEntry);
                PreparationCoordinator.this.mViewBarn.removeViewForEntry(notificationEntry);
            }
        };
        this.mOnBeforeFinalizeFilterListener = new OnBeforeFinalizeFilterListener() {
            public final void onBeforeFinalizeFilter(List list) {
                PreparationCoordinator.this.lambda$new$0$PreparationCoordinator(list);
            }
        };
        this.mNotifInflationErrorFilter = new NotifFilter("PreparationCoordinatorInflationError") {
            public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
                return PreparationCoordinator.this.getInflationState(notificationEntry) == -1;
            }
        };
        this.mNotifInflatingFilter = new NotifFilter("PreparationCoordinatorInflating") {
            public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
                return !PreparationCoordinator.this.isInflated(notificationEntry);
            }
        };
        AnonymousClass4 r0 = new NotifInflationErrorManager.NotifInflationErrorListener() {
            public void onNotifInflationError(NotificationEntry notificationEntry, Exception exc) {
                PreparationCoordinator.this.mViewBarn.removeViewForEntry(notificationEntry);
                PreparationCoordinator.this.mInflationStates.put(notificationEntry, -1);
                try {
                    ExpandedNotification sbn = notificationEntry.getSbn();
                    PreparationCoordinator.this.mStatusBarService.onNotificationError(sbn.getPackageName(), sbn.getTag(), sbn.getId(), sbn.getUid(), sbn.getInitialPid(), exc.getMessage(), sbn.getUserId());
                } catch (RemoteException unused) {
                }
                PreparationCoordinator.this.mNotifInflationErrorFilter.invalidateList();
            }

            public void onNotifInflationErrorCleared(NotificationEntry notificationEntry) {
                PreparationCoordinator.this.mNotifInflationErrorFilter.invalidateList();
            }
        };
        this.mInflationErrorListener = r0;
        this.mLogger = preparationCoordinatorLogger;
        this.mNotifInflater = notifInflaterImpl;
        this.mNotifErrorManager = notifInflationErrorManager;
        notifInflationErrorManager.addInflationErrorListener(r0);
        this.mViewBarn = notifViewBarn;
        this.mStatusBarService = iStatusBarService;
        this.mChildBindCutoff = i;
    }

    public void attach(NotifPipeline notifPipeline) {
        notifPipeline.addCollectionListener(this.mNotifCollectionListener);
        notifPipeline.addOnBeforeFinalizeFilterListener(this.mOnBeforeFinalizeFilterListener);
        notifPipeline.addFinalizeFilter(this.mNotifInflationErrorFilter);
        notifPipeline.addFinalizeFilter(this.mNotifInflatingFilter);
    }

    /* access modifiers changed from: private */
    /* renamed from: inflateAllRequiredViews */
    public void lambda$new$0(List<ListEntry> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            ListEntry listEntry = list.get(i);
            if (listEntry instanceof GroupEntry) {
                GroupEntry groupEntry = (GroupEntry) listEntry;
                groupEntry.setUntruncatedChildCount(groupEntry.getChildren().size());
                inflateRequiredGroupViews(groupEntry);
            } else {
                inflateRequiredNotifViews((NotificationEntry) listEntry);
            }
        }
    }

    private void inflateRequiredGroupViews(GroupEntry groupEntry) {
        NotificationEntry summary = groupEntry.getSummary();
        List<NotificationEntry> children = groupEntry.getChildren();
        inflateRequiredNotifViews(summary);
        int i = 0;
        while (i < children.size()) {
            NotificationEntry notificationEntry = children.get(i);
            if (i < this.mChildBindCutoff) {
                inflateRequiredNotifViews(notificationEntry);
            } else {
                if (this.mInflatingNotifs.contains(notificationEntry)) {
                    abortInflation(notificationEntry, "Past last visible group child");
                }
                if (isInflated(notificationEntry)) {
                    freeNotifViews(notificationEntry);
                }
            }
            i++;
        }
    }

    private void inflateRequiredNotifViews(NotificationEntry notificationEntry) {
        if (!this.mInflatingNotifs.contains(notificationEntry)) {
            int intValue = this.mInflationStates.get(notificationEntry).intValue();
            if (intValue == 0) {
                inflateEntry(notificationEntry, "entryAdded");
            } else if (intValue == 2) {
                rebind(notificationEntry, "entryUpdated");
            }
        }
    }

    private void inflateEntry(NotificationEntry notificationEntry, String str) {
        abortInflation(notificationEntry, str);
        this.mInflatingNotifs.add(notificationEntry);
        this.mNotifInflater.inflateViews(notificationEntry, new NotifInflater.InflationCallback() {
            public final void onInflationFinished(NotificationEntry notificationEntry) {
                PreparationCoordinator.this.onInflationFinished(notificationEntry);
            }
        });
    }

    private void rebind(NotificationEntry notificationEntry, String str) {
        this.mInflatingNotifs.add(notificationEntry);
        this.mNotifInflater.rebindViews(notificationEntry, new NotifInflater.InflationCallback() {
            public final void onInflationFinished(NotificationEntry notificationEntry) {
                PreparationCoordinator.this.onInflationFinished(notificationEntry);
            }
        });
    }

    /* access modifiers changed from: private */
    public void abortInflation(NotificationEntry notificationEntry, String str) {
        this.mLogger.logInflationAborted(notificationEntry.getKey(), str);
        notificationEntry.abortTask();
        this.mInflatingNotifs.remove(notificationEntry);
    }

    /* access modifiers changed from: private */
    public void onInflationFinished(NotificationEntry notificationEntry) {
        this.mLogger.logNotifInflated(notificationEntry.getKey());
        this.mInflatingNotifs.remove(notificationEntry);
        this.mViewBarn.registerViewForEntry(notificationEntry, notificationEntry.getRow());
        this.mInflationStates.put(notificationEntry, 1);
        this.mNotifInflatingFilter.invalidateList();
    }

    private void freeNotifViews(NotificationEntry notificationEntry) {
        this.mViewBarn.removeViewForEntry(notificationEntry);
        notificationEntry.setRow((ExpandableNotificationRow) null);
        this.mInflationStates.put(notificationEntry, 0);
    }

    /* access modifiers changed from: private */
    public boolean isInflated(NotificationEntry notificationEntry) {
        int inflationState = getInflationState(notificationEntry);
        return inflationState == 1 || inflationState == 2;
    }

    /* access modifiers changed from: private */
    public int getInflationState(NotificationEntry notificationEntry) {
        Integer num = this.mInflationStates.get(notificationEntry);
        Objects.requireNonNull(num, "Asking state of a notification preparation coordinator doesn't know about");
        return num.intValue();
    }
}
