package com.android.systemui;

import android.os.Handler;
import android.os.Looper;
import android.util.ArraySet;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.NotificationInteractionTracker;
import com.android.systemui.statusbar.NotificationLifetimeExtender;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.util.time.SystemClock;

public class ForegroundServiceLifetimeExtender implements NotificationLifetimeExtender {
    @VisibleForTesting
    static final int MIN_FGS_TIME_MS = 1000;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final NotificationInteractionTracker mInteractionTracker;
    private ArraySet<NotificationEntry> mManagedEntries = new ArraySet<>();
    private NotificationLifetimeExtender.NotificationSafeToRemoveCallback mNotificationSafeToRemoveCallback;
    private final SystemClock mSystemClock;

    public ForegroundServiceLifetimeExtender(NotificationInteractionTracker notificationInteractionTracker, SystemClock systemClock) {
        this.mSystemClock = systemClock;
        this.mInteractionTracker = notificationInteractionTracker;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setCallback(NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback) {
        this.mNotificationSafeToRemoveCallback = notificationSafeToRemoveCallback;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
        if ((notificationEntry.getSbn().getNotification().flags & 64) == 0 || notificationEntry.hasInterrupted()) {
            return false;
        }
        boolean hasUserInteractedWith = this.mInteractionTracker.hasUserInteractedWith(notificationEntry.getKey());
        if (this.mSystemClock.uptimeMillis() - notificationEntry.getCreationTime() >= 1000 || hasUserInteractedWith) {
            return false;
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public boolean shouldExtendLifetimeForPendingNotification(NotificationEntry notificationEntry) {
        return shouldExtendLifetime(notificationEntry);
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z) {
        if (!z) {
            this.mManagedEntries.remove(notificationEntry);
            return;
        }
        this.mManagedEntries.add(notificationEntry);
        this.mHandler.postDelayed(new Runnable(notificationEntry) {
            /* class com.android.systemui.$$Lambda$ForegroundServiceLifetimeExtender$eZMtetouaKnxc7j2jqc6zpz_AA */
            public final /* synthetic */ NotificationEntry f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ForegroundServiceLifetimeExtender.this.lambda$setShouldManageLifetime$0$ForegroundServiceLifetimeExtender(this.f$1);
            }
        }, 1000 - (this.mSystemClock.uptimeMillis() - notificationEntry.getCreationTime()));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setShouldManageLifetime$0 */
    public /* synthetic */ void lambda$setShouldManageLifetime$0$ForegroundServiceLifetimeExtender(NotificationEntry notificationEntry) {
        if (this.mManagedEntries.contains(notificationEntry)) {
            this.mManagedEntries.remove(notificationEntry);
            NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback = this.mNotificationSafeToRemoveCallback;
            if (notificationSafeToRemoveCallback != null) {
                notificationSafeToRemoveCallback.onSafeToRemove(notificationEntry.getKey());
            }
        }
    }
}
