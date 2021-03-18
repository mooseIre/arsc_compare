package com.android.systemui.statusbar.policy;

import android.net.Uri;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public class RemoteInputUriController {
    private final NotificationEntryListener mInlineUriListener = new NotificationEntryListener() {
        /* class com.android.systemui.statusbar.policy.RemoteInputUriController.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
        public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
            try {
                RemoteInputUriController.this.mStatusBarManagerService.clearInlineReplyUriPermissions(notificationEntry.getKey());
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }
    };
    private final IStatusBarService mStatusBarManagerService;

    public RemoteInputUriController(IStatusBarService iStatusBarService) {
        this.mStatusBarManagerService = iStatusBarService;
    }

    public void attach(NotificationEntryManager notificationEntryManager) {
        notificationEntryManager.addNotificationEntryListener(this.mInlineUriListener);
    }

    public void grantInlineReplyUriPermission(StatusBarNotification statusBarNotification, Uri uri) {
        try {
            this.mStatusBarManagerService.grantInlineReplyUriPermission(statusBarNotification.getKey(), uri, statusBarNotification.getUser(), statusBarNotification.getPackageName());
        } catch (Exception e) {
            Log.e("RemoteInputUriController", "Failed to grant URI permissions:" + e.getMessage(), e);
        }
    }
}
