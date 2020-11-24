package com.android.systemui.statusbar;

import android.app.Notification;
import android.os.RemoteException;
import android.util.ArraySet;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import java.util.Set;

public class SmartReplyController {
    private final IStatusBarService mBarService;
    private Callback mCallback;
    private final NotificationClickNotifier mClickNotifier;
    private final NotificationEntryManager mEntryManager;
    private Set<String> mSendingKeys = new ArraySet();

    public interface Callback {
        void onSmartReplySent(NotificationEntry notificationEntry, CharSequence charSequence);
    }

    public SmartReplyController(NotificationEntryManager notificationEntryManager, IStatusBarService iStatusBarService, NotificationClickNotifier notificationClickNotifier) {
        this.mBarService = iStatusBarService;
        this.mEntryManager = notificationEntryManager;
        this.mClickNotifier = notificationClickNotifier;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void smartReplySent(NotificationEntry notificationEntry, int i, CharSequence charSequence, int i2, boolean z) {
        this.mCallback.onSmartReplySent(notificationEntry, charSequence);
        this.mSendingKeys.add(notificationEntry.getKey());
        try {
            this.mBarService.onNotificationSmartReplySent(notificationEntry.getSbn().getKey(), i, charSequence, i2, z);
        } catch (RemoteException unused) {
        }
    }

    public void smartActionClicked(NotificationEntry notificationEntry, int i, Notification.Action action, boolean z) {
        int activeNotificationsCount = this.mEntryManager.getActiveNotificationsCount();
        this.mClickNotifier.onNotificationActionClick(notificationEntry.getKey(), i, action, NotificationVisibility.obtain(notificationEntry.getKey(), notificationEntry.getRanking().getRank(), activeNotificationsCount, true, NotificationLogger.getNotificationLocation(notificationEntry)), z);
    }

    public boolean isSendingSmartReply(String str) {
        return this.mSendingKeys.contains(str);
    }

    public void smartSuggestionsAdded(NotificationEntry notificationEntry, int i, int i2, boolean z, boolean z2) {
        try {
            this.mBarService.onNotificationSmartSuggestionsAdded(notificationEntry.getSbn().getKey(), i, i2, z, z2);
        } catch (RemoteException unused) {
        }
    }

    public void stopSending(NotificationEntry notificationEntry) {
        if (notificationEntry != null) {
            this.mSendingKeys.remove(notificationEntry.getSbn().getKey());
        }
    }
}
