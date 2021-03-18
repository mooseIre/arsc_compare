package com.android.systemui.statusbar.notification.collection;

import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.notification.collection.inflation.NotifInflater;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.collection.notifcollection.DismissedByUserStats;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager;
import com.android.systemui.statusbar.notification.row.NotificationRowContentBinder;

public class NotifInflaterImpl implements NotifInflater {
    private final NotifCollection mNotifCollection;
    private final NotifInflationErrorManager mNotifErrorManager;
    private final NotifPipeline mNotifPipeline;
    private NotificationRowBinderImpl mNotificationRowBinder;

    public NotifInflaterImpl(IStatusBarService iStatusBarService, NotifCollection notifCollection, NotifInflationErrorManager notifInflationErrorManager, NotifPipeline notifPipeline) {
        this.mNotifCollection = notifCollection;
        this.mNotifErrorManager = notifInflationErrorManager;
        this.mNotifPipeline = notifPipeline;
    }

    public void setRowBinder(NotificationRowBinderImpl notificationRowBinderImpl) {
        this.mNotificationRowBinder = notificationRowBinderImpl;
    }

    @Override // com.android.systemui.statusbar.notification.collection.inflation.NotifInflater
    public void rebindViews(NotificationEntry notificationEntry, NotifInflater.InflationCallback inflationCallback) {
        inflateViews(notificationEntry, inflationCallback);
    }

    @Override // com.android.systemui.statusbar.notification.collection.inflation.NotifInflater
    public void inflateViews(NotificationEntry notificationEntry, NotifInflater.InflationCallback inflationCallback) {
        try {
            requireBinder().inflateViews(notificationEntry, getDismissCallback(notificationEntry), wrapInflationCallback(inflationCallback));
        } catch (InflationException e) {
            this.mNotifErrorManager.setInflationError(notificationEntry, e);
        }
    }

    private Runnable getDismissCallback(final NotificationEntry notificationEntry) {
        return new Runnable() {
            /* class com.android.systemui.statusbar.notification.collection.NotifInflaterImpl.AnonymousClass1 */

            public void run() {
                NotifCollection notifCollection = NotifInflaterImpl.this.mNotifCollection;
                NotificationEntry notificationEntry = notificationEntry;
                notifCollection.dismissNotification(notificationEntry, new DismissedByUserStats(3, 1, NotificationVisibility.obtain(notificationEntry.getKey(), notificationEntry.getRanking().getRank(), NotifInflaterImpl.this.mNotifPipeline.getShadeListCount(), true, NotificationLogger.getNotificationLocation(notificationEntry))));
            }
        };
    }

    private NotificationRowContentBinder.InflationCallback wrapInflationCallback(final NotifInflater.InflationCallback inflationCallback) {
        return new NotificationRowContentBinder.InflationCallback() {
            /* class com.android.systemui.statusbar.notification.collection.NotifInflaterImpl.AnonymousClass2 */

            @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback
            public void handleInflationException(NotificationEntry notificationEntry, Exception exc) {
                NotifInflaterImpl.this.mNotifErrorManager.setInflationError(notificationEntry, exc);
            }

            @Override // com.android.systemui.statusbar.notification.row.NotificationRowContentBinder.InflationCallback
            public void onAsyncInflationFinished(NotificationEntry notificationEntry) {
                NotifInflaterImpl.this.mNotifErrorManager.clearInflationError(notificationEntry);
                NotifInflater.InflationCallback inflationCallback = inflationCallback;
                if (inflationCallback != null) {
                    inflationCallback.onInflationFinished(notificationEntry);
                }
            }
        };
    }

    private NotificationRowBinderImpl requireBinder() {
        NotificationRowBinderImpl notificationRowBinderImpl = this.mNotificationRowBinder;
        if (notificationRowBinderImpl != null) {
            return notificationRowBinderImpl;
        }
        throw new RuntimeException("NotificationRowBinder must be attached before using NotifInflaterImpl.");
    }
}
