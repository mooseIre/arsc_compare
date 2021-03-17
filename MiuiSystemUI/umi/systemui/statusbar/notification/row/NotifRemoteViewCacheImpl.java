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
    private final Map<NotificationEntry, SparseArray<RemoteViews>> mNotifCachedContentViews = new ArrayMap();

    NotifRemoteViewCacheImpl(CommonNotifCollection commonNotifCollection) {
        AnonymousClass1 r0 = new NotifCollectionListener() {
            /* class com.android.systemui.statusbar.notification.row.NotifRemoteViewCacheImpl.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryInit(NotificationEntry notificationEntry) {
                NotifRemoteViewCacheImpl.this.mNotifCachedContentViews.put(notificationEntry, new SparseArray());
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryCleanUp(NotificationEntry notificationEntry) {
                NotifRemoteViewCacheImpl.this.mNotifCachedContentViews.remove(notificationEntry);
            }
        };
        this.mCollectionListener = r0;
        commonNotifCollection.addCollectionListener(r0);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotifRemoteViewCache
    public boolean hasCachedView(NotificationEntry notificationEntry, int i) {
        return getCachedView(notificationEntry, i) != null;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotifRemoteViewCache
    public RemoteViews getCachedView(NotificationEntry notificationEntry, int i) {
        SparseArray<RemoteViews> sparseArray = this.mNotifCachedContentViews.get(notificationEntry);
        if (sparseArray == null) {
            return null;
        }
        return sparseArray.get(i);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotifRemoteViewCache
    public void putCachedView(NotificationEntry notificationEntry, int i, RemoteViews remoteViews) {
        SparseArray<RemoteViews> sparseArray = this.mNotifCachedContentViews.get(notificationEntry);
        if (sparseArray != null) {
            sparseArray.put(i, remoteViews);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.NotifRemoteViewCache
    public void removeCachedView(NotificationEntry notificationEntry, int i) {
        SparseArray<RemoteViews> sparseArray = this.mNotifCachedContentViews.get(notificationEntry);
        if (sparseArray != null) {
            sparseArray.remove(i);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.NotifRemoteViewCache
    public void clearCache(NotificationEntry notificationEntry) {
        SparseArray<RemoteViews> sparseArray = this.mNotifCachedContentViews.get(notificationEntry);
        if (sparseArray != null) {
            sparseArray.clear();
        }
    }
}
