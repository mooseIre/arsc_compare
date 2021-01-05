package com.android.systemui.statusbar.notification.row;

import android.util.ArrayMap;
import android.util.SparseArray;
import android.widget.RemoteViews;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import java.util.Map;

public class NotifRemoteViewCacheImpl implements NotifRemoteViewCache {
    private final NotifCollectionListener mCollectionListener;
    /* access modifiers changed from: private */
    public final Map<NotificationEntry, SparseArray<RemoteViews>> mNotifCachedContentViews = new ArrayMap();

    NotifRemoteViewCacheImpl(CommonNotifCollection commonNotifCollection) {
        AnonymousClass1 r0 = new NotifCollectionListener() {
            public void onEntryInit(NotificationEntry notificationEntry) {
                NotifRemoteViewCacheImpl.this.mNotifCachedContentViews.put(notificationEntry, new SparseArray());
            }

            public void onEntryCleanUp(NotificationEntry notificationEntry) {
                NotifRemoteViewCacheImpl.this.mNotifCachedContentViews.remove(notificationEntry);
            }
        };
        this.mCollectionListener = r0;
        commonNotifCollection.addCollectionListener(r0);
    }

    public boolean hasCachedView(NotificationEntry notificationEntry, int i) {
        return getCachedView(notificationEntry, i) != null;
    }

    public RemoteViews getCachedView(NotificationEntry notificationEntry, int i) {
        SparseArray sparseArray = this.mNotifCachedContentViews.get(notificationEntry);
        if (sparseArray == null) {
            return null;
        }
        return (RemoteViews) sparseArray.get(i);
    }

    public void putCachedView(NotificationEntry notificationEntry, int i, RemoteViews remoteViews) {
        SparseArray sparseArray = this.mNotifCachedContentViews.get(notificationEntry);
        if (sparseArray != null) {
            sparseArray.put(i, remoteViews);
        }
    }

    public void removeCachedView(NotificationEntry notificationEntry, int i) {
        SparseArray sparseArray = this.mNotifCachedContentViews.get(notificationEntry);
        if (sparseArray != null) {
            sparseArray.remove(i);
        }
    }

    public void clearCache(NotificationEntry notificationEntry) {
        SparseArray sparseArray = this.mNotifCachedContentViews.get(notificationEntry);
        if (sparseArray != null) {
            sparseArray.clear();
        }
    }
}
