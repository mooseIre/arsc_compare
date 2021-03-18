package com.android.systemui.statusbar.notification.modal;

import com.android.systemui.statusbar.NotificationLifetimeExtender;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.modal.ModalController;

/* access modifiers changed from: package-private */
public class ModalLifetimeExtender implements NotificationLifetimeExtender {
    private NotificationEntry mEntry;
    private ModalController mModalController;
    private NotificationLifetimeExtender.NotificationSafeToRemoveCallback mNotificationSafeToRemoveCallback;
    private boolean mShouldManager = false;

    public ModalLifetimeExtender(ModalController modalController) {
        this.mModalController = modalController;
        modalController.addOnModalChangeListener(new ModalController.OnModalChangeListener() {
            /* class com.android.systemui.statusbar.notification.modal.$$Lambda$ModalLifetimeExtender$Z4VhIhFHNazmO05kc8SzTocxo */

            @Override // com.android.systemui.statusbar.notification.modal.ModalController.OnModalChangeListener
            public final void onChange(boolean z) {
                ModalLifetimeExtender.this.lambda$new$0$ModalLifetimeExtender(z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$ModalLifetimeExtender(boolean z) {
        if (!z && this.mShouldManager) {
            this.mNotificationSafeToRemoveCallback.onSafeToRemove(this.mEntry.getKey());
        }
        this.mShouldManager = false;
        this.mEntry = null;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setCallback(NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback) {
        this.mNotificationSafeToRemoveCallback = notificationSafeToRemoveCallback;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
        return this.mModalController.shouldExtendLifetime(notificationEntry);
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z) {
        if (shouldExtendLifetime(notificationEntry)) {
            this.mShouldManager = z;
            if (!z) {
                notificationEntry = null;
            }
            this.mEntry = notificationEntry;
        }
    }
}
